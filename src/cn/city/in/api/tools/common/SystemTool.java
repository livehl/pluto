package cn.city.in.api.tools.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.Swap;

/** 系统工具，涉及到操作系统环境的都由这里搞定 */
public class SystemTool {
	/** 操作系统-是否window */
	private static Boolean isWinSystem;

	/**
	 * 获取临时文件，最多同时存在一百万个文件.
	 * 
	 * @return 临时文件
	 */
	public static File createTempFile() {
		return createTempFile("exp");
	}

	/**
	 * 获取指定后缀名的临时文件，最多同时存在一百万个文件.
	 * 
	 * @param exp
	 *            the exp
	 * @return the file
	 * @author 黄林
	 */
	public static File createTempFile(String exp) {
		int temp = Tool.random(1000000);
		String tempPath = "temp-" + temp + "." + exp;
		File file = new File(getTempFolder() + tempPath);
		while (file.exists()) {
			temp = Tool.random(1000000);
			tempPath = "temp-" + temp + "." + exp;
			file = new File(tempPath);
		}
		FileTool.createNewFile(file);
		return file;
	}

	public static String getHostName() {
		if (null != System.getenv("HOSTNAME")) {
			return System.getenv("HOSTNAME")
					+ ",comment:"
					+ PropertyTool.getProperties("host_"
							+ System.getenv("HOSTNAME"));
		} else {
			try {
				return InetAddress.getLocalHost().getHostName()
						+ ",comment:"
						+ PropertyTool.getProperties("host_"
								+ System.getenv("HOSTNAME"));
			} catch (UnknownHostException e) {
				return null;
			}
		}
	}

	/**
	 * 获取操作系统类型
	 * 
	 * @return the 操作系统-是否window
	 */
	public static Boolean getIsWinSystem() {
		if (null == isWinSystem) {
			getSystem();
		}
		return isWinSystem;
	}

