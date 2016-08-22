package team.od.taxi;

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

import team.net.graph.LngLat;
import team.support.function.Distance;
import team.trans.zone.TransZoneMap;

/**
 * 从一天的数据csv文件中找到taxi的OD信息
 * @author DELL
 *
 */
public class FindTaxiOD {

	
	@SuppressWarnings("null")
	public static void main(String[] args){
		// TODO Auto-generated method stub
		BufferedReader br = null;
		BufferedWriter bw = null;
		String line = "";

		String[] taxiinfo = new String[11]; 
		
		//List用来存每一条OD
		List<TaxiOD> ODList = new ArrayList<TaxiOD>();
		TaxiOD taxiod ;
		String startTime=null,endTime=null;
		LngLat startPoint=null,endPoint = null;
		int startZone = 0,endZone = 0;
		int pathTime = 0;
		
		int flag = 0;//因为瞎搞所以存在
		double right = 0 , all = 0;
		TransZoneMap zonemap = new TransZoneMap();
		zonemap.init();
		
		try {
			br = new BufferedReader(new FileReader("E:\\Data\\20130801_1_2_3_4_5_6.csv"));
			bw = new BufferedWriter(new FileWriter(new File("E:\\Data\\20130801_OD.csv")));
			while(line != null){
				flag = 0;
				line = br.readLine();
				String taxiID = line.split(",")[3];
				if(Integer.valueOf(line.split(",")[8]) == 1)
				{
					flag = 1;
					startPoint = new LngLat(line.split(",")[4],line.split(",")[5]);
					startTime = line.split(",")[10];
				}
				while((line = br.readLine())!=null && (taxiID.compareTo(line.split(",")[3])==0))
				{
					
					taxiinfo = line.split(",");
					if(Integer.valueOf(taxiinfo[8]) == 1)
					{

						if(flag == 0){
							startPoint = new LngLat(line.split(",")[4],line.split(",")[5]);
							startTime = line.split(",")[10];
						}
						flag = 0;
						while(Integer.valueOf(taxiinfo[8]) == 1)
						{
							endPoint = new LngLat(taxiinfo[4],taxiinfo[5]);
							endTime = taxiinfo[10];
							line = br.readLine();
							taxiinfo = line.split(",");
							
						}
						
						startZone = zonemap.findZoneId(startPoint);
						endZone = zonemap.findZoneId(endPoint);
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss;");
						Date date1 = null,date2 = null;
						date1 = sdf.parse(startTime);
						date2 = sdf.parse(endTime);
						pathTime = Distance.timeDistance(date2, date1);
						all++;
						if(pathTime>60)
						{
							taxiod = new TaxiOD(taxiID,startPoint,startTime,startZone,endPoint,endTime,endZone,pathTime);
							ODList.add(taxiod);
							bw.write(taxiod.taxiID+","+taxiod.startPoint+","+taxiod.startTime+","+taxiod.startZone+","+taxiod.endPoint+","+taxiod.endTime+","+taxiod.endZone+","+taxiod.pathTime+"\r\n");
							right++;
						}
						
					}
				}
			}
			br.close();
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//ODList里面存着20130801的所有OD-含车辆ID、TAZ、时间
//		System.out.println("FindDone, Unfind = "+zonemap.unfind);
//		try 
//		{
//			for(int i=0 ; i < ODList.size() ; i++ )
//			{
//				TaxiOD od = ODList.get(i);
//				System.out.println(od.taxiID+" "+od.startPoint+" "+od.startTime+" "+od.startZone+" "+od.endPoint+" "+od.endTime+" "+od.endZone);
//
//				bw.write(od.taxiID+","+od.startPoint+","+od.startTime+","+od.startZone+","+od.endPoint+","+od.endTime+","+od.endZone+"\r\n");
//
//			}
//			bw.close();
//		}
//		 catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("OD时间大于60比例:"+right/all+"-->Done");
	}
}
