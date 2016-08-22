package kmeans;

import gmm.GMMAlgorithm;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import team.net.graph.ClusterStation;
import team.net.graph.Graph;
import team.net.graph.LngLat;
import team.net.graph.Station;
import team.support.function.Distance;
import team.support.function.FilePath;

public class Cluster {

	public static void main(String[] args) {
		Date start = new Date();
		run();
		Date end = new Date();
		System.out.println("run time : " + (end.getTime() - start.getTime())
				/ 1000.0 + " s.");
	}
	
	private static double maxDis = 500;

	// 还应该写一个对客流的排序函数
	public static void run() {

		String recordsFile = "E:/TransData/RouteRec/TripChain/20150104_TripChain3.csv";
		String lineVolumeFile = recordsFile.replace(".csv", "_Volume.csv");
		String stationVolumeFile = recordsFile.replace(".csv", "_StationVolume.csv");
		
		
		System.out.println("/** line **/");
		// 统计线路客流
//		statLinePassVolume(recordsFile, lineVolumeFile);
		// 选取客流最大的70条线路
		Set<String> topNLines = Graph.getTopNLines(lineVolumeFile, 70);

		
		System.out.println("/** station **/");
		// 统计客流最大的70条线路的站点客流
//		statStationPassVolume(topNLines, recordsFile, stationVolumeFile);
		// 读取文件生成公交站点,作为聚类的数据源
		List<Station> stations = genStations(topNLines);
		System.out.println("stations num = " + stations.size());
		// 选取客流最大的N个站点
		List<String> topNStations = getTopNStations(stationVolumeFile, 1000);
		// 生成所选线路所有站点标识及GPS信息<Key,GPS>
		Map<String, LngLat> stationsInfo = genStationsInfo(topNLines);

		
		System.out.println("/** init cluster center **/");
		// 按照客流大小选取一部分点作为初始聚类中心
		// 两个聚类中心的距离小于所定阈值就删除一个
		/**还没实现此函数**/
		List<Station> centers = initClusterCenter(topNStations, stationsInfo, maxDis);
		System.out.println("class num = " + centers.size());

		System.out.println("/** kmeans cluster **/");
		KMeans<Station> kmeans = new KMeans<Station>(stations, centers.size(), centers) {
			@Override
			public double distance(Station e1, Station e2) {
				return Distance.lngLatDistance(e1.pos, e2.pos);
			}

			@Override
			public Station updateCenter(List<Station> list) {
				if(list == null || list.size() == 0)
					return null;
				double lng = 0;
				double lat = 0;
				for(int i = 0; i < list.size(); i++)
				{
					LngLat pos = list.get(i).pos;
					lng += pos.lng;
					lat += pos.lat;
				}
				return new Station(null, "不存在", -1, new LngLat(lng/list.size(), lat/list.size()), -1, null, null, -1, -1);
			}
		};
		kmeans.setMaxDis(maxDis);
		kmeans.cluster();
		System.out.println("迭代次数： " + kmeans.getIterTimes());
//		kmeans.display();
		
		
//		System.out.println("/** 以 kmeans 的结果作为 gmm 的初始状态，进行 gmm 聚类 **/");
//		GMMAlgorithm gmm = new GMMAlgorithm();
//		ArrayList<ArrayList<Double>> dataSet = new ArrayList<ArrayList<Double>>(); // 数据集
//		// 获取数据集
//		for(int i = 0; i < stations.size(); i++){
//			ArrayList<Double> p = new ArrayList<Double>();
//			LngLat pos = stations.get(i).pos;
//			p.add(pos.lng);
//			p.add(pos.lat);
//			dataSet.add(p);
//		}
//		
//		ArrayList<ArrayList<Double>> pMiu = new ArrayList<ArrayList<Double>>(); // 均值参数k个分布的中心点，每个中心点d维
//		// 获取pMiu
//		for(int i = 0; i < kmeans.centers.size(); i++){
//			ArrayList<Double> p = new ArrayList<Double>();
//			LngLat pos = kmeans.centers.get(i).pos;
//			p.add(pos.lng);
//			p.add(pos.lat);
//			pMiu.add(p);
//		}
//		int dataNum = dataSet.size(); // 数据条数，即 dataSet.size()
//		int k = centers.size(); // 分类数
//		int dataDimen = 2; // 维度，这里是二维坐标，所以是2
//		
//		System.out.println("  /** gmm 聚类 **/");
//		int[] classId = gmm.GMMCluster(dataSet, pMiu, dataNum, k, dataDimen);
		
		
		System.out.println("/** 根据聚类结果生成网络 **/");
		int[] classId = kmeans.getClusterResult();
		for(int i = 0; i < classId.length; i++)
			stations.get(i).classId = classId[i];
		HashMap<Integer, ClusterStation> clusterStationMap = Graph.genGraph(stations);

		System.out.println("/** 生成可以投影到ArcGis的文件 **/");
		// 生成文件，投影到ArcGis
		genStationsFile(stations, FilePath.dataFolder + "Cluster\\stations_gmm_500.csv");
		genClusterStationsFile(clusterStationMap, FilePath.dataFolder + "Cluster\\clusterStations_gmm_500.csv");
	}

