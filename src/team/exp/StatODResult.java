package team.exp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import team.routerec.main.RouteRec;
import team.trans.zone.TransZoneMap;

public class StatODResult {
	
	public static int[][] distance = new int[RouteRec.regionNum+1][RouteRec.regionNum+1];

	public static void main(String[] args) {
		List<ODResult> mode0_res = getODResultList("E:\\TransData\\RouteRec\\Result\\mode0_time2.csv");
		List<ODResult> mode123_res = getODResultList("E:\\TransData\\RouteRec\\Result\\mode123_time2.csv");
		List<ODResult> mode0123_res = getODResultList("E:\\TransData\\RouteRec\\Result\\mode0123_time2.csv");
		for( int dis = 5; dis <= 30; dis += 5 )
		{
			System.out.println("y" + dis + "=[");
			statProb(getDisResultList(mode0_res, dis));
			statProb(getDisResultList(mode123_res, dis));
			statProb(getDisResultList(mode0123_res, dis));
			System.out.println("];\r\n");
		}
	}
	
	public static List<ODResult> getODResultList( String filePath )
	{
		List<ODResult> res = null;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			res = new ArrayList<ODResult>();
			String line = "";
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				int costTime = Integer.parseInt(items[3]);
				int O = Integer.parseInt(items[0]);
				int D = Integer.parseInt(items[1]);
				// O	D	time	realTime	realDistance	transNum	Probability
				if(distance[O][D] < 1)
					distance[O][D] = (int) (TransZoneMap.getDisInRegions(O, D)/1000 + 1);

				if(costTime == -1)
				{
					res.add(new ODResult(O, D, Integer.parseInt(items[2]), 0, distance[O][D], 0, 0));
					continue;
				}
				ODResult obj = new ODResult(O, D, 
						Integer.parseInt(items[2]), costTime,
						distance[O][D], 
						Integer.parseInt(items[5]),
						Double.parseDouble(items[6]));
				res.add(obj);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public static List<ODResult> getDisResultList( List<ODResult> ods, int dis )
	{
		List<ODResult> res = new ArrayList<ODResult>();
		for( ODResult obj : ods )
		{
			if(obj.dis == dis)
				res.add(obj);
		}
		return res;
	}


	public static List<ODResult> getTimeBudgetResultList( List<ODResult> ods, int timeBudget )
	{
		List<ODResult> res = new ArrayList<ODResult>();
		for( ODResult obj : ods )
		{
			if(obj.timeBudget == timeBudget)
				res.add(obj);
		}
		return res;
	}

	public static void statProb( List<ODResult> disRes )
	{
		for(int t = 0; t <= 60; t+=10)
		{
			List<ODResult> timeBudget = getTimeBudgetResultList(disRes, t);
			if( timeBudget == null || timeBudget.size() == 0 )
			{
				System.out.print("\t0");
				continue;
			}
			double prob = 0.0;
			for( ODResult obj : timeBudget )
				prob += obj.prob;
			prob /= timeBudget.size();
			System.out.print("\t" + prob);
		}
		System.out.println();
	}
}
