package cn.city.in.api.tools.objectpool;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.city.in.api.tools.common.ReflectTool;

/**
 * 对象池
 * 
 * @author 黄林 The Interface ObjectPool.
 */
public class ObjectPool {
	private static Map<Class, List> poolMap = new ConcurrentHashMap<Class, List>();
	private static Map<Class, Integer> poolUserSize = new ConcurrentHashMap<Class, Integer>();
	private static Map<Class, Object[]> poolCreateMap = new HashMap<Class, Object[]>();

	/**
	 * 对象获取
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static Object borrowObject(Class clazz) throws Exception {
		if (poolCreateMap.containsKey(clazz)) {
			List list = poolMap.get(clazz);
			Object obj = null;
			synchronized (list) {
				Integer used = poolUserSize.get(clazz);
				if (list.size() <= 5) {
					int addSize = used / 5;
					addSize = addSize < 5 ? 5 : addSize;
					for (int i = 0; i < addSize; i++) {
						obj = createObject(clazz, poolCreateMap.get(clazz));
						list.add(obj);
					}
				}
				obj = list.get(list.size() - 1);
				list.remove(list.size() - 1);
			}
			poolUserSize.put(clazz, poolUserSize.get(clazz) + 1);
			return obj;
		} else {
			throw new Exception("class " + clazz + " no init");
		}

	}

	/**
	 * 清空指定的类
	 * 
	 * @param clazz
	 *            the clazz
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Clear.
	 */
	public synchronized static void clear(Class clazz) throws Exception {
		poolMap.put(clazz, new ArrayList());
		poolUserSize.put(clazz, 0);
		poolCreateMap.remove(clazz);
	}

	/**
	 * 清空所有数据
	 * 
	 * @throws Exception
	 *             <strong>deprecated</strong>: implementations should silently
	 *             fail if not all resources can be freed.
	 */
	public static void close() throws Exception {
		poolMap.clear();
		poolUserSize.clear();
		poolCreateMap.clear();
	}

	/**
	 * 创建一个对象
	 * 
	 * @param clazz
	 *            the clazz
	 * @param args
	 *            the args
	 * @return the object
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	private static Object createObject(Class clazz, Object... args)
			throws Exception {
		if (null == args || args.length == 0) {
			return clazz.newInstance();
		} else {
			Class[] argsType = ReflectTool.getParamTypes(args);
			Constructor cons = clazz.getConstructor(argsType);
			return cons.newInstance(args);
		}
	}

	/**
	 * 获取激活数
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the num active
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static int getNumActive(Class clazz) throws Exception {
		if (poolCreateMap.containsKey(clazz)) {
			Integer used = poolUserSize.get(clazz);
			return used;
		} else {
			throw new Exception("class " + clazz + " no init");
		}
	}

	/**
	 * 获取空闲数
	 * 
	 * @param clazz
	 *            the clazz
	 * @return the num idle
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static int getNumIdle(Class clazz) throws Exception {
		if (poolCreateMap.containsKey(clazz)) {
			List list = poolMap.get(clazz);
			Integer used = poolUserSize.get(clazz);
			return list.size() - used;
		} else {
			throw new Exception("class " + clazz + " no init");
		}
	}

	/**
	 * 初始化特定对象的对象池
	 * 
	 * @param clazz
	 *            the clazz
	 * @param args
	 *            the args
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Inits the object.
	 */
	public synchronized static void initObject(Class clazz, Object[] args)
			throws Exception {
		poolCreateMap.put(clazz, args);
		poolUserSize.put(clazz, 0);
		List list = list = new ArrayList();
		for (int i = 0; i < 5; i++) {
			list.add(createObject(clazz, poolCreateMap.get(clazz)));
		}
		poolMap.put(clazz, list);
	}

	/**
	 * 对象归还
	 * 
	 * @param obj
	 *            the obj
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Return object.
	 */
	public static void returnObject(Object obj) throws Exception {
		Class clazz = obj.getClass();
		if (poolCreateMap.containsKey(clazz)) {
			List list = poolMap.get(clazz);
			synchronized (list) {
				list.add(obj);
			}
			poolUserSize.put(clazz, poolUserSize.get(clazz) - 1);
		} else {
			throw new Exception("class " + clazz + " no init");
		}
	}
}
