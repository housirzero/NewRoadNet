package grid;

import java.util.List;
import java.util.Date;
import team.net.graph.LngLat;

public class Grid {
	
	private List<LngLat> centers; // ��������
	private LineSegment lineSeg; // �߶�
	
	public static void main(String[] args) {
		Date start = new Date();
		run();
		Date end = new Date();
		System.out.println("run time : " + (end.getTime() - start.getTime())
				/ 1000.0 + " s.");
	}

	public static void run() {

		// kmeans ��������Ϊ��ʼ
		// �ҵ�����վ�����С͹�հ�
		// �������������ĺ��ĵ�֮�仭�д��ߣ������������͹�հ��������򻮷ֳ���
		// �γ�ÿ��������Ϣ��¼���߽�����У�
		// �ڰٶȵ�ͼ��ͶӰ
	}
}
