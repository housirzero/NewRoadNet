package team.support.function;

/**
 * 差错控制
 * 
 * @author DELL
 * 
 */
public class ErrorControl
{
	/**
	 * 打印日志时获取当前的程序文件名、行号、方法名 输出格式为：[FileName | LineNumber | MethodName]
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
	 * 获取当前 jvm 的内存信息
	 * 
	 * @return
	 */
	public static String getMemoryInfo()
	{
		Runtime currRuntime = Runtime.getRuntime();
		int nFreeMemory = (int) (currRuntime.freeMemory() / 1024 / 1024);
		int nTotalMemory = (int) (currRuntime.totalMemory() / 1024 / 1024);
		String result =  nFreeMemory + "M/" + nTotalMemory + "M(free/total)";
		System.out.println(" 内存信息 :" + result);
		return result;
	}
}
