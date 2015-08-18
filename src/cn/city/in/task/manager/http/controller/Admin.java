package cn.city.in.task.manager.http.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.ReflectTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.api.tools.common.SystemTool;
import cn.city.in.api.tools.task.TaskTool;
import cn.city.in.task.manager.data.DataManager;
import cn.city.in.task.manager.http.codec.HttpFile;
import cn.city.in.task.manager.http.comment.HttpComment;
import cn.city.in.task.manager.http.comment.HttpParam;
import cn.city.in.task.manager.socket.SocketServer;
import cn.city.in.task.manager.task.TaskManager;

public class Admin extends BaseController {

	/**
	 * 清理执行错误的任务
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/clean_fail")
	public String cleanFail() throws Exception {
		StringBuffer sb = new StringBuffer("<pre>");
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		Set<Entry<Long, ObjectNode>> entrySet = taskMap.entrySet();
		for (Entry<Long, ObjectNode> entry : entrySet) {
			ObjectNode task = entry.getValue();
			int status = task.get("status").asInt();
			if (status == DataManager.TASK_STATUS_FAIL) {
				sb.append(entry.getKey()
						+ " \t "
						+ task
						+ " \t"
						+ DataManager
								.getStatString(DataManager.TASK_STATUS_REMOVE)
						+ "\r\n");
				DataManager.taskChange(task, DataManager.TASK_STATUS_REMOVE);
			}
		}
		return sb.toString();
	}

	/**
	 * 执行客户端命令
	 * 
	 * @param exp
	 *            the exp
	 * @param command
	 *            the command
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/client_command")
	public String clientCommand(
			@HttpParam(value = "exp", required = true) String exp,
			@HttpParam(value = "command", required = true) String command)
			throws Exception {
		int count = SocketServer.sendClientCommand(exp, command);
		return count + "client excute";
	}

	/**
	 * 保存文件至客户端
	 * 
	 * @param file
	 *            the file
	 * @param path
	 *            the path
	 * @return the integer
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/client_save_file")
	public Integer clientSaveFile(
			@HttpParam(value = "file", required = true) HttpFile file,
			@HttpParam(value = "path", required = true) String path)
			throws Exception {
		return SocketServer.clientSaveFile(path, file.getData());
	}

	/**
	 * 客户端升级
	 * 
	 * @param file
	 *            the file
	 * @param path
	 *            the path
	 * @return the integer
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/client_updata")
	public Integer clientUpdata(
			@HttpParam(value = "file", required = true) HttpFile file,
			@HttpParam(value = "path", required = true) String path)
			throws Exception {
		return SocketServer.clientUpdata(path, file.getData());
	}

	/**
	 * 重新发送任务至客户端
	 * 
	 * @param exp
	 *            the exp
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public String reSendTask(
			@HttpParam(value = "exp", required = true) String exp)
			throws Exception {
		StringBuffer sb = new StringBuffer("<pre>");
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		Set<Entry<Long, ObjectNode>> entrySet = taskMap.entrySet();
		for (Entry<Long, ObjectNode> entry : entrySet) {
			ObjectNode task = entry.getValue();
			if (StringTool.matche(entry.getKey().toString(), exp)) {
				sb.append(entry.getKey()
						+ " \t"
						+ DataManager
								.getStatString(DataManager.TASK_STATUS_RESEND)
						+ "\r\n");
				TaskManager.sendTask(task);
				DataManager.taskChange(task, DataManager.TASK_STATUS_RESEND);
			}
		}
		return sb.toString();
	}

	/**
	 * 重启客户端
	 * 
	 * @param exp
	 *            the exp
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/restart_client")
	public String restartClient(
			@HttpParam(value = "exp", required = true) String exp)
			throws Exception {
		int count = SocketServer.sendClientCommand(exp, "restart");
		return count + "client restart";
	}

	/**
	 * 管理界面
	 * 
	 * @author 黄林
	 * @param deviceToken
	 *            the device token
	 * @param channelId
	 *            the channel id
	 * @return the api model
	 * @throws Exception
	 *             the exception
	 */
	@HttpComment(uri = "admin/")
	public String root() throws Exception {
		Set<String> sessionSet = SocketServer.getAllSessionInfo().keySet();
		int clientCount = sessionSet.size();
		StringBuffer result = new StringBuffer("master status:"
				+ SystemTool.getLocalSystemInfo() + "<br/>"
				+ "<a href='stat' >status</a> <br/>"
				+ "<a href='clean_fail' >clean all fail task(warn)</a> <br/>"
				+ "<a href='shutdown'>shutdown</a><br/>"
				+ "<a href='shutdown_client?exp=*'>shutdown all client("
				+ clientCount + ")</a><br/>"
				+ "<a href='restart_client?exp=*'>restart all client("
				+ clientCount + ")</a><br/>");
		// 列出所有的客户端ip
		Map<String, Integer> ipMap = new HashMap<String, Integer>();
		for (String ip : sessionSet) {
			ip = ip.substring(1);
			ip = ip.substring(0, ip.indexOf(":"));
			if (ipMap.containsKey(ip)) {
				ipMap.put(ip, ipMap.get(ip) + 1);
			} else {
				ipMap.put(ip, 1);
			}
		}
		for (String ip : ipMap.keySet()) {
			result.append("ip:" + ip + " <a href='shutdown_client?exp=*" + ip
					+ "*'>shutdown all client(" + ipMap.get(ip)
					+ ")</a> \t <a href='restart_client?exp=*" + ip
					+ "*'>restart all client(" + ipMap.get(ip) + ")</a><br/>");
		}

		return result.toString();
	}

