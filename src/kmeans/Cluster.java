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

	// ��Ӧ��дһ���Կ�����������
	public static void run() {

		String recordsFile = "E:/TransData/RouteRec/TripChain/20150104_TripChain3.csv";
		String lineVolumeFile = recordsFile.replace(".csv", "_Volume.csv");
		String stationVolumeFile = recordsFile.replace(".csv", "_StationVolume.csv");
		
		
		System.out.println("/** line **/");
		// ͳ����·����
//		statLinePassVolume(recordsFile, lineVolumeFile);
		// ѡȡ��������70����·
		Set<String> topNLines = Graph.getTopNLines(lineVolumeFile, 70);

		
		System.out.println("/** station **/");
		// ͳ�ƿ�������70����·��վ�����
//		statStationPassVolume(topNLines, recordsFile, stationVolumeFile);
		// ��ȡ�ļ����ɹ���վ��,��Ϊ���������Դ
		List<Station> stations = genStations(topNLines);
		System.out.println("stations num = " + stations.size());
		// ѡȡ��������N��վ��
		List<String> topNStations = getTopNStations(stationVolumeFile, 1000);
		// ������ѡ��·����վ���ʶ��GPS��Ϣ<Key,GPS>
		Map<String, LngLat> stationsInfo = genStationsInfo(topNLines);

		
		System.out.println("/** init cluster center **/");
		// ���տ�����Сѡȡһ���ֵ���Ϊ��ʼ��������
		// �����������ĵľ���С��������ֵ��ɾ��һ��
		/**��ûʵ�ִ˺���**/
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
				return new Station(null, "������", -1, new LngLat(lng/list.size(), lat/list.size()), -1, null, null, -1, -1);
			}
		};
		kmeans.setMaxDis(maxDis);
		kmeans.cluster();
		System.out.println("���������� " + kmeans.getIterTimes());
