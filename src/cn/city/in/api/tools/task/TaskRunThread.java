package cn.city.in.api.tools.task;

import org.apache.log4j.Logger;

import cn.city.in.api.tools.common.TimeTool;
import cn.city.in.api.tools.monitor.MonitorTool;

/**
 * 功能:定时任务执行线程
 * 
 * @author 黄林 2011-8-23
 * @version
 */
public class TaskRunThread extends Thread {
	private static Logger log = Logger.getLogger(TaskRunThread.class);
	private BaseTaskInfo task;

	/**
	 * 功能: 创建者： 黄林 2011-8-23.
	 */
	public TaskRunThread(BaseTaskInfo task) {
		super("singleTaskThread" + task.getTaskCode());
		this.setDaemon(true);
		this.task = task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			Integer mark = TimeTool.addTimeMark();
			Object obj = task.getMethod().invoke(task.getBean(),
					task.getParams());
			String className = "";
			if (null != task.getBean()) {
				className = task.getBean().getClass().getName();
			}
			MonitorTool.addExecTime(className + "."
					+ task.getMethod().getName(), TimeTool.getTimeMarkDiff(mark));
			String taskLog = TimeTool.getFormatStringByNow()
					+ ":task-run-ok:"
					+ task.getTaskCode()
					+ ":"
					+ task.getBean()
					+ ":"
					+ task.getMethod().getName()
					+ " ,"
					+ ((null != task.getParams() && task.getParams().length > 0) ? task
							.getParams()[0] : "null") + " ," + obj;
			if (null != log) {
				log.debug(taskLog);
			}
		} catch (Exception e) {
			if (null != log) {
				log.error("run task fail!", e);
			}
		}
	}

}