	/**
	 * 停止服务
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/shutdown")
	public String shutdown() throws Exception {
		// //验证密码
		// if (pwd.trim().equals(PropertyTool.getProperties("http_admin_pwd")))
		// {
		// //不能直接关闭,会与停机方法死锁
		TaskTool.createTimeTask(1, 10l,
				ReflectTool.getMethod(System.class, "exit"), System.class, 0);
		return "ok";
		// }
		// getResponse().forbidden();
		// return null;
	}

	/**
	 * 关闭客户端
	 * 
	 * @param exp
	 *            表达式
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/shutdown_client")
	public String shutdownClient(
			@HttpParam(value = "exp", required = true) String exp)
			throws Exception {
		int count = SocketServer.sendClientCommand(exp, "exit");
		return count + "client shutdown";
	}

	/**
	 * 状态
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/stat")
	public String stat() throws Exception {
		long i = 0;
		StringBuffer sb = new StringBuffer("<pre>");
		Map<String, ObjectNode> sessionMap = SocketServer.getAllSessionInfo();
		Set<Entry<String, ObjectNode>> sessionEntrySet = sessionMap.entrySet();
		for (Entry<String, ObjectNode> entry : sessionEntrySet) {
			sb.append(i + "\t" + entry.getKey() + " \t" + entry.getValue()
					+ "<a href='restart_client?exp=" + entry.getKey()
					+ "'>restart</a> \t <a href='shutdown_client?exp="
					+ entry.getKey() + "'>shutdown</a> \r\n");
			i++;
		}
		i = 0;
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		Set<Entry<Long, ObjectNode>> entrySet = taskMap.entrySet();
		for (Entry<Long, ObjectNode> entry : entrySet) {
			ObjectNode task = entry.getValue();
			sb.append("\t" + entry.getKey() + "\t" + task + " \t"
					+ DataManager.getStatString(task.get("status").asInt())
					+ "\r\n");
			i++;
		}
		sb.append("</pre>");
		return sb.toString();
	}

	/**
	 * 获取任务日志
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "admin/task_log")
	public String taskLog() throws Exception {
		return PropertyTool.readFileAsString(DataManager.getLogFile());
	}

	@HttpComment(uri = "test/test")
	public String test() {
		return "<html><head><title>miao</title></head><body>ok</body></html>";
	}
}
