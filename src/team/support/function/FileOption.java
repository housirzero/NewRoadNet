package team.support.function;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileOption {

	/**
	 * ����filePath��Ҫ���ļ���
	 * @param filePath
	 */
	public static void mkDir( String filePath )
	{
		File file = new File(filePath);
		File parentFolder = file.getParentFile();
		if(!parentFolder.exists())
			parentFolder.mkdirs();
	}

	/**
	 * ��·��ת��
	 * ����ת����
	 * @return
	 */
	public static String name2Num( String name )
	{
		String res = "'";
		return res;
	}
	
	/**
	 * ��·��ת��
	 * ����ת����
	 * @return
	 */
	public static String num2Name(String num)
	{
		String res = "'";
		return res;
	}
	/**
	 * ����һ���ļ�������
	 * @param file
	 */
	public static int getFileLines(File file)
	{
		int lines = 0;
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while (reader.readLine() != null)
				++lines;
//			System.out.println("�ļ�" + filePath+ "������" + lines);
			reader.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return lines;
	}
	
	   /**
     * �ж��ļ��ı����ʽ
     * 
     * @param fileName
     *            :file
     * @return �ļ������ʽ
     * @throws Exception
     */
    public static String codeString(String fileName) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(
                fileName));
        int p = (bin.read() << 8) + bin.read();
        String code = null;
 
        switch (p) {
        case 0xefbb:
            code = "UTF-8";
            break;
        case 0xfffe:
            code = "Unicode";
            break;
        case 0xfeff:
            code = "UTF-16BE";
            break;
        default:
            code = "GBK";
        }
        bin.close();

        return code;
    }
    
    /**
     * ��resdFilePath�ж�ȡn�в�д��saveFilePath�ļ���
     * ��Ҫ���ڿ����͵��ļ���������һ����ı��༭���򿪣�
     * @param resdFilePath
     * @param saveFilePath
     * @param n
     */
    public static void readNLines( String resdFilePath, String saveFilePath, int n  )
    {
    	int lineCount = 0;
		String line;
		try
		{
//			BufferedReader reader = new BufferedReader(new FileReader(resdFilePath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resdFilePath), "GBK"));
			BufferedWriter writer = new BufferedWriter(new FileWriter(saveFilePath));
			while ( ++lineCount < n && ( line = reader.readLine() ) != null)
			{
				writer.write(line+"\r\n");
//				System.out.println(line);
				System.out.println(lineCount);
			}
			System.out.println("д���ļ�" + resdFilePath+ "������" + lineCount);
			reader.close();
			writer.close();
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
    }
}
