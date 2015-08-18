package cn.city.in.task.manager.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.task.manager.data.DataManager;
import cn.city.in.task.manager.socket.SocketServer;

public class TaskManager {
	private static Logger log = Logger.getLogger(TaskManager.class);
	private static HashMap<Long, ObjectNode> sendTaskMap = new HashMap<Long, ObjectNode>();
	private static HashMap<Long, ObjectNode> reTryTaskMap = new HashMap<Long, ObjectNode>();
	private static List<ObjectNode> noSendTaskList = new ArrayList<ObjectNode>();
	private static int client_task_limit = 3;
	private static int taskReTryCount = 3;
	private static HashMap<Long, Long> sessionSendTaskTime = new HashMap<Long, Long>();

	/**
	 * 客户端状态、任务状态变更,分发任务
	 * 
	 * @author 黄林 Client status change.
	 */
	public static void clientOrTaskStatusChange() {
		synchronized (noSendTaskList) {
			Iterator<ObjectNode> iterator = noSendTaskList.iterator();
			while (iterator.hasNext()) {
				ObjectNode node = iterator.next();
				// 跳过被暂停的任务
				try {
					String head = node.get("head").asText();
					if (DataManager.getHeadStatus(head) == DataManager.TASK_HEAD_STATUS_PAUSE) {
						continue;
					}
				} catch (Exception e) {
				}
				if (sendTaskToSession(node)) {
					iterator.remove();
				} else {
					break;
				}
			}
			log.debug(noSendTaskList.size() + " task send client");
		}
	}

	/**
	 * 任务执行失败
	 * 
	 * @param task
	 *            the task
	 * @author 黄林 Do task fail.
	 */
	public static void doTaskFail(IoSession session, ObjectNode task) {
		// 执行任务数自减
		Integer taskDealCount = (Integer) session
				.getAttribute("task_deal_count");
		session.setAttribute("task_deal_count", --taskDealCount);
		Integer taskFailCount = (Integer) session
				.getAttribute("task_fail_count");
		session.setAttribute("task_fail_count", ++taskFailCount);
		Long id = task.get("id").asLong();
		synchronized (sendTaskMap) {
			sendTaskMap.remove(id);
		}
		// 记录任务失败
		log.debug("fail:" + task);
		DataManager.taskChange(task, DataManager.TASK_STATUS_FAIL);
		Integer errorCount = 0;
		if (reTryTaskMap.containsKey(id)) {
			errorCount = reTryTaskMap.get(id).get("retry_count").asInt();
		}
		synchronized (reTryTaskMap) {
			if (null == reTryTaskMap.get(id) || errorCount < taskReTryCount) {
				if (errorCount < taskReTryCount) {
					// 重发任务
					errorCount++;
					task.put("retry_count", errorCount);
					reTryTaskMap.put(id, task);
					DataManager
							.taskChange(task, DataManager.TASK_STATUS_RESEND);
					// 继续存入待发列表
					synchronized (noSendTaskList) {
						noSendTaskList.add(task);
					}
				}
			} else {
				reTryTaskMap.remove(id);
			}
		}
		clientOrTaskStatusChange();
	}

	/**
	 * 任务执行成功
	 * 
	 * @param task
	 *            the task
	 * @author 黄林 Do task ok.
	 */
	public static void doTaskOk(IoSession session, ObjectNode task) {
		// 执行任务数自减
		Integer taskDealCount = (Integer) session
				.getAttribute("task_deal_count");
		session.setAttribute("task_deal_count", --taskDealCount);
		Long id = task.get("id").asLong();
		synchronized (sendTaskMap) {
			sendTaskMap.remove(id);
		}
		// 标记重试任务已执行
		if (reTryTaskMap.containsKey(id)) {
			synchronized (reTryTaskMap) {
				reTryTaskMap.remove(id);
			}
		}
		// 记录任务已完成
		log.debug("ok:" + task);
		DataManager.taskChange(task, DataManager.TASK_STATUS_OK);
		clientOrTaskStatusChange();
	}

