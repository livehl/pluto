package cn.city.in.task.manager.socket;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.ExceptionTool;
import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.TimeTool;
import cn.city.in.task.manager.data.DataManager;
import cn.city.in.task.manager.distributed.DistributedManager;
import cn.city.in.task.manager.task.TaskManager;

/**
 * 功能:连接线程，处理每个客户端的请求
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class ClientHandler extends IoHandlerAdapter {
	private static Logger log = Logger.getLogger(ClientHandler.class);

	@Override
	public void exceptionCaught(IoSession session, Throwable e)
			throws Exception {
		ExceptionTool.cutStackTrace(e, "cn.city.in", true);
		log.warn(
				"client " + session.getRemoteAddress() + " on error"
						+ e.getMessage(), e);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		JsonNode transferMessage = (JsonNode) message;
		log.debug("client from" + session.getRemoteAddress()
				+ " execution command:" + transferMessage);
		String type = transferMessage.get("type").asText();
		if ("status".equals(type)) {
			JsonNode data = transferMessage.get("data");
			if (session.getAttribute("client_type") == null) {
				String clientType = data.get("client_type").asText();
				session.setAttribute("client_type",
						SocketServer.getClientTypeByName(clientType));
				session.setAttribute("cpu_count", data.get("cpuCount").asInt());
				TaskManager.clientOrTaskStatusChange();
			}
			session.setAttribute("status", data);
		} else if ("log".equals(type)) {
			session.setAttribute("log", transferMessage.get("data"));
		} else if ("task".equals(type)) {
			String status = transferMessage.get("status").asText();
			if ("ok".equals(status)) {
				// 任务成功
				TaskManager.doTaskOk(session,
						(ObjectNode) transferMessage.get("data"));

			} else if ("fail".equals(status)) {
				// 任务失败
				TaskManager.doTaskFail(session,
						(ObjectNode) transferMessage.get("data"));
			} else if ("add".equals(status)) {
				// 添加任务
				DataManager.addTask((ObjectNode) transferMessage.get("data"));
			}
		} else if ("mds".equals(type)) {
			String status = transferMessage.get("status").asText();
			if ("ok".equals(status)) {
				// 计算成功
				DistributedManager.doDistributedOk(session,
						(ObjectNode) transferMessage.get("data"));
			} else if ("fail".equals(status)) {
				// 计算失败
				DistributedManager.doDistributedFail(session,
						(ObjectNode) transferMessage.get("data"));
			} else if ("add".equals(status)) {
				// 添加计算
				ObjectNode data = (ObjectNode) transferMessage.get("data");
				data.put("source_id", session.getId());
				DataManager.addDistributed(data);
			} else {
				log.info("unknow status:" + status);
			}
		}
		log.debug(transferMessage);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// 重新发送未成功执行的任务
		TaskManager.sessionFail(session);
		log.info("client from" + session.getRemoteAddress() + " exit");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		// 客户端状态
		session.setAttribute("task_deal_count", 0);
		session.setAttribute("task_count", 0);
		session.setAttribute("task_fail_count", 0);
		session.setAttribute("start_time", TimeTool.getFormatStringByNow());
		log.debug("client " + session.getRemoteAddress() + " connect:"
				+ session.getId());
		TaskManager.clientOrTaskStatusChange();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#sessionIdle(org.apache.
	 * mina.core.session.IoSession, org.apache.mina.core.session.IdleStatus)
	 */
	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		ObjectNode msg = JsonTool.createNewObjectNode();
		msg.put("type", "status");
		msg.put("data", (Integer) session.getAttribute("task_deal_count"));
		session.write(msg);
	}
}
