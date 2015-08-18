package cn.city.in.api.tools.taskpool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class BasePoolTaskImpl implements PoolInterface {

	/** 成功数. @author 黄林 The succes. */
	protected int succes = 0;

	/** 失败数 @author 黄林 The error. */
	protected int error = 0;

	/** 重试次数. @author 黄林 The retry count. */
	protected int retryCount = 3;

	/** 队列并发线程数 @author 黄林 The thread count. */
	protected int threadCount = 20;

	/** 线程列表 @author 黄林 The thread list. */
	protected ArrayList<PoolThread> threadList = new ArrayList<PoolThread>();

	/** 任务队列 @author 黄林 The task list. */
	protected ArrayList<Object[]> taskList = new ArrayList<Object[]>();

	/** 重试次数列表 */
	protected HashMap<Object, Integer> retryMap = new HashMap<Object, Integer>();

	@Override
	public void addError(Object obj) {
		synchronized (retryMap) {
			if (retryMap.containsKey(obj) && null != retryMap.get(obj)
					&& (retryMap.get(obj) >= retryCount)) {
				error++;
				return;
			}
			if (retryMap.containsKey(obj) && null != retryMap.get(obj)) {
				retryMap.put(obj, retryMap.get(obj) + 1);
			} else {
				retryMap.put(obj, 1);
			}
		}
		synchronized (taskList) {
			taskList.add((Object[]) obj);
		}
	}

	@Override
	public void addSucces(Object obj) {
		if (retryMap.containsKey(obj)) {
			synchronized (retryMap) {
				retryMap.remove(obj);
			}
		}
		succes++;
	}

	@Override
	public Object addTask(Object... task) {
		synchronized (taskList) {
			taskList.add(task);
		}
		if (threadList.size() < threadCount) {
			PoolThread thread = new PoolThread(this);
			synchronized (threadList) {
				threadList.add(thread);
			}
			thread.start();
		}
		return task;

	}

	@Override
	public Object doAfter(Object before, PoolThread thread) {
		synchronized (threadList) {
			threadList.remove(thread);
		}
		return null;
	}

	@Override
	public Object doBefore() throws Exception {
		return null;
	}

	@Override
	public abstract Object doTask(Object before, Object... task)
			throws Exception;

	@Override
	public Object[] getTask() {

		synchronized (taskList) {
			if (taskList.size() > 0) {
				Object[] result = taskList.get(0);
				taskList.remove(0);
				return result;
			}
		}
		return null;
	}

	public int getTaskCount() {
		return taskList.size();
	}

	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * 重置状态
	 * 
	 * @author 黄林 Re set.
	 */
	public void reSet() {
		succes = 0;
		error = 0;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	@Override
	public void waitAllTaskDone() {
		int count = 0;
		while (threadList.size() != 0) {
			count++;
			if (count >= 5 * 100) {
				count = 0;
				for (Iterator<PoolThread> iterator = threadList.iterator(); iterator
						.hasNext();) {
					Thread thread = iterator.next();
					if (null == thread || !thread.isAlive()) {
						iterator.remove();
					}
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
	}

	@Override
	public int waitAllTaskDoneAndReSet() {
		waitAllTaskDone();
		int error = this.error;
		reSet();
		return error;
	}

}
