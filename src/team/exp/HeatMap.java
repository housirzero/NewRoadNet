package team.exp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import team.net.graph.LngLat;
import team.routerec.main.CalProbability;
import team.support.function.Convert;
import team.trans.zone.TransZoneMap;

public class HeatMap {

	/**
	 * 获取不同站点的客流数<br>
	 * Map<站点位置, 客流>
	 */
	public static Map<String, Integer> getStations(String filePath, int timeRegion) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Map<String, Integer> stations = new HashMap<String, Integer>();
		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				String on_lng = items[9]; // ['J'-'A'];
				String on_lat = items[10]; // ['K'-'A'];
				String on_time = items[5]; // 上车时间
				String on_station = on_lng + "," + on_lat;
				if ( isInTimeRegion(on_time, timeRegion)) {
					if (!stations.containsKey(on_station))
						stations.put(on_station, 1);
					else
						stations.put(on_station, stations.get(on_station) + 1);
				}

				String off_lng = items[16]; // ['Q'-'A'];
				String off_lat = items[17]; // ['R'-'A'];
				String off_time = items[12]; // 下车时间
				String off_station = off_lng + "," + off_lat;
				if ( isInTimeRegion(off_time, timeRegion)) {
					if (!stations.containsKey(off_station))
						stations.put(off_station, 1);
					else
						stations.put(off_station, stations.get(off_station) + 1);
				}
			}
			br.close();
			return stations;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取lineId线路不同站点的客流数<br>
	 * Map<站点位置, 客流>
	 */
	public static void genLinePassFlow(String filePath, String lineId) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Map<String, Integer> stations = new HashMap<String, Integer>();
		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				String on_lng = items[9]; // ['J'-'A'];
				String on_lat = items[10]; // ['K'-'A'];
				String on_time = items[5]; // 上车时间
				String on_station = items['G'-'A'] + "," + items['H'-'A'];
				if ( (items['G'-'A'].equals(lineId) || items['G'-'A'].startsWith("快速公交")) && CalProbability.isInTimeRegion(on_time, 1)) {
//				if ( items['G'-'A'].equals(lineId) && CalProbability.isInTimeRegion(on_time, 1)) {
					if (!stations.containsKey(on_station))
						stations.put(on_station, 1);
					else
						stations.put(on_station, stations.get(on_station) + 1);
				}

				String off_lng = items[16]; // ['Q'-'A'];
				String off_lat = items[17]; // ['R'-'A'];
				String off_time = items[12]; // 下车时间
				String off_station = items['N'-'A'] + "," + items['O'-'A'];
				if ( (items['N'-'A'].equals(lineId) || items['N'-'A'].startsWith("快速公交")) && CalProbability.isInTimeRegion(off_time, 1)) {
//				if ( items['N'-'A'].equals(lineId) && CalProbability.isInTimeRegion(off_time, 1)) {
					if (!stations.containsKey(off_station))
						stations.put(off_station, 1);
					else
						stations.put(off_station, stations.get(off_station) + 1);
				}
			}
			br.close();
