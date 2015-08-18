package cn.city.in.api.tools.task;

import java.lang.reflect.Method;

/**
 * 功能:任务模块
 * 
 * @author 黄林 2011-8-23
 * @version
 */
public class BaseTaskInfo {
	/** The task code. */
	private Integer taskCode;
	/** The method. */
	private Method method;
	/** The bean. */
	private Object bean;

	/** The params. */
	private Object[] params;

	/**
	 * Gets the bean.
	 * 
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * Gets the task code.
	 * 
	 * @return the task code
	 */
	public Integer getTaskCode() {
		return taskCode;
	}

	/**
	 * Sets the bean.
	 * 
	 * @param bean
	 *            the new bean
	 */
	public void setBean(Object bean) {
		this.bean = bean;
	}

	/**
	 * Sets the method.
	 * 
	 * @param method
	 *            the new method
	 */
	public void setMethod(Method method) {
		this.method = method;
	}

	/**
	 * Sets the params.
	 * 
	 * @param params
	 *            the new params
	 */
	public void setParams(Object[] params) {
		this.params = params;
	}

	/**
	 * Sets the task code.
	 * 
	 * @param taskCode
	 *            the new task code
	 */
	public void setTaskCode(Integer taskCode) {
		this.taskCode = taskCode;
	}
}
