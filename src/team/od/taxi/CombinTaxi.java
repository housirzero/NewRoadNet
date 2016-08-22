package team.od.taxi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class CombinTaxi {
	public static String filedate ="20130801";
	public static String filePath = "E:\\Data\\����2\\����\\"+filedate;
	public static int folderNum = 0;//ͨ��init()����Ժ���ļ��и���
	static class SortByTaxiAndTime implements Comparator<String>
	{
		public static String[] taxiinfo1 = new String[11];
		public static String[] taxiinfo2 = new String[11];
		
		List<String> taxiinfo = new ArrayList<String>();
		//20130731,235957,BJJYJ,154925,116.1192627,39.7280006,0.0,74,0,1,2013-08-01 00:00:00;
		public int compare(String e1, String e2)
		{
			taxiinfo1 = e1.split(",");
			taxiinfo2 = e2.split(",");
			if(taxiinfo1[3].compareTo(taxiinfo2[3])>0)
				return 1;
			else if(taxiinfo1[3].compareTo(taxiinfo2[3])==0)
			{
				if(taxiinfo1[10].compareTo(taxiinfo2[10])>0)
					return 1;
				else
					return -1;
			}
			return -1;
		}

	}
	
	
	/**
	 * �Ƚ�e1��e2�Ĵ�С��e1С��e2����1��e1����e2����-1
	 * ������Ĳ�ͬ������������Ĳ�ͬ������������Ĳ�ͬ����������Ҫ�Ļ�˵����=L =��
	 * ��SortDoubleFile()���õ��ˡ���
	 * @param e1
	 * @param e2
	 * @return
	 */
	public static int compare(String e1, String e2)
	{
		String[] taxiinfo1 = new String[11];
		String[] taxiinfo2 = new String[11];
		taxiinfo1 = e1.split(",");
		taxiinfo2 = e2.split(",");
		if(taxiinfo1[3].compareTo(taxiinfo2[3])<0)
			return 1;
		else if(taxiinfo1[3].compareTo(taxiinfo2[3])==0)
		{
			if(taxiinfo1[10].compareTo(taxiinfo2[10])<0)
				return 1;
			else
				return -1;
		}
		return -1;
	}
	
	

	/**
	 * ���������ݵ��ļ��в��Ϊ����ļ��У������ڴ治����=��=
	 */
	public static void init()
	{
		int i = 1;
		BufferedReader br = null;
		BufferedWriter bw = null;
		File files = new File(filePath);
		int filesNum = (int) files.list().length;
		int count = 0;
		File newFile = new File(filePath+"\\"+filedate+"_"+i);
		newFile.mkdir();
		//System.out.println(filesNum);
		try {
			for(File file : files.listFiles()){
				if(!file.isFile())
					continue;

				br = new BufferedReader(new FileReader(file));
				bw = new BufferedWriter(new FileWriter(filePath+"\\"+filedate+"_"+i+"\\"+file.getName()));

				String line = null;
				while((line = br.readLine())!=null)
				{
					bw.write(line+"\r\n");
				}
				count++;
				if(count == (filesNum/6)*i)
				{
					System.out.println("������Ŀ¼");
					i++;
					if(count != filesNum){
						newFile = new File(filePath+"\\"+filedate+"_"+i);
						newFile.mkdir();
					}
					
				}
				br.close();
				bw.close();

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(i);
		folderNum = i;
		System.out.println("InitDone");
	}
	
	/**
	 * �����ļ����е��ļ����кϲ����򣬱���Ϊcsv�ļ�
	 */
	public static void SortSingleFolder()
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		List<String> taxiinfo = new ArrayList<String>();
		int count =0;

		try {
			for(int i = 1;i < folderNum ; i++){
				bw = new BufferedWriter(new FileWriter(new File(filePath+"\\"+filedate+"_"+i+".csv")));				
				File files = new File(filePath+"\\"+filedate+"_"+i);
				for(File file : files.listFiles())
				{
					if(!file.isFile())
						continue;
					br = new BufferedReader(new FileReader(file));
					String line = null;
					while( (line = br.readLine()) != null )
					{
						taxiinfo.add(line);
						count++;
						//bw.write(line+"\r\n");
					}
					System.out.println(file.getName()+" Done");
					br.close();
				}
				System.out.println("count = "+count+"; AddInfoDOne");
				
				Collections.sort(taxiinfo,new SortByTaxiAndTime());
				System.out.println("SortDone");
				
				for(String info : taxiinfo)
				{
					bw.write(info+"\r\n");
				}
				System.out.println("Folder "+i+" WriteFileDone");
				
				bw.close();
				taxiinfo = null;
				taxiinfo = new ArrayList<String>();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("SortSingleFolder Done");
	}
	
	/**
	 * ���ļ����ڵ�SortSingleFolder�еõ���csv�ļ�����������ϲ�������"����.csv"��
	 */
	public static void SortDoubleFile()
	{
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		BufferedWriter bw = null;
		List<String> taxiinfo1 = new ArrayList<String>();
		List<String> taxiinfo2 = new ArrayList<String>();
		String pathName = "20130801_1";
		try {
			for(int i = 2 ; i <= folderNum ; i++ )
			{
				bw = new BufferedWriter(new FileWriter(new File(filePath+"\\"+pathName+"_"+i+".csv")));
				br1 = new BufferedReader(new FileReader(filePath+"\\"+pathName+".csv"));
				br2 = new BufferedReader(new FileReader(filePath+"\\"+filedate+"_"+i+".csv"));
				
				String line1=null,line2=null;
				int count = 0;
				line1 = br1.readLine();
				line2 = br2.readLine();
				while( (line1!= null) && (line2 != null ))
				{
					if(compare(line1,line2)==1)
					{
						bw.write(line1+"\r\n");
						line1 = br1.readLine();
					}
					else if(compare(line1,line2)==-1)
					{
						bw.write(line2+"\r\n");
						line2 = br2.readLine();
					}
					count++;
				}
				System.out.println("HalfDone,count="+count);
				while((line1 = br1.readLine()) != null)
				{
					//System.out.println("file1��");
					bw.write(line1+"\r\n");
					count++;
				}
				while((line2 = br2.readLine()) != null)
				{
					//System.out.println("file2��");
					bw.write(line2+"\r\n");
				}
				System.out.println("WriteDone,count="+count);
				br1.close();
				br2.close();
				bw.close();	
				
				pathName = pathName + "_"+i;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("SortDoubleFile Done");
	}
	public static void main(String[] args) {
		init();
		SortSingleFolder();
		SortDoubleFile();
	}
}
