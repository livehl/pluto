package cn.city.in.task.manager.socket;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.task.manager.socket.codec.PlutoProtocolCodecFactory;

/**
 * 客户端服务
 * 
 * @author 黄林
 * 
 */
public class SocketServer {
	private static Logger log = Logger.getLogger(SocketServer.class);
	private static IoAcceptor acceptor;

	public static final int CLIENT_TYPE_EXECUTE = 0;
	public static final int CLIENT_TYPE_COMMIT = 1;
	public static final int CLIENT_TYPE_BACKUP = 2;

	/**
	 * 执行器保存文件
	 * 
	 * @param path
	 *            the path
	 * @param file
	 *            the file
	 * @return the int
	 * @author 黄林
	 */
	public static int clientSaveFile(String path, byte[] fileData) {
		int count = 0;
		ObjectNode transferMessage = JsonTool.createNewObjectNode();
		transferMessage.put("type", "control");
		ObjectNode data = JsonTool.createNewObjectNode();
		data.put("command", "saveFile");
		data.put("path", path);
		data.put("fileData", fileData);
		transferMessage.put("data", data);
		Collection<IoSession> allSessions = getSessionsByClientType(CLIENT_TYPE_EXECUTE);
		for (IoSession ioSession : allSessions) {
			ioSession.write(transferMessage);
			count++;
		}
		return count;
	}

	/**
	 * 执行器升级
	 * 
	 * @param path
	 *            the path
	 * @param file
	 *            the file
	 * @return the int
	 * @author 黄林
	 */
	public static int clientUpdata(String path, byte[] fileData) {
		int count = 0;
		ObjectNode transferMessage = JsonTool.createNewObjectNode();
		transferMessage.put("type", "control");
		ObjectNode data = JsonTool.createNewObjectNode();
		data.put("command", "updata");
		data.put("path", path);
		data.put("fileData", fileData);
		transferMessage.put("data", data);
		Collection<IoSession> allSessions = getSessionsByClientType(CLIENT_TYPE_EXECUTE);
		for (IoSession ioSession : allSessions) {
			ioSession.write(transferMessage);
			count++;
		}
		return count;
	}

	/**
	 * 强制关闭客户端
	 * 
	 * @param exp
	 *            客户端匹配模式
	 * @param command
	 *            the command
	 * @return 执行数
	 * @author 黄林 Shtudown.
	 */
	public static int closeShutdown(String exp) {
		int count = 0;
		Collection<IoSession> allSessions = acceptor.getManagedSessions()
				.values();
		for (IoSession ioSession : allSessions) {
			String key = ioSession.getRemoteAddress().toString();
			if (key.equals(exp) || StringTool.matche(key, exp)
					|| StringTool.matche(key, "*" + exp + "*")) {
				ioSession.close(false);
				count++;
			}
		}
		return count;
	}

	/**
	 * 获取所有的客户端日志
	 * 
	 * @return the all session info
	 * @author 黄林
	 */
	public static Map<String, ObjectNode> getAllExecuteSessionLog() {
		Map<String, ObjectNode> maps = new HashMap<String, ObjectNode>();
		Collection<IoSession> allSessions = getSessionsByClientType(CLIENT_TYPE_EXECUTE);
		for (IoSession ioSession : allSessions) {
			String key = ioSession.getRemoteAddress().toString();
			ObjectNode data = JsonTool.createNewObjectNode();
			data.put("log", (JsonNode) ioSession.getAttribute("log"));
			data.put("id", key);
			data.put("client_type",
					(Integer) ioSession.getAttribute("client_type"));
			maps.put(key, data);
		}
		return maps;
	}

	/**
	 * 获取所有的session信息
	 * 
	 * @return the all session info
	 * @author 黄林
	 */
	public static Map<String, ObjectNode> getAllSessionInfo() {
		Map<String, ObjectNode> maps = new HashMap<String, ObjectNode>();
		Collection<IoSession> allSessions = acceptor.getManagedSessions()
				.values();
		for (IoSession ioSession : allSessions) {
			String key = ioSession.getRemoteAddress().toString();
			ObjectNode data = JsonTool.createNewObjectNode();
			data.put("id", key);
			data.put("client_type",
					(Integer) ioSession.getAttribute("client_type"));
			data.put("taskCount",
					(Integer) ioSession.getAttribute("task_count"));
			data.put("taskDealCount",
					(Integer) ioSession.getAttribute("task_deal_count"));
			data.put("taskFailCount",
					(Integer) ioSession.getAttribute("task_fail_count"));
			data.put("startTime", (String) ioSession.getAttribute("start_time"));
			ObjectNode clientStatus = (ObjectNode) ioSession
					.getAttribute("status");
			if (null != clientStatus) {
				data.putAll(clientStatus);
			}
			maps.put(key, data);
		}
		return maps;
	}

