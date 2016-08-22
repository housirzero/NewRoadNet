package team.routerec.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import team.net.graph.Graph;
import team.net.graph.LngLat;
import team.support.function.Distance;
import team.support.function.FilePath;

public class CalProbability {

	public static final double MIN_C = 0;
	public static final double MAX_COST = 10000;
//	public static final double MAX_W = 1;
//	public static final double MAX_P = 1;
	public static final double MIN_W = 0;
	public static final double MIN_P = 0;

	public static int timeRegionIndex = -1; // ��ѡʱ��

	public static String[] subLine = new String[]{"1����","2����","4����","5����","6����","7����","8����","9����","10����",
		"13����","14����","15����","4���ߴ�����","��ƽ��","��ɽ��","��ׯ��","��ͨ��","������"};
	
	public static HashSet<String> subLineSet = new HashSet<String>(Arrays.asList(subLine));
	
	public static HashMap<Integer, Zone> zoneMap = new  HashMap<Integer, Zone>();
	/**
	 *  �����ٶȣ� 1.5m/s
	 */
	public static double speed = 1.5;

	public static void run() {
		init();
//		readP(FilePath.dataFolder + "RouteRec\\ReadData\\P.csv");
		
		
		File folder = new File(FilePath.dataFolder + "RouteRec\\TripChain");
		File[] files = folder.listFiles();
		for( File file : files)
		{
			if(file.isFile() && file.getName().endsWith("TripChain3.csv"))
			{
				System.out.println(file.getName() + " Start!");
				stat(file);
				System.out.println(file.getName() + " Done!");
			}
		}
		

//		File taxiFolder = new File(FilePath.dataFolder + "RouteRec\\Taxi");
//		File[] taxiFiles = taxiFolder.listFiles();
//		for( File file : taxiFiles)
//		{
//			if(file.isFile() && file.getName().endsWith("space.csv"))
//			{
//				System.out.println(file.getName() + " Start!");
//				DealTaxiNet2.dealNetBySpace(file);
//				System.out.println(file.getName() + " Done!");
//			}
//		}
		/**
		 * ��ͳ�Ƶ�ֵ���������
		 */
		calProbability();	
	}
	
	/**
	 * 1 : the commuter records(1106:00-09:30 am, 16:30-20:30pm)
	 * 2 : the daily records (09:30-16:30)
	 * 3 : the night records (19:00pm-07:00am)
	 */
	public static boolean isInTimeRegion_Old( String timeStr, int t )
	{
		String time = timeStr.split(" ")[1];
		switch (t) {
		case 1:
			return ( (time.compareTo("06:30") > 0 && time.compareTo("09:30") < 0)
					|| (time.compareTo("16:30") > 0 && time.compareTo("20:30") < 0) );
		case 2:
			return ( time.compareTo("09:30") > 0 && time.compareTo("16:30") < 0 );
		case 3:
			return ( time.compareTo("07:00") < 0 || time.compareTo("19:00") > 0 );
		default:
			System.out.println("ʱ���ֻ����1,2,3");
			return false;
		}
	}
	
	/**
	 * 1 : ZGF(06:30-09:30 am)
	 * 2 : the daily records (09:30-16:30)
	 * 3 : WGF (16:30-20:30pm)
	 */
	public static boolean isInTimeRegion( String timeStr, int t )
	{
		String time = timeStr.split(" ")[1];
		boolean zaf = time.compareTo("06:30") > 0 && time.compareTo("09:30") < 0;
		boolean pf = time.compareTo("09:30") > 0 && time.compareTo("16:30") < 0;
		boolean wgf = time.compareTo("16:30") > 0 && time.compareTo("20:30") < 0;
		switch (t) {
		case 1:
			return zaf; // 3 hours
		case 2:
			return pf; // 7 hours
		case 3:
			return wgf; // 4 hours
		default:
			System.out.println("ʱ���ֻ����1,2,3");
			return false;
		}
	}
	
	public static void init() {
//		init_c_W_P();
		for (int i = 1; i <= RouteRec.regionNum; i++) {
			zoneMap.put(i, new Zone());
		}
	}

