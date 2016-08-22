package team.od.taxi;



public class TaxiTimeDistribution {

	public String oZone = null, dZone = null;
	//OD时间段分布，10min为一段，一天一共144段，用int型数组存储每段中出现的次数
	public int distribution[] = new int[144];
		
}
