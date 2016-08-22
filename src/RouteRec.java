



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import team.routerec.main.CalProbability;
import team.routerec.main.Zone;
import team.support.function.FilePath;
import team.trans.zone.TransZoneMap;


/****************************************************
	                   _ooOoo_
	                  o8888888o
	                  88" . "88
	                  (| -_- |)
	                  O\  =  /O
	               ____/`---'\____
	             .'  \\|     |//  `.
	            /  \\|||  :  |||//  \
	           /  _||||| -:- |||||-  \
	           |   | \\\  -  /// |   |
	           | \_|  ''\---/''  |   |
	           \  .-\__  `-`  ___/-. /
	         ___`. .'  /--.--\  `. . __
	      ."" '<  `.___\_<|>_/___.'  >'"".
	     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
	     \  \ `-.   \_ __\ /__ _/   .-` /  /
	======`-.____`-.___\_____/___.-`____.-'======
	                   `=---='
	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                                                            佛祖保佑       永无BUG
****************************************************/

public class RouteRec {

	public final static int regionNum = 1920; // 站点数量
	public final static int timeSliceNum = 60; // 时间分段数
	/**
	 *  0 ：出租 1 公交 2 快速公交 3 地铁
	 */
	public final static int modeNum = 4; // 模式种类数量
	public static final int MAX_ITER_TIMES = 3; // 最大迭代次数

	public static double e = 0.1; // 收敛系数
	public static int k; // 迭代次数

	public static int timeSlice = 60 * 2; // 每个时间片单位（s）
	public static int MAX_TIME = timeSlice * 10000; // 初始化的最大花费时间
	// 
	public static double[][][][] u = new double[regionNum+1][timeSliceNum+1][modeNum][MAX_ITER_TIMES+1];
	// q[i][t] : the next station to visit at region i with time budget t
	public static int[][][] q = new int[regionNum+1][timeSliceNum+1][MAX_ITER_TIMES+1];
	// r[i][t] : the mode to choose at region i with time budget t
	public static int[][][] r = new int[regionNum+1][timeSliceNum+1][MAX_ITER_TIMES+1];
	// real cost time
	public static int[][][] ct = new int[regionNum+1][timeSliceNum+1][MAX_ITER_TIMES+1];
	
	// 不同模式间的换乘花费，为常数（不同模式组合的值不同）
	public static double[][] c = new double[modeNum][modeNum];

	public static Set<Integer> regionSet = new HashSet<Integer>();

	static BufferedWriter bw = null;
	
