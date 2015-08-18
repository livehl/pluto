package cn.city.in.api.tools.taskpool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PoolThread extends Thread {
	private static Log log = LogFactory.getLog(PoolThread.class);
	private PoolInterface taskListImpl;

	public PoolThread(PoolInterface task) {
		super("Pool task");
		this.taskListImpl = task;
	}

	@Override
	public void run() {
		Object before = null;
		try {
			before = taskListImpl.doBefore();
			Object[] task = taskListImpl.getTask();
			while (null != task) {
				try {
					taskListImpl.doTask(before, task);
					taskListImpl.addSucces(task);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					taskListImpl.addError(task);
				} finally {
					task = taskListImpl.getTask();
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			log.error(e.getCause().getMessage(), e.getCause());
		} finally {
			taskListImpl.doAfter(before, this);
		}

	}

}
