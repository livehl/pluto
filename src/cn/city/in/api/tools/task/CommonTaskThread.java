package cn.city.in.api.tools.task;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import cn.city.in.api.tools.common.NetTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.TimeTool;

/**
 * 功能:定时任务分析线程,
 * 
 * @author 黄林 2011-8-23
 * @version
 */
public class CommonTaskThread extends Thread {
	private static Logger log = Logger.getLogger(CommonTaskThread.class);
	private Map<Integer, BaseTaskInfo> taskMap;
	private Boolean isLive = true;
	private ExecutorService pool ;

	public CommonTaskThread() {
		this(new ConcurrentHashMap<Integer, BaseTaskInfo>());
	}

	/**
	 * 功能: 创建者： 黄林 2011-8-23.
	 * 
	 */
	public CommonTaskThread(Map<Integer, BaseTaskInfo> taskMap) {
		super("commonTaskThread");
		int threadCount=10;
		if(null!=PropertyTool.getProperties("taskRunCount"))
		{
			threadCount=PropertyTool.getNumProperties("taskRunCount").intValue();
		}
		pool= Executors.newFixedThreadPool(threadCount);
		if (taskMap == null) {
			taskMap = new HashMap<Integer, BaseTaskInfo>();
		}
		this.taskMap = new ConcurrentHashMap<Integer, BaseTaskInfo>(taskMap);
	}

	/**
	 * 功能:结束线程 创建者： 黄林 2011-8-25.
	 */
	public void die() {
		isLive = false;
		pool.shutdown();
	}

	public Map<Integer, BaseTaskInfo> getTaskMap() {
		return taskMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int lastSecond = cal.get(Calendar.SECOND);
		while (isLive != null && isLive) {
			Integer[] taskCodes = new Integer[0];
			taskCodes = taskMap.keySet().toArray(taskCodes);
			cal.setTimeInMillis(System.currentTimeMillis());
			int lastValue = cal.get(Calendar.SECOND);
			boolean isNewSecond = lastSecond != lastValue ? true : false;
			lastSecond = isNewSecond ? lastValue : lastSecond;
			Date now = new Date();
			for (Integer taskCode : taskCodes) {
				BaseTaskInfo task = taskMap.get(taskCode);
				if (null == task) {
					taskMap.remove(taskCode);
					continue;
				}
				if (task instanceof CronTaskInfo) {// cron
					CronTaskInfo cronTask = (CronTaskInfo) task;
					if (isNewSecond) {
						try {
							if (TimeTool.matchCron(cronTask.getCron(), now)) {
								pool.execute(new TaskRunThread(task));
							}
						} catch (Exception e) {
							log.warn("run task error ", e);
						}
					}
				} else {// 计时
					TimeTaskInfo timeTask = (TimeTaskInfo) task;
					try {
						timeTask.setTime(timeTask.getTime() - 1);
						if (timeTask.getTime() <= 0) {
							if (timeTask.getTime() == 0) {
								pool.execute(new TaskRunThread(task));
								log.debug("run task:"+task.getMethod().getName());
								timeTask.reSetTime();
							} else {
								taskMap.remove(taskCode);
							}
						}
					} catch (Throwable e) {
						log.warn(e.getMessage(), e);
						if (e instanceof Error) {
							// 发封邮件
							try {
								NetTool.sendErrorMail(e);
							} catch (Throwable e1) {
								log.warn("send mail fail", e1);
							}
						}
					}

				}
			}
			// 延时
			try {
				sleep(10);
			} catch (InterruptedException e) {
				log.warn(e);
			}
		}
	}

	public void setTaskMap(Map<Integer, BaseTaskInfo> taskMap) {
		this.taskMap = new ConcurrentHashMap<Integer, BaseTaskInfo>(taskMap);
	}

}
