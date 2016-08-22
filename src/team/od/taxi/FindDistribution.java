package team.od.taxi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FindDistribution {
	public static int getTimeRegion(String pathTime)
	{
		int region = 0;
		//每分钟一个区域，1~60，60以上一个区域，共60个区域
		//即                         60~3600，3600~
		int time = Integer.valueOf(pathTime);
		if(time>3600)
			return 60;
		return time/60;
	}
	public static void dealDistribution()
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		Map<String, int[]> map = new HashMap<String, int[]>();
		String oZone=null,dZone=null;
		String zoneKey = null;//作为键值
		String pathTime = null;
		int[] time;
		try {
			br = new BufferedReader(new FileReader("E:\\Data\\20130801_OD.csv"));
			bw = new BufferedWriter(new FileWriter(new File("E:\\Data\\20130801_Distribution.csv")));
			String line = null;
			while((line = br.readLine())!=null)
			{
				oZone = line.split(",")[4];
				dZone = line.split(",")[8];
				pathTime = line.split(",")[9];
				zoneKey = oZone + "," +dZone;
				//System.out.println("regionID = "+ getTimeRegion(pathTime));
				if(map.containsKey(zoneKey)==true)
					map.get(zoneKey)[getTimeRegion(pathTime)]++;
				else
				{
					time = new int[61];
					time[getTimeRegion(pathTime)]++;
					map.put(zoneKey,time);
				}
				//System.out.println(map.get(zoneKey)[getTimeRegion(pathTime)]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("AddDone");
		int len = 0;
		for(Map.Entry<String, int[]> entry : map.entrySet()){
			System.out.print(entry.getKey()+"--->");
			len = entry.getValue().length;
			System.out.println("len = "+len);
			for(int i = 0 ; i < len ; i++ )
				System.out.print(entry.getValue()[i]+" ");
			System.out.println();
		}
	}

	public static void main(String[] args) {
		dealDistribution();
	}

}
