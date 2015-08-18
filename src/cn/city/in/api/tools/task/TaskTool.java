package cn.city.in.api.tools.task;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.city.in.api.tools.common.FileMonitorTool;
import cn.city.in.api.tools.common.NumberTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.ReflectRunnable;
import cn.city.in.api.tools.common.ReflectTool;
import cn.city.in.api.tools.common.SoftHashMap;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.api.tools.common.TimeTool;

/**
 * 功能:定时任务功能模块
 * 
 * @author 黄林 2011-8-23
 * @version
 */
public class TaskTool {
	private static Logger log = Logger.getLogger(TaskTool.class);
	/** The max task code. */
	private static Integer sysTaskCode = -1;

	private static String fileName = "";

	/** The short name. */
	private static HashMap<String, String> shortName = new HashMap<String, String>();
	private static SoftHashMap<Object[], Integer> delayTaskCodeMap ;
	/** The comm task. */
	private static CommonTaskThread commTask;
	private static CommonTaskCheckThread ctct;
	public static boolean die = false;

	/**
	 * 功能:添加任务 创建者： 黄林 2011-8-23.
	 * 
	 * @param task
	 *            the task
	 * @return integer
	 */
	public static Integer addTask(BaseTaskInfo task) {
		if (null == commTask) {
			init(true);
		}
		if (null == task.getTaskCode()) {
			sysTaskCode--;
			task.setTaskCode(sysTaskCode);
		}
		commTask.getTaskMap().put(task.getTaskCode(), task);
		return task.getTaskCode();
	}

	/**
	 * 功能:检查公共线程是否正常运行 创建者： 黄林 2011-8-25.
	 */
	public static void check() {
		// 检查线程是否存活
		if (!die) {
			if (null != commTask && !commTask.isAlive()) {// 关闭线程，创建新线程
				Map<Integer, BaseTaskInfo> taskMap = commTask.getTaskMap();
				commTask.die();
				commTask = createCommonTask(taskMap, commTask.isDaemon());
				commTask.start();
			}
		}
	}

	/**
	 * 功能:创建公共任务线程 创建者： 黄林 2011-11-17.
	 * 
	 * @param taskMap
	 *            the task map
	 * @return common task thread
	 */
	private static CommonTaskThread createCommonTask(
			Map<Integer, BaseTaskInfo> taskMap, boolean daemon) {
		CommonTaskThread task = null;
		if (null != taskMap) {
			task = new CommonTaskThread(taskMap);
		} else {
			task = new CommonTaskThread();
		}
		task.setDaemon(daemon);
		return task;
	}

	/**
	 * 功能:创建任务 创建者： 黄林 2012-1-29.
	 * 
	 * @param single
	 *            是否单线程
	 * @param loopCount
	 *            循环次数
	 * @param time
	 *            时间
	 * @param method
	 *            方法
	 * @param obj
	 *            方法载体
	 * @param params
	 *            方法参数
	 * @return integer 任务唯一标识
	 */
	public static Integer createCronTask(String cron, Method method,
			Object obj, Object... params) {
		CronTaskInfo task = new CronTaskInfo();
		task.setBean(obj);
		task.setMethod(method);
		task.setParams(params);
		task.setCron(cron);
		return addTask(task);
	}

