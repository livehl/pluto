package cn.city.in.api.tools.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileTool {
	/** 文件操作根目录 */
	public static File BAST_CLASS_PATH = getClassPathFile();

	private static Log log = LogFactory.getLog(FileTool.class);

	/**
	 * 将inputStream转换为String
	 * 
	 * @param is
	 * @return
	 */
	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 * 文件复制.
	 * 
	 * @param src
	 *            the src
	 * @param obj
	 *            the obj
	 * @throws Exception
	 *             the exception
	 */
	public static boolean copyFile(File src, File obj) {
		if (!obj.exists()) {
			createNewFile(obj);
		}
		try {
			byte[] tempbytes = new byte[(int) src.length()];
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(obj);
			// 读入多个字节到字节数组中，byteread为一次读入的字节数
			in.read(tempbytes);
			out.write(tempbytes);
			out.flush();
			in.close();
			out.close();
		} catch (Exception e) {
			log.warn(e);
			return false;
		}

		return true;
	}

	/**
	 * 功能:创建文件 创建者： 黄林 2011-11-7.
	 * 
	 * @param path
	 *            the path
	 * @return file
	 */
	public static File createFile(String path) {
		if (path == null) {
			return null;
		}
		if (path.indexOf("classpath:") != -1) {
			path = "/" + path.substring(10);
		}
		File returnFile = null;
		if (path.substring(0, 1).equals("/")) {
			returnFile = new File(BAST_CLASS_PATH.getAbsolutePath() + path);
		} else {
			returnFile = new File(path);
		}
		if (!returnFile.exists()) {
			try {
				makeDirs(returnFile);
				returnFile.delete();
				returnFile.createNewFile();
			} catch (IOException e) {
				log.debug("fail to create file:" + path, e);
			}
		}
		return returnFile;
	}

	/**
	 * 功能:根据指定的路径创建文件 创建者： 黄林 2011-12-21.
	 * 
	 * @param file
	 *            the file
	 */
	public static void createNewFile(File file) {
		makeDirs(file);
		if (!file.delete()) {
			log.warn("delete dir:" + file.getAbsolutePath() + " fail!");
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			log.warn("create file:" + file.getAbsolutePath() + " fail", e);
		}
	}

	/**
	 * 获取运行根目录
	 * 
	 * @return the class path file
	 */
	private static File getClassPathFile() {
		File file = null;
		String classPath = FileTool.class.getResource("").toString();
		if (classPath.substring(0, 4).equals("rsrc")
				|| classPath.substring(0, 3).equals("jar")) {
			System.out.println("FileTool on jar,Initialize....");
		}
		URL url = FileTool.class.getResource("/config/");
		try {
			file = new File(url.toURI()).getParentFile();
		} catch (URISyntaxException e1) {
			System.out.println("fail to find classpath");
		}

		if (file != null) {
			file = file.getAbsoluteFile();
			if (null == file.list()) {
				file = file.getParentFile();
			}
			while (!ListTool.toList(file.list()).contains("config")) {
				file = file.getParentFile();
			}
		}
		return file;
	}

	/**
	 * 由java类路径获取文件
	 * 
	 * @param path
	 *            路径
	 * @return the file
	 */
	public static File getClassPathFile(String path) {
		if (null != path && path.indexOf("classpath:") != -1) {
			path = "/" + path.substring(10);
		}
		return new File(BAST_CLASS_PATH.getAbsoluteFile() + "/" + path);
	}

	/**
	 * 根据文件表达式，获取文件列表,忽略文件夹
	 * 
	 * @param paths
	 *            the paths
	 * @return the files
	 */
	public static ArrayList<File> getClassPathFiles(String paths) {
		return getClassPathFiles(paths, false);
	}

	/**
	 * 根据文件表达式获取文件列表
	 * 
	 * @param paths
	 *            the paths
	 * @param userDir
	 *            是否不排除文件夹
	 * @return the files
	 */
	public static ArrayList<File> getClassPathFiles(String paths,
			boolean userDir) {
		if (null == paths) {
			return null;
		}
		ArrayList<File> files = new ArrayList<File>();

		if (StringTool.isArray(paths)) {// 数组
			String[] strPaths = StringTool.getArray(paths);
			for (String string : strPaths) {
				files.addAll(getClassPathFiles(string, userDir));
			}
		} else // 非数组
		{
			if (paths.indexOf("*") != -1) {// 文件集合
				boolean classPath = false;
				if (paths.indexOf("classpath:") != -1) {// 处理根目录
					paths = BAST_CLASS_PATH.getAbsolutePath() + "/"
							+ paths.substring(10);
					classPath = true;
				}
				// 处理最小集合目录
				File dir = null;
				String fileName = paths.substring(paths.lastIndexOf("/") + 1);
				if (paths.lastIndexOf("/") > 0) {// 不管目录中包含*的情况
					dir = new File(paths.substring(0,
							paths.lastIndexOf("/") + 1));
				} else// 没有指明上级目录
				{
					dir = new File(
							classPath ? BAST_CLASS_PATH.getAbsolutePath() : ""
									+ "/");
				}
				// 匹配文件,并加入集合
				String[] fileNames = dir.list();
				if (null != fileNames) {
					fileNames = StringTool.matches(fileNames, fileName);
					for (String string : fileNames) {
						File file = new File(dir.getAbsolutePath() + "/"
								+ string);
						if (file.exists()) {
							files.add(file);
						}

					}
				}

			} else// 单个文件
			{
				files.add(getClassPathFile(paths));
			}
		}
		if (!userDir) {// 忽略文件夹
			for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
				File file = iterator.next();
				if (!file.isFile()) {
					iterator.remove();
				}
			}
		}
		return files;
	}

	/**
	 * 获取文件名的扩展名
	 * 
	 * @param f
	 *            文件
	 * @return the 扩展名
	 */
	public static String getFileExtension(File f) {
		return (f != null) ? getFileExtension(f.getName()) : "";
	}

	/**
	 * 获取文件名的扩展名
	 * 
	 * @param fileName
	 *            文件名
	 * @return the 扩展名
	 */
	public static String getFileExtension(String fileName) {
		return getFileExtension(fileName, "");
	}

	/**
	 * 获取扩展名.
	 * 
	 * @param fileName
	 *            文件名
	 * @param defExt
	 *            没有文件名时返回的默认扩展名
	 * @return the extension
	 */
	public static String getFileExtension(String fileName, String defExt) {
		if ((fileName != null) && (fileName.length() > 0)) {
			int i = fileName.lastIndexOf('.');

			if ((i > -1) && (i < (fileName.length() - 1))) {
				return fileName.substring(i + 1);
			}
		}
		return defExt;
	}

	/**
	 * 获取在指定范围内的扩展名
	 * 
	 * @param fileName
	 *            the file name
	 * @param defExt
	 *            the def ext
	 * @param ins
	 *            the ins
	 * @return the file extension
	 * @author 黄林
	 */
	public static String getFileExtension(String fileName, String defExt,
			String... ins) {
		String ext = getFileExtension(fileName, defExt);
		if (StringTool.in(ext, ins)) {
			return ext;
		}
		return defExt;
	}

	/**
	 * 反序列化对象.
	 * 
	 * @param file
	 *            the file
	 * @return object
	 */
	public static Object loadObject(File file) {
		try {

			FileInputStream in = new FileInputStream(file);
			ObjectInputStream s = new ObjectInputStream(in);
			Object o = s.readObject();
			s.close();
			return o;
		} catch (Exception e) {
			log.debug(e);
			return null;
		}
	}

	/**
	 * 功能:根据指定的路径创建文件夹 创建者： 黄林 2011-12-21.
	 * 
	 * @param file
	 *            the file
	 */
	public static void makeDirs(File file) {
		if (!file.mkdirs()) {
			log.debug("make dirs:" + file.getAbsolutePath() + " fail!");
		}
	}

	/**
	 * 移动文件
	 * 
	 * @param src
	 *            the src
	 * @param obj
	 *            the obj
	 * @return true, if successful
	 */
	public static boolean moveFile(File src, File obj) {
		if (copyFile(src, obj)) {
			return src.delete();
		} else {
			return false;
		}
	}

	/**
	 * 保存数据
	 * 
	 * @param file
	 *            the file
	 * @param data
	 *            the data
	 * @return true, if successful
	 * @author 黄林
	 */
	public static boolean saveByte(File file, byte[] data) {
		try {
			if (!file.exists()) {
				createNewFile(file);
			}
			FileOutputStream f = new FileOutputStream(file);
			f.write(data);
			f.flush();
			f.close();
		} catch (Exception e) {
			log.warn(e);
			return false;
		}
		return true;
	}

	/**
	 * 序列化对象.
	 * 
	 * @param file
	 *            the file
	 * @param obj
	 *            the obj
	 * @return true,
	 */
	public static boolean saveObject(File file, Serializable obj) {
		try {
			FileOutputStream f = new FileOutputStream(file);
			ObjectOutputStream s = new ObjectOutputStream(f);
			s.writeObject(obj);
			s.flush();
			s.close();
		} catch (Exception e) {
			log.warn(e);
			return false;
		}
		return true;
	}

	public static boolean saveText(File file, String text) {
		try {
			FileOutputStream f = new FileOutputStream(file);
			f.write(text.getBytes("utf-8"));
			f.flush();
			f.close();
		} catch (Exception e) {
			log.warn(e);
			return false;
		}
		return true;
	}

	/**
	 * 功能:返回无扩展名的文件名 创建者： 黄林 2011-7-26.
	 * 
	 * @param fileName
	 *            the filename
	 * @return string
	 */
	public static String trimExtensionFileName(String fileName) {
		if ((fileName != null) && (fileName.length() > 0)) {
			int i = fileName.lastIndexOf('.');
			if ((i > -1) && (i < (fileName.length()))) {
				return fileName.substring(0, i);
			}
		}
		return fileName;
	}
}
