package grid;

import team.net.graph.LngLat;

public class LineSegment {
	public String mark; // �߶ε�Ψһ��ʶ�����������Ǻ���Щ����й�
//	public double k; // б��
	public double dy;
	public double dx;
	public double b; // ����� y = k * x + b
	public LngLat start;
	public LngLat mid; // ��֪��
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
