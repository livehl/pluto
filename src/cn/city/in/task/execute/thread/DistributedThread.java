package cn.city.in.task.execute.thread;

import org.apache.log4j.Logger;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.task.execute.distributed.DistributedList;

/**
 * 功能:任务处理线程，循环处理单个任务
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class DistributedThread extends Thread {
	private static Logger log = Logger.getLogger(DistributedThread.class);

	public DistributedThread() {
		setDaemon(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		while (true) {
			ObjectNode mdst = (ObjectNode) DistributedList.getTask();
			if (StringTool.isNotNull(mdst)) {
				ObjectNode transferMessage = JsonTool.createNewObjectNode();
				transferMessage.put("data", mdst);
				DistributedExcuteThread det = new DistributedExcuteThread(mdst);
				det.start();
			}
			try {
				Thread.sleep(10);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
