package cn.city.in.api.tools.monitor;

/**
 * 功能:性能记录工具
 * 
 * @author 黄林 2011-11-2
 * @version
 */
public class MonitorTool {

	private static String ALL_TIME_TOTAL = "AET,AllTimeTotal,";
	private static String EXEC_COUNT = "EC,ExecCOUNT,";
	private static String MAX_TIME = "BT,MaxTime,";
	private static String MIN_TIME = "ST,MinTime,";
	private static String AVG_TIME = "AT,AvgTime,";
	private static String WEIGTHED_AVERAGE = "WA,WeightedAverage,";
	private static String EXCEPTION_COUNT = "EMC,ExceptionCount,";

	public static int PROFILER = 0;

	/** 状态表. */
	public static MonitorHashMap monitorMap = new MonitorHashMap();

	/**
	 * 功能:累加方法执行时间 创建者： 黄林 2011-11-2.
	 * 
	 * @param methodName
	 *            the method name
	 * @param time
	 *            the time
	 */
	public static void addExecTime(String methodName, Long time) {
		monitorMap.addKeyCount(ALL_TIME_TOTAL + methodName, time);
		monitorMap.addKeyCount("ET,TimeTotal", time);
		monitorMap.addKeyCount(EXEC_COUNT + methodName);
		monitorMap.getSetMax(MAX_TIME + methodName, time);
		monitorMap.getSetMin(MIN_TIME + methodName, time);
		monitorMap.addKeyCount("RC,RequestCount");
		monitorMap.setAvg(AVG_TIME + methodName, time);
		try {
			Long avg = getValue(ALL_TIME_TOTAL + methodName)
					/ getValue(EXEC_COUNT + methodName);
			monitorMap.put(WEIGTHED_AVERAGE + methodName, avg);
		} catch (Exception e) {
		}
	}

	/**
	 * 功能:增加异常次数 创建者： 黄林 2011-11-2.
	 * 
	 * @param exName
	 *            the ex name
	 */
	public static void addThrowableCount(String exName, String method) {
		monitorMap.addKeyCount("TC,ThrowableCount," + exName);
		monitorMap.addKeyCount("MT,ThrowableCount,ExceptionCount");
		monitorMap.addKeyCount(EXCEPTION_COUNT + method);
	}

	/**
	 * 获取所有键值.
	 * 
	 * @return the all keys
	 */
	public static String[] getAllKeys() {
		return monitorMap.keySet().toArray(new String[0]);
	}

	/**
	 * 返回值
	 * 
	 * @param key
	 *            the key
	 * @return the value
	 */
	public static Long getValue(String key) {
		return monitorMap.get(key);
	}
}
