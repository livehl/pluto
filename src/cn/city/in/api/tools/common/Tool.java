package cn.city.in.api.tools.common;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;

/**
 * 工具类.
 * 
 * @author 黄林 2011-7-4
 * @version
 */
public class Tool extends StringTool {

	protected static class CacheData implements Serializable {
		private static final long serialVersionUID = 2465064368591106973L;
		private Object value;
		private Date exp;

		public CacheData(Object value, Date exp) {
			this.value = value;
			this.exp = exp;
		}

		public Object get() {
			return value;
		}

		public Date getTime() {
			return exp;
		}
	}
	protected static Log log = LogFactory.getLog(Tool.class);

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 611511549229112675L;

	/** 运行模式.(Debug=1,Development=0,Release=2) */
	protected static Integer runMode = null;
	/** The Constant PI. */
	public static final double PI = 0.00872664626;

	/** The Constant EARTH_RADIUS. */
	public static final double EARTH_RADIUS = 6378.137;

	/** The Constant REGA. */
	public static final double REGA = 0.0174532925;

	/** 本地缓存. */
	protected static ConcurrentHashMap cache = null;

	protected static Random ran = new Random();

	/**
	 * 功能:添加本地缓存,超时时间由配置文件决定 创建者： 黄林 2011-8-3.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true,
	 */
	public static boolean addLocCache(Object key, Object value) {
		return addLocCache(key, value, TimeTool.getAfterDate(PropertyTool
				.getProperties("localCacheTime")));
	}

	/**
	 * 功能:添加本地缓存 创建者： 黄林 2011-8-3.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param exp
	 *            the exp
	 * @return true,
	 */
	@SuppressWarnings("unchecked")
	public static boolean addLocCache(Object key, Object value, Date exp) {
		if (exp.before(new Date())) {
			return false;
		} else {
			CacheData data = new CacheData(value, exp);
			Map map = getLocCache();
			map.put(key, data);
			return true;
		}
	}

	/**
	 * 功能:添加本地缓存,根据时间表达式 创建者： 黄林 2011-8-3. 注意:写入过多会导致堵塞
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param exp
	 *            the exp y-M-d-H-m-s
	 * @return true,
	 */
	public static boolean addLocCache(Object key, Object value, String exp) {
		return addLocCache(key, value, TimeTool.getAfterDate(exp));
	}

	/**
	 * 每公里的经纬度
	 * 
	 * @param kilometer
	 *            the kilometer
	 * @return the kilometer transit
	 */
	// public static double getKilometerTransit(double kilometer)
	// {
	// return 306/EARTH_RADIUS*kilometer;
	// }

	/**
	 * 功能:远程添加大数据 创建者： 黄林 2012-2-1.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param expiry
	 *            the expiry
	 * @return true,
	 */
	public static boolean addRemoteBigCache(String key, List list) {

		return addRemoteBigCache(key, list,
				getAfterDate(PropertyTool.getProperties("localCacheTime")));
	}

	/**
	 * 功能:远程添加大数据 创建者： 黄林 2012-2-1.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param expiry
	 *            the expiry
	 * @return true,
	 */
	public static boolean addRemoteBigCache(String key, List list, Date expiry) {
		int length = list.size() / 10000;
		for (int i = 0; i <= length; i++) {
			if (i == length) {
				addRemoteCache(
						key + i,
						new ArrayList<String>(list.subList(i * 10000,
								list.size())), expiry);
			} else {
				addRemoteCache(
						key + i,
						new ArrayList<String>(list.subList(i * 10000,
								(i + 1) * 10000)), expiry);
			}
		}
		addRemoteCache(key, length, expiry);
		return true;
	}

	/**
	 * 功能:添加或替换远程缓存对象 创建者： 黄林 2011-7-26.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return true,
	 */
	public static boolean addRemoteCache(String key, Object value) {
		return NetTool.getMcc().set(key, value,
				getAfterDate(PropertyTool.getProperties("localCacheTime")));
	}

	/**
	 * 功能:添加或替换远程缓存对象 创建者： 黄林 2011-7-26.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param expiry
	 *            the expiry
	 * @return true,
	 */
	public static boolean addRemoteCache(String key, Object value, Date expiry) {
		return NetTool.getMcc().add(key, value, expiry);
	}

	/**
	 * 功能:添加或替换远程缓存对象 创建者： 黄林 2011-7-26.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param exp
	 *            the exp
	 * @return true, if successful
	 * @author 黄林
	 */
	public static boolean addRemoteCache(String key, Object value, String exp) {
		return addRemoteCache(key, value, TimeTool.getAfterDate(exp));
	}

