package cn.city.in.api.tools.socket;

import cn.city.in.api.tools.cache.JedisTool;
import cn.city.in.api.tools.task.TaskTool;

/**
 * 功能:退出处理线程，服务器停机时被执行。
 * 
 * @author 黄林 2011-11-17
 * @version
 */
public class ShutdownThread extends Thread {

	// /**
	// * 功能:
	// * 创建者： 黄林 2011-11-17.
	// */
	// public ShutdownThread() {
	// super();
	// Runtime.getRuntime().addShutdownHook(this);
	// }

	/**
	 * 创建清理线程
	 * 
	 * @param 是否注册关机钩子
	 */
	public ShutdownThread(boolean hook) {
		super();
		if (hook) {
			Runtime.getRuntime().addShutdownHook(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		System.out.println("stop start");
		SocketService.shutdown();
		TaskTool.shutdown();
		JedisTool.getJedis().disconnect();
		System.out.println("stop ok");
	}

}
