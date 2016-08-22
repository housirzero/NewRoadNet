package grid;

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
import java.util.Stack;

import kmeans.KMeans;
import kmeans.MapClass;
import kmeans.MapSort;
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
		Set<String> topNLines = Graph.getTopNLines(lineVolumeFile, 100);

		
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
				return new Station("��������", "��������", -1, new LngLat(lng/list.size(), lat/list.size()), -1, null, null, -1, -1);
			}
		};
		kmeans.setMaxDis(maxDis);
		kmeans.cluster();
		System.out.println("���������� " + kmeans.getIterTimes());
//		kmeans.display();

		
		System.out.println("/** ���ݾ������������� **/");
		int[] classId = kmeans.getClusterResult();
		for(int i = 0; i < classId.length; i++)
			stations.get(i).classId = classId[i];
		HashMap<Integer, ClusterStation> clusterStationMap = Graph.genGraph(stations);

		
//		System.out.println("/** ��ʼ����͹�� **/");
//		ConvexHull<LngLat> convexHull = new ConvexHull<LngLat>() {
//			
//			@Override
//			public double getY(LngLat e) {
//				return e.lat;
//			}
//			
//			@Override
//			public double getX(LngLat e) {
//				return e.lng;
//			}
//			
//			@Override
//			public double distance(LngLat e1, LngLat e2) {
//				double dx = e1.lng - e2.lng;
//				double dy = e1.lat - e2.lat;
//				return dx * dx + dy * dy;
//			}
//		};
//		
//		List<Stack<LngLat>> edges = new ArrayList<Stack<LngLat>>();
//		System.out.println("/** 1. ��������վ���һ����͹�� **/");
//		Stack<LngLat> stationsEdge = convexHull.graham(getPosCopy(stations));
//		edges.add(stationsEdge);
//
//		System.out.println("/** 2. ����ÿһ������վ���С͹�� **/");
//		for(Map.Entry<Integer, ClusterStation> entry : clusterStationMap.entrySet())
//		{
//			ClusterStation clusterStation = entry.getValue();
//			if(entry.getKey() == -1 || clusterStation.stations.size() < 3)
//				continue;
//			System.out.println(clusterStation.stations.size());
//			Stack<LngLat> classEnge = convexHull.graham(getPosCopy(clusterStation.stations));
//			edges.add(classEnge);
//		}
//		
//		genEngeToJson(edges, FilePath.dataFolder + "Cluster\\edges.json");
		
		
//		System.out.println("/** ���ɿ���ͶӰ��ArcGis���ļ� **/");
//		genStationsFile(stations, FilePath.dataFolder + "Cluster\\stations_gmm_500.csv");
//		genClusterStationsFile(clusterStationMap, FilePath.dataFolder + "Cluster\\clusterStations_gmm_500.csv");
		
