package team.routerec.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import team.net.graph.LngLat;
import team.routerec.main.CalProbability;
import team.routerec.main.RouteRec;
import team.routerec.main.Zone;
import team.support.function.Distance;
import team.support.function.FilePath;
import team.trans.zone.Center;
import team.trans.zone.TransZoneMap;

public class DealTaxiNet2 {
	
	/**
	 * ����OD��tazID������List.get(OtazID).Map�У�
	 * @param id1
	 * @param id2
	 */
	public static void dealTaxiNet(int OtazID,int DtazID,String timeO , String timeD){

		Zone taz = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = null,date2 = null;
		double[][] value = null;
		
		try {
			date1 = sdf.parse(timeO);
			date2 = sdf.parse(timeD);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
//		System.out.println("pathTime="+date1+","+date2);
		double pathTime = Distance.timeDistance(date2, date1);
//		System.out.println("pathTime="+pathTime);
		if(pathTime>=RouteRec.timeSliceNum*RouteRec.timeSlice)
		{//RouteRec.timeSliceNum*RouteRec.timeSlice
//			System.out.println("pathTime="+date1+","+date2);
			pathTime = RouteRec.timeSliceNum*RouteRec.timeSlice - 1;
		}
//		System.out.println("pathTime="+date1+","+date2);
		taz = CalProbability.zoneMap.get(OtazID);//��List����ȡ��taz
		//��List�е�taz��map���ݱ������ݽ��и��¡�
		taz.addLink(DtazID);
		value = taz.linkList.get(DtazID);
		value[(int) (pathTime/RouteRec.timeSlice+1)][0]++;
	}
	
	/**
	 * ��ӡtaxiNet
	 */
	public static void printNet(){
		for(int i = 1 ; i < CalProbability.zoneMap.size() ; i++){
			System.out.println("====TAZ-ID:"+i+"====");
			Map<Integer, double[][]> map = CalProbability.zoneMap.get(i).linkList;
			for (Integer key : map.keySet()){
				   System.out.println( key + ":");
				   double[][] value = map.get(key);
				   for(int j = 0 ; j < value.length ; j++){
					   System.out.print(value[j][0]+" ");
				   }
				   System.out.println();
			}
		}
		System.out.println("Print Done");
	}
	
	/**
	 * ����һ��OD�е�������Ϣ
	 * @param oneOD
	 */
	public static void dealOneOD(List<String[]> oneOD){
		String[] info = null;
		int zoneID_1=0,zoneID_2=0,lastZone = 0;
		int zoneNum = 0;
		int isFirst = 0;
		String time = null,zoneTime = null, lastTime = null;
		double minLength = 0;
		
		Center center = null;
		LngLat Location = null;
		
		//���ȴ����һ�����ݣ�minLength <- ������gps����������ĵ��center����С����
		//				zoneTime <- �����ݵ�ʱ��
		info = oneOD.get(0);
		zoneID_1 = Integer.valueOf(info[11]);
		zoneTime = info[10].split(";")[0];

//		Location = new LngLat(info[4],info[5]);
//		System.out.println(zoneID_1);
//		center = TransZoneMap.centerList.get(zoneID_1-1);
//		minLength = Distance.lngLatDistanceInNum(Location, center.lngLat);
		
		
		
//		isFirst = 1;//����ΪOD�ĵ�һ������
//		int i=0;
//		for( i =1; i < oneOD.size()-1 ; i++){
			/*
			info = oneOD.get(i);
			zoneID_2 = Integer.valueOf(info[11]);
			time = info[10];
//			System.out.println(zoneID_2);
			
			if(zoneID_2!=zoneID_1){
//				System.out.println(zoneID_1+"->"+zoneID_2);
				zoneNum++;
				if(isFirst == 1)//����ǵ�һ������ֻ��Ҫ��lastZone <- zoneId_1,lastTime <- zoneTime����
				{
					lastZone = zoneID_1;
					lastTime = zoneTime;
					zoneID_1 = zoneID_2;
					isFirst = -1;//����isFirst
				}
				else//������ǵ�һ��������ô��ҪdealTaxiNet()
				{
//					System.out.println("deal->"+lastZone+","+zoneID_1+","+lastTime+","+zoneTime);
					dealTaxiNet(lastZone,zoneID_1,lastTime,zoneTime);
					lastTime = zoneTime;
					lastZone = zoneID_1;
					zoneID_1 = zoneID_2;
				}
				center = TransZoneMap.centerList.get(zoneID_2-1);
				minLength = Distance.lngLatDistanceInNum(Location, center.lngLat);//��ʼ��һ��minLength
			}
			
			double len = Distance.lngLatDistanceInNum(Location, center.lngLat);

			if(len <= minLength);
			{
				minLength = len ;
				zoneTime = info[10].split(";")[0];
			}
			*/
//		}
		info = oneOD.get(oneOD.size()-1);
		zoneID_2 = Integer.valueOf(info[11]);
		time = info[10];
		//System.out.println("oneOD Finish,"+zoneID_1+"->"+zoneID_2+","+zoneTime+"->"+time);
		dealTaxiNet(zoneID_1,zoneID_2,zoneTime,time);
	}
	
	/**
	 * ���ú���space���ļ����ж�·�� ˼·�� ��ͷ��ʼ���ļ���whileֱ����һ������
	 * isFirst==1��ÿ�v܇�ĵ�һ���^��isFirst==-1���� isZero>2&&isFirst==1
	 */
	public static void dealNetBySpace(File file) {
		BufferedReader br = null;

		List<String[]> oneOD = new ArrayList<String[]>();
		String[] info = null;
		String taxiId = null;
		LngLat Location = null;

		int zeroNum = 0;//�հ�������
		
		
		TransZoneMap.init();
		
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			// �����ͷ�����пհ׵�����
			
			while ((line = br.readLine()).equals(""))//�������
				;
//			System.out.println("�Ѿ������հ���"+line);
			
			while(line!=null){
				
//				System.out.println("zeroNum="+zeroNum);
				if(line.equals("")){
					zeroNum++;
					line = br.readLine();
					continue;
				}
				if(zeroNum >2){
					//��������˴��������Ŀ�����ô˵�������˷ֶΣ���Ҫ����oneOD
//					System.out.println("����oneOD");
					if( CalProbability.isInTimeRegion(oneOD.get(0)[10].split(";")[0], CalProbability.timeRegionIndex))
					{
						if(!RouteRec.regionSet.contains( Integer.parseInt(oneOD.get(0)[11]) )
								|| !RouteRec.regionSet.contains( Integer.parseInt(oneOD.get(oneOD.size()-1)[11]) ))
							;
						else
							dealOneOD(oneOD);
					}
					//��������г�ʼ��
					zeroNum = 0;
					oneOD = new ArrayList<String[]>();
				}
				
				info = line.split(",");
				oneOD.add(info);
//				System.out.println(line);
				line = br.readLine();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void interSpace(String filePath){
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		String[] info = null;
		
		try {
			br = new BufferedReader(new FileReader(filePath+".csv"));
			bw = new BufferedWriter(new FileWriter(new File(filePath+"_space.csv")));
			String line = null;
			String taxiID = null;
			
			line = br.readLine();
			info = line.split(",");
			taxiID = info[3];
			if(info[8].equals("1")&&!info[11].equals("-1"))
				bw.write(line+"\r\n");
			else 
				bw.write("\r\n");
			
			while((line = br.readLine())!=null)
			{
				info = line.split(",");
				if(!info[3].equals(taxiID)){
					bw.write("\r\n\r\n\r\n");
					taxiID = info[3];
				}
				if(info[8].equals("1")&&!info[11].equals("-1"))
					bw.write(line+"\r\n");
				else 
					bw.write("\r\n");
			}
			bw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ���ú���space���ļ����ж�·�� ˼·�� ��ͷ��ʼ���ļ���whileֱ����һ������
	 * isFirst==1��ÿ�v܇�ĵ�һ���^��isFirst==-1���� isZero>2&&isFirst==1
	 */
	public static void dealNetBySpace(String filePath) {
		dealNetBySpace(new File(filePath));
	}
	
	public static void main(String[] args) {
//		File taxiFolder = new File("H:\\oneweek");
//		File[] taxiFiles = taxiFolder.listFiles();
//		for( File file : taxiFiles)
//		{
//			if(file.isFile())
//			{
//				System.out.println(file.getName() + " Start!");
//				DealTaxiNet2.interSpace(file.getAbsolutePath());
//				System.out.println(file.getName() + " Done!");
//			}
//		}
		DealTaxiNet2.dealNetBySpace(FilePath.dataFolder + "RouteRec\\Taxi\\20130801_space.csv");
	}
}
