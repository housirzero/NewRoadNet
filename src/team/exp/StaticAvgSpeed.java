package team.exp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import team.routerec.main.CalProbability;
import team.routerec.main.RouteRec;
import team.support.function.FilePath;

/**
 * 统计立水桥-->和平西桥
 * 快速公交、公交和出租车在早、晚和平时的时候平均速度
 */
public class StaticAvgSpeed {

	public static void main(String[] args) {
		run();
	}

	public static int busLine = 0;
	public static int subLine = 0;
	public static int taxiLine = 0;
	public static String date = null;
	public static void run() {

		File folder = new File(FilePath.dataFolder + "RouteRec\\TripChain");
		File[] files = folder.listFiles();
		for( File file : files)
		{
			if(file.isFile() && file.getName().endsWith("TripChain3.csv"))
			{
				date = file.getName().split("_")[0].substring(6);
				System.out.println(file.getName() + " Start!");
				stat(file);
				System.out.println(file.getName() + " Done!");
			}
		}
		System.out.println(String.format("busLine=%d, subLine=%d", busLine, subLine));
		
//		File taxiFolder = new File(FilePath.dataFolder + "RouteRec\\Taxi");
//		File[] taxiFiles = taxiFolder.listFiles();
//		for( File file : taxiFiles)
//		{
//			if(file.isFile() && file.getName().endsWith("space.csv"))
//			{
//				System.out.println(file.getName() + " Start!");
////				DealTaxiNet2.dealNetBySpace(file);
//				try {
//					BufferedReader br = new BufferedReader(new FileReader(file));
//					while (br.readLine() != null)
//						taxiLine++;
//					br.close();
//				} catch (FileNotFoundException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				System.out.println(file.getName() + " Done!");
//			}
//		}
//		System.out.println(String.format("taxiLine=%d", taxiLine));

	}

	private static int O = 930; // 立水桥
//	private static int O = 164;
//	private static int D = 69; // 和平西桥
	private static int D = 503; // 安贞门
	
	private static String[] lines = new String[]{"108","124","301","328","379","380","387","408","419",
		"426","479","620","653","753","905","快速公交3线"};
	private static Set<String> lineSet = new HashSet<String>(Arrays.asList(lines));
	
	private static int modeNum = 4; // 快速公交、公交和出租车、地铁四种模式
	private static int timeRegionNum = 3; // 早、晚和平时三个时段
	

	private static int time = 0;
	private static int count = 0;
	
	public static void stat( File file )
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(file));
			bw = new BufferedWriter(new FileWriter("C:\\Users\\DELL\\Desktop\\ODPair.csv"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			String line = "";
			int typeIndex = 2; // B,R,K
			int aboardTimeIndex = 'F' - 'A'; // 上车时间索引
			int alightTimeIndex = 'M' - 'A'; // 下车时间索引
			int aboardZoneIndex = 'L' - 'A'; // 上车区号索引
			int alightZoneIndex = 'S' - 'A'; // 下车区号索引
			int dtIndex = 'U' - 'A'; // 乘车时间间隔索引
			int aboardLineIndex = 'G' - 'A'; // 上车线路名索引
			
			int linecount = 0;
			int fineLine = 0;

//			Set<String> ODPairs = new HashSet<String>();
			String O = "大屯路东"; // 499
			String D = "安贞门"; // 
			Map<String,Integer> ODPairMap = new HashMap<String,Integer>();
			// W,P 都是先计算次数，最后再算百分比
			while ((line = br.readLine()) != null) {
				linecount++;
				if(linecount % 1000000 == 0)
					System.out.println(linecount/10000+"W");
				String[] items = line.split(",");

				int dt = Integer.parseInt(items[dtIndex]);
				if(	dt < 1 ||
						!items[aboardTimeIndex].split(" ")[0].split("-")[2].equals(date) ||
						!items[alightTimeIndex].split(" ")[0].split("-")[2].equals(date))
					continue;

//				int aboardZone = -1;
//				int alightZone = -1;
//				try {
//					aboardZone = Integer.parseInt(items[aboardZoneIndex]);
//					alightZone = Integer.parseInt(items[alightZoneIndex]);
//				} catch (NumberFormatException e) {
//					e.printStackTrace();
//				}
//				if(aboardZone != O || alightZone != D)
//					continue;

				String aboardLine = items[aboardLineIndex]; // 上车线路名
//				if(aboardZone != -1 && alightZone != -1 && lineSet.contains(aboardLine))
//				{
//					String key = aboardLine+","+aboardZone+","+alightZone;
//					if(!ODPairMap.containsKey(key))
//						ODPairMap.put(key, 1);
//					else
//						ODPairMap.put(key, ODPairMap.get(key) + 1);
//				}
//				if(dt >= RouteRec.timeSlice * RouteRec.timeSliceNum ) // 超过数组界限
//					dt = RouteRec.timeSlice * RouteRec.timeSliceNum - 1;
//
//				int m = 1;
				
//				// 0 出租 1 公交 2 快速公交 3 地铁
				if(items[typeIndex].equals("R") && 
						items['H'-'A'].contains(O) && 
						items['O'-'A'].contains(D) )
				{
					time += dt;
					count++;
				}

//				if(items[aboardTimeIndex].split(" ")[0].equals("2015-01-04") &&
//						items[alightTimeIndex].split(" ")[0].equals("2015-01-04") &&
//						items[typeIndex].equals("R") && aboardLine.startsWith("5号线") )
//				{ 
//					if(items['H'-'A'].contains(O) && items['O'-'A'].contains(D) )
//					{
//
//						if(dt > 10000)
//							System.out.println(line);
//						time += dt;
//						count++;
//					}
//				}
				
				if(items[typeIndex].equals("B"))
					busLine++;
				else if(items[typeIndex].equals("R"))
					subLine++;
//				else // K : 自行车
//					continue;
//
//				int timeRegion = 2;
//				if( CalProbability.isInTimeRegion(items[aboardTimeIndex], 1)  )
//					timeRegion = 0; 
//				else if( CalProbability.isInTimeRegion(items[aboardTimeIndex], 2)  )
//					timeRegion = 1; 
//
//				costTime[m][timeRegion] += dt;
//				count[m][timeRegion]++;
			}
			if(count > 0)
				System.out.println(O + "-->" + D + " : " + time/count + "(count=" + count + ")");
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
