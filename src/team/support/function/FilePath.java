package team.support.function;

/**
 * �ļ�·����
 * ȫ�ֱ���
 * @author summer
 *
 */
public class FilePath {
	/**
	 * ������������ڣ�����Ϊ��λ
	 */
	static String date = "20150108";
	/**
	 * �������ļ���
	 */
	public static String dataFolder = "E:\\TransData\\";
	
	
	/********************************************����ˢ�������ļ�*********************************************/
	
	/**����վ����Ϣ�ļ�**/
	public static String busStation = dataFolder+"BaseInformation\\BusStation2015.csv";

	/********************************************����GPS�ļ�*************************************************/
	
	/**Gps���ݰ���·�洢�ļ���**/
	public static String gps = dataFolder+ "ResultData\\GPS\\20150108";
	
	/**Gps��վʱ�䰴��·�洢�ļ���**/
	public static String gpsArrivalTime = dataFolder+ "ResultData\\GPS\\20150108_BusArriveTimev2\\";
	
	
	/*********************************************�����ļ�**************************************************/
	/**
	 * ����վ��
	 */
	public static String subwayStation = dataFolder+"BaseInformation\\SubwayStation2015.csv";
	/**
	 * ��������Ԥ������ļ�
	 */
	public static String subwayCardRecord = dataFolder+"DailyData\\AFC\\"+date+".csv";
	/**
	 * �������˳��������������ļ�
	 */
	public static String subwayTripChain = dataFolder+ "ResultData\\AFC\\"+date+"_TripChain.csv";
	
    
	/****************************************�������г��ļ�**************************************************/
	/**
	 * ���г�վ��
	 */
	public static String bikeStation = dataFolder+"BaseInformation\\BikeStation2015.csv";
	/**
	 * �������г�����Ԥ������ļ�
	 */
	public static String bikeCardRecord = dataFolder+"DailyData\\BIKE\\"+date+".csv";
	/**
	 * �������г����˳��������������ļ�
	 */
	public static String bikeTripChain = dataFolder+ "ResultData\\BIKE\\"+date+"_TripChain.csv";
	
	
	/****************************************����������Ϣ�ļ�************************************************/
	
	/**
	 * ����IC��ˢ��������·��ת�ɹ���Gps������·���Ķ�Ӧ���ļ���57300��300����
	 */
	public static String NumToStr = dataFolder+"BaseInformation\\NumToStr.csv";
	
	/**
	 * ��ͨС�����ݴ洢�ļ���
	 */
	public static String TransZone = dataFolder+"BaseInformation\\TransZone";
	
}
