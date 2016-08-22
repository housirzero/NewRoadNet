package team.routerec.main;

/**
 * 记录OD之间花费的时间，距离等信息
 */
public class ODTrip {
	public int O;
	public int D;
	public int costTime; // 花费时间
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
