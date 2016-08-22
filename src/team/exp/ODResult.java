package team.exp;

/**
 * OD 运行结果
 */
public class ODResult {
	
	public int O;
	public int D;
	public int timeBudget;
	public int costTime;
//	public double dis; // 具体距离，单位是米
	public int dis; // 分段距离，从1开始
	public int transTime;
	public double prob; // 概率
	
	// O	D	time	realTime	realDistance	transNum	Probability
	public ODResult(int o, int d, int timeBudget, int costTime, int dis,
			int transTime, double prob) {
		super();
		O = o;
		D = d;
		this.timeBudget = timeBudget;
		this.costTime = costTime;
		this.dis = dis;
		this.transTime = transTime;
		this.prob = prob;
	}

}