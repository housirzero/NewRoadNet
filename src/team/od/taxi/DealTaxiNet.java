package team.od.taxi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import team.net.graph.LngLat;
import team.routerec.main.CalProbability;
import team.routerec.main.RouteRec;
import team.routerec.main.Zone;
import team.support.function.Distance;
import team.trans.zone.Center;
import team.trans.zone.TransZoneMap;

public class DealTaxiNet {
	
	/**
	 * 给定OD的tazID，插入List.get(OtazID).Map中！
	 * @param id1
	 * @param id2
	 */
	public static void dealTaxiNet(int OtazID,int DtazID,String timeO , String timeD){

		Zone taz = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date1 = null,date2 = null;
		int timenum = RouteRec.timeSliceNum;
		double[][] value = null;
		
		try {
			date1 = sdf.parse(timeO);
			date2 = sdf.parse(timeD);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int pathTime = Distance.timeDistance(date2, date1);

		if(pathTime>RouteRec.timeSliceNum*RouteRec.timeSlice)
			pathTime = RouteRec.timeSliceNum*RouteRec.timeSlice - 1;
//		System.out.println("pathTime="+pathTime);
		taz = CalProbability.zoneMap.get(OtazID);//从List里面取出taz
		//给List中的taz的map根据本条数据进行更新。
		if(taz.linkList.containsKey(DtazID)){
			
			value = taz.linkList.get(DtazID);
			value[pathTime/RouteRec.timeSlice+1][0]++;
			taz.linkList.put(DtazID,value);
		}
		else{
			value = new double[timenum+1][RouteRec.modeNum];
			value[pathTime/RouteRec.timeSlice+1][0]++;
			taz.linkList.put(DtazID,value);
		}	
		
	}
	
	/**
	 * 打印taxiNet
	 */
	public static void printNet(){
//		for(int i = 1 ; i < CalProbability.zoneMap.size() ; i++){
//			System.out.println("====TAZ-ID:"+i+"====");
//			Map<Integer, double[][]> map = tazNet.get(i).linkList;
//			for (Integer key : map.keySet()){
//				   System.out.println( key + ":");
//				   double[][] value = map.get(key);
//				   for(int j = 0 ; j < value.length ; j++){
//					   System.out.print(value[j][0]+" ");
//				   }
//				   System.out.println();
//			}
//		}
		System.out.println("Print Done");
	}
	
	public static void taxiNet(){
		BufferedReader br = null;
		
		//引用TransZone以获得TransZone质心
		
		TransZoneMap tzm = new TransZoneMap();
		LngLat Location = null;
		int zoneId_1 = 0,zoneId_2 = 0,lastId = 0; 
		String zoneTime = null, lastTime = null;
		double minLength = 0;
		Center center = null;
		int zoneNum=0;//记录每辆车经过的区域数量，如果是1的话就不用处理了

		int isFirst = -1;
		
		try {
			//读取一天的GPS文件（按照车号排序过）
			br = new BufferedReader(new FileReader("E:\\TransData\\RouteRec\\20130801_3.csv"));
			//首先读取第一条记录
			String line = null;
			String taxiId = null;
			String[] info = null;
			
			line = br.readLine();
			info = line.split(",");
			taxiId = info[3];
			
			Location = new LngLat(info[4],info[5]);
			zoneId_1 = TransZoneMap.findZoneId(Location);
			if(zoneId_1 == -1)//如果没有找到直接读下一行
				;
			else{
				//找到zoneID后，得到质心，初始化最小距离为第一个点的数据
				center = TransZoneMap.centerList.get(zoneId_1-1);
//				System.out.println("center= "+ center.lngLat);
				minLength = Math.abs(Distance.lngLatDistance(Location, center.lngLat));
				zoneTime = line.split(",")[10];
			}
			int count = 0;
			while((line = br.readLine())!=null){
				
				if(++count%100000==0)
					System.out.println(count/10000 + "W");
				info = line.split(",");
				if(!taxiId.equals(info[3]))//处理上一辆车最后一个zone
				{
					isFirst = -1;
					if(zoneNum>1){
//						System.out.println(taxiId);
//						System.out.println("CarChange->deal: "+"carid:"+taxiId+"->"+lastId+","+zoneId_1+","+lastTime+","+zoneTime);
						dealTaxiNet(lastId,zoneId_1,lastTime,zoneTime);
					}
					
					taxiId = info[3];
					zoneTime = info[10].split(";")[0];
					zoneNum = 0;
				}

				Location = new LngLat(info[4],info[5]);
//				zoneId_2 = tzm.findZoneId(Location);
				zoneId_2 = (int) (Math.random()*450)+500;
				
				if(zoneId_2 == -1)//如果没有找到直接读下一行
					continue;
				center = TransZoneMap.centerList.get(zoneId_2-1);
				
				if(zoneId_1 != zoneId_2){
					zoneNum++;
					if(isFirst == -1){//处于每辆车的第一个zone
						
						isFirst = 1;//把isFirst变了
						lastId = zoneId_1;
						lastTime = zoneTime;
						
						zoneId_1 = zoneId_2;
						
					}
					else{
						//处于非第一个zone，那么需要处理上两个zone的数据

//						System.out.println("deal: "+"carid:"+taxiId+"->"+lastId+","+zoneId_2+","+lastTime+","+zoneTime);
						dealTaxiNet(lastId,zoneId_1,lastTime,zoneTime);
						lastId = zoneId_1;
						zoneId_1 = zoneId_2;
						lastTime = zoneTime;
						
					}
					//保存此区域time为lastTime						
					
					minLength = Distance.lngLatDistanceInNum(Location, center.lngLat);
				}
				
				double len = Distance.lngLatDistanceInNum(Location, center.lngLat);
				if(len <= minLength);
				{
					minLength = len ;
					zoneTime = info[10].split(";")[0];
				}
				
			}
			
			if(isFirst == -1){
				dealTaxiNet(lastId,zoneId_1,lastTime,zoneTime);
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("NetDone, "+TransZoneMap.unfind+" unfind");

	}
	
	public static void run()
	{
		TransZoneMap.init();
		System.out.println("TextNet Start!");
		taxiNet();
		System.out.println("TextNet Done!");
		//printNet();
	}
	public static void main(String[] args) {
		taxiNet();
		//dealDistribution();
		//printNet();
	}

}
