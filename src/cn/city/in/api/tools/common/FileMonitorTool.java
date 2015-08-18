package cn.city.in.api.tools.common;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.city.in.api.tools.task.TaskTool;

/**
 * 文件监视类,注意!这货引用了定时任务
 * 
 * @author 黄林 The Class FileMonitorTool.
 */
public class FileMonitorTool {
	public static final FileMonitorTool FILE_MONITOR_TOOL = new FileMonitorTool();
	private static boolean hasInit = false;

	/** 表达式\定时任务 @author 黄林 The exp task map. */
	private static Map<String, ReflectRunnable> expTaskMap = new ConcurrentHashMap<String, ReflectRunnable>();

	/** 表达式\文件信息 @author 黄林 The exp info map. */
	private static Map<String, Map> expInfoMap = new ConcurrentHashMap<String, Map>();

	/**
	 * 添加一个文件监听.
	 * 
	 * @param fileExp
	 *            文件表达式
	 * @param run
	 *            回调执行器，使用ReflectTool 依据方法、实例创建
	 * @author 黄林 Adds the file watch.
	 */
	public static void addFileWatch(String fileExp, ReflectRunnable run) {
		if (!hasInit) {
			// 添加扫描任务
			try {
				TaskTool.createTimeTask(-1, 50L, "scan", FILE_MONITOR_TOOL);
			} catch (Exception e) {
				throw new RuntimeException("add file watch task fail", e);
			}
		}
		expTaskMap.put(fileExp, run);
		// 初次扫描
		expInfoMap.put(fileExp, getExpInfo(fileExp));
	}

	/**
	 * 获取表达式所指定的文件的信息
	 * 
	 * @param exp
	 *            the exp
	 * @return the exp info
	 * @author 黄林
	 */
	public static Map<String, Long> getExpInfo(String exp) {
		Map<String, Long> fileInfoMap = new HashMap<String, Long>();
		List<File> files = FileTool.getClassPathFiles(exp);
		for (File file : files) {
			fileInfoMap.put(file.getAbsolutePath(), file.lastModified());
		}
		return fileInfoMap;
	}

	/**
	 * 扫描任务
	 * 
	 * @author 黄林 Scan.
	 */
	public static void scan() {
		// 对比扫描
		Set<String> keySet = expTaskMap.keySet();
		for (String exp : keySet) {
			Map<String, Long> oldInfo = expInfoMap.get(exp);
			Map<String, Long> newInfo = getExpInfo(exp);
			if (oldInfo.size() != newInfo.size()) {// 文件缺失或增加
				// 异步回调
				TaskTool.createTimeTask(1, 10L, expTaskMap.get(exp));
				expInfoMap.put(exp, newInfo);
			}
			Set<Entry<String, Long>> files = newInfo.entrySet();
			for (Entry<String, Long> entry : files) {
				if (oldInfo.containsKey(entry.getKey())
						&& entry.getValue().equals(oldInfo.get(entry.getKey()))) {
					continue;
				} else// 文件新增或最后修改时间变更
				{
					// 异步回调
					TaskTool.createTimeTask(1, 10L, expTaskMap.get(exp));
					expInfoMap.put(exp, newInfo);
					break;
				}
			}
		}
	}
}
