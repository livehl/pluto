package cn.city.in.task.manager;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.task.manager.backup.BackInputThread;
import cn.city.in.task.manager.data.DataManager;
import cn.city.in.task.manager.http.HttpManager;
import cn.city.in.task.manager.socket.SocketServer;

// 任务管理器启动类
/**
 * The Class ManagerMain.
 * 
 * @author 黄林 The Class ManagerMain.
 */
public class ManagerMain {
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String... args) {
		// 初始化日志及配置文件
		PropertyConfigurator.configureAndWatch(FileTool.getClassPathFile(
				"log4j.properties").getAbsolutePath());
		PropertyTool
				.init("classpath:config/*.properties,classpath:serverconf/*.properties");
		// 启动定时任务
		// TaskTool.init("taskPropertyFile",false);
		// 启动远程终端
		// if ("true".equals(PropertyTool.getProperties("use_socket"))) {
		// SocketService.init();
		// }
		// 启动数据管理器
		File saveFile = PropertyTool.getFileInProperty("task_data_file");
		File logSaveFile = PropertyTool.getFileInProperty("task_log_file");
		try {
			DataManager.init(saveFile, logSaveFile);
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
			System.exit(-1);
		}
		// 启动http管理器
		int httpProt = PropertyTool.getNumProperties("http_bind").intValue();
		int httpThread = PropertyTool.getNumProperties("http_thread")
				.intValue();
		int httpTimeOUt = PropertyTool.getNumProperties("http_timeOut")
				.intValue();
		String packageScan = PropertyTool.getProperties("packageScan");
		try {
			HttpManager.init(httpProt, httpTimeOUt, httpThread, packageScan);
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
			System.exit(-1);
		}
		// 启动客户端通讯端口
		int serverProt = PropertyTool.getNumProperties("client_bind")
				.intValue();
		try {
			SocketServer.init(serverProt);
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
			System.exit(-1);
		}
		// 如果有备份配置，则连接主服务器
		String backupMasterIp = PropertyTool.getProperties("backup_master_ip");
		String backupMasterPort = PropertyTool
				.getProperties("backup_master_port");
		if (null != backupMasterIp && null != backupMasterPort) {
			BackInputThread bit = new BackInputThread(backupMasterIp,
					Integer.valueOf(backupMasterPort));
			bit.start();
		}
		// 修正数据
		try {
			DataManager.taskRepair();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 注册停机清理线程
		new ManagerShutdownThread(true);
	}

	/**
	 * 关服
	 * 
	 * @author 黄林 Shutdown.
	 */
	public static void shutdown() {
		HttpManager.shutdown();
		DataManager.shutdown();
		SocketServer.shutdown();
	}
}
