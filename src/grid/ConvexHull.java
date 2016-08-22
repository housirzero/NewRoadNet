package grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * 凸包<br>
 * https://zh.wikipedia.org/wiki/%E5%87%B8%E5%8C%85<br>
 * https://en.wikipedia.org/wiki/Computational_geometry<br>
 */
public abstract class ConvexHull<E> {

	public abstract double getX(E e);
	public abstract double getY(E e);
	
	/**
	 * 找到y坐标最小的点；若有多个，则选择x坐标最小的
	 * 返回最小点的索引
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
	 * 包裹法（Jarvis步进法）<br>
	 * <br>
	 * 【注】 运行此算法会改变points中元素的顺序
	 * <br>
	 * 首先由一点必定在凸包的点开始，例如最左的一点A_1。
	 * 然后选择A_2点使得所有点都在A_1A_2的右方，这步骤的时间复杂度是O(n)，要比较所有点以A_1为原点的极坐标角度。
	 * 以A_2为原点，重复这个步骤，依次找到A_3,A_4,...,A_k,A_1。
	 * 这总共有k步。因此，时间复杂度为O(kn)。
	 */
	public List<E> jarvis(List<E> points){
		List<E> result = new ArrayList<E>();
		return result;
	}
	
	/**
	 * 葛立恒（Graham）扫描法<br>
	 * <br>
	 * 【注】 运行此算法会改变points中元素的顺序
	 * <br><br>
	 * 由最底的一点A_1开始（如果有多个这样的点，那么选择最左边的），
	 * 计算它跟其他各点的连线和x轴正向的角度，按小至大将这些点排序，称它们的对应点为A_2,A_3,...,A_n。这里的时间复杂度可达O(nlog{n})。
	 * 考虑最小的角度对应的点A_3。
	 * 若由A_2到A_3的路径相对A_1到A_2的路径是向右转的（可以想象一个人沿A_1走到A_2，他站在A_2时，是向哪边改变方向），表示A_3不可能是凸包上的一点，考虑下一点由A_2到A_4的路径；
	 * 否则就考虑A_3到A_4的路径是否向右转……直到回到A_1。
	 * 这个算法的整体时间复杂度是O(nlog{n})，注意每点只会被考虑一次，而不像Jarvis步进法中会考虑多次。
	 * 这个算法由葛立恒在1972年发明。它的缺点是不能推广到二维以上的情况。
	 * <br>
	 */
	public Stack<E> graham(List<E> points){
		int minIndex = minE(points); // 找到y坐标最小的点；若有多个，则选择x坐标最小的
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
	 * 距离
	 */
	public abstract double distance(E e1, E e2);
	

	private static final double eps = 0.000001;
	
	/**
	 * 用向量叉积来判断方向
	 * 在进行排序时  o1==o2
	 * 在判断拐向时 o1=o, o2=p1
	 * area>0时，说明PolarAngle(p1) < PolarAngle(p2), o-->p1-->p2是左转
	 * 
	 * @return -1 : o-->p1-->p2是左转；
	 * 			1 ： 非左转
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
		private E origin; // 原点
		
		public sortByPolarAngle(E origin) {
			this.origin = origin;
		}

		@Override
		public int compare(E o1, E o2) {
			// 向量O1*O2>0 : PolarAngle(O2) > PolarAngle(O1)
			// 则当O1*O2<0时，说明PolarAngle(O1) > PolarAngle(O2)
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

