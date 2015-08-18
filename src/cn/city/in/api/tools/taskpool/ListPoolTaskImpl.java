package cn.city.in.api.tools.taskpool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 执行列表结果的多线程任务队列实现类
 * 
 * @author 黄林 The Class ListPoolTaskImpl.
 */
public class ListPoolTaskImpl extends BasePoolTaskImpl {

	/** The do task. @author 黄林 The do task. */
	protected Method doTask;

	/** The bean. @author 黄林 The bean. */
	protected Object bean;

	/** The list. @author 黄林 The list. */
	protected static ArrayList list = new ArrayList();

	/**
	 * 参数为可执行的方法
	 * 
	 * @param bean
	 *            the bean
	 * @param doTask
	 *            the do task
	 */
	public ListPoolTaskImpl(Object bean, Method doTask) {
		super();
		this.bean = bean;
		this.doTask = doTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.city.in.api.tools.taskpool.BasePoolTaskImpl#doTask(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public Object doTask(Object before, Object... task) throws Exception {
		Object result = doTask.invoke(bean, task);
		synchronized (list) {
			if (result instanceof Collection) {
				list.addAll((Collection) result);
			} else {
				list.add(result);
			}
		}
		return result;
	}

	/**
	 * 等待执行结束并返回所有结果.
	 * 
	 * @return the all result
	 * @author 黄林
	 */
	public List getAllResult() {
		waitAllTaskDone();
		return list;
	}

}
