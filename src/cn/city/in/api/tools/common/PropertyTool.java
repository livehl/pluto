package cn.city.in.api.tools.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 配置工具类.
 * 
 * @author 黄林 2011-7-4
 * @version
 */
public class PropertyTool {
	protected static Log log = LogFactory.getLog(PropertyTool.class);
	/** The properties uri. */
	protected static String propertiesFileName;
	/** 重载时间 */
	protected static long reloadTime = -1;
	/** 上一次重载时间 */
	protected static long lastLoadTime = 0;
	/** 配置信息. */
	protected static Map<String, String> properties;

	/** 子类实现需要替换这货 @author 黄林 The singleton. */
	protected static PropertyTool singleton = new PropertyTool();

	/**
	 * 从配置文件中的文件路径获取文件.
	 * 
	 * @param key
	 *            the key
	 * @return the file in property
	 * @author 黄林
	 */
	public static File getFileInProperty(String key) {
		String filePath = getProperties(key);
		if (null == filePath) {
			log.warn("fail to find properties value!:" + key);
			return null;
		}
		return FileTool.getClassPathFile(filePath);
	}

	/**
	 * 从配置文件中的文件路径获取文件列表.
	 * 
	 * @param key
	 *            the key
	 * @return the file in property
	 */
	public static List<File> getFilesInProperty(String key) {
		String filePath = getProperties(key);
		if (null == filePath) {
			log.warn("fail to find properties value!:" + key);
			return null;
		}
		return FileTool.getClassPathFiles(filePath);
	}

	/**
	 * 获取数字类型配置文件内容.
	 * 
	 * @param key
	 *            the key
	 * @return the num properties
	 */
	public static Number getNumProperties(String key) {
		String str = getProperties(key);
		return NumberTool.valueOf(str);
	}

	
	/**
	 * 获取配置文件的整数值.
	 *
	 * @param key the key
	 * @return the int
	 * @author 黄林
	 */
	public static int getIntConf(String key,int defaultValue)
	{
		if (Tool.isNotNull(getProperties(key)))
		{
			return PropertyTool.getNumProperties(key).intValue();
		}
		return defaultValue;
	}
	
	/**
	 * 获取配置文件值
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the conf
	 * @author 黄林
	 */
	public static String getConf(String key,String defaultValue)
	{
		if(null!=getProperties(key))
			return getProperties(key);
		else
			return defaultValue;
	}
	/**
	 * 获取整数配置
	 *
	 * @param key the key
	 * @return the int array
	 * @author 黄林
	 */
	public static Integer[] getIntArray(String key)
	{
		String value=getProperties(key);
		if (Tool.isNotNull(value)) {
			return Tool.arrayValueOfString(value);
		}
		return new Integer[0];
	}

	/**
	 * 获取配置文件内容. 注意:当配置文件加载错误后只有日志输出，返回空，不会抛出异常!
	 * 
	 * @param key
	 *            the key
	 * @return the properties
	 */
	public static String getProperties(String key) {
		if (reloadTime > 0
				&& System.currentTimeMillis() >= (lastLoadTime + reloadTime)) {
			// 重载配置
			try {
				properties = new ConcurrentHashMap<String, String>(
						readFilesAsMap(FileTool
								.getClassPathFiles(propertiesFileName)));
				properties = properties != null ? properties
						: new ConcurrentHashMap<String, String>();
				loadIncludePorperties();
				lastLoadTime = System.currentTimeMillis();
			} catch (Exception e) {
				log.error("loading properties file fail", e);
				properties = properties != null ? properties
						: new ConcurrentHashMap<String, String>();
			}
		}
		if (properties.containsKey(key)) {
			return properties.get(key);
		} else {
			return null;
		}
	}

