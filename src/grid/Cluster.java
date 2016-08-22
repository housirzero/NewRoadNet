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

	// 还应该写一个对客流的排序函数
	public static void run() {

		String recordsFile = "E:/TransData/RouteRec/TripChain/20150104_TripChain3.csv";
		String lineVolumeFile = recordsFile.replace(".csv", "_Volume.csv");
		String stationVolumeFile = recordsFile.replace(".csv", "_StationVolume.csv");
		
		
		System.out.println("/** line **/");
		// 统计线路客流
//		statLinePassVolume(recordsFile, lineVolumeFile);
		// 选取客流最大的70条线路
		Set<String> topNLines = Graph.getTopNLines(lineVolumeFile, 100);

		
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
				return new Station("聚类中心", "聚类中心", -1, new LngLat(lng/list.size(), lat/list.size()), -1, null, null, -1, -1);
			}
		};
		kmeans.setMaxDis(maxDis);
		kmeans.cluster();
		System.out.println("迭代次数： " + kmeans.getIterTimes());
//		kmeans.display();

		
		System.out.println("/** 根据聚类结果生成网络 **/");
		int[] classId = kmeans.getClusterResult();
		for(int i = 0; i < classId.length; i++)
			stations.get(i).classId = classId[i];
		HashMap<Integer, ClusterStation> clusterStationMap = Graph.genGraph(stations);

		
//		System.out.println("/** 开始生成凸包 **/");
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
//		System.out.println("/** 1. 生成所有站点的一个大凸包 **/");
//		Stack<LngLat> stationsEdge = convexHull.graham(getPosCopy(stations));
//		edges.add(stationsEdge);
//
//		System.out.println("/** 2. 生成每一类所有站点的小凸包 **/");
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
		
		
//		System.out.println("/** 生成可以投影到ArcGis的文件 **/");
//		genStationsFile(stations, FilePath.dataFolder + "Cluster\\stations_gmm_500.csv");
//		genClusterStationsFile(clusterStationMap, FilePath.dataFolder + "Cluster\\clusterStations_gmm_500.csv");
		