	/**
	 * 获取一个发送任务的Session 分发数量限制 可能一个都没,返回null
	 * 
	 * @return the session
	 * @author 黄林
	 */
	public static IoSession getTaskSession() {
		Collection<IoSession> allSessions = SocketServer
				.getSessionsByClientType(SocketServer.CLIENT_TYPE_EXECUTE);
		IoSession lowSession = null;
		Long lowTaskCount = Long.MAX_VALUE;
		for (IoSession session : allSessions) {
			Long id = session.getId();
			Integer cpuCount = (Integer) session.getAttribute("cpu_count");
			if (cpuCount == null || cpuCount < 1) {
				cpuCount = 1;
			}
			if ((Integer) session.getAttribute("task_deal_count") < client_task_limit
					* cpuCount) {
				if ((!sessionSendTaskTime.containsKey(id))) {
					sessionSendTaskTime.put(id, System.currentTimeMillis());
					return session;
				} else {
					if (sessionSendTaskTime.get(id) < lowTaskCount) {
						lowTaskCount = sessionSendTaskTime.get(id);
						lowSession = session;
					}
				}
			}
		}
		if (lowSession != null) {
			sessionSendTaskTime.put(lowSession.getId(),
					System.currentTimeMillis());
		}
		return lowSession;
	}

	/**
	 * 将任务放入待发列表
	 * 
	 * @param task
	 *            the task
	 * @return 任务是否已经分发
	 * @author 黄林 Send task.
	 */
	public static void sendTask(ObjectNode task) {
		// 存入待发列表
		synchronized (noSendTaskList) {
			noSendTaskList.add(task);
		}
		clientOrTaskStatusChange();
	}

	/**
	 * 发送任务至Session
	 * 
	 * @param task
	 *            the task
	 * @param session
	 *            the session
	 * @author 黄林 Send task.
	 */
	public static void sendTask(ObjectNode task, IoSession session) {
		try {
			ObjectNode transferMessage = JsonTool.createNewObjectNode();
			transferMessage.put("type", "task");
			transferMessage.put("data", task);
			SocketServer.writeSession(transferMessage, session);
			// 任务数自加
			Integer taskCount = (Integer) session.getAttribute("task_count");
			session.setAttribute("task_count", ++taskCount);
			// 执行任务数自加
			Integer taskDealCount = (Integer) session
					.getAttribute("task_deal_count");
			session.setAttribute("task_deal_count", ++taskDealCount);
			task.put("session_id", session.getId());
			long id = task.get("id").asLong();
			synchronized (sendTaskMap) {
				sendTaskMap.put(id, task);
			}
			// 记录任务已发送
			log.debug("hasSend:" + task);
			DataManager.taskChange(task, DataManager.TASK_STATUS_SEND);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送任务至客户端执行.
	 * 
	 * @param task
	 *            the task
	 * @return 任务是否已经分发
	 * @author 黄林
	 */
	public static boolean sendTaskToSession(ObjectNode task) {
		log.debug("send:" + task);
		IoSession session = getTaskSession();
		if (session != null) {
			sendTask(task, session);
			return true;
		}
		return false;
	}

	/**
	 * session失效,重发任务
	 * 
	 * @param session
	 *            the session
	 * @author 黄林 Session fail.
	 */
	public static void sessionFail(IoSession session) {
		Long id = session.getId();
		List<ObjectNode> taskList = new ArrayList<ObjectNode>();
		synchronized (sendTaskMap) {
			Collection<ObjectNode> values = sendTaskMap.values();
			for (ObjectNode entry : values) {
				if (id.equals(entry.get("session_id").asLong())) {
					taskList.add(entry);
				}
			}
		}
		// 重发
		synchronized (noSendTaskList) {
			noSendTaskList.addAll(taskList);
		}
		clientOrTaskStatusChange();
	}
}
