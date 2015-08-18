package cn.city.in.task.execute;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.task.TaskTool;
import cn.city.in.task.execute.socket.SocketClientService;
import cn.city.in.task.execute.thread.DistributedThread;
import cn.city.in.task.execute.thread.SocketClientIOThread;
import cn.city.in.task.execute.thread.TaskThread;

// 任务执行器启动类
/**
 * The Class ManagerMain.
 * 
 * @author 黄林 The Class ManagerMain.
 */
public class ExecuteMain {
	private static SocketClientIOThread clientThread;
	private static ExcuteShutdownThread excuteShutdownThread;
	private static DistributedThread dThread;
	private static List<TaskThread> taskThread = new ArrayList<TaskThread>();

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String... args) {
		// 初始化日志及配置文件
		PropertyConfigurator.configure(FileTool.getClassPathFile(
				"log4j.properties").getAbsolutePath());
		PropertyTool
				.init("classpath:config/*.properties,classpath:serverconf/*.properties");
		// 启动定时任务
		TaskTool.init(false);
		// 启动远程终端
		// if ("true".equals(PropertyTool.getProperties("use_socket"))) {
		// SocketService.init();
		// }
		// 连接服务端通讯端口
		int port = PropertyTool.getNumProperties("manager_port").intValue();
		String serviceIp = PropertyTool.getProperties("manager_ip");
		try {
			SocketClientService.init(serviceIp, port);
			// 启动通讯处理线程
			clientThread = new SocketClientIOThread();
			clientThread.start();
			// 启动分布式计算线程
			dThread = new DistributedThread();
			dThread.start();
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
			System.exit(-1);
		}
		// 启动任务执行线程 cpu-1
		int cpuCount = Runtime.getRuntime().availableProcessors() - 1;
		if (cpuCount < 1) {
			cpuCount = 1;
		}
		for (int i = 0; i < cpuCount; i++) {
			TaskThread thread = new TaskThread();
			taskThread.add(thread);
			thread.start();
		}
		// 注册停机清理线程
		excuteShutdownThread = new ExcuteShutdownThread(true);
	}

	public static void setClientThread(SocketClientIOThread clientThread) {
		ExecuteMain.clientThread = clientThread;
	}

	/**
	 * 设置重启
	 * 
	 * @param reboot
	 *            the new reboot
	 * @author 黄林
	 */
	public static void setReboot(boolean reboot) {
		excuteShutdownThread.reboot = reboot;
	}

	/**
	 * 关服
	 * 
	 * @author 黄林 Shutdown.
	 */
	public static void shutdown() {
		clientThread.die();
		SocketClientService.shutdown();
		// 销毁所有执行线程
		for (TaskThread thread : taskThread) {
			thread.die();
		}

	}

}