	/**
	 * 功能:检查属性，如果为空，则填入默认值 创建者： 黄林 2011-9-27.
	 * 
	 * @param bean
	 *            the bean
	 * @param filed
	 *            the filed
	 * @param defaultValue
	 *            the default value
	 * @throws Exception
	 *             the exception
	 */
	public static void checkProperty(Object bean, String filed,
			Object defaultValue) throws Exception {
		Object result = PropertyUtils.getProperty(bean, filed);
		if (null == result || "".equals(result)) {
			PropertyUtils.setProperty(bean, filed, defaultValue);
		}
	}

	/**
	 * 清除所有本地缓存
	 * 
	 * @author 黄林 Clan all cache.
	 */
	public static int cleanAllCache() {
		Map cache = getLocCache();
		int cleanCount = cache.size();
		cache.clear();
		return cleanCount;
	}

	/**
	 * 功能:清理缓存中超时的数据,返回下一个需要清理的对象剩余生存时间 创建者： 黄林 2012-1-3.
	 */
	public static Integer cleanCache() {
		Long minTime = Long.MAX_VALUE;
		Map<?, CacheData> map = new HashMap<Object, CacheData>(getLocCache());
		for (Iterator<?> iterator = map.entrySet().iterator(); iterator
				.hasNext();) {
			Entry<?, CacheData> entry = (Entry<?, CacheData>) iterator.next();
			CacheData data = entry.getValue();
			if (null == data) {
				iterator.remove();
			} else {
				if (new Date().after(data.getTime())) {
					getLocCache().remove(entry.getKey());
					iterator.remove();
				} else if (minTime > data.getTime().getTime()) {
					minTime = data.getTime().getTime();
				}
			}
		}
		if (minTime < Long.MAX_VALUE) {
			return minTime.intValue();
		} else {
			return null;
		}
	}

	/**
	 * 功能:复制类，外部工具类 创建者： 黄林 2011-12-21.
	 * 
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @throws Exception
	 *             the exception
	 */
	public static void copyBean(Object source, Object target) throws Exception {
		BeanUtils.copyProperties(target, source);
	}

	/**
	 * 功能:复制类,自写 创建者： 黄林 2011-12-21.
	 * 
	 * @param source
	 *            the source
	 * @param target
	 *            the target
	 * @param fileds
	 *            the fileds
	 * @throws Exception
	 *             the exception
	 */
	public static void copyBean(Object source, Object target, String... fileds)
			throws Exception {
		if (null == source) {
			target = null;
			throw new Exception("source can't be null");
		} else if (target == null) {
			throw new Exception("target can't be null");
		}
		if (null != fileds && fileds.length > 1 && fileds[0].equals(":full")) {// 检查所有字段
			fileds = getFileds(source, fileds);
		}
		if (null == fileds) {
			fileds = new String[0];
		}
		for (String filed : fileds) {
			try {
				Method methodGet = source.getClass().getMethod(
						"get" + upFrist(filed));
				Object data = methodGet.invoke(source);
				Method methodSet = target.getClass().getMethod(
						"set" + upFrist(filed), data.getClass());
				methodSet.invoke(target, data);
			} catch (SecurityException e) {
				log.warn(e);
				break;
			} catch (NoSuchMethodException e) {
				log.debug("object " + source.getClass() + " no have method:get"
						+ upFrist(filed));
				log.debug("object " + target.getClass() + " no have method:set"
						+ upFrist(filed));
			} catch (Exception e) {
				log.warn("object source " + source.getClass() + " method get"
						+ upFrist(filed) + " result can set to "
						+ target.getClass() + " " + filed + " detail:"
						+ e.getMessage());
			}
		}
	}