	/**
	 * 功能:依据cron、方法名‘类名、参数创建任务 创建者： 黄林 2012-1-29.
	 * 
	 * @param single
	 *            是否单线程
	 * @param time
	 *            时间
	 * @param method
	 *            方法
	 * @param bean
	 *            方法载体
	 * @param params
	 *            参数
	 * @return integer 任务唯一参数
	 * @throws Exception
	 *             the exception
	 */
	public static Integer createCronTask(String cron, String method,
			Object bean, Object... params) throws Exception {
		Class<?>[] parameterTypes = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			parameterTypes[i] = params[i].getClass();
		}
		if (bean instanceof String) {
			if (bean.toString().indexOf(".") == -1) {
				return createCronTask(cron, Class.forName(shortName.get(bean))
						.getMethod(method, parameterTypes),
						Class.forName(shortName.get(bean)).newInstance(),
						params);
			}
			return createCronTask(cron, Class.forName(bean.toString())
					.getMethod(method, parameterTypes),
					Class.forName(bean.toString()).newInstance(), params);
		} else {
			return createCronTask(cron,
					bean.getClass().getMethod(method, parameterTypes), bean,
					params);
		}

	}

	/**
	 * 功能:依据字符串参数创建任务 创建者： 黄林 2011-11-30.
	 * 
	 * @param taskCode
	 *            任务码
	 * @param params
	 *            参数
	 * @return integer 任务码
	 */
	public static Integer createTask(Integer taskCode, String[] params) {
		BaseTaskInfo task;
		int paramLength = 0;
		if (params[0].trim().equals("t")) {
			task = new TimeTaskInfo();
			paramLength = 5;
			((TimeTaskInfo) task).setLoopCount(NumberTool.valueOf(params[1])
					.intValue());
			((TimeTaskInfo) task).setTime(NumberTool.valueOf(params[2])
					.longValue());
		} else {
			task = new CronTaskInfo();
			paramLength = 4;
			if (TimeTool.validCron(params[1])) {
				((CronTaskInfo) task).setCron(params[1]);
			} else {
				log.error("task " + taskCode + " is not cron :" + params[2]);
				return 0;
			}
		}
		int methodLength = paramLength - 1;
		int classLength = methodLength - 1;
		task.setTaskCode(taskCode);
		String[] methodParams = null;
		if (params.length > paramLength) {
			methodParams = new String[params.length - paramLength];
			for (int i = paramLength; i < params.length; i++) {
				methodParams[i - paramLength] = params[i];
			}
			try {
				task.setParams(ReflectTool.getObjectParams(methodParams));
			} catch (Exception e) {
			}
		}
		try {
			if (params[classLength].indexOf(".") == -1) {// 别名判断
				if (null == shortName.get(params[classLength])) {
					String message = taskCode + ":task no short name:"
							+ params[classLength];
					log.warn(message);
					throw new Exception(message);
				}
				task.setBean(Class.forName(shortName.get(params[classLength]))
						.newInstance());
				task.setMethod(Class
						.forName(shortName.get(params[classLength])).getMethod(
								params[methodLength],
								ReflectTool.getParamTypes(methodParams)));
			} else {
				task.setBean(Class.forName(params[classLength]).newInstance());
				task.setMethod(Class.forName(params[classLength]).getMethod(
						params[methodLength]));
			}
		} catch (Exception e) {
			log.warn("create task fail:", e);
		}
		taskCode = addTask(task);
		return taskCode;
	}

	/**
	 * 功能:依据方法名‘类名、参数创建任务，0.1s后执行 创建者： 黄林 2011-8-24.
	 * 
	 * @param method
	 *            方法
	 * @param bean
	 *            类名
	 * @param params
	 *            参数
	 * @return integer 任务唯一参数
	 * @throws Exception
	 *             the exception
	 */
	public static Integer createTask(String method, Object bean,
			Object... params) throws Exception {
		return createTimeTask(10L, method, bean, params);
	}

	/**
	 * 功能:依据配置文件创建任务 创建者： 黄林 2011-8-24.
	 * 
	 * @param key
	 *            配置项
	 * @return integer
	 */
	public static Integer createTask(String key, String value) {
		String[] rules = null;
		if (value.indexOf("'") != -1) {// 处理参数
			int keycount = 0;
			List<String> paList = new ArrayList<String>();
			while (value.indexOf("'") != -1) {
				keycount++;
				String pa = StringTool.getExpr(value, "'");
				value = value.replace(pa, "\\p" + keycount);
				pa = pa.substring(1, pa.length() - 1);
				paList.add(pa);
			}
			rules = value.split(",");
			for (int i = 0; i < rules.length; i++) {
				String string = rules[i];
				if (string.contains("\\p")) {
					int index = string.indexOf("\\p");
					int num = Integer.valueOf(string.substring(index + 2,
							index + 3));
					string = string.replace("\\p" + num, paList.get(num - 1));
					rules[i] = string;
				}
			}

		} else {
			rules = value.split(",");
		}
		Integer taskCode = createTask(Integer.valueOf(key), rules);
		return taskCode;
	}

	public static Integer createTask(String[] params) {
		sysTaskCode--;
		return createTask(sysTaskCode, params);
	}

	/**
	 * 创建延迟任务，如任务存在并且未执行，则将执行时间延后，否则创建一个新的延迟任务
	 * 
	 * @param time
	 *            延迟执行时间
	 * @param method
	 *            方法
	 * @param obj
	 *            对象
	 * @param params
	 *            参数
	 * @author 黄林 Creates the time delay task.
	 */
	public static void createTimeDelayTask(Long time, Method method,
			Object obj, Object... params) {
		Object[] onlyArray = new Object[] { method, obj, params };
		Integer delayCode;
		if (null==delayTaskCodeMap) {
			delayTaskCodeMap = new SoftHashMap<Object[], Integer>();
			try {
				createTimeTask(-1,60*100l,"cleanTimeDelayTask", TaskTool.class, new Object[0]);
			} catch (Exception e) {
				log.warn("can't create cleanTimeDelayTask",e);
			}
		}
		synchronized (delayTaskCodeMap) {
			delayCode = delayTaskCodeMap.get(onlyArray);
		}
		if (null != delayCode && null != getTask(delayCode)) {
			TimeTaskInfo timeTask = (TimeTaskInfo) getTask(delayCode);
			if (timeTask.getTime() != -1) {
				timeTask.setTime(time);
				timeTask.setLoopCount(1);
				return;
			}
		}
		Integer code = createTimeTask(1, time, method, obj, params);
		synchronized (delayTaskCodeMap) {
			delayTaskCodeMap.put(onlyArray, code);
		}
	}
	
	/**
	 * 回收延迟任务内存空间
	 *
	 * @author 黄林
	 * Clean time delay task.
	 */
	public static void cleanTimeDelayTask()
	{
		Set<Integer> codes=getTaskCodes();
		synchronized (delayTaskCodeMap) {
			for (Iterator iterator = delayTaskCodeMap.entrySet().iterator(); iterator.hasNext();) {
				Entry<Object[], Integer> entry = (Entry<Object[], Integer>) iterator.next();
				if (!codes.contains(entry.getValue())) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * 创建延迟任务，如任务存在并且未执行，则将执行时间延后，否则创建一个新的延迟任务
	 * 
	 * @param time
	 *            延迟执行时间
	 * @param method
	 *            方法
	 * @param obj
	 *            对象
	 * @param params
	 *            参数
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Creates the time delay task.
	 */
	public static void createTimeDelayTask(Long time, String method,
			Object bean, Object... params) throws Exception {
		Class<?>[] parameterTypes = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			parameterTypes[i] = params[i].getClass();
		}
		if (bean instanceof String) {
			if (bean.toString().indexOf(".") == -1) {
				createTimeDelayTask(time, Class.forName(shortName.get(bean))
						.getMethod(method, parameterTypes),
						Class.forName(shortName.get(bean)).newInstance(),
						params);
			}
			createTimeDelayTask(
					time,
					Class.forName(bean.toString()).getMethod(method,
							parameterTypes), Class.forName(bean.toString())
							.newInstance(), params);
		} else {
			createTimeDelayTask(time,
					bean.getClass().getMethod(method, parameterTypes), bean,
					params);
		}
	}

	/**
	 * 创建延迟任务，如任务存在并且未执行，则将执行时间延后，否则创建一个新的延迟任务. 默认延迟0.1s 执行
	 * 
	 * @param method
	 *            方法
	 * @param bean
	 *            对象
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Creates the time delay task.
	 */
	public static void createTimeDelayTask(String method, Object bean)
			throws Exception {
		createTimeDelayTask(10L, method, bean, new Object[0]);
	}

	/**
	 * 功能:创建任务 创建者： 黄林 2011-8-24.
	 * 
	 * @param loopCount
	 *            循环次数
	 * @param time
	 *            时间
	 * @param method
	 *            方法
	 * @param obj
	 *            方法载体
	 * @param params
	 *            方法参数
	 * @return integer 任务唯一标识
	 */
	public static Integer createTimeTask(Integer loopCount, Long time,
			Method method, Object obj, Object... params) {
		TimeTaskInfo task = new TimeTaskInfo();
		task.setBean(obj);
		task.setMethod(method);
		task.setParams(params);
		task.setLoopCount(loopCount);
		task.setTime(time);
		return addTask(task);
	}

	/**
	 * 功能:创建任务，传入可执行类型
	 * 
	 * @param loopCount
	 *            the loop count
	 * @param time
	 *            the time
	 * @param runnable
	 *            the runnable
	 * @return the integer
	 * @author 黄林
	 */
	public static Integer createTimeTask(Integer loopCount, Long time,
			ReflectRunnable runnable) {
		TimeTaskInfo task = new TimeTaskInfo();
		task.setBean(runnable.getBean());
		task.setMethod(runnable.getMethod());
		task.setParams(runnable.getParams());
		task.setLoopCount(loopCount);
		task.setTime(time);
		return addTask(task);
	}

	/**
	 * 功能:依据循环次数、类型、时间、方法名‘类名、参数创建任务 创建者： 黄林 2011-8-31.
	 * 
	 * @param loopCount
	 *            the loop count
	 * @param time
	 *            the time
	 * @param method
	 *            the method
	 * @param bean
	 *            the bean
	 * @param params
	 *            the params
	 * @return the integer
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static Integer createTimeTask(Integer loopCount, Long time,
			String method, Object bean, Object... params) throws Exception {
		Class<?>[] parameterTypes = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			parameterTypes[i] = params[i].getClass();
		}
		Method m;
		Object o;
		if (bean instanceof String) {
			if (bean.toString().indexOf(".") == -1) {
				o = Class.forName(shortName.get(bean)).newInstance();
				m = o.getClass().getMethod(method, parameterTypes);
			} else {
				o = Class.forName(bean.toString()).newInstance();
				m = o.getClass().getMethod(method, parameterTypes);
			}
		} else if (bean instanceof Class) {
			o = ((Class) bean).newInstance();
			m = ((Class) bean).getMethod(method, parameterTypes);
		} else {
			m = bean.getClass().getMethod(method, parameterTypes);
			o = bean;
		}
		return createTimeTask(loopCount, time, m, o, params);

	}

	/**
	 * 功能:依据类型、时间、方法名‘类名、参数创建任务 创建者： 黄林 2011-8-31.
	 * 
	 * @param time
	 *            时间
	 * @param method
	 *            方法
	 * @param bean
	 *            方法载体
	 * @param params
	 *            参数
	 * @return integer 任务唯一参数
	 * @throws Exception
	 *             the exception
	 */
	public static Integer createTimeTask(Long time, String method, Object bean,
			Object... params) throws Exception {
		return createTimeTask(1, time, method, bean, params);

	}

	/**
	 * 功能:依据方法名‘类名创建任务，0.1s后执行 创建者： 黄林 2011-8-24.
	 * 
	 * @param method
	 *            方法
	 * @param bean
	 *            类名
	 * @return integer 任务唯一标识
	 * @throws Exception
	 *             the exception
	 */
	public static Integer createTimeTask(String method, Object bean)
			throws Exception {
		return createTimeTask(10L, method, bean, new Object[0]);
	}

	/**
	 * 功能:将短名转化为全名 创建者： 黄林 2011-11-30.
	 * 
	 * @param name
	 *            the name
	 * @return string
	 */
	public static String dealShortName(String name) {
		return shortName.get(name);
	}

	/**
	 * 功能:获取所有短方法名 创建者： 黄林 2011-11-30.
	 * 
	 * @param name
	 *            the name
	 * @return string
	 */
	public static HashMap<String, String> getShortName() {
		return shortName;
	}

	/**
	 * 获取任务.
	 * 
	 * @param taskCode
	 *            the task code
	 * @return the task
	 */
	public static BaseTaskInfo getTask(Integer taskCode) {
		return commTask.getTaskMap().get(taskCode);
	}

	/**
	 * 获取任务唯一标识.
	 * 
	 * @return the task codes
	 */
	public static Set<Integer> getTaskCodes() {
		return commTask.getTaskMap().keySet();
	}

	/**
	 * 功能:初始化空任务列表,注意! 仅用于测试或者不需要配置文件的情况下，使用该方法初始化不能使用reload方法重载配置文件! 创建者： 黄林
	 * 2011-8-24.
	 * 
	 * @return true,
	 */
	public static boolean init(boolean daemon) {
		commTask = createCommonTask(null, daemon);
		ctct = new CommonTaskCheckThread();
		// 创建线程
		log.debug("task load ok,size:" + commTask.getTaskMap().size());
		commTask.start();		
		return true;
	}

	/**
	 * 功能:初始化 创建者： 黄林 2011-8-24.
	 * 
	 * @return true,
	 */
	public static boolean putAllTaskByPropertyFile(String propertyKey,
			boolean daemon) {
		fileName = propertyKey;
		if (commTask != null) {
			// 定时任务已经以最小化方式初始化
			if (commTask.isDaemon() != daemon) {
				// 重新初始化新线程
				Map<Integer, BaseTaskInfo> taskMap = commTask.getTaskMap();
				commTask.die();
				commTask = createCommonTask(taskMap, daemon);
				commTask.start();
			}
		} else {
			commTask = createCommonTask(null, daemon);
			commTask.start();
		}
		HashMap<String, String> shutName = PropertyTool
				.readFilesAsMap(PropertyTool.getFilesInProperty(fileName));
		for (Entry<String, String> entry : shutName.entrySet()) {// 添加别名
			try {
				Integer.valueOf(entry.getKey());
			} catch (NumberFormatException e) {
				shortName.put(entry.getKey(), entry.getValue());
			}
		}
		for (Entry<String, String> entry : shutName.entrySet()) {// 创建任务
			try {
				Integer.valueOf(entry.getKey());
			} catch (NumberFormatException e) {
				continue;
			}
			try {
				createTask(entry.getKey(), entry.getValue());
				log.info("create task ok:" + entry.getKey());
			} catch (NumberFormatException e) {
				log.warn("create task fail:" + entry.getKey(), e);
			}
		}
		// 添加配置文件监视
		try {
			FileMonitorTool.addFileWatch(fileName,
					ReflectTool.getRunnable("reload", new TaskTool()));
		} catch (Exception e) {
			log.error("fail add file watch ", e);
		}
		log.debug("task load ok,size:" + commTask.getTaskMap().size());
		return true;
	}

	/**
	 * 功能:重载任务 创建者： 黄林 2011-8-24.
	 */
	public static Integer reload() {
		// 重新加载任务
		HashMap<String, String> shutName = PropertyTool
				.readFilesAsMap(PropertyTool.getFilesInProperty(fileName));
		Integer addTaskCount = 0;
		for (Entry<String, String> entry : shutName.entrySet()) {// 添加别名
			try {
				Integer.valueOf(entry.getKey());
			} catch (NumberFormatException e) {
				shortName.put(entry.getKey(), entry.getValue());
			}
		}
		for (Entry<String, String> entry : shutName.entrySet()) {// 创建任务
			try {
				Integer.valueOf(entry.getKey());
			} catch (NumberFormatException e) {
				continue;
			}
			try {
				if (entry.getValue().split(",")[0].equals("c")) {
					if (null == getTask(Integer.valueOf(entry.getKey()))) {
						createTask(entry.getKey(), entry.getValue());
						addTaskCount++;
						log.info("create task ok:" + entry.getKey());
					}
				}
			} catch (NumberFormatException e) {
				log.warn("create task fail:" + entry.getKey());
			}
		}
		log.debug("task load ok,size:" + commTask.getTaskMap().size());
		return addTaskCount;
	}

	/**
	 * 功能:移除任务 创建者： 黄林 2011-8-23.
	 * 
	 * @param taskCode
	 *            the task code
	 */
	public static void removeTask(Integer taskCode) {
		if (commTask.getTaskMap().containsKey(taskCode)) {
			commTask.getTaskMap().remove(taskCode);
		}
	}

	public static void shutdown() {
		die = true;
		if (null != commTask) {
			commTask.die();
		}
		if (null != ctct) {
			ctct.die();
		}
	}

}
