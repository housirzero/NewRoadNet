package team.routerec.main;

/**
 * ��¼OD֮�仨�ѵ�ʱ�䣬�������Ϣ
 */
public class ODTrip {
	public int O;
	public int D;
	public int costTime; // ����ʱ��
	public double distance;
	
	public ODTrip(int o, int d, int costTime, double distance) {
		super();
		O = o;
		D = d;
		this.costTime = costTime;
		this.distance = distance;
	}

	@Override
	public String toString() {
		return O + ", " + D + ", " + costTime + ", " + distance;
	}

}
