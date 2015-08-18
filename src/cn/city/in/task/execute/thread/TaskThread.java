package cn.city.in.task.execute.thread;

import org.apache.log4j.Logger;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.ExceptionTool;
import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.task.execute.socket.SocketClientService;
import cn.city.in.task.execute.task.TaskList;
import cn.city.in.task.execute.taskdeal.TaskDealConstants;
import cn.city.in.task.execute.taskdeal.TaskDealInterface;

/**
 * 功能:任务处理线程，循环处理单个任务
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class TaskThread extends Thread {
	private static Logger log = Logger.getLogger(TaskThread.class);
	private boolean isLive = true;
	private TaskDealInterface[] tdis;

	public TaskThread() {
		setDaemon(true);
		tdis = new TaskDealInterface[TaskDealConstants.allDeal.length];
		for (int i = 0; i < TaskDealConstants.allDeal.length; i++) {
			try {
				tdis[i] = TaskDealConstants.allDeal[i].newInstance();
			} catch (Exception e) {
				throw new RuntimeException("cant init clazz:"
						+ TaskDealConstants.allDeal[i].getName());
			}
		}
	}

	public void die() {
		isLive = false;
	}

	public TaskDealInterface getDealBean(String task) throws Exception {
		for (TaskDealInterface tdi : tdis) {
			if (tdi.accept(task)) {
				return tdi;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		ObjectNode task = null;
		int count = 100;
		while (isLive) {
			ObjectNode transferMessage = JsonTool.createNewObjectNode();
			try {
				task = (ObjectNode) TaskList.getTask();
				if (StringTool.isNotNull(task)) {
					transferMessage.put("data", task);
					// 获取任务实际内容
					String head = task.get("head").asText();
					// 获取能够处理任务的实例
					TaskDealInterface tdi = getDealBean(head);
					if (null == tdi) {
						throw new Exception("can deal task:" + task);
					}
					// 处理任务
					tdi.doTask(task);
					transferMessage.put("type", "task");
					transferMessage.put("status", "ok");
					log.debug("do task:" + task + " ok");
					// 标志已成功执行
					SocketClientService.write(transferMessage);
				} else {
					count--;
					if (count <= 0) {
						count = 100;
						for (TaskDealInterface tdi : tdis) {
							tdi.idle();
						}
					}
				}
				Thread.sleep(10);
			} catch (Throwable e) {
				// 任务失败
				try {
					System.out.println(task);
					task.put("error", ExceptionTool.getStackTraceString(e));
					transferMessage.put("type", "task");
					transferMessage.put("status", "fail");
					SocketClientService.write(transferMessage);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				log.info("client task error:" + e.getMessage(), e);
			}
		}
		// 停机清理
		for (TaskDealInterface tdi : tdis) {
			tdi.stop();
		}
	}
}
