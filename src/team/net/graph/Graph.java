package team.net.graph;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import team.support.function.FilePath;
import team.trans.zone.TransZoneMap;
import clu_dbscan.DBScan;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

/**
 * 问题：
 * 1. 站点聚类需要手动进行，matlab修改失败
 * 2. 
 */
public class Graph {
	
	// 大站（图的节点）哈希列表，key 为聚类类别号
	public HashMap<Integer, ClusterStation> clusterStationMap = new  HashMap<Integer, ClusterStation>();
	
	/**
	 * 读取文件生成公交站点
	 * @param filePath 
	 */
	public static ArrayList<Station> genStations(String filePath)
	{
		ArrayList<Station> stations = new ArrayList<Station>();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = "";
		// 刚开始创建一条不存在的, 在循环里的if语句就能少判断一个lastStation是否为null
		Station lastStation = new Station(null, "不存在", -1, null, -1, null, null, -1, -1);
		try {
			HashSet<String> topSet = getTop50(FilePath.dataFolder + "RouteRec/TripChain/20150104_TripChain3_volume.csv");
			// 标注,名称,DATAID,途经车次,站序号,节点序号,分公司,图片,REGIONALIS,RINGPOSITI,ISINMAINRO,DIRECT,STAPOS,POINT_X,POINT_Y
			br.readLine(); // 跳过表头（字段名）
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				
				int kind = 1; // 1 公交 2 快速公交 4 地铁 (这样表示，分别代表不同的二进制位，便于进行或运算)
				if(items[6].equals("地铁")) // <分公司>字段
					kind = 4;
				else if(items[3].contains("快速公交"))
					kind = 2;
				else if(!topSet.contains(items[3].split("[(]")[0])) // 选择Top50的公交
					continue;
				
				int classId = -1;
				int zoneId = -1;
				// 老山公交场站,老山公交场站,,1(老山公交场站-四惠枢纽站),1,1,客六,,110107,95,0,12,8890,116.226922,39.913583
				// station.lastStation没用, 所以置为null, 但station.nextStation有用, 会在下一条补上
				Station station = new Station(items[1], items[3], Integer.parseInt(items[4]), new LngLat(items[13], items[14]), 
						kind, null, null, classId, zoneId);
				stations.add(station);
				

				// 线路没有变化就加上下一站的信息
				// 整个文件的最后一条不用处理，因为它没有下一站
				if(lastStation.lineId.equals(station.lineId))
				{
					lastStation.nextStation = station;
				}
				lastStation = station;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return stations;
	}
	
	/**
	 * 获取客流在前五十的线路编号集合
	 * @return
	 */
	public static HashSet<String> getTop50( String filePath )
	{
		HashSet<String> topSet = new HashSet<String>();
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
			int count = 0;
			while((line = br.readLine())!= null){
				String[] items = line.split(",");
				topSet.add(items[0]);
				count++;
				if(count > 56) // 只要前五十,其中有6条快速公交
					break;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return topSet;
	}
	
	/**
	 * 获取客流在前N位的线路编号集合
	 */
	public static HashSet<String> getTopNLines( String filePath, int n )
	{
		HashSet<String> topSet = new HashSet<String>();
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
			int count = 0;
			while((line = br.readLine())!= null){
				topSet.add(line.split(",")[0]);
				if(++count >= n)
					break;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return topSet;
	}
	
	/**
	 * 显示站的位置信息，手动用matlab聚类
	 */
	public static void disStationsPos(ArrayList<Station> stations)
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(FilePath.dataFolder + "BaseInformation\\clusterGps.csv")));
		
			for(int i = 0; i < stations.size(); i++)
			{
				Station station = stations.get(i);
				System.out.println(station.pos);
				bw.write(station.pos+"\r\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 手动聚类
	 * @param stations
	 */
	public static void clusterByHand(ArrayList<Station> stations)
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(FilePath.dataFolder + "DailyData\\Analy\\classId.csv")));
		
			for(int i = 0; i < stations.size(); i++)
			{
				Station station = stations.get(i);
				station.classId = Integer.parseInt(br.readLine());
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 给站点聚类
	 * 在此过程中需要给各个站标上类别号
	 * 
	 * 目前matlab转jar包一直有问题
	 */
	public static void cluster(ArrayList<Station> stations)
	{
		try {
			DBScan dbScan = new DBScan();
			Object[] result = null;
			double[][] pos = new double[stations.size()][2];
			for(int i = 0; i < stations.size(); i++)
			{
				pos[i][0] = stations.get(i).pos.lng;
				pos[i][1] = stations.get(i).pos.lat;
			}
			result = dbScan.clu_dbscan_fn(3, pos, 3, 300); //第一个参数为输出个数，从第二个开始为输入
			
			MWNumericArray  mw=(MWNumericArray)result[0]; //0<=k<n,假如要取返回列表中第k个返回变量的值
			double[] classId=(mw.getDoubleData());    //如果rs[k]是一维double型数组
			
			for(int i = 0; i < classId.length; i++)
				stations.get(i).classId = (int)classId[i];
			
			BufferedWriter bw = null;
			try {
				
				bw = new BufferedWriter(new FileWriter(new File(FilePath.dataFolder + "BaseInformation\\clusterGps.csv")));
	
				for(Station station : stations)
				{
					bw.write(station.pos.toString() + "," + station.classId + "\r\n");
				}
				bw.close();
			}catch (IOException e) {
				e.printStackTrace();
			}

		} catch (MWException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 计算所有类别的质心，为以后并入交通小区服务
	 */
	public static void calAllClusterCenter(HashMap<Integer, ClusterStation> clusterStationMap)
	{
		for( Map.Entry<Integer, ClusterStation> entry : clusterStationMap.entrySet())
			entry.getValue().calCenter();
	}
	
	/**
	 * 读取所有已经聚好类的小站，生成clusterStations
	 */
	public static HashMap<Integer, ClusterStation> genGraph(List<Station> stations)
	{
		HashMap<Integer, ClusterStation> clusterStationMap = new  HashMap<Integer, ClusterStation>();
		
		for(Station station : stations)
		{
			int classId = station.classId;
//			if(classId == -1)
//				continue;
			if(!clusterStationMap.containsKey(classId))
			{
				clusterStationMap.put(classId, new ClusterStation(classId));
			}
			ClusterStation clusterStation = clusterStationMap.get(classId);
			
			clusterStation.stations.add(station);
			
			// 处理大站之间的关联
			Station nextStation = station.nextStation;
			if(nextStation != null)
			{
				if(!clusterStation.containsClusterStation(nextStation.classId))
				{
					clusterStation.clusterRoads.add(new ClusterRoad(nextStation.classId));
				}
				// 目的大站点的类别号
				ClusterRoad clusterRoad = clusterStation.getClusterStation(nextStation.classId);
				clusterRoad.kind = clusterRoad.kind | station.kind;
			}
		}
		
		// 计算质心
		calAllClusterCenter(clusterStationMap);
		
		return clusterStationMap;
	}
	
	/**
	 * 获取所有聚合站点的道路信息
	 * @param clusterStationMap
	 */
	public static void getRoadsInfo(HashMap<Integer, ClusterStation> clusterStationMap, ArrayList<Station> stations)
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(FilePath.dataFolder + "DailyData\\Analy\\getRoadsInfo1.csv")));
		
			for(Map.Entry<Integer, ClusterStation> entry : clusterStationMap.entrySet())
			{
				ClusterStation clusterStation = entry.getValue();
				LngLat startPos = clusterStation.center.Copy();
//				for(ClusterRoad clusterRoad : clusterStation.clusterRoads)
//				{
//					LngLat endPos = clusterStationMap.get(clusterRoad.endClustStatId).center.Copy();
//					System.out.println(startPos.toString() + "," + endPos.toString() + "," + clusterRoad.kind );
//					bw.write(startPos.toString() + "," + endPos.toString() + "," + clusterRoad.kind + "\r\n");
//				}
//				System.out.println();
				System.out.println(startPos.toString());
				bw.write(startPos.toString() + "," + clusterStation.classId + "\r\n");
			}
//			System.out.println();
//			for(Station station : stations)
//			{
//				bw.write(station.pos.toString() + "," + station.classId + "\r\n");
//			}
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Set<Integer> getRegions()
	{
		ArrayList<Station> stations = genStations(FilePath.dataFolder + "BaseInformation\\BusStation.csv");
		Set<Integer> regionSet = new HashSet<Integer>();
		for(Station station : stations)
		{
			int zoneId = TransZoneMap.findZoneId(station.pos);
			if( zoneId != -1 )
				regionSet.add(zoneId);
		}
		return regionSet;
	}
	
	/**
	 * 统计图的节点和边数
	 */
	public static void statVE(Map<Integer, ClusterStation> clusterStationMap)
	{
		int V = 0;
		int E = 0;
		V = clusterStationMap.size();
		for(Map.Entry<Integer, ClusterStation> entry : clusterStationMap.entrySet())
		{
			ClusterStation clusterStation = entry.getValue();
			E += clusterStation.clusterRoads.size();
		}
		System.out.println("V=" + V);
		System.out.println("E=" + E);
	}

	public static void main(String[] args) {
		ArrayList<Station> stations = genStations(FilePath.dataFolder + "BaseInformation\\BusStation.csv");
		cluster(stations); // 聚类
		
		// 下面两句是手动聚类
//		disStationsPos(stations);
//		clusterByHand(stations);
		
		HashMap<Integer, ClusterStation> clusterStationMap = genGraph(stations);
		statVE(clusterStationMap);
//		getRoadsInfo(clusterStationMap,stations);
		
		System.out.println("Done!");
	}

}