	/**
	 * 获取客户端类型
	 * 
	 * @param name
	 *            the name
	 * @return the client type by name
	 * @author 黄林
	 */
	public static int getClientTypeByName(String name) {
		if (name.equals("execute")) {
			return CLIENT_TYPE_EXECUTE;
		} else if (name.equals("commit")) {
			return CLIENT_TYPE_COMMIT;
		} else if (name.equals("backup")) {
			return CLIENT_TYPE_BACKUP;
		}
		return CLIENT_TYPE_EXECUTE;
	}

	/**
	 * 获取指定类型的客户端
	 * 
	 * @param type
	 *            the type
	 * @return the sessions by client type
	 * @author 黄林
	 */
	public static Collection<IoSession> getSessionsByClientType(Integer type) {
		Collection<IoSession> sessions = acceptor.getManagedSessions().values();
		Collection<IoSession> returnSessions = new HashSet<IoSession>();
		for (IoSession ioSession : sessions) {
			if (null != ioSession.getAttribute("client_type")
					&& type.equals(ioSession.getAttribute("client_type"))) {
				returnSessions.add(ioSession);
			}
		}
		return returnSessions;
	}

	/**
	 * 功能:初始化服务器，监听端口 创建者： 黄林 2011-11-4.
	 */
	public static void init(int port) throws Exception {
		acceptor = new NioSocketAcceptor();
		// acceptor.getFilterChain().addLast(
		// "codec",
		// new ProtocolCodecFilter(new TextLineCodecFactory(Charset
		// .forName("utf-8"))));
		// 使用自定义协议
		acceptor.getFilterChain().addLast("protocol",
				new ProtocolCodecFilter(new PlutoProtocolCodecFactory()));
		acceptor.setHandler(new ClientHandler());
		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 5);
		((SocketSessionConfig) acceptor.getSessionConfig()).setSoLinger(0);
		acceptor.bind(new InetSocketAddress(port));
		log.info("start socket service ok");
	}

	/**
	 * 发送任务至备份服务器
	 * 
	 * @param context
	 *            the context
	 * @param status
	 *            the status
	 * @return the int
	 * @author 黄林
	 */
	public static int sendBackup(ObjectNode context, int status) {
		int count = 0;
		ObjectNode transferMessage = JsonTool.createNewObjectNode();
		transferMessage.put("type", "backup");
		context.put("backup_status", status);
		transferMessage.put("data", context);
		Collection<IoSession> allSessions = getSessionsByClientType(CLIENT_TYPE_BACKUP);
		for (IoSession ioSession : allSessions) {
			ioSession.write(transferMessage);
			count++;
		}
		return count;
	}

	/**
	 * 指定的命令到客户端.
	 * 
	 * @param exp
	 *            客户端匹配模式
	 * @param command
	 *            the command
	 * @return 执行数
	 * @author 黄林 Shtudown.
	 */
	public static int sendClientCommand(String exp, String command) {
		int count = 0;
		Collection<IoSession> allSessions = acceptor.getManagedSessions()
				.values();
		for (IoSession ioSession : allSessions) {
			String key = ioSession.getRemoteAddress().toString();
			if (key.equals(exp) || StringTool.matche(key, exp)
					|| StringTool.matche(key, "*" + exp + "*")) {
				ObjectNode transferMessage = JsonTool.createNewObjectNode();
				transferMessage.put("type", "control");
				ObjectNode data = JsonTool.createNewObjectNode();
				data.put("command", command);
				transferMessage.put("data", data);
				ioSession.write(transferMessage);
				count++;
			}
		}
		return count;
	}

	/**
	 * 服务器停机
	 */
	public static void shutdown() {
		// 关闭所有客户端
		if (null != acceptor) {
			try {
				sendClientCommand("*", "exit");
			} catch (Exception e) {
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}
			Collection<IoSession> allSessions = acceptor.getManagedSessions()
					.values();
			for (IoSession ioSession : allSessions) {
				try {
					ioSession.close(false);
				} catch (Exception e) {
				}
			}
			if (null != acceptor) {
				acceptor.unbind();
				acceptor.dispose();
			}
		}
	}

	/**
	 * session写入数据
	 * 
	 * @param obj
	 *            the obj
	 * @param session
	 *            the session
	 * @author 黄林 Write session.
	 */
	public static void writeSession(ObjectNode obj, IoSession session) {
		synchronized (session) {
			session.write(obj);
		}
	}
}
