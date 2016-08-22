package team.routerec.main;

import java.util.HashMap;

public class Zone {
	public HashMap<Integer, double[][]> linkList = new HashMap<Integer, double[][]>();
	public double[][] W = new double[RouteRec.timeSliceNum+1][RouteRec.modeNum];
	public double[][][] c = new double[RouteRec.modeNum][RouteRec.modeNum][RouteRec.timeSliceNum+1];
	
	public void addLink(int nextZoneId)
	{
		if(linkList.containsKey(nextZoneId))
			return ;
		double[][] P = new double[RouteRec.timeSliceNum+1][RouteRec.modeNum];
		for(int t = 1 ; t <= RouteRec.timeSliceNum; t++)
		{
			for(int m = 0 ; m < RouteRec.modeNum ; m++ )
			{
				P[t][m] = 0;
			}
		}
		linkList.put(nextZoneId, P);
	}
	
	public Zone()
	{
		for( int m = 1; m < RouteRec.modeNum; m++)
		{
			for( int d = 1; d < RouteRec.modeNum; d++)
			{
				for(int t=1;t<=RouteRec.timeSliceNum;t++)
				{
					try {
						c[m][d][t] = 0;
					} catch (Exception e) {
					e.printStackTrace();
					}
				}
				
			}
		}
		
		// 其他模式（包括出租车）与出租车互换，花费忽略不计，在这里取一个很小的值
		for( int m = 0; m < RouteRec.modeNum; m++)
		{
			for(int t=1;t<=RouteRec.timeSliceNum;t++)
			{
				c[m][0][t] = CalProbability.MIN_C;// 其他模式换出租车，花费忽略不计，在这里取一个很小的值
				c[0][m][t] = CalProbability.MIN_C;// 出租车换其他模式，花费忽略不计，在这里取一个很小的值
			}
			
		}
	}

	@Override
	public String toString() {
		return "Zone [linkList=" + linkList.entrySet() + "]";
	}

	
}
