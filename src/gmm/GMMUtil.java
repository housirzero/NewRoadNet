package gmm;

import java.util.ArrayList;

public class GMMUtil {
	/**
	 * 
	* @Title: computeDistance 
	* @Description: �������������ڵ��ľ���
	* @return double
	* @throws
	 */
	public static double computeDistance(ArrayList<Double> d1, ArrayList<Double> d2) {
		double squareSum = 0;
		for(int i = 0; i < d1.size() - 1; i++) {
			squareSum += (d1.get(i) - d2.get(i)) * (d1.get(i) - d2.get(i));
		}
		return Math.sqrt(squareSum);
	}
	
	/**
	 * 
	* @Title: computeCov 
	* @Description: ����Э�������
	* @return ArrayList<ArrayList<Double>>
	* @throws
	 */
	public static ArrayList<ArrayList<Double>> computeCov(ArrayList<ArrayList<Double>> dataSet, int dataDimen, int dataNum) {
		ArrayList<ArrayList<Double>> res = new ArrayList<ArrayList<Double>>();
		
		// ����ÿһά���ݵľ�ֵ
		double[] sum = new double[dataDimen];
		for(ArrayList<Double> data : dataSet) {
			for(int i = 0; i < dataDimen; i++) {
				sum[i] += data.get(i);
			}
		}
		for(int i = 0; i < dataDimen; i++) {
			sum[i] = sum[i] / dataNum;
		}
		
		// ���������������ݵ�Э����
		for(int i = 0; i < dataDimen; i++) {
			ArrayList<Double> tmp = new ArrayList<Double>();
			for(int j = 0; j < dataDimen; j++) {
				double cov = 0;
				for(ArrayList<Double> data : dataSet) {
					cov += (data.get(i) - sum[i]) * (data.get(j) - sum[j]);
				}
				tmp.add(cov);
			}
			res.add(tmp);
		}
		return res;
	}
	
	/**
	 * 
	* @Title: computeInv 
	* @Description: �������������
	* @return ArrayList<ArrayList<Double>>
	* @throws
	 */
	public static double[][] computeInv(ArrayList<ArrayList<Double>> dataSet) {
		int dataDimen = dataSet.size();
		double[][] res = new double[dataDimen][dataDimen];
		
		// ��listת��Ϊarray
		double[][] a = toArray(dataSet);
		
		// ����������
		double detA = computeDet(dataSet, dataDimen); // �������������ʽ
		 for (int i = 0; i < dataDimen; i++) {  
	            for (int j = 0; j < dataDimen; j++) {  
	                double num;  
	                if ((i + j) % 2 == 0) {  
	                    num = computeDet(toList(computeAC(a, i + 1, j + 1)), dataDimen - 1);  
	                } else {  
	                    num = -computeDet(toList(computeAC(a, i + 1, j + 1)), dataDimen - 1);  
	                }  
	                res[j][i] = num / detA;  
	            }  
	        }  
		return res;
	}
	
	/**
	 * 
	* @Title: computeAC 
	* @Description: ��ָ���С��еĴ�������ʽ(algebraic complement)
	* @return double[][]
	* @throws
	 */
	public static double[][] computeAC(double[][] dataSet, int r, int c) {
		int H = dataSet.length;  
        int V = dataSet[0].length;  
        double[][] newData = new double[H - 1][V - 1];  
  
        for (int i = 0; i < newData.length; i++) {  
            if (i < r - 1) {  
                for (int j = 0; j < newData[i].length; j++) {  
                    if (j < c - 1) {  
                        newData[i][j] = dataSet[i][j];  
                    } else {  
                        newData[i][j] = dataSet[i][j + 1];  
                    }  
                }  
            } else {  
                for (int j = 0; j < newData[i].length; j++) {  
                    if (j < c - 1) {  
                        newData[i][j] = dataSet[i + 1][j];  
                    } else {  
                        newData[i][j] = dataSet[i + 1][j + 1];  
                    }  
                }  
  
            }  
        } 
		return newData;
	}
	
	/**
	 * 
	* @Title: computeDet 
	* @Description: ��������ʽ
	* @return double
	* @throws
	 */
	public static double computeDet(ArrayList<ArrayList<Double>> dataSet, int dataDimen) {
		// ��listת��Ϊarray
		double[][] a = toArray(dataSet);
		
		if(dataDimen == 2) {
			return a[0][0] * a[1][1] - a[0][1] * a[1][0];
		}
		double res = 0;
		for(int i = 0; i < dataDimen; i++) {
			if(i % 2 == 0) {
				res += a[0][i] * computeDet(toList(computeAC(toArray(dataSet), 1, i + 1)), dataDimen - 1);
			} else {
				res += -a[0][i] * computeDet(toList(computeAC(toArray(dataSet), 1, i + 1)), dataDimen - 1);
			}
		}
		
		return res;
	}
	
