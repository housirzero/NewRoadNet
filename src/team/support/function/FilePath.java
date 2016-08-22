package team.support.function;

/**
 * 文件路径名
 * 全局变量
 * @author summer
 *
 */
public class FilePath {
	/**
	 * 处理的数据日期，按天为单位
	 */
	static String date = "20150108";
	/**
	 * 数据总文件夹
	 */
	public static String dataFolder = "E:\\TransData\\";
	
	
	/********************************************公交刷卡数据文件*********************************************/
	
	/**公交站点信息文件**/
	public static String busStation = dataFolder+"BaseInformation\\BusStation2015.csv";

	/********************************************公交GPS文件*************************************************/
	
	/**Gps数据按线路存储文件夹**/
	public static String gps = dataFolder+ "ResultData\\GPS\\20150108";
	
	/**Gps到站时间按线路存储文件夹**/
	public static String gpsArrivalTime = dataFolder+ "ResultData\\GPS\\20150108_BusArriveTimev2\\";
	
	
	/*********************************************地铁文件**************************************************/
	/**
	 * 地铁站点
	 */
	public static String subwayStation = dataFolder+"BaseInformation\\SubwayStation2015.csv";
	/**
	 * 地铁按天预处理后文件
	 */
	public static String subwayCardRecord = dataFolder+"DailyData\\AFC\\"+date+".csv";
	/**
	 * 地铁个人出行链数据生成文件
	 */
	public static String subwayTripChain = dataFolder+ "ResultData\\AFC\\"+date+"_TripChain.csv";
	
    
	/****************************************公租自行车文件**************************************************/
	/**
	 * 自行车站点
	 */
	public static String bikeStation = dataFolder+"BaseInformation\\BikeStation2015.csv";
	/**
	 * 公租自行车按天预处理后文件
	 */
	public static String bikeCardRecord = dataFolder+"DailyData\\BIKE\\"+date+".csv";
	/**
	 * 公租自行车个人出行链数据生成文件
	 */
	public static String bikeTripChain = dataFolder+ "ResultData\\BIKE\\"+date+"_TripChain.csv";
	
	
	/****************************************其它基础信息文件************************************************/
	
	/**
	 * 公交IC卡刷卡数字线路名转成公交Gps中文线路名的对应表文件，57300》300快内
	 */
	public static String NumToStr = dataFolder+"BaseInformation\\NumToStr.csv";
	
	/**
	 * 交通小区数据存储文件夹
	 */
	public static String TransZone = dataFolder+"BaseInformation\\TransZone";
	
}