//			saveLineFlowToFile(stations);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取不同站点各个时段的客流<br>
	 * Map<(line,station,t), 客流>
	 */
	public static void genTStationPassFlow(String filePath, String lineId) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Map<String, int[]> stations = new HashMap<String, int[]>();
		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				String on_time = items[5]; // 上车时间
				// 2015-01-04 06:51:55
				int on_t = Integer.parseInt(on_time.split(" ")[1].split(":")[0]);
				String on_station = items['G'-'A'] + "," + items['H'-'A'];
				if ( (items['G'-'A'].equals(lineId) || items['G'-'A'].startsWith("快速公交")) ) {
					if (!stations.containsKey(on_station))
						stations.put(on_station, new int[24]);
					else
					{
						int[] a = stations.get(on_station);
						a[on_t]++;
					}
				}

				String off_time = items[12]; // 下车时间
				int off_t = Integer.parseInt(off_time.split(" ")[1].split(":")[0]);
				String off_station = items['N'-'A'] + "," + items['O'-'A'];
				if ( (items['N'-'A'].equals(lineId) || items['N'-'A'].startsWith("快速公交"))) {
					if (!stations.containsKey(off_station))
						stations.put(off_station, new int[24]);
					else
					{
						int[] a = stations.get(off_station);
						a[on_t]++;
					}
				}
			}
			br.close();
			saveLineFlowToFile(stations);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取不同小区的客流数<br>
	 * Map<站点位置, 客流>
	 */
	public static Map<Integer, Integer> getZones(String filePath) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Map<Integer, Integer> stations = new HashMap<Integer, Integer>();
		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				int on_zone = Integer.parseInt( items[11] ); // ['L'-'A'];
				String on_time = items[5]; // 上车时间
				if ( CalProbability.isInTimeRegion(on_time, 1)) {
					if (!stations.containsKey(on_zone))
						stations.put(on_zone, 1);
					else
						stations.put(on_zone, stations.get(on_zone) + 1);
				}

				int off_zone = Integer.parseInt( items[18] ); // ['S'-'A'];
				String off_time = items[12]; // 下车时间
				if ( CalProbability.isInTimeRegion(off_time, 1)) {
					if (!stations.containsKey(off_zone))
						stations.put(off_zone, 1);
					else
						stations.put(off_zone, stations.get(off_zone) + 1);
				}
			}
			br.close();
			return stations;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * 将统计结果保存成热力图的json结果<br>
	 * div是因为统计的时间长短不一样，高峰期客流大时间段，平峰期客流小但时间长，这就导致二者总量相近，故而除以相应的时间(小时数)
	 */
	public static void saveToFile(Map<String, Integer> stationMap, String savefile, int div) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(savefile)));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		int counts = 0;
		try {
			bw.write("var points =[\r\n");
			for (Entry<String, Integer> entry : stationMap.entrySet()) {
				String[] items = entry.getKey().split(",");
				LngLat baidu = Convert.GG2BD(new LngLat(items[0], items[1]));
				int times = entry.getValue() / div;
				if(times < 1)
					continue;
				bw.write(String.format("{\"lng\":%f,\"lat\":%f,\"count\":%d},\r\n",
						baidu.lng, baidu.lat, times));
				counts += times;
			}
			bw.write("];\r\n");
			bw.write("// counts = " + counts);
			System.out.println(counts);
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 保存5号线客流结果
	public static void saveLineFlowToFile(Map<String, int[]> stationMap) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\DELL\\Desktop\\data_duke_line5_time1.csv")));
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		try {
			for (Entry<String, int[]> entry : stationMap.entrySet()) {
				String[] items = entry.getKey().split(",");
//				LngLat baidu = Convert.GG2BD(new LngLat(items[1], items[2]));
				int[] times = entry.getValue();
				String res = items[0] + "," + items[1];
				for(int i = 0; i < times.length; i++)
					res += "," + times[i];
				bw.write(res + "\r\n");
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void genOD(String filePath) 
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\DELL\\Desktop\\OD.json")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Set<String> ODSet = new HashSet<String>();
		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
//				if ( !(items['G'-'A'].equals("5号线") || items['G'-'A'].startsWith("快速公交32")) )
//					continue;
//				if ( !(items['N'-'A'].equals("5号线") || items['N'-'A'].startsWith("快速公交32")) )
//					continue;
				if( !items['H'-'A'].startsWith("惠新西街南口") && !items['O'-'A'].startsWith("惠新西街南口") )
					continue;

				String on_lng = items[9]; // ['J'-'A'];
				String on_lat = items[10]; // ['K'-'A'];

				String off_lng = items[16]; // ['Q'-'A'];
				String off_lat = items[17]; // ['R'-'A'];
				
				String key = on_lng + "," + on_lat + "," + off_lng + "," + off_lat;
				ODSet.add(key);
				if(ODSet.size() > 800)
					break;
			}
			br.close();
			System.out.println(ODSet.size());
			
			bw.write("var data = [");
			for(String key : ODSet)
			{
				String[] s = key.split(",");
				bw.write(String.format("{on_lng : %s, on_lat : %s, off_lng : %s, off_lat : %s},", s[0], s[1], s[2], s[3]));
			}
			bw.write("];");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void genODRegion(String filePath) 
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\DELL\\Desktop\\OD.json")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String,Integer> ODMap = new HashMap<String, Integer>();
		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
//				if ( !(items['G'-'A'].equals("5号线") || items['G'-'A'].startsWith("快速公交32")) )
//					continue;
//				if ( !(items['N'-'A'].equals("5号线") || items['N'-'A'].startsWith("快速公交32")) )
//					continue;
//				if( !items['H'-'A'].startsWith("惠新西街南口") && !items['O'-'A'].startsWith("惠新西街南口") )
//					continue;

//				String on_lng = items[9]; // ['J'-'A'];
//				String on_lat = items[10]; // ['K'-'A'];
				String on_zone = items[11];

//				String off_lng = items[16]; // ['Q'-'A'];
//				String off_lat = items[17]; // ['R'-'A'];
				String off_zone = items[18];

//				String key = on_lng + "," + on_lat + "," + off_lng + "," + off_lat;
				String key = on_zone + "," + off_zone;
				if (!ODMap.containsKey(key))
					ODMap.put(key, 1);
				else
					ODMap.put(key, ODMap.get(key) + 1);
			}
			br.close();
			System.out.println(ODMap.size());
			
			bw.write("var data = [\r\n");
			for(Entry<String, Integer> entry : ODMap.entrySet())
			{
//				bw.write( entry.getValue() + "\r\n" );
				if( entry.getValue() < 1000 )
					continue;
				String[] s = entry.getKey().split(",");
				int on_zone = Integer.parseInt(s[0]);
				int off_zone = Integer.parseInt(s[1]);
				if(on_zone == -1 || off_zone == -1)
					continue;

				LngLat on_pos = TransZoneMap.getRegionGps(on_zone);
				LngLat off_pos = TransZoneMap.getRegionGps(off_zone);
				// getRegionGps
				bw.write(String.format("{on_lng : %s, on_lat : %s, off_lng : %s, off_lat : %s},", 
						on_pos.lng, on_pos.lat, off_pos.lng, off_pos.lat));
			}
			bw.write("];");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 保存交通小区客流统计结果
	public static void saveZonesToFile(Map<Integer, Integer> stationMap) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\DELL\\Desktop\\data_duke_time1.csv")));
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		try {
			for (Entry<Integer, Integer> entry : stationMap.entrySet()) {
				int zone = entry.getKey();
				int times = entry.getValue();
				bw.write(zone + "," + times + "\r\n");
			}
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 1 : ZGF(06:00-09:30 am)
	 * 2 : the daily records (09:30-16:30)
	 * 3 : WGF (16:30-20:30pm)
	 */
	public static boolean isInTimeRegion( String timeStr, int t )
	{
		String time = timeStr.split(" ")[1];
		switch (t) {
		case 1:
			return time.compareTo("06:30") > 0 && time.compareTo("09:30") < 0; // 3 hours
		case 2:
			return time.compareTo("09:30") > 0 && time.compareTo("16:30") < 0; // 7 hours
		case 3:
			return time.compareTo("16:30") > 0 && time.compareTo("20:30") < 0; // 4 hours
		default:
			System.out.println("时间段只能是1,2,3");
			return false;
		}
	}
	
	public static void main(String[] args) {
		Date start = new Date();
		String filePath = "E:\\TransData\\RouteRec\\TripChain\\20150104_TripChain3.csv";
		Map<String, Integer> stationMap = getStations(filePath,1);
		saveToFile(stationMap, "C:\\Users\\DELL\\Desktop\\heatMap1.js", 3);
		stationMap = getStations(filePath,2);
		saveToFile(stationMap, "C:\\Users\\DELL\\Desktop\\heatMap2.js", 7);
		stationMap = getStations(filePath,3);
		saveToFile(stationMap, "C:\\Users\\DELL\\Desktop\\heatMap3.js", 4);
//		Map<Integer, Integer> zoneMap = getZones("E:\\TransData\\RouteRec\\TripChain\\20150104_TripChain3.csv");
//		saveZonesToFile(zoneMap);
		
//		genLinePassFlow(filePath, "5号线");
//		genTStationPassFlow(filePath, "5号线");
//		genODRegion(filePath);
//		genOD(filePath);
		Date end = new Date();
		System.out.println("cost time : " + (end.getTime() - start.getTime()) / 1000.0 + " s.");
		System.out.println("**********************************************************");
	}

}