	/**
	 * 获取两点之间的距离. 单位(公里)
	 * 
	 * @param lat1
	 *            the lat1
	 * @param lng1
	 *            the lng1
	 * @param lat2
	 *            the lat2
	 * @param lng2
	 *            the lng2
	 * @return the distance
	 */
	public static double getDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double dis = EARTH_RADIUS
				* 2
				* Math.asin(Math.sqrt(Math.pow(Math.sin(lat1 - lat2) * PI, 2)
						+ Math.cos(lat1 * REGA) * Math.cos(lat2 * REGA)
						* Math.pow(Math.sin((lng1 - lng2) * PI), 2)));
		return dis;
	}

	/**
	 * 获取两点之间的距离. 单位(米)
	 * 
	 * @param lat1
	 *            the lat1
	 * @param lng1
	 *            the lng1
	 * @param lat2
	 *            the lat2
	 * @param lng2
	 *            the lng2
	 * @return the distance
	 */
	public static int getDistanceForMeter(double lat1, double lng1,
			double lat2, double lng2) {
		return (int) (getDistance(lat1, lng1, lat2, lng2) * 1000);
	}

	/**
	 * 获取实体类以及其父类的字段
	 * 
	 * @param className
	 *            the class name
	 * @return the fileds
	 */
	@SuppressWarnings("unchecked")
	public static String[] getFileds(Class className) {
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
			String[] fileds = getFileds(className.getSuperclass());
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
	 * 获取实体类的所有字段(包含父类)
	 * 
	 * @param bean
	 *            the bean
	 * @param fileds
	 *            the fileds
	 * @return the fileds
	 */
	public static String[] getFileds(Object bean, String... fileds) {
		if (null == fileds) {
			return getFileds(bean.getClass());
		} else {
			String[] oldFileds = fileds.clone();
			String[] beanField = getFileds(bean.getClass()); // 获取所有字段
			int newFiledSize = fileds.length + beanField.length - 1;
			fileds = new String[newFiledSize];
			for (int i = 0; i < beanField.length; i++) {// 加入所有字段
				fileds[i] = beanField[i];
			}
			for (int i = 0; i < oldFileds.length; i++) {// 加入其它字段
				String string = oldFileds[i];
				fileds[beanField.length - 1 + i] = string;
			}
			return fileds;
		}
	}

	/**
	 * 每公里纬度
	 * 
	 * @return the kilometer latitude
	 * @author 黄林
	 */
	public static double getKilometerLatitude() {
		return 1d / 111d;
	}

	/**
	 * 每公里经度
	 * 
	 * @param lat
	 *            the lat
	 * @return the kilometer longitude
	 * @author 黄林
	 */
	public static double getKilometerLongitude(double lat) {
		return getKilometerLatitude() * Math.cos(lat);
	}

	/**
	 * 缓存.
	 * 
	 * @return the 缓存
	 */
	@SuppressWarnings("unchecked")
	private static Map getLocCache() {
		if (null == cache) {
			synchronized (Tool.class) {
				try {
					File file = FileTool.getClassPathFile("classpath:cache/"
							+ PropertyTool.getProperties("appname")
							+ ".cache.data");
					cache = (ConcurrentHashMap) FileTool.loadObject(file);
					log.debug("cache File is " + file.getAbsolutePath());
					log.debug("load cache success ,size:" + cache.size());
				} catch (Exception e) {
					log.warn("load cache fail:" + e.getMessage());
				} finally {
					if (null == cache) {
						cache = new ConcurrentHashMap(1000 * 1000, 0.75f, 50);
					}
				}
			}
		}
		return cache;
	}

	/**
	 * 获取本地缓存
	 * 
	 * @param key
	 *            the key
	 * @return the loc cache
	 */
	public static Object getLocCache(Object key) {
		CacheData data = (CacheData) getLocCache().get(key);
		if (null == data) {
			return null;
		} else {
			if (new Date().after(data.getTime())) {
				Map map = getLocCache();
				map.remove(key);
				return null;
			} else {
				return data.get();
			}
		}
	}

	/**
	 * 获取本地缓存,并从队列中移除
	 * 
	 * @param key
	 *            the key
	 * @return the loc cache
	 */
	public static Object getLocCacheAndRemove(Object key) {
		Object result = getLocCache(key);
		removeLocCache(key);
		return result;

	}

	public static Object getLocCacheAndSet(Object key, Object value) {
		Object result = getLocCache(key);
		addLocCache(key, value);
		return result;
	}

	/**
	 * 随即生成一个不重复的数组 size = 数组长度 limit = 取值范围.
	 * 
	 * @param size
	 *            the size
	 * @param limit
	 *            the limit
	 * @return the random array
	 */
	public static int[] getRandomArray(int size, int limit) {
		int[] arr = new int[size];
		for (int i = 0; i < size; i++) {
			arr[i] = (int) Math.round(Math.random() * limit);
			for (int j = 0; j < i; j++) {
				if (arr[j] == arr[i] || arr[i] == 0) {
					i--;
					break;
				}
			}
		}
		return arr;
	}

	/**
	 * 远程获取大数据
	 * 
	 * @param key
	 *            the key
	 * @return the remote big cache
	 */
	public static ArrayList getRemoteBigCache(String key) {
		ArrayList keys = new ArrayList<Object>();
		Integer keyLength = (Integer) getRemoteCache(key);
		if (keyLength == null) {
			return keys;
		}
		for (int i = 0; i <= keyLength; i++) {
			keys.addAll((ArrayList) getRemoteCache(key + i));
		}
		return keys;
	}

	/**
	 * 根据指定的关键字获取远程缓存对象.
	 * 
	 * @param key
	 *            the key
	 * @return the cache
	 */
	public static Object getRemoteCache(String key) {
		return NetTool.getMcc().get(key);
	}

	/**
	 * 批量返回远程缓存
	 * 
	 * @param keys
	 *            the keys
	 * @return the remote caches
	 */
	public static Map<String, Object> getRemoteCaches(String... keys) {
		if (null == keys || keys.length < 1) {
			return new HashMap<String, Object>();
		}
		return NetTool.getMcc().getMulti(keys);
	}

	/**
	 * 功能:检查本地缓存是否存在该键值对 创建者： 黄林 2011-8-3.
	 * 
	 * @param key
	 *            the key
	 * @return true,
	 */
	public static boolean locContainsKey(Object key) {
		return null != getLocCache(key);
	}

	/**
	 * 功能:将字符串数组转换为int数组 创建者： 黄林 2011-7-4.
	 * 
	 * @param input
	 *            the input
	 * @return int[]
	 */
	public static int[] parseIntArray(String[] input) {
		int[] ia = new int[input.length];
		for (int i = 0; i < input.length; i++) {
			ia[i] = Integer.parseInt(input[i]);
		}
		return ia;
	}

	/**
	 * 随机数字0-int Max.
	 * 
	 * @return int
	 */
	public static int random() {
		return ran.nextInt();
	}

	/**
	 * 随机数字0-r.注意:不包含R
	 * 
	 * @param r
	 *            the r
	 * @return int
	 */
	public static int random(int r) {
		return ran.nextInt(r);
	}

	/**
	 * 随即数字s-r 不包含s和r
	 * 
	 * @param s
	 * @param r
	 * @return
	 */
	public static int random(int s, int r) {
		int i = random(r);
		while (i <= s) {
			i = random(r);
		}
		return i;
	}

	/**
	 * 功能:检查远程缓存对象是否存在 创建者： 黄林 2011-8-2.
	 * 
	 * @param key
	 *            the key
	 * @return true,
	 */
	public static boolean remoteContainsKey(String key) {
		return NetTool.getMcc().keyExists(key);
	}

	/**
	 * 功能:移除本地缓存 创建者： 黄林 2011-12-27.
	 * 
	 * @param key
	 *            the key
	 */
	public static boolean removeLocCache(Object key) {
		Map map = getLocCache();
		return map.remove(key) == null ? false : true;
	}

	/**
	 * 功能:移除远程缓存 创建者： 黄林 2011-12-27.
	 * 
	 * @param key
	 *            the key
	 */
	public static boolean removeRemoteCache(String key) {
		return NetTool.getMcc().delete(key);
	}

	// 注释解说:优势，可以在运行期切换工作状态
	/**
	 * 运行模式.(Debug=1,Development=0,Release=2)
	 * 
	 * @return integer
	 */
	public static Integer runMode() {
		try {
			runMode = PropertyTool.getNumProperties("runMode").intValue();
		} catch (Exception e) {
			runMode = 2;
		}
		runMode = runMode == null ? 2 : runMode;
		return runMode;
	}

	/**
	 * 功能:保存缓存 创建者： 黄林 2011-12-21.
	 */
	public synchronized static void saveCache() {
		if (cache == null) {
			return;
		}
		File file = FileTool.createFile("classpath:cache/"
				+ PropertyTool.getProperties("appname") + ".cache.data");
		try {
			FileTool.saveObject(file, cache);
			log.debug("cache save file " + file.getAbsolutePath());
			log.debug("save cache success ,size:" + cache.size());
		} catch (Exception e) {
			log.warn("save cache fail:" + e.getMessage());
		}
	}

	/**
	 * 功能:字符串转时间 创建者： 黄林 2011-12-21.
	 * 
	 * @param str
	 *            the str
	 * @return date
	 */
	public static Date stringToDate(String str) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date d = null;
		try {
			d = format.parse(str);
		} catch (ParseException e) {
			log.debug(str, e);
		}
		return d;
	}

	/**
	 * 验证密码是否为有效字符
	 * 
	 * @param password
	 *            the password
	 * @return true, if successful
	 * @author 黄林
	 */
	public static boolean verifyPassword(String password) {
		for (int i = 0; i < password.length(); i++) {
			byte[] array = password.substring(i, i + 1).getBytes();
			if (array.length > 1) {
				return false;
			}
			if (array[0] < 32 || array[0] > 126) {
				return false;
			}
		}
		return true;
	}

}
