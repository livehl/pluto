package cn.city.in.task.manager;

import cn.city.in.api.tools.common.TimeTool;
import cn.city.in.api.tools.task.TaskTool;

/**
 * 功能:退出处理线程，Manager停机时被执行。
 * 
 * @author 黄林 2011-11-17
 * @version
 */
public class ManagerShutdownThread extends Thread {

	/**
	 * 创建清理线程
	 * 
	 * @param 是否注册关机钩子
	 */
	public ManagerShutdownThread(boolean hook) {
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
		System.out.println(TimeTool.getFormatStringByNow() + " stop start");
		ManagerMain.shutdown();
		TaskTool.shutdown();
		System.out.println(TimeTool.getFormatStringByNow() + " stop ok");
	}

}
