package kmeans;

public class ClusterPoint<E> {

	private int classId;
//	private boolean isClassed; // �Ƿ��ѱ����࣬���ڻ��ò���
	
	private E e;
	
	public ClusterPoint(E e) {
		this.classId = -1; // δ����
		this.e = e;
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

//	public boolean isClassed() {
//		return isClassed;
//	}
//
//	public void setClassed(boolean isClassed) {
//		this.isClassed = isClassed;
//	}

	public E getE() {
		return e;
	}

	public void setE(E e) {
		this.e = e;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return classId+"";
	}
	
	
}
