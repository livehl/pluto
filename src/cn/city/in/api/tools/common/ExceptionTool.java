package cn.city.in.api.tools.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ExceptionTool {

	/**
	 * 裁取堆栈,仅筛选包含cn.city.in的包
	 * 
	 * @param tx
	 *            the tx
	 * @return the throwable
	 * @author 黄林
	 */
	public static Throwable cutStackTrace(Throwable tx) {
		return cutStackTrace(tx, "cn.city.in", false);
	}

	/**
	 * 裁取堆栈,仅筛选包含指定包的条目.
	 * 
	 * @param tx
	 *            the tx
	 * @param packName
	 *            the pack name
	 * @param printHead
	 *            是否打印第一次出现包以前的详细堆栈
	 * @return the throwable
	 * @author 黄林 Cut stack trace.
	 */
	public static Throwable cutStackTrace(Throwable tx, String packName,
			boolean printHead) {
		StackTraceElement[] ste = tx.getStackTrace();
		List<StackTraceElement> stel = new ArrayList<StackTraceElement>();
		boolean isHead = true;
		for (StackTraceElement stackTraceElement : ste) {
			if (stackTraceElement.toString().indexOf(packName) != -1) {
				isHead = false;
				stel.add(stackTraceElement);
			} else if (isHead && printHead) {
				stel.add(stackTraceElement);
			}
		}
		tx.setStackTrace(stel.toArray(new StackTraceElement[0]));
		return tx;
	}

	/**
	 * 裁取堆栈,仅筛选包含cn.city.in的包,包含前段错误堆栈
	 * 
	 * @param tx
	 *            the tx
	 * @return the throwable
	 * @author 黄林
	 */
	public static Throwable cutStackTraceWithHead(Throwable tx) {
		return cutStackTrace(tx, "cn.city.in", true);
	}

	/**
	 * 获取所有堆栈的文本消息
	 * 
	 * @return the all
	 * @author 黄林
	 */
	public static String getAllStackTraces() {
		Map<Thread, StackTraceElement[]> threadDumpMap = Thread
				.getAllStackTraces();
		StringBuffer sb = new StringBuffer();
		Set<Entry<Thread, StackTraceElement[]>> threadDump = threadDumpMap
				.entrySet();
		for (Entry<Thread, StackTraceElement[]> entry : threadDump) {
			sb.append(entry.getKey().getName() + "\r\n");
			for (StackTraceElement ste : entry.getValue()) {
				sb.append("\t" + ste.toString() + "\r\n");
			}
		}
		return sb.toString();
	}

	/**
	 * 获取堆栈的文本消息
	 * 
	 * @return the all
	 * @author 黄林
	 */
	public static String getStackTraceString(StackTraceElement[] StackTraces) {
		StringBuffer sb = new StringBuffer();
		for (StackTraceElement ste : StackTraces) {
			sb.append("\t" + ste.toString() + "\r\n");
		}
		return sb.toString();
	}

	/**
	 * 迭代输出堆栈为字符串
	 * 
	 * @param tx
	 *            the tx
	 * @return the stack trace string
	 * @author 黄林
	 */
	public static String getStackTraceString(Throwable tx) {
		StringBuffer sb = new StringBuffer(tx.getMessage() + "\r\n");
		for (int i = 0; i < tx.getStackTrace().length; i++) {
			sb.append(tx.getStackTrace()[i].toString() + "\r\n");
		}
		tx = tx.getCause();
		if (null != tx) {
			sb.append("\t" + getStackTraceString(tx) + "\r\n");
		}
		return sb.toString();
	}
}