//		kmeans.display();
		
		
//		System.out.println("/** �� kmeans �Ľ����Ϊ gmm �ĳ�ʼ״̬������ gmm ���� **/");
//		GMMAlgorithm gmm = new GMMAlgorithm();
//		ArrayList<ArrayList<Double>> dataSet = new ArrayList<ArrayList<Double>>(); // ���ݼ�
//		// ��ȡ���ݼ�
//		for(int i = 0; i < stations.size(); i++){
//			ArrayList<Double> p = new ArrayList<Double>();
//			LngLat pos = stations.get(i).pos;
//			p.add(pos.lng);
//			p.add(pos.lat);
//			dataSet.add(p);
//		}
//		
//		ArrayList<ArrayList<Double>> pMiu = new ArrayList<ArrayList<Double>>(); // ��ֵ����k���ֲ������ĵ㣬ÿ�����ĵ�dά
//		// ��ȡpMiu
//		for(int i = 0; i < kmeans.centers.size(); i++){
//			ArrayList<Double> p = new ArrayList<Double>();
//			LngLat pos = kmeans.centers.get(i).pos;
//			p.add(pos.lng);
//			p.add(pos.lat);
//			pMiu.add(p);
//		}
//		int dataNum = dataSet.size(); // ������������ dataSet.size()
//		int k = centers.size(); // ������
//		int dataDimen = 2; // ά�ȣ������Ƕ�ά���꣬������2
//		
//		System.out.println("  /** gmm ���� **/");
//		int[] classId = gmm.GMMCluster(dataSet, pMiu, dataNum, k, dataDimen);
		
		
		System.out.println("/** ���ݾ������������� **/");
		int[] classId = kmeans.getClusterResult();
		for(int i = 0; i < classId.length; i++)
			stations.get(i).classId = classId[i];
		HashMap<Integer, ClusterStation> clusterStationMap = Graph.genGraph(stations);

		System.out.println("/** ���ɿ���ͶӰ��ArcGis���ļ� **/");
		// �����ļ���ͶӰ��ArcGis
		genStationsFile(stations, FilePath.dataFolder + "Cluster\\stations_gmm_500.csv");
		genClusterStationsFile(clusterStationMap, FilePath.dataFolder + "Cluster\\clusterStations_gmm_500.csv");
	}

	/**
	 * ͳ��ÿ����·�Ŀ�����
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
			
			// W,P�����ȼ���������������ٷֱ�
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
	* ͳ����ѡ��·��վ�������
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
			
			// W,P�����ȼ���������������ٷֱ�
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
			
				String onLine = items[onLineIndex];
				String offLine = items[offLineIndex];
				
				if(lineSet.contains(onLine))
				{
					// stationNo ����Ҫ��ֻ��lineId+stationName����
					String key = items[onLineIndex] + "," + items[onStationNameIndex];
					if(!passVolume.containsKey(key))
						passVolume.put(key, 1);
					else
						passVolume.put(key, passVolume.get(key)+1);
				}
				
				if(lineSet.contains(offLine))
				{
					// stationNo ����Ҫ��ֻ��lineId+stationName����
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
	 * ��ȡ�ļ�����վ���б�
	 */
	public static ArrayList<Station> genStations(Set<String> lineSet)
	{
		ArrayList<Station> stations = new ArrayList<Station>();
		
		String busLineFile = "E:/TransData/BaseInformation/BusStation2015v2.csv";
		// 1(��ɽ������վ-�Ļ���Ŧվ),��ɽ������վ,1,116.226922,39.913583,1,894,
		List<String> busLines = read(busLineFile);
	
		String subLineFile = "E:/TransData/BaseInformation/SubwayStation2015.csv";
		// 1����,ƻ��԰,103,116.172853,39.923962,150995203,897
		List<String> subLines = read(subLineFile);
		
		busLines.addAll(subLines); // �ϲ�
		// �տ�ʼ����һ�������ڵ�, ��ѭ�����if���������ж�һ��lastStation�Ƿ�Ϊnull
		Station lastStation = new Station(null, "������", -1, null, -1, null, null, -1, -1);
		for(int i = 0; i < busLines.size(); i++) {
			String line = busLines.get(i);
			String[] items = line.split(",");
			
			String lineId = items[0].split("[(]")[0];
			
			if(!lineSet.contains(lineId))
				continue;
	
			Station station = new Station(items[1], lineId, -1, // stationNo ����Ҫ��ֻ��lineId+stationName����
					new LngLat(items[3], items[4]), -1, null, null, -1, -1);
			stations.add(station);
	
			// ��·û�б仯�ͼ�����һվ����Ϣ
			// �����ļ������һ�����ô�����Ϊ��û����һվ
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
		// 1(��ɽ������վ-�Ļ���Ŧվ),��ɽ������վ,1,116.226922,39.913583,1,894,
		List<String> busLines = read(busLineFile);

		String subLineFile = "E:/TransData/BaseInformation/SubwayStation2015.csv";
		// 1����,ƻ��԰,103,116.172853,39.923962,150995203,897
		List<String> subLines = read(subLineFile);
		
		busLines.addAll(subLines); // �ϲ�
		for(int i = 0; i < busLines.size(); i++) {
			String line = busLines.get(i);
			String[] items = line.split(",");
			
			String lineId = items[0].split("[(]")[0];
			if(!topNLines.contains(lineId))
				continue;
			
			String stationName = items[1];
//			int stationNo = -1; // stationNo ����Ҫ��ֻ��lineId+stationName����
			
			String key = lineId + "," + stationName;
			if(!stationsInfo.containsKey(key))
				stationsInfo.put(key, new LngLat(items[3], items[4]));
		}
		
		return stationsInfo;
	}

	/**
	 * Сվ��������classId
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
	 * ��վ���ļ�������classId
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
	 * ��ʼ����������
	 * ���տ�����Сѡȡһ���ֵ���Ϊ��ʼ��������
	 * �����������ĵľ���С��������ֵ��ɾ��һ��
	 */
	private static List<Station> initClusterCenter(List<String> stations, Map<String, LngLat> stationsInfo, double maxDis) {
		List<Station> centers = new ArrayList<Station>();
		centers.add(new Station(null, "", -1, stationsInfo.get(stations.get(0)).Copy(), -1, null, null, -1, -1));
		for(int i = 1; i < stations.size(); i++)
		{
			String stationKey = stations.get(i);
			LngLat pos = stationsInfo.get(stationKey);
			boolean isClose = false; // �Ƿ�����Ѿ�ѡ���վ��ܽ�
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
	 * ��ȡ�ļ�
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
