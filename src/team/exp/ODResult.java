package team.exp;

/**
 * OD ���н��
 */
public class ODResult {
	
	public int O;
	public int D;
	public int timeBudget;
	public int costTime;
//	public double dis; // ������룬��λ����
	public int dis; // �ֶξ��룬��1��ʼ
	public int transTime;
	public double prob; // ����
	
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