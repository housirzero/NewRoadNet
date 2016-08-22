package team.support.function;

/**
 * ������
 * 
 * @author DELL
 * 
 */
public class ErrorControl
{
	/**
	 * ��ӡ��־ʱ��ȡ��ǰ�ĳ����ļ������кš������� �����ʽΪ��[FileName | LineNumber | MethodName]
	 * 
	 * @return
	 */
	public static String getFileLineMethod()
	{
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		StringBuffer toStringBuffer = new StringBuffer("[").append(traceElement.getFileName()).append(" | ").append(traceElement.getLineNumber()).append(" | ").append(traceElement.getMethodName()).append("]");
		return toStringBuffer.toString();
	}

	/**
	 * 
	 * ��ȡ��ǰ jvm ���ڴ���Ϣ
	 * 
	 * @return
	 */
	public static String getMemoryInfo()
	{
		Runtime currRuntime = Runtime.getRuntime();
		int nFreeMemory = (int) (currRuntime.freeMemory() / 1024 / 1024);
		int nTotalMemory = (int) (currRuntime.totalMemory() / 1024 / 1024);
		String result =  nFreeMemory + "M/" + nTotalMemory + "M(free/total)";
		System.out.println(" �ڴ���Ϣ :" + result);
		return result;
	}
}