//		// kmeans 聚类结果作为初始
//		// 找到所有站点的最小凸闭包，测试时简单的将北京画在一个矩形内
//		// edge中的mark设为"-1,-1"
//		List<LineSegment> edge = genConvexHull(stations); // 包含所有站点的最小凸闭包，元素主要有起点和终点确定
//		
//		// 在相近的两个类的核心点之间画中垂线，并结合最外层的凸闭包，将区域划分出来
//		/*
//		 * for s in stations
//		 * 		find s1,st dis(s,s1) is min
//		 * 		line1 is <s1,s> 的 中垂线
//		 * 			遍历已经有的lines，根据交点，更新线段的端点，并加入lineSeg列表中
//		 * 只要存在多个点被划分到一个区的情况，就在他们之间增加平分线
//		 */
//		Set<String> existLine = new HashSet<String>(); // 已经存在的线段，<class1,class2>
//		List<LineSegment> lineSegList = new ArrayList<LineSegment>(); // 线段列表
//		lineSegList.addAll(edge); // 包含所有站点的最小凸闭包
//		
//		// 聚类中心在centers 中的索引中作为id
//		for(int i = 0; i < centers.size(); i++){
//			// 找到最近的点
//			int index = nearest(existLine, centers, i);
//			if(index == -1) continue;
//			
//			// 生成两个聚类中心的中垂线
//			LineSegment lineSeg = genLineSeg(centers.get(i).pos, centers.get(index).pos); // 生成两个聚类中心的中垂线
//			if(lineSeg == null) continue;
//			
//			// 更新线段及交点
//			lineSeg.mark = i+","+index; // 线段标识
//			double disToMid = MAX_DIS;
//			// 遍历已经有的lines，根据交点，更新相关线段的端点
//			for(int j = 0; j < lineSegList.size(); j++)
//			{
//				LineSegment seg = lineSegList.get(j);
//				// 因为确定了最外围的凸闭包，所以lineSegList中的所有线段的 start 和 end 都已经确定了
//				LngLat crossPos = getCrossPos(lineSeg, seg);
//				if(crossPos == null) // 两条线端没有交点
//					continue;
////				double dis = Distance.lngLatDistance(lineSeg.mid, crossPos);
//				// 判断crossPos在mid的哪一侧，使用以mid为原点的向量,以左为start，右为end;斜率为0时，以下为start,上为end
//				// 根据向量符号判断是和start还是和end在同一侧，然后根据向量绝对值得大小来判断哪个离mid最近
//				updateLineSeg(lineSeg, crossPos);
////				updateLineSeg(seg, crossPos);
//			}
//			lineSegList.add(lineSeg);
//			
////			if(lineSegList.size() >= 10)
////				break;
//		}
//		
//		// 形成每个区的信息记录（边界点序列）
//			// 遍历所有线段,根据mark找到某一类的所有相关顶点
//		Map<Integer, ArrayList<LineSegment>> zoneLineSegMap = genZoneInfo(lineSegList);
//		/***尚未解决****/
//		
//		
//		System.out.println("/** 生成所有线段到百度地图上的 json 文件 **/");
//		genLineSegToJson(lineSegList, FilePath.dataFolder + "Cluster\\lineSeg.json");
//		
////		System.out.println("/** 生成可以投影到百度地图上的 json 文件 **/");
////		genStationsToJsonFile(clusterStationMap, FilePath.dataFolder + "Cluster\\stations_kmeans_500.json");
	}

	/**
	 * 生成边界的json数据，保存在文件 filePath 中
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
				bw.write(String.format("[%s]", edge.get(0).toBaidu())); // 头尾相接
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
	 * 找到某区号所有相关的线段
	 * Key : 区号（即类别号）
	 * Value : 此区域周围的线段列表
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
	 * 生成所有线段在百度地图上的 json 文件
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
	 * 判断crossPos在mid的哪一侧，使用以mid为原点的向量,以左为start，右为end;斜率为0时，以下为start,上为end
	 * 根据向量符号判断是和start还是和end在同一侧，然后根据向量绝对值得大小来判断哪个离mid最近
	 */
	public static void updateLineSeg(LineSegment lineSeg, LngLat crossPos) {
		if(lineSeg.mark.equals("-1,-1"))
			return;
		LngLat crossPosVector = new LngLat(crossPos.lng - lineSeg.mid.lng, crossPos.lat - lineSeg.mid.lat);
		if(crossPosVector.lng > 0 || (crossPosVector.lng == 0 && crossPosVector.lat > 0)) // crossPos在end一侧
		{
			if(lineSeg.end == null)
				lineSeg.end = crossPos.Copy();
			else
			{
				LngLat endVector = new LngLat(lineSeg.end.lng - lineSeg.mid.lng, lineSeg.end.lat - lineSeg.mid.lat);
				if(Math.abs(crossPosVector.lng) < Math.abs(endVector.lng) 
						|| Math.abs(crossPosVector.lat) < Math.abs(endVector.lat)) // crossPos比end更靠近mid
					lineSeg.end = crossPos.Copy();
			}
		}
		else // crossPos在start一侧
		{
			if(lineSeg.start == null)
				lineSeg.start = crossPos.Copy();
			else
			{
				LngLat startVector = new LngLat(lineSeg.start.lng - lineSeg.mid.lng, lineSeg.start.lat - lineSeg.mid.lat);
				if(Math.abs(crossPosVector.lng) < Math.abs(startVector.lng) 
						|| Math.abs(crossPosVector.lat) < Math.abs(startVector.lat)) // crossPos比start更靠近mid
					lineSeg.start = crossPos.Copy();
			}
		}
	}
	
	/**
	 * 找到所有站点的最小凸闭包
	 * @param stations
	 * @return
	 */
	public static List<LineSegment> genConvexHull(List<Station> stations) {
		List<LineSegment> edge = new ArrayList<LineSegment>();
		double offset = 0.0001; // 都稍微多一点，确保封闭
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
		edge.add( new LineSegment(mark, 0, 1, yMax, upLeft, null, upRight) ); // 上边界
		edge.add( new LineSegment(mark, 0, 1, yMin, downLeft, null, downRight) ); // 下边界
		edge.add( new LineSegment(mark, 1, 0, 0, downLeft, null, upLeft) ); // 左边界
		edge.add( new LineSegment(mark, 1, 0, 0, downRight, null, upRight) ); // 右边界
		return edge;
	}

	/**
	 * 找到 直线/线段 与 线段 的交点
	 * L1可能是直线，射线或线段
	 * L2一定为线段
	 */
	public static LngLat getCrossPos(LineSegment l1, LineSegment l2) {
		
		double x = 0;
		double y = 0;
		boolean deal = false; // 是否有交点
		// 先把他们当成两条直线，找到交点后，在判断此点是否在直线、射线或线段上
		if(Math.abs(l1.dx) < eps) // l1 平行于 y 轴
		{
			if(Math.abs(l2.dx) >= eps) // l2 不平行于 y 轴
			{
				x = l1.mid.lng; // x = x1
				y = l2.dy * x / l2.dx + l2.b; // y = k2 * x + b2
				deal = true;
			}
			else // l2 平行于 y 轴,不处理
				;
		}
		else if(Math.abs(l2.dx) < eps) // l2 平行于 y 轴
		{
			x = l2.mid.lng;
			y = l1.dy * x / l1.dx + l1.b;
			deal = true;
		}
		else if(Math.abs(l1.dy) < eps) // l1 平行于 x 轴
		{
			if(Math.abs(l2.dy) >= eps) // l2 不平行于 x 轴
			{
				y = l1.mid.lat; // y = y1
				x = (y - l2.b) * l2.dx / l2.dy; // x = (y - b2) / k2
				deal = true;
			}
			else // l2 平行于 x 轴,不处理
				;
		}
		else if(Math.abs(l2.dy) < eps) // l2 平行于 x 轴
		{
			y = l2.mid.lat; // y = y1
			x = (y - l1.b) * l1.dx / l1.dy; // x = (y - b1) / k1
			deal = true;
		}
		else
		{
			if(Math.abs(l1.dy/l1.dx - l2.dy/l2.dx) < eps) // l1 平行于 l2, 不处理
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
		if(isInLineSeg(p, l1) && isInLineSeg(p, l2)) // // 此交点是否在这两条线段（直线）上
			return p;
		
		return null;
	}
	
	/**
	 * 点 P 是否在 线段 line 上
	 * @param p(前提：p 一定在直线上，但不一定在线段上)
	 * @return
	 */
	public static boolean isInLineSeg(LngLat p, LineSegment line)
	{
		if(line.start == null && line.end == null) // 直线
		{
			return true;
		}
		else // 线段或射线
		{
			LngLat pVector = new LngLat(p.lng - line.mid.lng, p.lat - line.mid.lat);
			if(pVector.lng > 0 || (pVector.lng == 0 && pVector.lat > 0)) // crossPos在end一侧
			{
				if(line.end == null)
					return true;
				else
				{
					LngLat endVector = new LngLat(line.end.lng - line.mid.lng, line.end.lat - line.mid.lat);
					if(Math.abs(pVector.lng) < Math.abs(endVector.lng) 
							|| Math.abs(pVector.lat) < Math.abs(endVector.lat)) // crossPos比end更靠近mid
						return true;
				}
			}
			else // crossPos在start一侧
			{
				if(line.start == null)
					return true;
				else
				{
					LngLat startVector = new LngLat(line.start.lng - line.mid.lng, line.start.lat - line.mid.lat);
					if(Math.abs(pVector.lng) < Math.abs(startVector.lng) 
							|| Math.abs(pVector.lat) < Math.abs(startVector.lat)) // crossPos比start更靠近mid
						return true;
				}
			}
		}
		return false;
		
	}

	public static double eps = 0.000001;
	public static double MAX_DIS = 100000; // 距离上限，用于作为初始的minDis
	public static double Inf = Double.MAX_VALUE;
	public static LineSegment genLineSeg(LngLat pos1, LngLat pos2) {
		double dLat = pos1.lat - pos2.lat; // 相当于dy
		double dLng = pos1.lng - pos2.lng; // 相当于dx
		double k = 0;
		double b = 0;
		LngLat mid = new LngLat( (pos1.lng + pos2.lng)/2, (pos1.lat + pos2.lat)/2 );
		
		if(Math.abs(dLat) < eps) // 可以认为 dy == 0,则两点间斜率为0，中垂线斜率为1/0=Inf
			k = Inf;
		else if(Math.abs(dLng) < eps) // 可以认为 dx == 0,则两点间斜率为Inf，中垂线斜率为0
		{
			k = 0;
			b = mid.lat;
		}
		else
		{
//			k = dLng / dLat; // k = 1 / (dy/dx) = dx / dy
			b = mid.lat - dLng * mid.lng / dLat;
		}
		// 正常应该是dy=dLat,dx=dLng; 但由于所求的中垂线，斜率正好是中垂线的的倒数
		return new LineSegment(null, dLng, dLat, b, null, mid, null);
	}
	
	/**
	 * 找到 centers 中距离索引为 i 的元素最近的元素（且此元素与 i 之间还没有线段）索引
	 */
	public static int nearest(Set<String> existLine, List<Station> centers, int curIndex) {
		double minDis = MAX_DIS;
		int minIndex = -1;
		LngLat curPos = centers.get(curIndex).pos;
		for( int i = 0; i < centers.size(); i++){
			if(curIndex == i 
					|| existLine.contains(i+","+curIndex) || existLine.contains(curIndex+","+i)) // 寻找当前还不存在的
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