	/**
	 * 统计每条线路的客流量
	 */
	public static Map<String, Integer> statLinePassVolume( String readFile, String saveFile ) 
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(readFile));
			bw = new BufferedWriter(new FileWriter(saveFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, Integer> passVolume = new HashMap<String, Integer>();
		
		try {
			String line = null;
			int onLineIndex = 'G' - 'A';
			int offLineIndex = 'N' - 'A';
			
			// W,P都是先计算次数，最后再算百分比
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
			
				String onLine = items[onLineIndex];
				String offLine = items[offLineIndex];
				
				if(!passVolume.containsKey(onLine))
					passVolume.put(onLine, 1);
				else
					passVolume.put(onLine, passVolume.get(onLine)+1);
				
				if(!passVolume.containsKey(offLine))
					passVolume.put(offLine, 1);
				else
					passVolume.put(offLine, passVolume.get(offLine)+1);
			}
			br.close();
			
			MapSort<String, Integer> mapSort = new MapSort<String, Integer>(passVolume){

				@Override
				public int valueComp(Integer v1, Integer v2) {
					return v2 - v1;
				}

				@Override
				public int keyComp(String k1, String k2) {
					return k1.compareTo(k2);
				}
				
			};
			
			List<MapClass<String, Integer>> sortList = mapSort.sortByValue();
			for(int i = 0; i < sortList.size(); i++)
			{
				MapClass<String, Integer> mapClass = sortList.get(i);
				bw.write(mapClass.k + "," + mapClass.v + "\r\n");
			}
			
//			
//			for(Entry<K, V> entry : passVolume.entrySet())
//				bw.write(entry.getKey() + "," + entry.getValue() + "\r\n");
			bw.close();
			return passVolume;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	* 统计所选线路的站点客流量
	*/
	public static Map<String, Integer> statStationPassVolume( Set<String> lineSet, String readFile, String saveFile ) 
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(readFile));
			bw = new BufferedWriter(new FileWriter(saveFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Key = lineId,StationNo,StationName
		Map<String, Integer> passVolume = new HashMap<String, Integer>();
		
		try {
			String line = null;
			int onLineIndex = 'G' - 'A';
			int offLineIndex = 'N' - 'A';
//			int onStationNoIndex = 'I' - 'A';
//			int offStationNoIndex = 'P' - 'A';
			int onStationNameIndex = 'H' - 'A';
			int offStationNameIndex = 'O' - 'A';
			
			// W,P都是先计算次数，最后再算百分比
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
			
				String onLine = items[onLineIndex];
				String offLine = items[offLineIndex];
				
				if(lineSet.contains(onLine))
				{
					// stationNo 不需要，只需lineId+stationName即可
					String key = items[onLineIndex] + "," + items[onStationNameIndex];
					if(!passVolume.containsKey(key))
						passVolume.put(key, 1);
					else
						passVolume.put(key, passVolume.get(key)+1);
				}
				
				if(lineSet.contains(offLine))
				{
					// stationNo 不需要，只需lineId+stationName即可
//					String key = items[offLineIndex] + "," + items[offStationNoIndex] + "," + items[offStationNameIndex];
					String key = items[offLineIndex] + "," + items[offStationNameIndex];
					if(!passVolume.containsKey(key))
						passVolume.put(key, 1);
					else
						passVolume.put(key, passVolume.get(key)+1);
				}
			}
			br.close();
			
			MapSort<String, Integer> mapSort = new MapSort<String, Integer>(passVolume){
				@Override
				public int valueComp(Integer v1, Integer v2) {
					return v2 - v1;
				}
				@Override
				public int keyComp(String k1, String k2) {
					return k1.compareTo(k2);
				}
			};
			List<MapClass<String, Integer>> sortList = mapSort.sortByValue();
			
			for(int i = 0; i < sortList.size(); i++)
			{
				MapClass<String, Integer> mapClass = sortList.get(i);
				bw.write(mapClass.k + "," + mapClass.v + "\r\n");
			}
			
//			for(Entry<K, V> entry : passVolume.entrySet())
//				bw.write(entry.getKey() + "," + entry.getValue() + "\r\n");

			bw.close();
			return passVolume;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 读取文件生成站点列表
	 */
	public static ArrayList<Station> genStations(Set<String> lineSet)
	{
		ArrayList<Station> stations = new ArrayList<Station>();
		
		String busLineFile = "E:/TransData/BaseInformation/BusStation2015v2.csv";
		// 1(老山公交场站-四惠枢纽站),老山公交场站,1,116.226922,39.913583,1,894,
		List<String> busLines = read(busLineFile);
	
		String subLineFile = "E:/TransData/BaseInformation/SubwayStation2015.csv";
		// 1号线,苹果园,103,116.172853,39.923962,150995203,897
		List<String> subLines = read(subLineFile);
		
		busLines.addAll(subLines); // 合并
		// 刚开始创建一条不存在的, 在循环里的if语句就能少判断一个lastStation是否为null
		Station lastStation = new Station(null, "不存在", -1, null, -1, null, null, -1, -1);
		for(int i = 0; i < busLines.size(); i++) {
			String line = busLines.get(i);
			String[] items = line.split(",");
			
			String lineId = items[0].split("[(]")[0];
			
			if(!lineSet.contains(lineId))
				continue;
	
			Station station = new Station(items[1], lineId, -1, // stationNo 不需要，只需lineId+stationName即可
					new LngLat(items[3], items[4]), -1, null, null, -1, -1);
			stations.add(station);
	
			// 线路没有变化就加上下一站的信息
			// 整个文件的最后一条不用处理，因为它没有下一站
			if(lastStation.lineId.equals(station.lineId))
			{
				lastStation.nextStation = station;
			}
			lastStation = station;
		}
		
		return stations;
	}

	private static Map<String, LngLat> genStationsInfo(Set<String> topNLines) {
		
		Map<String, LngLat> stationsInfo = new HashMap<String, LngLat>();
		
		String busLineFile = "E:/TransData/BaseInformation/BusStation2015v2.csv";
		// 1(老山公交场站-四惠枢纽站),老山公交场站,1,116.226922,39.913583,1,894,
		List<String> busLines = read(busLineFile);

		String subLineFile = "E:/TransData/BaseInformation/SubwayStation2015.csv";
		// 1号线,苹果园,103,116.172853,39.923962,150995203,897
		List<String> subLines = read(subLineFile);
		
		busLines.addAll(subLines); // 合并
		for(int i = 0; i < busLines.size(); i++) {
			String line = busLines.get(i);
			String[] items = line.split(",");
			
			String lineId = items[0].split("[(]")[0];
			if(!topNLines.contains(lineId))
				continue;
			
			String stationName = items[1];
//			int stationNo = -1; // stationNo 不需要，只需lineId+stationName即可
			
			String key = lineId + "," + stationName;
			if(!stationsInfo.containsKey(key))
				stationsInfo.put(key, new LngLat(items[3], items[4]));
		}
		
		return stationsInfo;
	}

	/**
	 * 小站及其所属classId
	 * @param stations 
	 */
	private static void genStationsFile(List<Station> stations, String file) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
			for(Station station : stations)
				bw.write(station.pos.toString() + "," + station.classId + "\r\n");
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 大站质心及其所属classId
	 */
	private static void genClusterStationsFile(Map<Integer, ClusterStation> clusterStationMap, String file) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
			for(Map.Entry<Integer, ClusterStation> entry : clusterStationMap.entrySet())
			{
				ClusterStation clusterStation = entry.getValue();
				LngLat startPos = clusterStation.center;
//				System.out.println(startPos.toString());
				bw.write(startPos.toString() + "," + clusterStation.classId + "\r\n");
			}
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<String> getTopNStations(String stationVolumeFile, int n) {
		List<String> topList = new ArrayList<String>();
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(stationVolumeFile)));
			int count = 0;
			while((line = br.readLine())!= null){
				String[] items = line.split(",");
				topList.add(items[0]+","+items[1]);
				if(count++ > n)
					break;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return topList;
	}

	/**
	 * 初始化聚类中心
	 * 按照客流大小选取一部分点作为初始聚类中心
	 * 两个聚类中心的距离小于所定阈值就删除一个
	 */
	private static List<Station> initClusterCenter(List<String> stations, Map<String, LngLat> stationsInfo, double maxDis) {
		List<Station> centers = new ArrayList<Station>();
		centers.add(new Station(null, "", -1, stationsInfo.get(stations.get(0)).Copy(), -1, null, null, -1, -1));
		for(int i = 1; i < stations.size(); i++)
		{
			String stationKey = stations.get(i);
			LngLat pos = stationsInfo.get(stationKey);
			boolean isClose = false; // 是否距离已经选择的站点很近
			for(Station station : centers)
			{
				if(Distance.lngLatDistance(pos, station.pos) < maxDis)
				{
					isClose = true;
					break;
				}
			}
			
			if(!isClose)
				centers.add(new Station(null, "", -1, pos.Copy(), -1, null, null, -1, -1));
		}
		return centers;
	}
	
	/**
	 * 读取文件
	 */
	public static List<String> read( String filePath )
    {
    	List<String> fileLines = null;
    	try {
    		BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
			fileLines = new ArrayList<String>();
	    	String line = null;
			while ( (line = br.readLine()) != null )
				fileLines.add(line);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileLines;
    }
}
