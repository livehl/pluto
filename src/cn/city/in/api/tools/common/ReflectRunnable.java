package cn.city.in.api.tools.common;

import java.lang.reflect.Method;

/**
 * 基于反射的可执行类,适用于无返回的执行
 * 
 * @author 黄林 The Class ReflectRunnable.
 */
public class ReflectRunnable implements Runnable {

	/** The method. @author 黄林 The method. */
	private Method method;

	/** The bean. @author 黄林 The bean. */
	private Object bean;

	/** The params. @author 黄林 The params. */
	private Object[] params;

	/**
	 * Instantiates a new reflect runnable.
	 * 
	 * @param method
	 *            the method
	 * @param bean
	 *            the bean
	 * @param params
	 *            the params
	 */
	public ReflectRunnable(Method method, Object bean, Object[] params) {
		super();
		this.method = method;
		this.bean = bean;
		this.params = params;
	}

	public Object getBean() {
		return bean;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getParams() {
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			method.invoke(bean, params);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

}
