package cn.city.in.task.execute;

import cn.city.in.api.tools.common.SystemTool;
import cn.city.in.api.tools.task.TaskTool;

/**
 * 功能:退出处理线程，Manager停机时被执行。
 * 
 * @author 黄林 2011-11-17
 * @version
 */
public class ExcuteShutdownThread extends Thread {
	public boolean reboot = false;

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
	public ExcuteShutdownThread(boolean hook) {
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
		ExecuteMain.shutdown();
		TaskTool.shutdown();
		if (reboot) {
			// 重启
			SystemTool.start();
			System.out.println("restart ing");
		} else {
			System.out.println("stop ok");
		}
	}

}
