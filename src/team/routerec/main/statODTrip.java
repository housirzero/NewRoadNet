package team.routerec.main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import team.support.function.FilePath;


public class statODTrip {

	public static void main(String[] args) {
		int time = 3;
		Map<String, ODTrip> mode3Map = genODTripMap(FilePath.dataFolder + "mode_time\\mode3_time"+time+"_D305_306.csv");
		Set<String> mode3Set = getODPairSet(mode3Map);
		Map<String, ODTrip> mode123Map = genODTripMap(FilePath.dataFolder + "mode_time\\mode123_time"+time+"_D305_306.csv");
		Set<String> mode123Set = getODPairSet(mode123Map);

		mode3Set.retainAll(mode123Set);
		System.out.println(mode3Set.size());
		System.out.println("O\tD\tDistance\tmode3\tmode123");
//		System.out.println("O\tD\tDistance\tTaxi\tFastBus\tSubWay");
		for(String key : mode3Set)
		{
			System.out.print(key.replace(",", "\t")+"\t");
			System.out.print(String.format("%.1f\t", mode3Map.get(key).distance/1000));
			System.out.print(mode3Map.get(key).costTime + "\t");
			System.out.print(mode123Map.get(key).costTime + "\n");
		}
	}
	
	public static void run() {
		int time = 2;
		Map<String, ODTrip> mode0Map = genODTripMap(FilePath.dataFolder + "mode_time\\mode0_time"+time+"_D502_503.csv");
		Set<String> mode0Set = getODPairSet(mode0Map);
		Map<String, ODTrip> mode1Map = genODTripMap(FilePath.dataFolder + "mode_time\\mode1_time"+time+"_D502_503.csv");
		Set<String> mode1Set = getODPairSet(mode1Map);
		Map<String, ODTrip> mode2Map = genODTripMap(FilePath.dataFolder + "mode_time\\mode2_time"+time+"_D502_503.csv");
		Set<String> mode2Set = getODPairSet(mode2Map);
		Map<String, ODTrip> mode3Map = genODTripMap(FilePath.dataFolder + "mode_time\\mode3_time"+time+"_D502_503.csv");
		Set<String> mode3Set = getODPairSet(mode3Map);

		mode0Set.retainAll(mode1Set);
		mode0Set.retainAll(mode2Set);
		mode0Set.retainAll(mode3Set);
		System.out.println(mode0Set.size());
		System.out.println("O\tD\tDistance\tTaxi\tBus\tFastBus\tSubWay");
//		System.out.println("O\tD\tDistance\tTaxi\tFastBus\tSubWay");
		for(String key : mode0Set)
		{
			System.out.print(key.replace(",", "\t")+"\t");
			System.out.print(String.format("%.1f\t", mode0Map.get(key).distance/1000));
			System.out.print(mode0Map.get(key).costTime + "\t");
			System.out.print(mode1Map.get(key).costTime + "\t");
			System.out.print(mode2Map.get(key).costTime + "\t");
			System.out.print(mode3Map.get(key).costTime + "\n");
		}
	}
	
	public static Set<String> getODPairSet(Map<String, ODTrip> ODTripMap)
	{
		Set<String> ODPairs = new HashSet<String>();
		for(Entry<String, ODTrip> entry : ODTripMap.entrySet())
			ODPairs.add(entry.getKey());
		return ODPairs;
	}
	
	/**
	 * 生成有路径可达的ODTrip
	 */
	public static Map<String, ODTrip> genODTripMap(String filePath)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			reader.readLine(); // 表头
			String line = null;
			
			Map<String, ODTrip> ODTripMap = new HashMap<String, ODTrip>();
			while ((line = reader.readLine()) != null)
			{
				String[] items = line.split(",");
				if(items.length < 7)
					continue;
				int o = Integer.parseInt(items[0]);
				int d = Integer.parseInt(items[1]);
				int costTime = Integer.parseInt(items[3]);
				double distance = Double.parseDouble(items[4]);
				ODTripMap.put(o+","+d, new ODTrip(o, d, costTime, distance));
			}
			reader.close();
			return ODTripMap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
