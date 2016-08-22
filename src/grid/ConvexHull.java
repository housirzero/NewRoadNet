package grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * ͹��<br>
 * https://zh.wikipedia.org/wiki/%E5%87%B8%E5%8C%85<br>
 * https://en.wikipedia.org/wiki/Computational_geometry<br>
 */
public abstract class ConvexHull<E> {

	public abstract double getX(E e);
	public abstract double getY(E e);
	
	/**
	 * �ҵ�y������С�ĵ㣻���ж������ѡ��x������С��
	 * ������С�������
	 */
	private int minE(List<E> points)
	{
		if(points == null || points.size() == 0)
			return -1;
		int index = 0;
		E res = points.get(index);
		for(int i = 1; i < points.size(); i++)
		{
			E cur = points.get(i);
			if( (getY(cur) < getY(res)) ||
					(getY(cur) == getY(res) && getX(cur) < getX(res)) )
			{
				res = cur;
				index = i;
			}
		}
		return index;
	}
	
	/**
	 * ��������Jarvis��������<br>
	 * <br>
	 * ��ע�� ���д��㷨��ı�points��Ԫ�ص�˳��
	 * <br>
	 * ������һ��ض���͹���ĵ㿪ʼ�����������һ��A_1��
	 * Ȼ��ѡ��A_2��ʹ�����е㶼��A_1A_2���ҷ����ⲽ���ʱ�临�Ӷ���O(n)��Ҫ�Ƚ����е���A_1Ϊԭ��ļ�����Ƕȡ�
	 * ��A_2Ϊԭ�㣬�ظ�������裬�����ҵ�A_3,A_4,...,A_k,A_1��
	 * ���ܹ���k������ˣ�ʱ�临�Ӷ�ΪO(kn)��
	 */
	public List<E> jarvis(List<E> points){
		List<E> result = new ArrayList<E>();
		return result;
	}
	
	/**
	 * �����㣨Graham��ɨ�跨<br>
	 * <br>
	 * ��ע�� ���д��㷨��ı�points��Ԫ�ص�˳��
	 * <br><br>
	 * ����׵�һ��A_1��ʼ������ж�������ĵ㣬��ôѡ������ߵģ���
	 * ��������������������ߺ�x������ĽǶȣ���С������Щ�����򣬳����ǵĶ�Ӧ��ΪA_2,A_3,...,A_n�������ʱ�临�Ӷȿɴ�O(nlog{n})��
	 * ������С�ĽǶȶ�Ӧ�ĵ�A_3��
	 * ����A_2��A_3��·�����A_1��A_2��·��������ת�ģ���������һ������A_1�ߵ�A_2����վ��A_2ʱ�������ı߸ı䷽�򣩣���ʾA_3��������͹���ϵ�һ�㣬������һ����A_2��A_4��·����
	 * ����Ϳ���A_3��A_4��·���Ƿ�����ת����ֱ���ص�A_1��
	 * ����㷨������ʱ�临�Ӷ���O(nlog{n})��ע��ÿ��ֻ�ᱻ����һ�Σ�������Jarvis�������лῼ�Ƕ�Ρ�
	 * ����㷨�ɸ�������1972�귢��������ȱ���ǲ����ƹ㵽��ά���ϵ������
	 * <br>
	 */
	public Stack<E> graham(List<E> points){
		int minIndex = minE(points); // �ҵ�y������С�ĵ㣻���ж������ѡ��x������С��
		E p0 = points.get(minIndex);
		Stack<E> stack = new Stack<E>();
		stack.push(p0);
		points.remove(minIndex);
		
		Collections.sort(points, new sortByPolarAngle(p0));

		int index = 0;
		stack.push(points.get(index++));
		stack.push(points.get(index++));
		for( ; index < points.size(); index++){
			E o = stack.elementAt(stack.size()-2);
			E p1 = stack.peek();
			E p2 = points.get(index);
			while(direction(o, p1, p1, p2) == 1) // non-left
			{
				stack.pop();
				if(stack.size() < 2)
					return null;
				o = stack.get(stack.size()-2);
				p1 = stack.peek();
			}
			stack.push(p2);
		}
		
		return stack;
	}
	
	/**
	 * ����
	 */
	public abstract double distance(E e1, E e2);
	

	private static final double eps = 0.000001;
	
	/**
	 * ������������жϷ���
	 * �ڽ�������ʱ  o1==o2
	 * ���жϹ���ʱ o1=o, o2=p1
	 * area>0ʱ��˵��PolarAngle(p1) < PolarAngle(p2), o-->p1-->p2����ת
	 * 
	 * @return -1 : o-->p1-->p2����ת��
	 * 			1 �� ����ת
	 */
	public int direction(E o1, E p1, E o2, E p2){
		double x1 = getX(p1) - getX(o1);
		double y1 = getY(p1) - getY(o1);
		double x2 = getX(p2) - getX(o2);
		double y2 = getY(p2) - getY(o2);
		double area = x1 * y2 - x2 * y1;
		if(area > 0)
			return -1;
		return 1;
	}
	
	class sortByPolarAngle implements Comparator<E>
	{
		private E origin; // ԭ��
		
		public sortByPolarAngle(E origin) {
			this.origin = origin;
		}

		@Override
		public int compare(E o1, E o2) {
			// ����O1*O2>0 : PolarAngle(O2) > PolarAngle(O1)
			// ��O1*O2<0ʱ��˵��PolarAngle(O1) > PolarAngle(O2)
			double x1 = getX(o1) - getX(origin);
			double y1 = getY(o1) - getY(origin);
			double x2 = getX(o2) - getX(origin);
			double y2 = getY(o2) - getY(origin);
			double area = x1 * y2 - x2 * y1;
//			if(Math.abs(area) < eps && distance(o1,origin) < distance(o2,origin))
//				return 1;
//			else 
			if(area < 0)
				return 1;
			return -1;
		}
		
	}
}