	/**
	 * 功能:设置配置文件,并初始化其他相关设置 创建者： 黄林 2011-7-5.
	 * 
	 * @param properties
	 *            the properties
	 * @return true,
	 */
	public static boolean init(String propertiesName) {
		PropertyTool.propertiesFileName = propertiesName;
		// 初始化
		try {
			properties = new ConcurrentHashMap<String, String>(
					readFilesAsMap(FileTool
							.getClassPathFiles(propertiesFileName)));
			properties = properties != null ? properties
					: new ConcurrentHashMap<String, String>();
			loadIncludePorperties();
			lastLoadTime = System.currentTimeMillis();
		} catch (Exception e) {
			log.error("loading properties file fail", e);
		}
		ArrayList<File> fileList = FileTool
				.getClassPathFiles(propertiesFileName);
		for (File file : fileList) {
			log.info("properties file :" + file.getAbsoluteFile());
		}
		// 创建配置文件监听器
		try {
			FileMonitorTool.addFileWatch(propertiesFileName,
					ReflectTool.getRunnable("reload", singleton));
		} catch (Exception e) {
			log.error("fail add file watch ", e);
		}
		return true;
	}

	/**
	 * 功能:设置配置文件并指定刷新间隔 创建者： 黄林 2012-2-21. 刷新时间已经被废弃，改用内置文件监视器重载文件
	 * 
	 * @param properties
	 *            the properties
	 * @param time
	 *            the time
	 * @return true,
	 */
	@Deprecated()
	public static boolean init(String properties, Integer time) {
		reloadTime = time;
		return init(properties);
	}

	/**
	 * 功能:载入内置配置文件 创建者： 黄林 2012-2-15.
	 */
	private static void loadIncludePorperties() {
		if (PropertyTool.properties.containsKey("includeProperties")) {
			String include = PropertyTool.properties
					.get("includeProperties");
			PropertyTool.properties.remove("includeProperties");
			try {
				if (StringTool.isNotNull(include)) {
					Map<String, String> map = readFilesAsMap(FileTool
							.getClassPathFiles(include));
					log.debug("load include properties :" + include + ",size:"
							+ (map == null ? 0 : map.size()));
					PropertyTool.properties.putAll(map);
					// 递归载入
					loadIncludePorperties();
				}
			} catch (Exception e) {
				log.warn("fail load include properties :" + include);
			}
		}
	}