	/**
	 * 
	* @Title: toList 
	* @Description: ��arrayת����list
	* @return ArrayList<ArrayList<Double>>
	* @throws
	 */
	public static ArrayList<ArrayList<Double>> toList(double[][] a) {
		ArrayList<ArrayList<Double>> res = new ArrayList<ArrayList<Double>>();
		for(int i = 0; i < a.length; i++) {
			ArrayList<Double> tmp = new ArrayList<Double>();
			for(int j = 0; j < a[i].length; j++) {
				tmp.add(a[i][j]);
			}
			res.add(tmp);
		}
		return res;
	}
	
	public static double[][] matrixMultiply(double[][] a, double[][] b) {
		double[][] res = new double[a.length][b[0].length];
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < b[0].length; j++) {
				for(int k = 0; k < a[0].length; k++) {
					res[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		return res;
	}
	
	/**
	 * 
	* @Title: dotMatrixMultiply 
	* @Description: ����ĵ�ˣ�����ӦԪ�����
	* @return double[][]
	* @throws
	 */
	public static double[][] dotMatrixMultiply (double[][] a, double[][] b) {
		double[][] res = new double[a.length][a[0].length];
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a[0].length; j++) {
				res[i][j] = a[i][j] * b[i][j];
			}
		}
		return res;
	}
	
	/**
	 * 
	* @Title: dotMatrixMultiply 
	* @Description: ����ĵ��������ӦԪ�����
	* @return double[][]
	* @throws
	 */
	public static double[][] dotMatrixDivide(double[][] a, double[][] b) {
		double[][] res = new double[a.length][a[0].length];
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a[0].length; j++) {
				res[i][j] = a[i][j] / b[i][j];
			}
		}
		return res;
	}
	
	/**
	 * 
	* @Title: repmat 
	* @Description: ��Ӧmatlab��repmat�ĺ������Ծ�����к���������ƽ��
	* @return double[][]
	* @throws
	 */
	public static double[][] repmat(double[][] a, int row, int clo) {
		double[][] res = new double[a.length * row][a[0].length * clo];
		
		return null;
	}
	
	/**
	 * 
	* @Title: matrixMinux 
	* @Description: ���㼯��ֻ��
	* @return ArrayList<ArrayList<Double>>
	* @throws
	 */
	public static ArrayList<Double> matrixMinus(ArrayList<Double> a1, ArrayList<Double> a2) {
		ArrayList<Double> res = new ArrayList<Double>();
		for(int i = 0; i < a1.size(); i++) {
			res.add(a1.get(i) - a2.get(i));
		}
		return res;
	}
	
	/**
	 * 
	* @Title: matrixSum 
	* @Description: ���ؾ���ÿ��֮��(mark==2)��ÿ��֮��(mark==1)
	* @return ArrayList<Double>
	* @throws
	 */
	public static double[] matrixSum(double[][] a, int mark) {
		double res[] = new double[a.length];
		if(mark == 1) { // ����ÿ��֮�ͣ�����������
			res = new double[a[0].length];
			for(int i = 0; i < a[0].length; i++) {
				for(int j = 0; j < a.length; j++) {
					res[i] += a[j][i];
				}
			}
		} else if (mark == 2) { // ����ÿ��֮�ͣ� ����������
			for(int i = 0; i < a.length; i++) {
				for(int j = 0; j < a[0].length; j++) {
					res[i] += a[i][j];
				}
		}
		
	}
	return res;
}
	
	public static double[][] toArray(ArrayList<ArrayList<Double>> a) {
		int row = a.size();
		int col = a.get(0).size();
		
		double[][] res = new double[row][col];
		
		for(int i = 0; i < row; i++) {
			for(int j = 0; j < col; j++) {
				res[i][j] = a.get(i).get(j);
			}
		}
		
		return res;
	}
	
	public static double[][] toArray1(ArrayList<Double> a) {
		int dataDimen = a.size();
		double[][] res = new double[1][dataDimen];
		
		for(int i = 0; i < dataDimen; i++) {
				res[0][i] = a.get(i);
		}
		
		return res;
	}
	
	/**
	 * 
	* @Title: matrixReverse 
	* @Description: ����ר��
	* @return double[][]
	* @throws
	 */
	public static double[][] matrixReverse(double[][] a) {
		double[][] res = new double[a[0].length][a.length];
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a[0].length; j++) {
				res[j][i] = a[i][j];
			}
		}
		return res;
	}
	
	/**
	 * 
	* @Title: diag 
	* @Description: �����Խǻ�
	* @return double[][]
	* @throws
	 */
	public static double[][] diag(double[] a) {
		double[][] res = new double[a.length][a.length];
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < a.length; j++) {
				if(i == j) {
					res[i][j] = a[i];
				}
			}
		}
		return res;
	}
}
