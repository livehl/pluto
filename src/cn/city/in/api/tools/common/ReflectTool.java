package cn.city.in.api.tools.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 功能:反射工具类
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class ReflectTool {
	protected static Log log = LogFactory.getLog(ReflectTool.class);

	/**
	 * 功能:执行指定的方法 创建者： 黄林 2011-10-27.
	 * 
	 * @param methodName
	 *            方法名
	 * @param bean
	 *            执行对象
	 * @param params
	 *            执行参数
	 * @return object 执行结果
	 * @throws Exception
	 *             异常
	 */
	public static Object execMethodScript(String methodName, Object bean,
			String... params) throws Exception {
		Method method;
		if (bean instanceof Class) {
			method = ((Class) bean)
					.getMethod(methodName, getParamTypes(params));
			bean = ((Class) bean).newInstance();
		} else {
			method = bean.getClass().getMethod(methodName,
					getParamTypes(params));
		}
		return method.invoke(bean, getObjectParams(params));
	}

	/**
	 * 功能:执行指定的静态方法脚本 创建者： 黄林 2011-10-27.
	 * 
	 * @param className
	 *            类名
	 * @param methodName
	 *            方法名
	 * @param params
	 *            参数列表
	 * @return object 执行结果
	 * @throws Exception
	 *             异常
	 */
	public static Object execMethodScript(String className, String methodName,
			String... params) throws Exception {
		return execMethodScript(methodName, Class.forName(className)
				.newInstance(), params);
	}

	/**
	 * 获取一个类的所有父类的简短名称，包含本身
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the all class name with super
	 */
	public static String[] getAllClassNameWithSuper(Class<?> clazz) {
		Class<?>[] clazzs = getAllClassWithSuper(clazz);
		String[] classNames = new String[clazzs.length];
		for (int i = 0; i < clazzs.length; i++) {
			classNames[i] = clazzs[i].getSimpleName();
		}
		return classNames;
	}

	/**
	 * 功能:获取一个类的所有父类，包含本身 创建者： 黄林 2012-2-27.
	 * 
	 * @param clazz
	 *            the clazz
	 * @return class[]
	 */
	public static Class<?>[] getAllClassWithSuper(Class<?> clazz) {
		if (null == clazz) {
			return null;
		}
		ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
		classList.add(clazz);
		Class<?> superClass = clazz.getSuperclass();
		do {
			classList.add(superClass);
		} while (!superClass.equals(Object.class));
		return classList.toArray(new Class[0]);
	}

	/**
	 * Gets the bytes by object.
	 * 
	 * @param obj
	 *            the ser
	 * @return the bytes by object
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static byte[] getBytesByObject(Object obj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		return baos.toByteArray();
	}

	/**
	 * 获取字段
	 * 
	 * @param clazz
	 *            the clazz
	 * @param methodName
	 *            the method name
	 * @return the field
	 * @author 黄林
	 */
	public static Field getField(Class<?> clazz, String methodName) {
		Field field = null;
		HashSet<Field> filedSet = new HashSet<Field>();
		Field[] beanField = clazz.getDeclaredFields();
		for (Field field3 : beanField) {
			if (field3.getName().equals("serialVersionUID")) {
				continue;
			}
			filedSet.add(field3);
		}
		// 获取父类的字段
		while (null != clazz.getSuperclass()
				&& !clazz.getSuperclass().getName().equals("java.lang.Object")) {
			clazz = clazz.getSuperclass();
			beanField = clazz.getDeclaredFields();
			for (Field field3 : beanField) {
				if (field3.getName().equals("serialVersionUID")) {
					continue;
				}
				filedSet.add(field3);
			}
		}
		for (Field field2 : filedSet) {
			if (field2.getName().equals(methodName)) {
				field = field2;
				break;
			}
		}
		return field;
	}

	/**
	 * 获取类及父类字段名，不包含object
	 * 
	 * @param className
	 *            the class name
	 * @return the fileds
	 */
	@SuppressWarnings("unchecked")
	public static String[] getFiledNames(Class className) {
		HashSet<String> filedSet = new HashSet<String>();
		Field[] beanField = className.getDeclaredFields();
		for (Field field : beanField) {
			if (field.getName().equals("serialVersionUID")) {
				continue;
			}
			filedSet.add(field.getName());
		}
		// 获取父类的字段
		if (null != className.getSuperclass()
				&& !className.getSuperclass().getName()
						.equals("java.lang.Object")) {
			String[] fileds = getFiledNames(className.getSuperclass());
			for (String string : fileds) {
				if (string.equals("serialVersionUID")) {
					continue;
				}
				filedSet.add(string);
			}
		}
		return filedSet.toArray(new String[1]);
	}

	/**
	 * 获取类及父类字段，不包含object
	 * 
	 * @param className
	 *            the class name
	 * @return the fileds
	 */
	@SuppressWarnings("unchecked")
	public static Field[] getFileds(Class className) {
		HashSet<Field> filedSet = new HashSet<Field>();
		Field[] beanField = className.getDeclaredFields();
		for (Field field : beanField) {
			if (field.getName().equals("serialVersionUID")) {
				continue;
			}
			filedSet.add(field);
		}
		// 获取父类的字段
		if (null != className.getSuperclass()
				&& !className.getSuperclass().getName()
						.equals("java.lang.Object")) {
			Field[] fields = getFileds(className.getSuperclass());
			for (Field field : fields) {
				if (field.getName().equals("serialVersionUID")) {
					continue;
				}
				filedSet.add(field);
			}
		}
		return filedSet.toArray(new Field[1]);
	}

	/**
	 * 获取指定字段的值
	 * 
	 * @param obj
	 *            the obj
	 * @param field
	 *            the field
	 * @return the filed value
	 * @author 黄林
	 */
	public static Object getFiledValue(Object obj, String field) {
		try {
			Method m = obj.getClass().getMethod(
					"get" + StringTool.upFrist(field));
			return m.invoke(obj);
		} catch (Exception e) {
			log.debug(e);
			return null;
		}
	}

	/**
	 * 获取所有的Get方法名,排除Object自带方法
	 * 
	 * @param className
	 *            the class name
	 * @return the gets the method names
	 * @author 黄林
	 */
	public static String[] getGetMethodNames(Class className) {
		HashSet<String> methodSet = new HashSet<String>();
		Method[] beanMethod = className.getMethods();
		for (Method method : beanMethod) {
			if (StringTool.in(method.getName(), "serialVersionUID", "clone",
					"equals", "getClass", "hashCode", "toString")
					|| method.getName().indexOf("set") != -1) {
				continue;
			}
			methodSet.add(method.getName());
		}
		return methodSet.toArray(new String[1]);
	}

	/**
	 * 获取指定返回类型的方法
	 * 
	 * @param clazz
	 *            the clazz
	 * @param getClazz
	 *            返回class
	 * @return the gets the method
	 */
	@SuppressWarnings("unchecked")
	public static Method getMethod(Class clazz, Class getClazz) {
		Method method = null;
		Method[] methods = clazz.getMethods();
		for (Method method2 : methods) {
			if (getClazz.isAssignableFrom((method2.getReturnType()))) {
				method = method2;
				break;
			}
		}
		return method;
	}

	/**
	 * 按方法名获取方法(注意:存在同名方法时返回第一个，如需要指定参数，请使用jdk提供的方法)
	 * 
	 * @param clazz
	 *            the clazz
	 * @param methodName
	 *            the method name
	 * @return the gets the method
	 */
	public static Method getMethod(Class<?> clazz, String methodName) {
		Method method = null;
		Method[] methods = clazz.getMethods();
		for (Method method2 : methods) {
			if (method2.getName().equals(methodName)) {
				method = method2;
				break;
			}
		}
		return method;
	}

	/**
	 * 获取所有的方法名
	 * 
	 * @param className
	 *            the class name
	 * @return the method names
	 * @author 黄林
	 */
	public static String[] getMethodNames(Class className) {
		HashSet<String> methodSet = new HashSet<String>();
		Method[] beanMethod = className.getMethods();
		for (Method method : beanMethod) {
			if (method.getName().equals("serialVersionUID")) {
				continue;
			}
			methodSet.add(method.getName());
		}
		return methodSet.toArray(new String[1]);
	}

	/**
	 * Gets the object by bytes.
	 * 
	 * @param data
	 *            the data
	 * @return the object by bytes
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static Object getObjectByBytes(byte[] data) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}

	/**
	 * Gets the object by zip bytes.
	 * 
	 * @param data
	 *            the data
	 * @return the object by zip bytes
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static Object getObjectByZipBytes(byte[] data) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ZipInputStream zip = new ZipInputStream(bais);
		zip.getNextEntry();
		ObjectInputStream ois = new ObjectInputStream(zip);
		return ois.readObject();
	}

	/**
	 * 解析字符串，返回相应类型的实例,仅限定数字、字符串、时间
	 * 
	 * @param param
	 *            the param
	 * @return the param type
	 */
	public static Object getObjectParam(String param) throws ParseException {
		if (null == param || param.length() < 1) {
			return null;
		}
		String value = param.substring(1);
		String head = param.substring(0, 1);
		if (head.equals("S")) {
			return value;
		} else if (head.toUpperCase().equals("I")) {
			return Integer.valueOf(value);
		} else if (head.toUpperCase().equals("D")) {
			return Double.valueOf(value);
		} else if (head.toUpperCase().equals("F")) {
			return Float.valueOf(value);
		} else if (head.equals("T")) {
			try {
				return DateFormat.getInstance().parse(value);
			} catch (ParseException e) {
				throw e;
			}
		} else {// 直接返回，当字符串处理
			return param;
		}
	}

	/**
	 * 将参数转化为java对象，仅限定数字、字符串、时间
	 * 
	 * @param params
	 *            the params
	 * @return the object params
	 */
	public static Object[] getObjectParams(String[] params) throws Exception {
		if (null == params) {
			return null;
		}
		Object[] objs = new Object[params.length];
		for (int i = 0; i < params.length; i++) {
			objs[i] = getObjectParam(params[i]);
		}
		return objs;
	}

	/**
	 * 解析字符串，获取参数类型，仅限定数字、字符串、真假
	 * 
	 * @param param
	 *            the param
	 * @return the param type
	 */
	@SuppressWarnings("unchecked")
	public static Class getParamType(String param) throws Exception {
		String head = param.substring(0, 1);
		if (head.equals("i")) {
			return int.class;
		} else if (head.equals("d")) {
			return double.class;
		} else if (head.equals("f")) {
			return float.class;
		} else if (head.equals("b")) {
			return boolean.class;
		} else if (head.equals("s")) {
			return short.class;
		}
		Object result = getObjectParam(param);
		if (null != result) {
			return result.getClass();
		}
		return null;
	}

	/**
	 * 获取参数类型
	 * 
	 * @param params
	 *            the params
	 * @return the gets the param types
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static Class[] getParamTypes(Object[] params) throws Exception {
		if (null == params) {
			return null;
		}
		Class[] clazzs = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			clazzs[i] = params[i].getClass();
		}
		return clazzs;
	}

	/**
	 * 获取参数类型
	 * 
	 * @param params
	 *            the params
	 * @return the param types
	 */
	@SuppressWarnings("unchecked")
	public static Class[] getParamTypes(String[] params) throws Exception {
		if (null == params) {
			return null;
		}
		Class[] clazzs = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			clazzs[i] = getParamType(params[i]);
		}
		return clazzs;
	}

	/**
	 * 获取执行器
	 * 
	 * @param method
	 *            the method
	 * @param bean
	 *            the bean
	 * @return the runnable
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static ReflectRunnable getRunnable(String method, Object bean)
			throws Exception {
		return getRunnable(method, bean, new Object[0]);
	}

	/**
	 * 获取执行器
	 * 
	 * @param method
	 *            the method
	 * @param bean
	 *            the bean
	 * @param params
	 *            the params
	 * @return the runnable
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static ReflectRunnable getRunnable(String method, Object bean,
			Object... params) throws Exception {
		if (null == params) {
			params = new Object[0];
		}
		Class<?>[] parameterTypes = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			parameterTypes[i] = params[i].getClass();
		}
		if (bean instanceof String) {
			return new ReflectRunnable(Class.forName(bean.toString())
					.getMethod(method, parameterTypes), Class.forName(
					bean.toString()).newInstance(), params);
		} else {
			return new ReflectRunnable(bean.getClass().getMethod(method,
					parameterTypes), bean, params);
		}
	}

	/**
	 * 获取指定参数类型的方法
	 * 
	 * @param clazz
	 *            the clazz
	 * @param setClazz
	 *            参数class
	 * @return the sets the method
	 */
	@SuppressWarnings("unchecked")
	public static Method getSetMethod(Class clazz, Class setClazz) {
		Method method = null;
		Method[] methods = clazz.getMethods();
		Outer: for (Method method2 : methods) {
			Class[] paramTypes = method2.getParameterTypes();
			for (Class class1 : paramTypes) {
				if (class1.equals(setClazz) && paramTypes.length == 1) {
					method = method2;
					break Outer;
				}
			}
		}
		return method;
	}

	/**
	 * Gets the zip bytes by object.
	 * 
	 * @param obj
	 *            the obj
	 * @return the zip bytes by object
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static byte[] getZipBytesByObject(Object obj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zop = new ZipOutputStream(baos);
		ZipEntry ze = new ZipEntry("data");
		zop.putNextEntry(ze);
		ObjectOutputStream oos = new ObjectOutputStream(zop);
		oos.writeObject(obj);
		zop.finish();
		zop.flush();
		return baos.toByteArray();
	}

	/**
	 * 设置指定字段的值
	 * 
	 * @param obj
	 *            the obj
	 * @param field
	 *            the field
	 * @param value
	 *            the value
	 * @return true, if successful
	 * @author 黄林
	 */
	public static boolean setFiledValue(Object obj, String field, Object value) {
		try {
			Field f=obj.getClass().getDeclaredField(field);
			f.setAccessible(true);
			f.set(obj, value);
			return true;
		} catch (Exception e) {
			log.debug(e);
			return false;
		}
	}
	/**
	 * 
	 * <p>
	 * 获取方法参数名称
	 * </p>
	 * 
	 * @param cm
	 * @return
	 */
	protected static String[] getMethodParamNames(CtMethod cm) throws Exception{
		CtClass cc = cm.getDeclaringClass();
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute
				.getAttribute(LocalVariableAttribute.tag);
		if (attr == null) {
			throw new Exception(cc.getName());
		}
		String[] paramNames = null;
		paramNames = new String[cm.getParameterTypes().length];
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		for (int i = 0; i < paramNames.length; i++) {
			paramNames[i] = attr.variableName(i + pos);
		}
		return paramNames;
	}

	/**
	 * 获取方法参数名称，按给定的参数类型匹配方法
	 * 
	 * @param clazz
	 * @param method
	 * @param paramTypes
	 * @return
	 */
	public static String[] getMethodParamNames(Class<?> clazz, String method,
			Class<?>... paramTypes) throws Exception{
		ClassPool pool = ClassPool.getDefault();
		CtClass cc = null;
		CtMethod cm = null;
		cc = pool.get(clazz.getName());
		String[] paramTypeNames = new String[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++)
			paramTypeNames[i] = paramTypes[i].getName();
		cm = cc.getDeclaredMethod(method, pool.get(paramTypeNames));
		return getMethodParamNames(cm);
	}

	/**
	 * 获取方法参数名称，匹配同名的某一个方法
	 * 
	 * @param clazz
	 * @param method
	 * @return
	 * @throws NotFoundException
	 *             如果类或者方法不存在
	 * @throws MissingLVException
	 *             如果最终编译的class文件不包含局部变量表信息
	 */
	public static String[] getMethodParamNames(Class<?> clazz, String method) throws Exception{
		ClassPool pool = ClassPool.getDefault();
		CtClass cc;
		CtMethod cm = null;
		cc = pool.get(clazz.getName());
		cm = cc.getDeclaredMethod(method);
		return getMethodParamNames(cm);
	}
}
