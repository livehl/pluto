package cn.city.in.api.tools.taskpool;

import java.lang.reflect.Method;

public class MethodPoolTaskImpl extends BasePoolTaskImpl {

	private Method method;
	private Object obj;

	public MethodPoolTaskImpl(Method method, Object obj) {
		super();
		this.method = method;
		this.obj = obj;
	}

	@Override
	public Object doTask(Object before, Object... task) throws Exception {
		return method.invoke(obj, task);
	}
}
