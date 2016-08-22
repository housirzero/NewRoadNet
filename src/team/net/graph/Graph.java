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
 * ���⣺
 * 1. վ�������Ҫ�ֶ����У�matlab�޸�ʧ��
 * 2. 
 */
public class Graph {
	
	// ��վ��ͼ�Ľڵ㣩��ϣ�б�key Ϊ��������
	public HashMap<Integer, ClusterStation> clusterStationMap = new  HashMap<Integer, ClusterStation>();
	
	/**
	 * ��ȡ�ļ����ɹ���վ��
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
		// �տ�ʼ����һ�������ڵ�, ��ѭ�����if���������ж�һ��lastStation�Ƿ�Ϊnull
		Station lastStation = new Station(null, "������", -1, null, -1, null, null, -1, -1);
		try {
			HashSet<String> topSet = getTop50(FilePath.dataFolder + "RouteRec/TripChain/20150104_TripChain3_volume.csv");
			// ��ע,����,DATAID,;������,վ���,�ڵ����,�ֹ�˾,ͼƬ,REGIONALIS,RINGPOSITI,ISINMAINRO,DIRECT,STAPOS,POINT_X,POINT_Y
			br.readLine(); // ������ͷ���ֶ�����
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				
				int kind = 1; // 1 ���� 2 ���ٹ��� 4 ���� (������ʾ���ֱ����ͬ�Ķ�����λ�����ڽ��л�����)
				if(items[6].equals("����")) // <�ֹ�˾>�ֶ�
					kind = 4;
				else if(items[3].contains("���ٹ���"))
					kind = 2;
				else if(!topSet.contains(items[3].split("[(]")[0])) // ѡ��Top50�Ĺ���
					continue;
				
				int classId = -1;
				int zoneId = -1;
				// ��ɽ������վ,��ɽ������վ,,1(��ɽ������վ-�Ļ���Ŧվ),1,1,����,,110107,95,0,12,8890,116.226922,39.913583
				// station.lastStationû��, ������Ϊnull, ��station.nextStation����, ������һ������
				Station station = new Station(items[1], items[3], Integer.parseInt(items[4]), new LngLat(items[13], items[14]), 
						kind, null, null, classId, zoneId);
				stations.add(station);
				

				// ��·û�б仯�ͼ�����һվ����Ϣ
				// �����ļ������һ�����ô�����Ϊ��û����һվ
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
	 * ��ȡ������ǰ��ʮ����·��ż���
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
				if(count > 56) // ֻҪǰ��ʮ,������6�����ٹ���
					break;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return topSet;
	}
	
	/**
	 * ��ȡ������ǰNλ����·��ż���
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
	 * ��ʾվ��λ����Ϣ���ֶ���matlab����
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
	 * �ֶ�����
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
	 * ��վ�����
	 * �ڴ˹�������Ҫ������վ��������
	 * 
	 * Ŀǰmatlabתjar��һֱ������
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
			result = dbScan.clu_dbscan_fn(3, pos, 3, 300); //��һ������Ϊ����������ӵڶ�����ʼΪ����
			
			MWNumericArray  mw=(MWNumericArray)result[0]; //0<=k<n,����Ҫȡ�����б��е�k�����ر�����ֵ
			double[] classId=(mw.getDoubleData());    //���rs[k]��һάdouble������
			
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
	 * ���������������ģ�Ϊ�Ժ��뽻ͨС������
	 */
	public static void calAllClusterCenter(HashMap<Integer, ClusterStation> clusterStationMap)
	{
		for( Map.Entry<Integer, ClusterStation> entry : clusterStationMap.entrySet())
			entry.getValue().calCenter();
	}
	
	/**
	 * ��ȡ�����Ѿ��ۺ����Сվ������clusterStations
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
			
			// �����վ֮��Ĺ���
			Station nextStation = station.nextStation;
			if(nextStation != null)
			{
				if(!clusterStation.containsClusterStation(nextStation.classId))
				{
					clusterStation.clusterRoads.add(new ClusterRoad(nextStation.classId));
				}
				// Ŀ�Ĵ�վ�������
				ClusterRoad clusterRoad = clusterStation.getClusterStation(nextStation.classId);
				clusterRoad.kind = clusterRoad.kind | station.kind;
			}
		}
		
		// ��������
		calAllClusterCenter(clusterStationMap);
		
		return clusterStationMap;
	}
	
	/**
	 * ��ȡ���оۺ�վ��ĵ�·��Ϣ
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
	 * ͳ��ͼ�Ľڵ�ͱ���
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
		cluster(stations); // ����
		
		// �����������ֶ�����
//		disStationsPos(stations);
//		clusterByHand(stations);
		
		HashMap<Integer, ClusterStation> clusterStationMap = genGraph(stations);
		statVE(clusterStationMap);
//		getRoadsInfo(clusterStationMap,stations);
		
		System.out.println("Done!");
	}

}