	public static void main(String[] args) {
		CalProbability.timeRegionIndex = 1; // 选择时间段
//		更改mode时，需要更改以下几个地方
//		1. 文件名 new BufferedWriter(new FileWriter(FilePath.dataFolder + "mode123_time1.csv"));
//		2. routeRec 函数中的 mode 循环
//		3. max_u 函数中的 mode 循环
//		4. CalProbability.run() 中的统计数据部分，只统计需要的数据
		Date start = new Date(); 	System.out.println("开始时间：" + start);
		regionSet = CalProbability.statZonePassFlow(FilePath.dataFolder + "RouteRec\\TripChain\\20150104_TripChain3.csv", 50000);
//		regionSet = Graph.getRegions();
		System.out.println("regionSet.size = " + regionSet.size());
		CalProbability.run();
		
		Date end = new Date(); System.out.println("数据准备结束：" + end);
		System.out.println("耗时：" + (end.getTime() - start.getTime()) / 1000.0 + "s");
		try {
			bw = new BufferedWriter(new FileWriter(FilePath.dataFolder + "mode123_time1.csv"));
			bw.write("O,D,time,realTime,realDistance,transNum,Probability,\r\n");
			randomTime();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("总耗时：" + (new Date().getTime() - start.getTime()) / 1000.0 + "s");
	}
	
	public static void randomTime(){

		for(int D = 1; D <= regionNum;D++){
//		for(int D = 64; D <= 64; D++){
			if(!regionSet.contains(D)) // 只计算相关小区
				continue;
			routeRec(D,60*timeSlice-1);
			for(int O=1;O< regionNum+1 ;O++){
				if(!regionSet.contains(O)) // 只计算相关小区
					continue;
				if(D!=O)
				{
					for(int t = 10 ; t<= timeSliceNum ; t+=10){
						getResult(O, D, t*timeSlice-1);
					}
					System.out.println(O+"->"+D+" Done");
				}
				
			}
		}
		
	}
	
	
	
	// 路径推荐(目的：D)
	public static void routeRec( int D, int time )
	{
		if(time <= 0)
		{
			System.out.println("时间为非正数，没有找到合适路线！");
			System.out.println("*****************************");
		}
		init(D);
		
		int m = 0;
		int T = time/timeSlice + 1;
		int iterationTimes = 0;
		k = 0;
		// deal
		do{
			k = k+1;
			for (int i = 1; i < regionNum+1; i++)
			{
				if(!regionSet.contains(i)) // 只计算相关小区
					continue;
				if(i==D) // i == D
				{
					for(int t = 1 ; t <= timeSliceNum ; t++){
						for(int d = 0 ; d < modeNum ; d++ ){
							u[i][t][d][k-1] = 1;
						}
					}
					continue;
				}
				
				for( int t = 1; t <= timeSliceNum; t++)
				{
					boolean haveRoad = false;
					double max = max_u(i,t,k-1); // 过滤掉y太小的情况
					m = r[i][t][k-1];
					for( int d = 1; d <= 3; d++ )
					{
						u[i][t][d][k] = u[i][t][d][k-1]; // 用这句来替代else后的语句
						for(int j = 1; j < regionNum+1; j++)
						{
							if(!regionSet.contains(j)) // 只计算相关小区
								continue;
							if( j == i ) //  j == i  不作处理
								continue;
							if( !canPass(i,j) ) // 不存在i到j的路线
								continue;
							Result res = calc_u(t,m,d,i,j);
							if( res != null && res.y > max + 0.001 )
							{
								max = res.y;
								haveRoad = true;
								u[i][t][d][k] = res.y;
								q[i][t][k] = j;
								r[i][t][k] = d;
								ct[i][t][k] = res.costtime;
							}
						}
					}
					
					if(!haveRoad)
					{
						q[i][t][k] = q[i][t][k-1];
						r[i][t][k] = r[i][t][k-1];
						ct[i][t][k] = ct[i][t][k-1];
					}
				}
			}
			if(++iterationTimes >= MAX_ITER_TIMES)
				break;
			System.out.println("迭代次数:" + iterationTimes);
		}while( !isConvergence( ) );
	}

	private static double max_u(int i, int t, int j) {
		double max = 0.0;
		for( int d = 1; d <= 3; d++ )
		{
			if( max < u[i][t][d][j])
				max = u[i][t][d][j];
		}
		return max;
	}

	static class Result
	{
		
		public Result(double y, int t, int costtime) {
			super();
			this.y = y;
			this.t = t;
			this.costtime = costtime;
		}

		double y;
		int t;
		int costtime;
	}
	
	public static Result calc_u(int t, int m, int d, int i, int j)
	{
		if(m == -1) // 初始时为-1
			return null;
		Result res = new Result(0,MAX_TIME,MAX_TIME);
		
		// 找到P(i,j)
		Zone zone = CalProbability.zoneMap.get(i);
		if(zone == null)
			return null;
		double[][] P = zone.linkList.get(j);
		if(P == null)
			return null;
		double max = 0;
		for( int h = 0; h <= t; h++ )
		{
//			double dt = P[h][d] * u[j][t-h][d][k-1];
			double dt = calc_v(i,j,h,m,d) * u[j][t-h][d][k-1];
			if(dt > 0.001)
			{
				res.y += dt;
				res.t = h;
			}
			if(dt > max)
			{
				max = dt;
				res.costtime = h;
			}
		}
		if(res.y > 1)
			res.y = 1;
		return res;
	}		

	public static double calc_v(int i, int j, int h, int m, int d)
	{
		double res = 0;
		Zone zone = CalProbability.zoneMap.get(i);
		if(zone == null)
			return 0;
		double[][] P = zone.linkList.get(j);
		if(P == null)
			return 0;
		if(d == 0)
			return P[h][d] / changeModeCost(m,d);
		for( int g = 0; g <= h; g++ )
		{
//			res += zone.c[m][d] * zone.W[g][d] * P[h-g][d];
//			res += zone.W[g][d] * P[h-g][d] / changeModeCost(m,d);
			res += zone.W[g][d] * P[h-g][d];
		}
		return res;
	}
	
	public static double changeModeCost( int m, int d)
	{
		if( m == 0 || d == 0 )
			return 1;
		else if(m == d || ( (m == 1 || m == 2) && (d == 1 || d == 2) )) // 相同模式件换乘（公交和快速公交算作一种）
			return 1.2;
		else // 公交与地铁之间的换乘
			return 1.5;
	}

	/**
	 * 获取最后结果
	 */
	public static void getResult(int O, int D, int time) {
		System.out.println("k=" + k);
		int cnt = 0; // 10次换乘是上限
		ArrayList<String> res = new ArrayList<String>();
		int i = O;
		int T = time/timeSlice + 1;
		boolean find = false;
		int t = T;

		int r1 = -1;
		int t1 = -1;
		
		int realTime = 0;
		double realDistance = 0;
		double distance = 0;
		
		int TT = T;
		while(cnt++ < MAX_ITER_TIMES)
		{
			if(t > timeSliceNum)
				t = timeSliceNum;
			
			int next = q[i][t][k];
			int d = r[i][t][k];
//			int costtime = fineNextStation(ct[i][t][k], -1, d, i, next);
			int costtime = ct[i][t][k];
			
			if(cnt == 1)
			{
				r1 = d;
				t1 = t;
			}
			
			if( next == -1 )
				break;
			System.out.println(next + "," + d + "," + costtime);
			distance = TransZoneMap.getDisInRegions(i,next);
			res.add(next + "," + d + "," + costtime+","+distance);
			
			if(next == D)
			{
				find = true;
				break;
			}
			
			T -= ct[i][t][k];
			t = T;
			i = next;
			if(t <= 0)
				break ;
		}
		if(!find)
		{
			realTime = -1;
			System.out.println("没有找到合适路线！");
			try {
				bw.write(O+","+D+","+TT+",-1\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ;
		}
		else
		{
			System.out.println( String.format("%d-->%d推荐路线为(概率：%f)：", O, D, u[O][t1][r1][k]) );
			for( int j = 0; j < res.size(); j++ )
			{
				String node = res.get(j);
				String[] info = node.split(",");
				
				realDistance += Double.valueOf(info[3]);
				realTime += Integer.valueOf(info[2]);
				
				System.out.println( String.format( "--> (%s) ", node));
			}
			
			try {
				bw.write(O+","+D+","+TT+","+realTime+","+realDistance+","+res.size()+","+u[O][t1][r1][k]+",");
				for(int i1=0;i1<res.size();i1++){
					bw.write(res.get(i1)+",");
				}
				bw.write("\r\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("totalTime --> "+realTime);
		System.out.println("*****************************");

	}
	
	// 是否有路径
	private static boolean canPass(int i, int j) {
		// 需要用到路网
		Zone zone = CalProbability.zoneMap.get(i);
		if(zone == null)
			return false;
		double[][] P = zone.linkList.get(j);
		if(P == null)
			return false;
		return true;
	}

	/**
	 * 初始化
	 * @param D->目的regionID
	 */
	public static void init(int D)
	{
		k = 1;
		for(int i = 1 ; i < regionNum+1 ; i++ )
		{
			if(i == D)
			{
				for(int t = 0 ; t <= timeSliceNum ; t++){
					for(int m = 0 ; m < modeNum ; m++ ){
						u[i][t][m][0] = 1;
						q[i][t][0] = -1;
						r[i][t][0] = 0;
						ct[i][t][0] = 0; // i == D, 则D-->D没有花费
					}
				}
			}
			else
			{
				for(int t = 0 ; t <= timeSliceNum; t++){
					for(int m = 0 ; m < modeNum ; m++ ){
						u[i][t][m][0] = 0;
						q[i][t][0] = -1;
						r[i][t][0] = 0;
						ct[i][t][0] = MAX_TIME;
					}
				}				
			}
		}
		
		// 不同模式间换乘的花费
		for(int m = 0 ; m < modeNum ; m++ )
		{
			for(int d = 0 ; d < modeNum ; d++ )
			{
				c[m][d] = changeModeCost(m, d);
			}
		}
	}

	/**
	 * 是否收敛
	 * @param o 
	 */
	public static boolean isConvergence( ) {
		double max = 0;
		double min = 1;
		
		boolean uponAvg = false;
		for(int i = 1 ; i < regionNum ; i++ ){
//
			if(!regionSet.contains(i)) // 只计算相关小区
				continue;
			for(int t = 1 ; t <= timeSliceNum ; t++){
				for(int m = 0 ; m < modeNum ; m++ ){
//					double d = Math.abs(u[i][t][m][k]-u[i][t][m][k-1]);
//					if(max < d)
//						max = d;
//					if(min > d)
//						min = d;
					if(Math.abs(u[i][t][m][1]-u[i][t][m][0]) > e)
						return false;
				}
			}
		}
//		if(max > e)
//			return false;
		return true;
	}

}