	/**
	 * ͳ�Ƹ������Ŀ�����
	 */
	public static Set<Integer> statZonePassFlow( String filePath, int minFlow ) 
	{
		return statZonePassFlow(new File(filePath), minFlow);
	}
	
	/**
	 * ͳ�Ƹ������Ŀ�����
	 * @return 
	 */
	public static Set<Integer> statZonePassFlow( File file, int minFlow  ) 
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Map<String, Integer> passFlow = new HashMap<String, Integer>();
		
		try {
			String line = null;
			int aboardZoneIndex = 'L' - 'A'; // �ϳ���������
			int alightZoneIndex = 'S' - 'A'; // �³���������
			
			// W,P�����ȼ���������������ٷֱ�
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				if(!passFlow.containsKey(items[aboardZoneIndex]))
					passFlow.put(items[aboardZoneIndex], 1);
				else
					passFlow.put(items[aboardZoneIndex], passFlow.get(items[aboardZoneIndex])+1);
				
				if(!passFlow.containsKey(items[alightZoneIndex]))
					passFlow.put(items[alightZoneIndex], 1);
				else
					passFlow.put(items[alightZoneIndex], passFlow.get(items[alightZoneIndex])+1);
			}
			br.close();

			Set<Integer> regionSet = new HashSet<Integer>();
			for(Entry<String, Integer> entry : passFlow.entrySet())
			{
				if(entry.getValue() > minFlow)
					regionSet.add(Integer.parseInt(entry.getKey()));
			}
			return regionSet;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void stat( String filePath ) 
	{
		stat(new File(filePath));
	}
	
	public static void stat( File file ) 
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			String line = "";
			int aboardTimeIndex = 'F' - 'A'; // �ϳ�ʱ������
			int alightTimeIndex = 'M' - 'A'; // �³�ʱ������
			int aboardZoneIndex = 'L' - 'A'; // �ϳ���������
			int alightZoneIndex = 'S' - 'A'; // �³���������
			int dtIndex = 'U' - 'A'; // �˳�ʱ��������
			int aboardLineIndex = 'G' - 'A'; // �ϳ���·������
			
			int cardIdIndex = 'E' - 'A'; // ˢ����������
			int linkIdIndex = 'A' - 'A'; // �������������

			// ��γ������
			int aboardLngIndex = 'J' - 'A';
			int aboardLatIndex = 'K' - 'A';
			int alightLngIndex = 'Q' - 'A';
			int alightLatIndex = 'R' - 'A';

			String lastCardId = "";
			String lastLinkId = ""; // ��һ����������ţ�ͬһ���ŵ���ͬ���Ϊͬһ��������
			String lastAlightTime = null;
			LngLat lastAlightPos = null;
			int lastM = -1;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			
			HashSet<String> topSet = Graph.getTop50(FilePath.dataFolder + "DailyData\\Analy\\bus.csv");
			
			// W,P�����ȼ���������������ٷֱ�
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
				if( !isInTimeRegion(items[aboardTimeIndex], timeRegionIndex)  ) // ����timeRegionIndex����ʱ�����
					continue; 
				if(!RouteRec.regionSet.contains( Integer.parseInt(items[aboardZoneIndex]) )
						|| !RouteRec.regionSet.contains( Integer.parseInt(items[alightZoneIndex]) ))
					continue;
				
				/*
				 *  DT=0�ļ�¼���쳣���ݣ�������
				 */
				int dt = Integer.parseInt(items[dtIndex]);
				if(dt < 1)
					continue;
				if(dt >= RouteRec.timeSlice * RouteRec.timeSliceNum ) // �����������
					dt = RouteRec.timeSlice * RouteRec.timeSliceNum - 1;