//		// kmeans ��������Ϊ��ʼ
//		// �ҵ�����վ�����С͹�հ�������ʱ�򵥵Ľ���������һ��������
//		// edge�е�mark��Ϊ"-1,-1"
//		List<LineSegment> edge = genConvexHull(stations); // ��������վ�����С͹�հ���Ԫ����Ҫ�������յ�ȷ��
//		
//		// �������������ĺ��ĵ�֮�仭�д��ߣ������������͹�հ��������򻮷ֳ���
//		/*
//		 * for s in stations
//		 * 		find s1,st dis(s,s1) is min
//		 * 		line1 is <s1,s> �� �д���
//		 * 			�����Ѿ��е�lines�����ݽ��㣬�����߶εĶ˵㣬������lineSeg�б���
//		 * ֻҪ���ڶ���㱻���ֵ�һ�������������������֮������ƽ����
//		 */
//		Set<String> existLine = new HashSet<String>(); // �Ѿ����ڵ��߶Σ�<class1,class2>
//		List<LineSegment> lineSegList = new ArrayList<LineSegment>(); // �߶��б�
//		lineSegList.addAll(edge); // ��������վ�����С͹�հ�
//		
//		// ����������centers �е���������Ϊid
//		for(int i = 0; i < centers.size(); i++){
//			// �ҵ�����ĵ�
//			int index = nearest(existLine, centers, i);
//			if(index == -1) continue;
//			
//			// ���������������ĵ��д���
//			LineSegment lineSeg = genLineSeg(centers.get(i).pos, centers.get(index).pos); // ���������������ĵ��д���
//			if(lineSeg == null) continue;
//			
//			// �����߶μ�����
//			lineSeg.mark = i+","+index; // �߶α�ʶ
//			double disToMid = MAX_DIS;
//			// �����Ѿ��е�lines�����ݽ��㣬��������߶εĶ˵�
//			for(int j = 0; j < lineSegList.size(); j++)
//			{
//				LineSegment seg = lineSegList.get(j);
//				// ��Ϊȷ��������Χ��͹�հ�������lineSegList�е������߶ε� start �� end ���Ѿ�ȷ����
//				LngLat crossPos = getCrossPos(lineSeg, seg);
//				if(crossPos == null) // �����߶�û�н���
//					continue;
////				double dis = Distance.lngLatDistance(lineSeg.mid, crossPos);
//				// �ж�crossPos��mid����һ�࣬ʹ����midΪԭ�������,����Ϊstart����Ϊend;б��Ϊ0ʱ������Ϊstart,��Ϊend
//				// �������������ж��Ǻ�start���Ǻ�end��ͬһ�࣬Ȼ�������������ֵ�ô�С���ж��ĸ���mid���
//				updateLineSeg(lineSeg, crossPos);
////				updateLineSeg(seg, crossPos);
//			}
//			lineSegList.add(lineSeg);
//			
////			if(lineSegList.size() >= 10)
////				break;
//		}
//		
//		// �γ�ÿ��������Ϣ��¼���߽�����У�
//			// ���������߶�,����mark�ҵ�ĳһ���������ض���
//		Map<Integer, ArrayList<LineSegment>> zoneLineSegMap = genZoneInfo(lineSegList);
//		/***��δ���****/
//		
//		
//		System.out.println("/** ���������߶ε��ٶȵ�ͼ�ϵ� json �ļ� **/");
//		genLineSegToJson(lineSegList, FilePath.dataFolder + "Cluster\\lineSeg.json");
//		
////		System.out.println("/** ���ɿ���ͶӰ���ٶȵ�ͼ�ϵ� json �ļ� **/");
////		genStationsToJsonFile(clusterStationMap, FilePath.dataFolder + "Cluster\\stations_kmeans_500.json");
	}

	/**
	 * ���ɱ߽��json���ݣ��������ļ� filePath ��
	 * @param edges
	 * @param string
	 */
	private static void genEngeToJson(List<Stack<LngLat>> edges, String filePath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
			bw.write("var lines = [\r\n");
			for(Stack<LngLat> edge : edges)
			{
				if( edge == null || edge.size() == 0 )
					continue;
				bw.write("[");
				for(int i = 0; i < edge.size(); i++)
				{
					LngLat pos = edge.get(i);
					bw.write(String.format("[%s],", pos.toBaidu()));
				}
				bw.write(String.format("[%s]", edge.get(0).toBaidu())); // ͷβ���
				bw.write("],\r\n");
			}
			bw.write("];\r\n");
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}		
		
	}

	public static List<LngLat> getPosCopy(List<Station> stations)
	{
		List<LngLat> res = new ArrayList<LngLat>();
		for(Station station : stations)
			res.add(station.pos.Copy());
		return res;
	}

	/**
	 * �ҵ�ĳ����������ص��߶�
	 * Key : ���ţ������ţ�
	 * Value : ��������Χ���߶��б�
	 * @param lineSegList 
	 * @return
	 */
	private static Map<Integer, ArrayList<LineSegment>> genZoneInfo(List<LineSegment> lineSegList) {
		Map<Integer, ArrayList<LineSegment>> zoneLineSegMap = new HashMap<Integer, ArrayList<LineSegment>>();
		for(LineSegment lineSeg : lineSegList)
		{
			String[] items = lineSeg.mark.split(",");
			for(int i = 0; i < items.length; i++)
			{
				int zoneId = Integer.parseInt(items[i]);
				if(zoneId == -1)
					break;
				if(!zoneLineSegMap.containsKey(zoneId))
					zoneLineSegMap.put(zoneId, new ArrayList<LineSegment>());
				zoneLineSegMap.get(zoneId).add(lineSeg);
			}
			
		}
		return zoneLineSegMap;
	}

	/**
	 * ���������߶��ڰٶȵ�ͼ�ϵ� json �ļ�
	 * @param lineSegList
	 * @param filePath
	 */
	public static void genLineSegToJson(List<LineSegment> lineSegList, String filePath) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
			bw.write("var lines = [\r\n");
			for(int i = 0; i < lineSegList.size(); i++)
			{
				LineSegment lineSeg = lineSegList.get(i);
				bw.write(String.format("[[%s],[%s]],\r\n", lineSeg.start.toBaidu(), lineSeg.end.toBaidu()));
			}
			bw.write("];\r\n");
			bw.close();
		}catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public static void genStationsToJsonFile(
			HashMap<Integer, ClusterStation> clusterStationMap, String filePath) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(filePath)));
		
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

	/**
	 * �ж�crossPos��mid����һ�࣬ʹ����midΪԭ�������,����Ϊstart����Ϊend;б��Ϊ0ʱ������Ϊstart,��Ϊend
	 * �������������ж��Ǻ�start���Ǻ�end��ͬһ�࣬Ȼ�������������ֵ�ô�С���ж��ĸ���mid���
	 */
	public static void updateLineSeg(LineSegment lineSeg, LngLat crossPos) {
		if(lineSeg.mark.equals("-1,-1"))
			return;
		LngLat crossPosVector = new LngLat(crossPos.lng - lineSeg.mid.lng, crossPos.lat - lineSeg.mid.lat);
		if(crossPosVector.lng > 0 || (crossPosVector.lng == 0 && crossPosVector.lat > 0)) // crossPos��endһ��
		{
			if(lineSeg.end == null)
				lineSeg.end = crossPos.Copy();
			else
			{
				LngLat endVector = new LngLat(lineSeg.end.lng - lineSeg.mid.lng, lineSeg.end.lat - lineSeg.mid.lat);
				if(Math.abs(crossPosVector.lng) < Math.abs(endVector.lng) 
						|| Math.abs(crossPosVector.lat) < Math.abs(endVector.lat)) // crossPos��end������mid
					lineSeg.end = crossPos.Copy();
			}
		}
		else // crossPos��startһ��
		{
			if(lineSeg.start == null)
				lineSeg.start = crossPos.Copy();
			else
			{
				LngLat startVector = new LngLat(lineSeg.start.lng - lineSeg.mid.lng, lineSeg.start.lat - lineSeg.mid.lat);
				if(Math.abs(crossPosVector.lng) < Math.abs(startVector.lng) 
						|| Math.abs(crossPosVector.lat) < Math.abs(startVector.lat)) // crossPos��start������mid
					lineSeg.start = crossPos.Copy();
			}
		}
	}
	
	/**
	 * �ҵ�����վ�����С͹�հ�
	 * @param stations
	 * @return
	 */
	public static List<LineSegment> genConvexHull(List<Station> stations) {
		List<LineSegment> edge = new ArrayList<LineSegment>();
		double offset = 0.0001; // ����΢��һ�㣬ȷ�����
		double xMin = 116;
		double xMax = 117;
		double yMin = 39.6;
		double yMax = 40.5;
		String mark = "-1,-1";
//		LngLat upLeft = new LngLat(xMin - offset, yMax + offset);
//		LngLat upRight = new LngLat(xMax + offset, yMax + offset);
//		LngLat downLeft = new LngLat(xMin - offset, yMin - offset);
//		LngLat downRight = new LngLat(xMax + offset, yMin - offset);
		LngLat upLeft = new LngLat(xMin, yMax);
		LngLat upRight = new LngLat(xMax, yMax);
		LngLat downLeft = new LngLat(xMin, yMin);
		LngLat downRight = new LngLat(xMax, yMin);
		edge.add( new LineSegment(mark, 0, 1, yMax, upLeft, null, upRight) ); // �ϱ߽�
		edge.add( new LineSegment(mark, 0, 1, yMin, downLeft, null, downRight) ); // �±߽�
		edge.add( new LineSegment(mark, 1, 0, 0, downLeft, null, upLeft) ); // ��߽�
		edge.add( new LineSegment(mark, 1, 0, 0, downRight, null, upRight) ); // �ұ߽�
		return edge;
	}

	/**
	 * �ҵ� ֱ��/�߶� �� �߶� �Ľ���
	 * L1������ֱ�ߣ����߻��߶�
	 * L2һ��Ϊ�߶�
	 */
	public static LngLat getCrossPos(LineSegment l1, LineSegment l2) {
		
		double x = 0;
		double y = 0;
		boolean deal = false; // �Ƿ��н���
		// �Ȱ����ǵ�������ֱ�ߣ��ҵ���������жϴ˵��Ƿ���ֱ�ߡ����߻��߶���
		if(Math.abs(l1.dx) < eps) // l1 ƽ���� y ��
		{
			if(Math.abs(l2.dx) >= eps) // l2 ��ƽ���� y ��
			{
				x = l1.mid.lng; // x = x1
				y = l2.dy * x / l2.dx + l2.b; // y = k2 * x + b2
				deal = true;
			}
			else // l2 ƽ���� y ��,������
				;
		}
		else if(Math.abs(l2.dx) < eps) // l2 ƽ���� y ��
		{
			x = l2.mid.lng;
			y = l1.dy * x / l1.dx + l1.b;
			deal = true;
		}
		else if(Math.abs(l1.dy) < eps) // l1 ƽ���� x ��
		{
			if(Math.abs(l2.dy) >= eps) // l2 ��ƽ���� x ��
			{
				y = l1.mid.lat; // y = y1
				x = (y - l2.b) * l2.dx / l2.dy; // x = (y - b2) / k2
				deal = true;
			}
			else // l2 ƽ���� x ��,������
				;
		}
		else if(Math.abs(l2.dy) < eps) // l2 ƽ���� x ��
		{
			y = l2.mid.lat; // y = y1
			x = (y - l1.b) * l1.dx / l1.dy; // x = (y - b1) / k1
			deal = true;
		}
		else
		{
			if(Math.abs(l1.dy/l1.dx - l2.dy/l2.dx) < eps) // l1 ƽ���� l2, ������
				;
			else
			{
				// x = (b2 - b1) / (k1 - k2)
				double k1 = l1.dy / l1.dx;
				double k2 = l2.dy / l2.dx;
				x = (l2.b - l1.b) / (k1 - k2);
				// y = k1 * x + b1
				y = k1 * x + l1.b;
				deal = true;
			}				
		}
		
		if(!deal)
			return null;
		
		LngLat p = new LngLat(x, y);
		if(isInLineSeg(p, l1) && isInLineSeg(p, l2)) // // �˽����Ƿ����������߶Σ�ֱ�ߣ���
			return p;
		
		return null;
	}
	
	/**
	 * �� P �Ƿ��� �߶� line ��
	 * @param p(ǰ�᣺p һ����ֱ���ϣ�����һ�����߶���)
	 * @return
	 */
	public static boolean isInLineSeg(LngLat p, LineSegment line)
	{
		if(line.start == null && line.end == null) // ֱ��
		{
			return true;
		}
		else // �߶λ�����
		{
			LngLat pVector = new LngLat(p.lng - line.mid.lng, p.lat - line.mid.lat);
			if(pVector.lng > 0 || (pVector.lng == 0 && pVector.lat > 0)) // crossPos��endһ��
			{
				if(line.end == null)
					return true;
				else
				{
					LngLat endVector = new LngLat(line.end.lng - line.mid.lng, line.end.lat - line.mid.lat);
					if(Math.abs(pVector.lng) < Math.abs(endVector.lng) 
							|| Math.abs(pVector.lat) < Math.abs(endVector.lat)) // crossPos��end������mid
						return true;
				}
			}
			else // crossPos��startһ��
			{
				if(line.start == null)
					return true;
				else
				{
					LngLat startVector = new LngLat(line.start.lng - line.mid.lng, line.start.lat - line.mid.lat);
					if(Math.abs(pVector.lng) < Math.abs(startVector.lng) 
							|| Math.abs(pVector.lat) < Math.abs(startVector.lat)) // crossPos��start������mid
						return true;
				}
			}
		}
		return false;
		
	}

	public static double eps = 0.000001;
	public static double MAX_DIS = 100000; // �������ޣ�������Ϊ��ʼ��minDis
	public static double Inf = Double.MAX_VALUE;
	public static LineSegment genLineSeg(LngLat pos1, LngLat pos2) {
		double dLat = pos1.lat - pos2.lat; // �൱��dy
		double dLng = pos1.lng - pos2.lng; // �൱��dx
		double k = 0;
		double b = 0;
		LngLat mid = new LngLat( (pos1.lng + pos2.lng)/2, (pos1.lat + pos2.lat)/2 );
		
		if(Math.abs(dLat) < eps) // ������Ϊ dy == 0,�������б��Ϊ0���д���б��Ϊ1/0=Inf
			k = Inf;
		else if(Math.abs(dLng) < eps) // ������Ϊ dx == 0,�������б��ΪInf���д���б��Ϊ0
		{
			k = 0;
			b = mid.lat;
		}
		else
		{
//			k = dLng / dLat; // k = 1 / (dy/dx) = dx / dy
			b = mid.lat - dLng * mid.lng / dLat;
		}
		// ����Ӧ����dy=dLat,dx=dLng; ������������д��ߣ�б���������д��ߵĵĵ���
		return new LineSegment(null, dLng, dLat, b, null, mid, null);
	}
	
	/**
	 * �ҵ� centers �о�������Ϊ i ��Ԫ�������Ԫ�أ��Ҵ�Ԫ���� i ֮�仹û���߶Σ�����
	 */
	public static int nearest(Set<String> existLine, List<Station> centers, int curIndex) {
		double minDis = MAX_DIS;
		int minIndex = -1;
		LngLat curPos = centers.get(curIndex).pos;
		for( int i = 0; i < centers.size(); i++){
			if(curIndex == i 
					|| existLine.contains(i+","+curIndex) || existLine.contains(curIndex+","+i)) // Ѱ�ҵ�ǰ�������ڵ�
				continue;
			double dis = Distance.lngLatDistance(curPos, centers.get(i).pos);
			if(dis < minDis)
			{
				minDis = dis;
				minIndex = i;
			}
		}
		if(minIndex != -1)
			existLine.add(curIndex+","+minIndex);
		return minIndex;
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
