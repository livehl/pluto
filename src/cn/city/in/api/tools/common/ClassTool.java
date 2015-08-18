package cn.city.in.api.tools.common;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 获取指定的包下所有的类
 * 
 * @author 黄林 The Class ClassTool.
 */
public class ClassTool {
	// ClassFilter filter,
	/**
	 * Gets the class from directory.
	 * 
	 * @param packageName
	 *            the package name
	 * @param directory
	 *            the directory
	 * @param results
	 *            the results
	 * @return the class from directory
	 * @author 黄林
	 */
	private static void getClassFromDirectory(String packageName,
			File directory, List results) {
		// 如果不存在或者不是目录就直接返回
		if (!directory.exists() || !directory.isDirectory())
			return;
		// 获取目录下文件列表 过滤规则为是目录或者是class文件并且不是内部类
		File[] files = directory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.isDirectory() || (pathname.getName().endsWith(
						".class") && pathname.getName().indexOf("$") == -1));
			}
		});
		// 遍历
		for (File file : files) {
			// 如果是目录 迭代
			if (file.isDirectory()) {
				getClassFromDirectory(packageName + "." + file.getName(), file,
						results);
			} else {
				// 去掉末尾的.class 组装成className
				String className = packageName
						+ "."
						+ file.getName().substring(0,
								file.getName().length() - 6);
				// 过滤器
				// if (filter != null && !filter.filter(className)) continue;
				try {
					results.add(Class.forName(className));
				} catch (ClassNotFoundException e) {
					continue;
				}
			}
		}
	}

	// , ClassFilter filter
	/**
	 * Gets the class from package with filter.
	 * 
	 * @param packageName
	 *            the package name
	 * @return the class from package with filter
	 * @author 黄林
	 */
	public static List getClassFromPackage(String packageName) {
		List results = new ArrayList();
		// 将包名转换为路径名
		String packageDirName = packageName.replace('.', '/');
		// 通过路径获取URL
		Enumeration<URL> resources = null;
		try {
			resources = Thread.currentThread().getContextClassLoader()
					.getResources(packageDirName);
		} catch (IOException e) {
			return results;
		}
		// 遍历获取的URL
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			// 如果是jar包
			if ("jar".equals(url.getProtocol())) {
				JarFile jarFile = null;
				try {
					JarURLConnection conn = (JarURLConnection) url
							.openConnection();
					jarFile = conn.getJarFile();
				} catch (IOException e) {
					continue;
				}
				// 遍历jar包中的所有entry
				for (Enumeration<JarEntry> entries = jarFile.entries(); entries
						.hasMoreElements();) {
					JarEntry entry = entries.nextElement();
					String entryName = entry.getName();
					// 是以指定路径开始&&是class文件&&不是内部类
					if (entryName.startsWith(packageDirName)
							&& entryName.endsWith(".class")
							&& entryName.indexOf("$") == -1) {
						// 把路径转变为类名 us/vifix/a/Class.class - us.vifix.a.Class
						String className = entryName.replace('/', '.')
								.substring(0, entryName.length() - 6);
						// 过滤器
						// if (filter != null && !filter.filter(className))
						// continue;
						try {
							results.add(Class.forName(className));
						} catch (ClassNotFoundException e) {
							continue;
						}
					}
				}
				// 如果是文件系统
			} else if ("file".equals(url.getProtocol())) {
				String filePath = null;
				try {
					filePath = URLDecoder.decode(url.getFile(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					continue;
				}
				// 获取根目录并迭代获取所有class
				File rootDirectory = new File(filePath);
				getClassFromDirectory(packageName, rootDirectory, results);
			}
		}

		return results;
	}

//	/**
//	 * The main method.
//	 * 
//	 * @param args
//	 *            the arguments
//	 */
//	public static void main(String[] args) {
//		List<Class> result = getClassFromPackage("javax.crypto");
//		System.out.println("=== result1 ===");
//		for (Class clazz : result) {
//			System.out.println(clazz);
//		}
//		// result = getClassFromPackageWithFilter("us.vifix.a.ape",
//		// new ExecutorAnnonationFilter());
//		// System.out.println("=== result2 ===");
//		// for (Class clazz : result) {
//		// System.out.println(clazz);
//		// }
//	}
}