				/*
				 *  ����ÿһ��OD,����P
				 */
				int aboardZone = -1;
				int alightZone = -1;
				try {
					aboardZone = Integer.parseInt(items[aboardZoneIndex]);
					alightZone = Integer.parseInt(items[alightZoneIndex]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				String aboardLine = items[aboardLineIndex]; // �ϳ���·��
				
				int m = 1;
				// 0 ���� 1 ���� 2 ���ٹ��� 3 ����
				if(aboardLine.startsWith("���ٹ���"))
					m = 2;
				else if(subLineSet.contains(aboardLine))
					m = 3;
				else if(!topSet.contains(aboardLine)) // ����������Ҫ����Щ��·��
					continue;
				
				if(	!zoneMap.containsKey(aboardZone) )
					zoneMap.put(aboardZone, new Zone());
				Zone zone = zoneMap.get(aboardZone);
				zone.addLink(alightZone);
				double[][] P = zone.linkList.get(alightZone);
				P[dt/RouteRec.timeSlice+1][m]++;
				
				/*
				 * ����ÿһ��������������C,W
				 */
				String cardId = items[cardIdIndex];
				String linkId = items[linkIdIndex];
				String aboardTime = items[aboardTimeIndex];
				LngLat aboardPos = new LngLat(items[aboardLngIndex], items[aboardLatIndex]);
				if( cardId.equals(lastCardId) && linkId.equals(lastLinkId) ) // ��ͬһ��������
				{
					int transTime= Distance.timeDistance(aboardTime, lastAlightTime, sdf);

//					if(transTime >= RouteRec.timeSlice * RouteRec.timeSliceNum ) // �����������
//						transTime = RouteRec.timeSlice * RouteRec.timeSliceNum - 1;
					
					int walkTime = (int) (Distance.lngLatDistance(lastAlightPos, aboardPos)/speed);
					if(walkTime >= RouteRec.timeSlice * RouteRec.timeSliceNum ) // �����������
						walkTime = RouteRec.timeSlice * RouteRec.timeSliceNum - 1;
					
					int waitTime = transTime - walkTime;
					if(waitTime >= RouteRec.timeSlice * RouteRec.timeSliceNum ) // �����������
						waitTime = RouteRec.timeSlice * RouteRec.timeSliceNum - 1;
					if(waitTime < 0)
						waitTime = 0;
					zone.c[lastM][m][walkTime/RouteRec.timeSlice+1]++;
					zone.W[waitTime/RouteRec.timeSlice + 1][m]++;
				}

				lastCardId = cardId;
				lastLinkId = linkId;
				lastAlightTime = items[alightTimeIndex];
				lastAlightPos = new LngLat(items[alightLngIndex], items[alightLatIndex]);
				lastM = m;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��ͳ�Ƶ�ֵ���������
	 */
	public static void calProbability() {
		for( int i = 1; i < RouteRec.regionNum; i++)
		{
			Zone zone = zoneMap.get(i);
			if(zone == null)
				continue;
			for(int j = 0; j < RouteRec.regionNum; j++)
			{
				if( i == j ) // ������������ͳ��
					continue;
				double[][] P = zone.linkList.get(j);
				if(P == null)
					continue;
				for( int m = 0; m < RouteRec.modeNum; m++)
				{
					double all = 0;
					for( int t = 0; t < RouteRec.timeSliceNum + 1; t++)
					{
						all += P[t][m];
					}
					
					for( int t = 0; t < RouteRec.timeSliceNum + 1; t++)
					{
						if(P[t][m] != MIN_P)
						{
							P[t][m] /= all;
							if(P[t][m] > 0.01)
								System.out.println(String.format("%d-->%d: P[%d][%d]=%f", i, j, t, m, P[t][m]));
						}
					}
				}
			}
		}
		System.out.println();
		System.out.println();
		for( int i = 0; i < RouteRec.regionNum; i++)
		{
			Zone zone = zoneMap.get(i);
			if(zone == null)
				continue;
			for( int m = 0; m < RouteRec.modeNum; m++)
			{
				double all = 0;
				for( int t = 0; t < RouteRec.timeSliceNum + 1; t++)
				{
					all += zone.W[t][m];
				}
				
				for( int t = 0; t < RouteRec.timeSliceNum + 1; t++)
				{
					if(zone.W[t][m] != MIN_W)
					{
						zone.W[t][m] /= all;
//						System.out.print(zone.W[t][m] + "\t");
					}
				}
			}				
		}
		System.out.println();
		System.out.println();
		for( int i = 0; i < RouteRec.regionNum; i++)
		{
			Zone zone = zoneMap.get(i);
			if(zone == null)
				continue;
			for( int m1 = 0; m1 < RouteRec.modeNum; m1++)
			{
				for( int m2 =0 ; m2<RouteRec.modeNum;m2++)
				{
					double all = 0;
					for( int t = 0; t < RouteRec.timeSliceNum + 1; t++)
					{
						all += zone.c[m1][m2][t];
					}
					
					for( int t = 0; t < RouteRec.timeSliceNum + 1; t++)
					{
						if(zone.c[m1][m2][t] != MIN_W)
						{
							zone.c[m1][m2][t] /= all;
	//						System.out.print(zone.W[t][m] + "\t");
						}
					}
				}
				
			}				
		}

	}
	
	/**
	 * ���ļ��ж�ȡP,���㹹����ֳ�ʼ����
	 * @param filePath
	 */
	public static void readP(String filePath){
		
		BufferedReader br = null;
		CalProbability.init();
		try {
			br = new BufferedReader(new FileReader(filePath));
			String line = null;
			String[] zonePair = new String[2];
			String lastZoneId = "-1";
			Zone zone = new Zone();
			
		
			while((line = br.readLine())!=null){
				zonePair = line.split(",");
				zone = CalProbability.zoneMap.get(Integer.valueOf(zonePair[0]));
				lastZoneId = zonePair[0];
					
				zone.addLink(Integer.valueOf(zonePair[1]));
				
				line = br.readLine();
				String[] info = null;
				double[][] distribution = new double[RouteRec.timeSliceNum+1][RouteRec.modeNum];

				for(int m = 0 ; m < RouteRec.modeNum ;m++){//mode
					line = br.readLine();
					info = line.split(",");
					//System.out.println(info.length);
					for(int t = 1; t <= RouteRec.timeSliceNum ; t++){
						//timeSlice
						distribution[t][m] = Double.valueOf(info[t]);
					}
					
				}
				zone.linkList.put(Integer.valueOf(zonePair[1]), distribution);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���������ڴ�ӡp��w
	 * P(ijm) ~ t�ķֲ�
	 * w(im)  ~ t�ķֲ�
	 * c(m1 m2) ~ t�ķֲ�
	 */
	public static void print(){
		Zone zone = null;
		BufferedWriter bw_p = null;
		BufferedWriter bw_w = null;
		BufferedWriter bw_c = null;
		try {
			bw_p = new BufferedWriter(new FileWriter(new File(FilePath.dataFolder + "RouteRec\\Data\\P.csv")));
			bw_w = new BufferedWriter(new FileWriter(new File(FilePath.dataFolder + "RouteRec\\Data\\W.csv")));
			bw_c = new BufferedWriter(new FileWriter(new File(FilePath.dataFolder + "RouteRec\\Data\\C.csv")));

			for(int i=1;i<RouteRec.regionNum ;i++){
				zone = zoneMap.get(i);
				if(zone == null)
					continue;
				//��ӡP
				for(int j=1;j<zone.linkList.size() ;j++){
					if(i==j)
						continue;
					double[][] P = zone.linkList.get(j);
					if(P == null)
						continue;
					bw_p.write(i+","+j+"\r\n");
					for(int m=-1;m<RouteRec.modeNum;m++){
						if(m==-1){
							bw_p.write(",");
							for(int t=1;t<=RouteRec.timeSliceNum;t++){
								bw_p.write(t+",");
							}
							bw_p.write("\r\n");
						}
						else{
							bw_p.write(m+",");
							for(int t=1;t<=RouteRec.timeSliceNum;t++){
								bw_p.write(P[t][m]+",");
							}
							bw_p.write("\r\n");
						}
					}
					bw_p.write("\r\n");

				}
				
				//��ӡW
				bw_w.write(i+"\r\n");
				for(int m=-1;m<RouteRec.modeNum;m++){
					if(m==-1){
						bw_w.write(",");
						for(int t=1;t<=RouteRec.timeSliceNum;t++){
							bw_w.write(t+",");
						}
						bw_w.write("\r\n");
					}
					else{
						bw_w.write(m+",");
						for(int t=1;t<=RouteRec.timeSliceNum;t++){
							bw_w.write(zone.W[t][m]+",");
						}
						bw_w.write("\r\n");
					}
				}
				bw_w.write("\r\n");
				
				//��ӡC
				bw_c.write(i+"\r\n");
				for(int m1=-1;m1<RouteRec.modeNum;m1++){
					if(m1==-1){
						bw_c.write(",,");
						for(int t=1;t<=RouteRec.timeSliceNum;t++){
							bw_c.write(t+",");
						}
						bw_c.write("\r\n");
					}
					else{
						for(int m2=0;m2<RouteRec.modeNum;m2++){
							bw_c.write(m1+","+m2+",");
							for(int t=1;t<=RouteRec.timeSliceNum;t++){
								bw_c.write(zone.c[m1][m2][t]+",");
							}
							bw_c.write("\r\n");
						}
						bw_c.write("\r\n");
					}
				}
			}
			bw_p.close();
			bw_w.close();
			bw_c.close();
			System.out.println("PrintDone");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void statPassFlow(String file)
	{
		Map<String,Integer> passFlowMap = new HashMap<String,Integer>();
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(file)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			String line = null;
			
			HashSet<String> topSet = Graph.getTop50(FilePath.dataFolder + "DailyData\\Analy\\bus.csv");
			int count = 0;
			while ((line = br.readLine()) != null) {
				String[] items = line.split(",");
		        if(!items[6].startsWith("���ٹ���") && !subLineSet.contains(items[6])
		        		&& !topSet.contains(items[6])) // ����������Ҫ����Щ��·��
					continue;
//				String on_time = items[5]; // �ϳ�ʱ��
//				int on_t = Integer.parseInt(on_time.split(" ")[1].split(":")[0]); // ��ȡСʱ��
//				String on_lineId = items[6]; //['G'-'A'];
//		        String on_name = items[7]; //['H'-'A'];
//		        String on_lng = items[9]; //['J'-'A'];
//		        String on_lat = items[10]; //['K'-'A'];
//		        String on_key = on_t + "," + on_lng + "," + on_lat;
		        String on_key = items[9] + "," + items[10];
		        if(!passFlowMap.containsKey(on_key))
		        	passFlowMap.put(on_key, 1);
		        else
		        	passFlowMap.put(on_key, passFlowMap.get(on_key)+1);
	
//				String off_time = items[12]; // �³�ʱ��
//				int off_t = Integer.parseInt(off_time.split(" ")[1].split(":")[0]); // ��ȡСʱ��
//		        String off_lineId = items[13]; //['N'-'A'];
//		        String off_name = items[14]; //['O'-'A'];
//		        String off_lng = items[16]; //['Q'-'A'];
//		        String off_lat = items[17]; //['R'-'A'];
//		        String off_key = off_t + "," + off_lng + "," + off_lat;
		        String off_key = items[16] + "," + items[17];
		        if(!passFlowMap.containsKey(off_key))
		        	passFlowMap.put(off_key, 1);
		        else
		        	passFlowMap.put(off_key, passFlowMap.get(off_key)+1);
		        
		        if(++count % 100000 == 0)
		        	System.out.println(count/10000 + "W");
			}
			br.close();
			
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(new File("C:\\Users\\DELL\\Desktop\\data")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			bw.write("var points =[\r\n");
			for (Entry<String, Integer> entry : passFlowMap.entrySet()) {
		    	 String[] items = entry.getKey().split(",");
		    	 int times = entry.getValue();
		    	 bw.write(String.format("{\"lng\":%s,\"lat\":%s,\"count\":%d},\r\n", items[0], items[1], times));
		    	 // var points =[ {"lng":116.353080,"lat":39.913278,"count":139},
		    }
			bw.write("];");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    
	}
	
	public static void main(String[] args) {
//		statPassFlow(FilePath.dataFolder + "RouteRec\\TripChain\\20150104_TripChain3.csv");
	}

}
