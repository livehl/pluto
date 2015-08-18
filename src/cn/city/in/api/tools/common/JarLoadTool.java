package cn.city.in.api.tools.common;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class JarLoadTool {

	/** URLClassLoader的addURL方法 */
	private static Method addURL = initAddMethod();

	private static URLClassLoader system = (URLClassLoader) ClassLoader
			.getSystemClassLoader();

	/** 初始化方法 */
	private static final Method initAddMethod() {
		try {
			Method add = URLClassLoader.class.getDeclaredMethod("addURL",
					new Class[] { URL.class });
			add.setAccessible(true);
			return add;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加载所有的jar文件
	 * 
	 * @param file
	 *            the file
	 * @author 黄林 Load all jar.
	 */
	public static void loadAllJar(String path) {
		List<File> files = new ArrayList<File>();
		File lib = new File(path);
		loopFiles(lib, files);
		for (File file : files) {
			loadJarFile(file);
		}
	}

	/**
	 * <pre>
	 * 加载JAR文件
	 * </pre>
	 * 
	 * @param file
	 */
	public static final void loadJarFile(File file) {
		try {
			addURL.invoke(system, new Object[] { file.toURI().toURL() });
			System.out.println("加载JAR包：" + file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 循环遍历目录，找出所有的JAR包
	 */
	private static final void loopFiles(File file, List<File> files) {
		if (file.isDirectory()) {
			File[] tmps = file.listFiles();
			for (File tmp : tmps) {
				loopFiles(tmp, files);
			}
		} else {
			if (file.getAbsolutePath().endsWith(".jar")
					|| file.getAbsolutePath().endsWith(".zip")) {
				files.add(file);
			}
		}
	}

}
