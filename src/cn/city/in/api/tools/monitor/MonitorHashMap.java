package cn.city.in.api.tools.monitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能:性能记录表,本身线程安全
 * 
 * @author 黄林 2011-11-2
 * @version
 */
public class MonitorHashMap extends ConcurrentHashMap<String, Long> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 410417253949676735L;

	public MonitorHashMap() {
		super();
	}

	public MonitorHashMap(int initialCapacity) {
		super(initialCapacity);
	}

	public MonitorHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public MonitorHashMap(int initialCapacity, float loadFactor,
			int concurrencyLevel) {
		super(initialCapacity, loadFactor, concurrencyLevel);
	}

	public MonitorHashMap(Map<? extends String, ? extends Long> m) {
		super(m);
	}

	/**
	 * 功能:值加1,返回结果 创建者： 黄林 2011-11-2.
	 * 
	 * @param key
	 *            the key
	 * @return long
	 */
	public Long addKeyCount(String key) {
		return addKeyCount(key, 1L);
	}

	/**
	 * 功能:值加,返回结果 创建者： 黄林 2011-11-2.
	 * 
	 * @param key
	 *            the key
	 * @return long
	 */
	public Long addKeyCount(String key, Long v) {
		v = v == null ? 1L : v;
		if (super.get(key) != null) {
			v = super.get(key) + v;
		}
		super.put(key, v);
		return v;
	}

	/**
	 * 功能:值减1 创建者： 黄林 2011-11-2.
	 * 
	 * @param key
	 *            the key
	 * @return long
	 */
	public Long decKeyCount(String key) {
		return decKeyCount(key, 1L);
	}

	/**
	 * 功能:值减 创建者： 黄林 2011-11-2.
	 * 
	 * @param key
	 *            the key
	 * @return long
	 */
	public Long decKeyCount(String key, Long v) {
		v = v == null ? 1L : v;
		if (super.get(key) != null) {
			v = super.get(key) - v;
		}
		super.put(key, v);
		return v;
	}

	/**
	 * 返回结果同时设置改值.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the sets the
	 */
	public Long getSet(String key, Long value) {
		Long v = super.get(key);
		if (value == null) {
			super.put(key, 1L);
		} else {
			super.put(key, value);
		}
		return v;
	}

	/**
	 * 返回原值并如果新值比原值大，则覆盖。.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the sets the max
	 */
	public Long getSetMax(String key, Long value) {
		Long v = super.get(key);
		if (v != null && value != null && value > v) {
			super.put(key, value);
		} else if (v == null && value != null) {
			super.put(key, value);
		}
		return v;
	}

	/**
	 * 返回原值并如果新值比原值大，则覆盖。.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the sets the max
	 */
	public Long getSetMin(String key, Long value) {
		Long v = super.get(key);
		if (v != null && value != null && value < v) {
			super.put(key, value);
		} else if (v == null && value != null) {
			super.put(key, value);
		}
		return v;
	}

	/**
	 * 设置平均值
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the long
	 * @author 黄林
	 */
	public Long setAvg(String key, Long value) {
		Long v = super.get(key);
		if (v != null && value != null) {
			v = (v + value) / 2;
		} else if (value != null) {
			v = value;
		} else {
			v = 0L;
		}
		super.put(key, v);
		return v;
	}
}