	/**
	 * 获取系统信息
	 * 
	 * @return the local system info
	 * @author 黄林
	 */
	public static ObjectNode getLocalSystemInfo() {
		ObjectNode stat = new ObjectNode(JsonNodeFactory.instance);
		stat.put("path", System.getProperty("user.dir"));
		stat.put("jvmTotalMemory",
				humamSize(Runtime.getRuntime().totalMemory()));
		stat.put("jvmFreeMemory", humamSize(Runtime.getRuntime().freeMemory()));
		stat.put("cpuCount", Runtime.getRuntime().availableProcessors());
		Sigar sigar = new Sigar();
		try {
			CpuPerc[] infos = sigar.getCpuPercList();
			for (int i = 0; i < infos.length; i++) {
				CpuPerc cpuPerc = infos[i];
				stat.put("cpu" + i, CpuPerc.format(cpuPerc.getCombined()));
			}
			Mem mem = sigar.getMem();
			stat.put("memTotal", humamSize(mem.getTotal()));
			stat.put("memUsed", mem.getUsedPercent());
			stat.put("memFree", mem.getFreePercent());
			Swap swap = sigar.getSwap();
			stat.put("swapTotal", humamSize(swap.getTotal()));
			stat.put("swapUsed", humamSize(swap.getUsed()));
			stat.put("swapFree", humamSize(swap.getFree()));
			// FileSystem[] fileSystems=sigar.getFileSystemList();
			// ArrayNode list=new ArrayNode(JsonNodeFactory.instance);
			// for (FileSystem fs : fileSystems) {
			// ObjectNode fileSystemNode=new
			// ObjectNode(JsonNodeFactory.instance);
			// FileSystemUsage usage=sigar.getFileSystemUsage(fs.getDirName());
			// fileSystemNode.put("fileSystemDev", fs.getDevName());
			// fileSystemNode.put("fileSystemName", fs.getDirName());
			// fileSystemNode.put("fileSystemTypeName", fs.getSysTypeName());
			// if (fs.getType()==2) {
			// // 文件系统总大小
			// fileSystemNode.put("fileSystemTotal",
			// humamSize(usage.getTotal()*1000));
			// // 文件系统剩余大小
			// fileSystemNode.put("fileSystemFree",
			// humamSize(usage.getFree()*1000));
			// // 文件系统可用大小
			// fileSystemNode.put("fileSystemAvail",
			// humamSize(usage.getAvail()*1000));
			// // 文件系统已经使用量
			// fileSystemNode.put("fileSystemUsed",
			// humamSize(usage.getUsed()*1000));
			// // 文件系统资源的利用率
			// fileSystemNode.put("fileSystemUsage", usage.getUsePercent());
			// }
			// list.add(fileSystemNode);
			// }
			// stat.put("fileSystem", list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stat;
	}

	/**
	 * 获取系统信息.
	 * 
	 * @return 操作系统类型 true windows
	 */
	private static void getSystem() {
		String os = System.getProperty("os.name");
		if (os.indexOf("Windows") != -1) {
			isWinSystem = true;
		} else {
			isWinSystem = false;
		}
	}

	/**
	 * 获取系统信息
	 * 
	 * @return the system info
	 */
	public static String getSystemInfo() {
		String str = "";
		str += "JVM VersionDTO:" + System.getProperty("java.vm.version")
				+ "\r\n";
		str += "JRE VersionDTO:" + System.getProperty("java.runtime.version")
				+ "\r\n";
		str += "OS:" + System.getProperty("os.name") + "\r\n";
		str += "OS VersionDTO:" + System.getProperty("os.version") + "\r\n";
		str += "------------------------------------------\r\n";
		str += "HOST Name:" + getHostName() + "\r\n";
		try {
			str += "IP:" + InetAddress.getLocalHost().getHostAddress() + "\r\n";
		} catch (UnknownHostException e) {
		}
		str += "------------------------------------------\r\n";
		str += "Total Memory:" + humamSize(Runtime.getRuntime().totalMemory())
				+ "\r\n";
		str += "Free Memory:" + humamSize(Runtime.getRuntime().freeMemory())
				+ "\r\n";
		return str;
	}

	/**
	 * 获取指定后缀名的临时文件，最多同时存在一百万个文件.
	 * 
	 * @param 文件后缀名
	 * @return 临时文件
	 */
	public static File getTempFile(String exp) {
		int temp = Tool.random(1000000);
		String tempPath = "temp" + "/" + temp + "." + exp;
		File file = new File(getTempFolder() + tempPath);
		while (file.exists()) {
			temp = Tool.random(1000000);
			tempPath = "temp" + "/" + temp + "." + exp;
			file = new File(tempPath);
		}
		file.mkdirs();
		file.delete();
		return file;
	}

	/**
	 * 获取临时文件目录.
	 * 
	 * @return 临时文件目录
	 */
	public static String getTempFolder() {
		if (getIsWinSystem()) {
			return System.getenv("TMP") + "/";
		} else {
			String appName = PropertyTool.getProperties("appname");
			if (StringTool.isNull(appName)) {
				appName = "icd_unknow_app" + Tool.random();
			}
			return "/tmp/" + appName + "/";
		}
	}

	/**
	 * 转化比特数为易读格式
	 * 
	 * @param size
	 *            the size
	 * @return the string
	 */
	private static String humamSize(long size) {
		String[] sizeStr = new String[] { "b", "kb", "mb", "gb", "tb", "pb" };
		int index = 0;
		while (size > 1000) {
			index++;
			size = size / 1000;
		}
		return size + " " + sizeStr[index];
	}

	/**
	 * 功能:执行本地命令 创建者： 黄林 2011-9-15.
	 * 
	 * @param commond
	 *            the commond
	 * @param params
	 *            the params
	 * @return string
	 */
	private static String runCommond(String commond, String... params) {
		try {
			Process p = Runtime.getRuntime().exec(commond, params);
			StringBuffer strBuf = new StringBuffer();
			BufferedReader bf = new BufferedReader(new InputStreamReader(
					p.getInputStream(), "gb2312"));
			String line = "";
			while ((line = bf.readLine()) != null) {
				strBuf.append(line + "\r\n");
			}
			bf.close();
			return strBuf.toString();
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
	}

	/**
	 * 快速执行shell命令
	 * 
	 * @param commond
	 *            the commond
	 * @author 黄林 Run commond fast.
	 */
	public static void runCommondFast(String commond) {
		try {
			if (getIsWinSystem()) {
				commond = "cmd /c start " + commond;
			} else {
				commond = "/bin/sh -c /bin/" + commond;
			}
			Runtime.getRuntime().exec(commond);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 功能:执行本地命令,参数以#开头表示加上选项符(windows / linux -) 创建者： 黄林 2011-9-16.
	 * 
	 * @param commond
	 *            the commond
	 * @param params
	 *            the params
	 * @return string
	 */
	public static String runLocalCommond(String commond, String... params) {
		if (getIsWinSystem()) {
			StringBuffer sb = new StringBuffer(commond);
			for (String string : params) {
				if (string.subSequence(0, 1).equals("#")) {
					sb.append(" -" + string.substring(1));
				} else {
					sb.append(" " + string);
				}
			}
			commond = sb.toString();
			return runCommond(commond);
		} else {
			return runCommond(commond, params);
		}
	}

	/**
	 * 使用通用启动方式启动.
	 * 
	 * @param path
	 *            启动
	 * @author 黄林 Restart.
	 */
	public static void start() {
		try {
			String path = System.getProperty("user.dir");
			if (getIsWinSystem()) {
				Runtime.getRuntime().exec("cmd /c start CS " + path);
			} else {
				Runtime.getRuntime().exec(
						new String[] { "/bin/sh", "-c", "/bin/CS " + path });
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
