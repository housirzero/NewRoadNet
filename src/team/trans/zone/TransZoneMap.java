package team.trans.zone;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import team.net.graph.LngLat;
import team.support.function.Distance;
import team.support.function.FilePath;

/**
 * 命名太浪费时间了，随便命一个
 */
public class TransZoneMap
{
	public static String parentPath = FilePath.TransZone;
	public static HashMap<Integer, TransZone> transZoneMap;
	public static ArrayList<Center> centerList;

	/**
	 * 获取一个点与所有小区质心的距离列表（从小到大排序）
	 */
	private static ArrayList<ZoneDis> getDisList(LngLat lngLat)
	{
		ArrayList<ZoneDis> zoneDisList = new ArrayList<ZoneDis>();
		for (Center center : centerList)
		{
			zoneDisList.add(new ZoneDis(center.zoneId, Distance.lngLatDistanceInNum(center.lngLat, lngLat)));
		}
		Collections.sort(zoneDisList, new ZoneDisSort());
		return zoneDisList;
	}

	public static HashMap<Integer, TransZone> genTransZoneMap()
	{
		HashMap<Integer, TransZone> transZoneHashMap = null;
		try
		{
			// 从文件中读取
			String mifpath = parentPath + "\\Afterchangetranszone (2).mif";
			String centerpath = parentPath + "\\center (2).csv";

			// 读取质心
			BufferedReader reader = new BufferedReader(new FileReader(centerpath));
			centerList = new ArrayList<Center>();
			String line = null;
			String[] item = null;
			while ((line = reader.readLine()) != null)
			{
				item = line.split(",");
				Center center = new Center(item[0], item[1], item[2]);
				centerList.add(center);
			}
			reader.close();

			// 读取交通小区边界
			BufferedReader br = new BufferedReader(new FileReader(mifpath));
			int cnt = 0;
			transZoneHashMap = new HashMap<Integer, TransZone>();

			while ((line = br.readLine()) != null)
			{
				if (line.equals("Region  1"))
				{
					TransZone transZone = new TransZone(centerList.get(cnt).zoneId, centerList.get(cnt).lngLat);
					// 扫每个区域的节点数
					line = br.readLine();
					// 从经纬度开始读
					line = br.readLine();
					cnt++;
					while (!line.equals("PEN(1,2,7237230)"))
					{
						item = line.split(" ");
						transZone.pointsList.add(new LngLat(item[0], item[1]));
						line = br.readLine();
					}
					transZoneHashMap.put(transZone.id, transZone);
				}
			}
			br.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return transZoneHashMap;
	}
	
	/**
	 * 获取两个交通小区质心的距离（单位：米）
	 */
	public static double getDisInRegions( int o, int d )
	{
		if(transZoneMap == null)
			init();
		TransZone O = transZoneMap.get(o);
		TransZone D = transZoneMap.get(d);
		if(O == null || D == null)
			return 9999999;
		
		return Distance.lngLatDistance(O.center, D.center);
	}
	
	public static LngLat getRegionGps( int id )
	{
		if(transZoneMap == null)
			init();
		TransZone O = transZoneMap.get(id);
		if(O != null)
			return O.center;
		return null;
		
	}

	public static int find = 0;
	public static int unfind = 0;
	/**
	 * 找到一个经纬度的交通小区号
	 * 没有找到返回-1
	 */
	public static int findZoneId(LngLat lngLat)
	{
		if(transZoneMap == null)
			init();
//		if(transZoneMap == null)
//			System.err.println("transZoneMap == null");
		ArrayList<ZoneDis> zoneDisList = getDisList(lngLat);
		for (int i = 0; i < zoneDisList.size(); ++i)
		{
			int id = zoneDisList.get(i).zoneId;
			TransZone transZone = transZoneMap.get(id);
			if (transZone != null && transZone.contains(lngLat))
			{
//				System.out.println(i + "找到！ at findZoneId()");
				find++;
				return id;
			}
//			if (i >= 100)
//			{
//				System.err.println("前10个都没有找到！ at findZoneId()");
//				unfind++;
//				return -1;
//			}
		}
//		System.err.println("没有找到" + lngLat.toString() + "at findZoneId()");
		unfind++;
		return -1;
	}

	/**
	 * 判断一个经纬度是否在某一交通小区（zoneId）
	 */
	public static boolean isInZone(LngLat lngLat, int zoneId)
	{
		TransZone transZone = transZoneMap.get(zoneId);
		if (transZone != null && transZone.contains(lngLat))
		{
			return true;
		}
		return false;
	}
	
	public static void deal(String filePath)
	{
		// 读最早的聚类记录文件
		// 每读一个记录，就获取其所属交通小区
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			List<ClusterRecord> clusterRecordList = new ArrayList<ClusterRecord>();
			String line = null;
			String[] item = null;
			// 地铁天通苑北站,116.4138012,40.0858129,4063,599,1
			while ((line = reader.readLine()) != null)
			{
				item = line.split(",");
				if (item.length >= 6)
				{
					int index = 0;
					ClusterRecord record = new ClusterRecord(item[index++], item[index++], item[index++], item[index++], item[index++], item[index++]);
					record.realZoneId = findZoneId(record.lngLat);
					clusterRecordList.add(record);
				}
			}
			reader.close();
			System.out.println("找到" + find + "\r\n未找到" + unfind);
			
//			if(1+4>1)
//				return ;
	
			// 按原聚类号排序
			Collections.sort(clusterRecordList, new ClusterRecordSortByClassId());
			
			// 相同聚类号的记录列表
			List<ClusterRecord> sameClassRecordList = new ArrayList<ClusterRecord>();
			int lastClassId = -2;
			
			int MAX_ZONE_NUM = 2000;
			int[] zoneNum = new int[MAX_ZONE_NUM];
			// 用到的交通小区号, 值为此小区号内的客流
			int[] useZoneId = new int[MAX_ZONE_NUM];
			// 用到的交通小区号, 值为此小区号内的瞬时客流
			int[] useZoneId_timeFlow = new int[MAX_ZONE_NUM];
			// 用到的交通小区出现的天数
			int[] useZoneId_AppearTimes = new int[MAX_ZONE_NUM];
			// 在某一天是否出现
			boolean[] useZoneId_Appear = new boolean[MAX_ZONE_NUM];
			
			/**
			 * 对原聚类号相同的一组数据 根据少数服从多数的情况，重新更新其所属交通小区
			 */
			for (int i = 0; i < zoneNum.length; ++i)
				zoneNum[i] = 0;
	
			for (int i = 0; i < clusterRecordList.size(); ++i)
			{
				ClusterRecord record = clusterRecordList.get(i);
				if (record.classId == -1)
				{
					// attention 此处不能忘
					record.zoneId = record.realZoneId;
					useZoneId[record.zoneId] += record.passengerFlowAll;
					useZoneId_timeFlow[record.zoneId] += Integer.parseInt( record.timeFlow );
					useZoneId_Appear[record.zoneId] = true;
					
				} else
				{
					if (record.classId != lastClassId)
					{
						int zoneId = getMaxNumZoneId(zoneNum);
						// 将sameClassRecordList的zoneId更新
						for (ClusterRecord sameRecord : sameClassRecordList)
						{
							sameRecord.zoneId = zoneId;
							useZoneId[zoneId] += sameRecord.passengerFlowAll;
							useZoneId_timeFlow[zoneId] += Integer.parseInt( sameRecord.timeFlow );
						}
						sameClassRecordList.clear();
						for (int j = 0; j < zoneNum.length; ++j)
							zoneNum[j] = 0;
						useZoneId_Appear[zoneId] = true;
					}
					sameClassRecordList.add(record);
					++zoneNum[record.realZoneId];
				}
				lastClassId = record.classId;
			}
			// 处理尾巴
			int zoneId = getMaxNumZoneId(zoneNum);
			// 将sameClassRecordList的zoneId更新
			for (ClusterRecord sameRecord : sameClassRecordList)
			{
				sameRecord.zoneId = zoneId;
				useZoneId[zoneId] += sameRecord.passengerFlowAll;
				useZoneId_timeFlow[zoneId] += Integer.parseInt( sameRecord.timeFlow );
			}
			useZoneId_Appear[zoneId] = true;
	
			saveToFile(filePath.replace(".csv", "_Result.csv"), clusterRecordList);
	
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void init(){
		transZoneMap = genTransZoneMap();
//		116.338314	39.975043
//		LngLat lngLat = new LngLat(116.338314, 39.975043); // 知春路 ：312
		// 116.347857	39.975182
//		LngLat lngLat = new LngLat(116.347857,	39.975182); // 西土城 ：157
		// 116.347672	39.981449
//		LngLat lngLat = new LngLat(116.347572,	39.981449); // 北京航空航天大学：164
		// 北京西站 ： 116.316883,	39.895949
//		LngLat lngLat = new LngLat(116.316883,	39.895949); // 北京西站：747
		// 国图  116.318903,	39.943588   ======   298
//		LngLat lngLat = new LngLat(116.318903,	39.943588);
		//  惠新西街南口 ： 116.411502,	39.974691  ======= 399
//		LngLat lngLat = new LngLat(116.411502,	39.974691);
		// 立水桥北： 116.405763,	40.048119 ======= 932
//		LngLat lngLat = new LngLat(116.405763,	40.048119);
		// 116.406876,	40.051737
//		LngLat lngLat = new LngLat(116.311499,39.974675);
		// 地铁立水桥： 116.406878	40.05174
		// 和平西桥： 	116.409894	39.967918
		// 地铁安贞门站:  116.3992, 39.975547 ====== 503
		// 地铁大屯路东站: 116.411179, 40.00283 ====== 499
		LngLat lngLat = new LngLat(116.3992, 39.975547);
		int zoneId = findZoneId(lngLat);
		System.out.println(zoneId);
	}
	
	public static void main(String[] args)
	{
		init();
//		transZoneMap = genTransZoneMap();
//		deal("E:\\TransData\\BaseInformation\\TransZone\\StationInput2.csv");
	}

	/**
	 * 记录一个point与一个Zone质心的距离，及该质心的标号
	 */
	static class ZoneDis
	{
		public int zoneId;
		public double dis;

		public ZoneDis(int zoneId, double dis)
		{
			this.zoneId = zoneId;
			this.dis = dis;
		}
	}

	static class ZoneDisSort implements Comparator<ZoneDis>
	{
		@Override
		public int compare(ZoneDis e1, ZoneDis e2)
		{
			return (e1.dis > e2.dis) ? 1 : -1;
		}
	}
	
	/*************************deal 相关**********************************/
	static class ClusterRecordSortByClassId implements Comparator<ClusterRecord>
	{
		@Override
		public int compare(ClusterRecord e1, ClusterRecord e2)
		{
			return (e1.classId > e2.classId) ? 1 : -1;
		}
	}
	
	/**
	 * 获取数目最多的交通小区号
	 * 
	 * @param num
	 * @return
	 */
	public static int getMaxNumZoneId(int[] num)
	{
		int maxindex = 0;
		for (int i = 1; i < num.length; ++i)
		{
			if (num[maxindex] < num[i])
				maxindex = i;
		}
		return maxindex;
	}
	
	public static void saveToFile(String filePath,
			List<ClusterRecord> RecordList)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			for (int i = 0; i < RecordList.size(); ++i)
			{
				ClusterRecord record = RecordList.get(i);
				writer.write(record.toFileString() + "\r\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