	/**
	 * 读取文件为byte数组
	 * 
	 * @param file
	 *            the file
	 * @return the byte[]
	 * @author 黄林
	 */
	public static byte[] readFileAsByte(File file) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			byte[] bs = new byte[(int) file.length()];
			fis.read(bs);
			fis.close();
			return bs;
		} catch (FileNotFoundException e) {
			log.warn("fail to find File!" + file.getAbsoluteFile(), e);
			return null;
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}

	/**
	 * 功能:读取文件，按行拆分 创建者： 黄林 2012-2-23.
	 * 
	 * @param file
	 *            the file
	 * @return list
	 */
	public static List<String> readFileAsList(File file) {
		ArrayList<String> contextList = new ArrayList<String>();
		String[] lines = readFileAsString(file).split("[\r\n|\n]");
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			if (lines[i].length() > 2
					&& lines[i].substring(0, 1).getBytes()[0] == (byte) 63) {
				lines[i] = lines[i].substring(1);
			}
			if (StringTool.isNull(lines[i]) || "#".equals(lines[i].substring(0, 1))
					|| lines[i].equals("")) {
				continue;
			}
			contextList.add(lines[i]);
		}
		return contextList;
	}

	/**
	 * 功能:将键值对文件读取为hashmap 创建者： 黄林 2011-10-14.
	 * 
	 * @param file
	 *            键值对文件
	 * @return hash map
	 */
	public static HashMap<String, String> readFileAsMap(File file) {
		HashMap<String, String> map = new HashMap<String, String>();
		List<String> contextList = readFileAsList(file);
		for (int i = 0; i < contextList.size(); i++) {
			String context = contextList.get(i);
			if (context.indexOf("=") == -1) {
				continue;
			}
			int index = context.indexOf("=");
			String key = context.substring(0, index);
			String value = context.substring(index + 1);
			map.put(key.trim(), value.trim());
		}
		return map;
	}

	/**
	 * 以utf-8编码读取文本文件.
	 * 
	 * @param file
	 *            the file
	 * @return string
	 */
	public static String readFileAsString(File file) {
		return readFileAsString(file, "utf-8");
	}

	/**
	 * 以指定编码读取文本文件.
	 * 
	 * @param file
	 *            the file
	 * @param encoder
	 *            the encoder
	 * @return string
	 */
	public static String readFileAsString(File file, String encoder) {
		try {
			return new String(readFileAsByte(file), encoder);
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}

	/**
	 * 功能:读取文件列表，按行拆分 创建者： 黄林 2012-2-23.
	 * 
	 * @param files
	 *            the files
	 * @return list
	 */
	public static List<String> readFilesAsList(List<File> files) {
		ArrayList<String> contextList = new ArrayList<String>();
		for (File file : files) {
			contextList.addAll(readFileAsList(file));
		}
		return contextList;
	}

	/**
	 * 功能:将键值对文件列表读取为hashmap 创建者： 黄林 2011-12-21.
	 * 
	 * @param files
	 *            the files
	 * @return hash map
	 */
	public static HashMap<String, String> readFilesAsMap(List<File> files) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (File file : files) {
			map.putAll(readFileAsMap(file));
		}
		return map;
	}

	/**
	 * 功能:utf-8编码读取文本文件列表. 创建者： 黄林 2011-12-21.
	 * 
	 * @param files
	 *            the files
	 * @return string
	 */
	public static String readFilesAsString(List<File> files) {
		StringBuffer str = new StringBuffer();
		for (File file : files) {
			str.append(readFileAsString(file, "utf-8"));
		}
		return str.toString();
	}

	/**
	 * 功能:从配置文件中的文件路径获取文件并读取数据为Map 创建者： 黄林 2011-8-23.
	 * 
	 * @param key
	 *            the key
	 * @return map
	 */
	public static List<String> readPropertyFileAsList(String key) {
		return readFilesAsList(getFilesInProperty(key));
	}

	/**
	 * 功能:从配置文件中的文件路径获取文件并读取数据为Map 创建者： 黄林 2011-8-23.
	 * 
	 * @param key
	 *            the key
	 * @return map
	 */
	public static HashMap<String, String> readPropertyFileAsMap(String key) {
		return readFilesAsMap(getFilesInProperty(key));
	}

	/**
	 * 功能:刷新配置文件 创建者： 黄林 2011-8-23.
	 */
	public synchronized static void reload() {
		// 重载主配置文件
		try {
			properties = new ConcurrentHashMap<String, String>(
					readFilesAsMap(FileTool
							.getClassPathFiles(propertiesFileName)));
			properties = properties != null ? properties
					: new ConcurrentHashMap<String, String>();
			loadIncludePorperties();
		} catch (Exception e) {
			log.warn("reload properties file fail", e);
		}
		log.debug("property reload ok");
	}

	/**
	 * 功能:将配置文件中的数字数组转为数组 创建者： 黄林 2011-7-26.
	 * 
	 * @param str
	 *            the str
	 * @return integer[]
	 */
	public static Integer[] stringToArrays(String str) {
		String[] strs = str.split(",");
		Integer[] ints = new Integer[strs.length];
		for (int i = 0; i < strs.length; i++) {
			ints[i] = Integer.valueOf(strs[i]);
		}
		return ints;
	}

	/**
	 * 获取配置文件真假值
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the boolean conf
	 * @author 黄林
	 */
	public static boolean getBooleanConf(String key,boolean defaultValue) {
		if (null!=getProperties(key))
		{
			return "true".equalsIgnoreCase(PropertyTool.getProperties(key))||"1".equalsIgnoreCase(PropertyTool.getProperties(key));
		}
		return defaultValue;
	}
}
