package grid;

import team.net.graph.LngLat;

public class LineSegment {
	public String mark; // 线段的唯一标识，用来区分是和那些类别有关
//	public double k; // 斜率
	public double dy;
	public double dx;
	public double b; // 构造成 y = k * x + b
	public LngLat start;
	public LngLat mid; // 已知点
	public LngLat end;
	
	public LineSegment(String mark, double dy, double dx, double b, LngLat start,
			LngLat mid, LngLat end) {
		super();
		this.mark = mark;
		this.dy = dy;
		this.dx = dx;
		this.b = b;
		this.start = start;
		if(mid == null && start != null && end != null)
			mid = new LngLat( (start.lng+end.lng)/2, (start.lat+end.lat)/2 );
		this.mid = mid;
		this.end = end;
	}

	@Override
	public String toString() {
		return start + "," + end;
	}
	
}
