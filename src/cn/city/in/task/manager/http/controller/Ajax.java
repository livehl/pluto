package cn.city.in.task.manager.http.controller;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.ReflectTool;
import cn.city.in.api.tools.common.SystemTool;
import cn.city.in.api.tools.common.TimeTool;
import cn.city.in.api.tools.task.TaskTool;
import cn.city.in.task.manager.data.DataManager;
import cn.city.in.task.manager.http.HttpManager;
import cn.city.in.task.manager.http.codec.HttpFile;
import cn.city.in.task.manager.http.comment.HttpComment;
import cn.city.in.task.manager.http.comment.HttpParam;
import cn.city.in.task.manager.socket.SocketServer;
import cn.city.in.task.manager.task.TaskManager;

public class Ajax extends BaseController {

	private static Logger log = Logger.getLogger(Ajax.class);

	/**
	 * 清理执行错误的任务
	 * 
	 * @return the 清理的id列表
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/clean_fail")
	public JsonNode cleanFail() throws Exception {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		Set<Entry<Long, ObjectNode>> entrySet = taskMap.entrySet();
		for (Entry<Long, ObjectNode> entry : entrySet) {
			ObjectNode task = entry.getValue();
			int status = task.get("status").asInt();
			if (status == DataManager.TASK_STATUS_FAIL) {
				array.add(task.get("id"));
				DataManager.taskChange(task, DataManager.TASK_STATUS_REMOVE);
			}
		}
		return array;
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
	@HttpComment(uri = "ajax/client_command")
	public Integer clientCommand(
			@HttpParam(value = "exp", required = false, defaultValue = "*") String exp,
			@HttpParam(value = "command", required = true) String command)
			throws Exception {
		return SocketServer.sendClientCommand(exp, command);
	}

	/**
	 * 列出所有ip一样的客户端
	 * 
	 * @return the json node
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/client_computers")
	public JsonNode clientComputers() {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<String, ObjectNode> sessionSet = SocketServer.getAllSessionInfo();
		// 列出所有的客户端ip
		Map<String, ObjectNode> ipMap = new HashMap<String, ObjectNode>();
		for (String key : sessionSet.keySet()) {
			String ip = key;
			ip = ip.substring(1);
			ip = ip.substring(0, ip.indexOf(":"));
			if (ipMap.containsKey(ip)) {
				ObjectNode client = ipMap.get(ip);
				client.put("count", client.get("count").asInt() + 1);
				ipMap.put(ip, client);
			} else {
				ObjectNode client = new ObjectNode(JsonNodeFactory.instance);
				client.put("id", ip);
				client.put("count", 1);
				ipMap.put(ip, client);
			}
		}
		for (String key : ipMap.keySet()) {
			array.add(ipMap.get(key));
		}
		return array;
	}

	/**
	 * 客户端日志
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/client_log")
	public JsonNode clientLog(
			@HttpParam(value = "exp", required = false, defaultValue = "*") String exp)
			throws Exception {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<String, ObjectNode> sessionMap = SocketServer
				.getAllExecuteSessionLog();
		Set<Entry<String, ObjectNode>> sessionEntrySet = sessionMap.entrySet();
		for (Entry<String, ObjectNode> entry : sessionEntrySet) {
			array.add(entry.getValue());
		}
		return array;
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
	@HttpComment(uri = "ajax/client_save_file")
	public Integer clientSaveFile(
			@HttpParam(value = "file", required = true) HttpFile file,
			@HttpParam(value = "path", required = true) String path)
			throws Exception {
		return SocketServer.clientSaveFile(path, file.getData());
	}

	/**
	 * 客户端状态
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/client_stat")
	public JsonNode clientStat(
			@HttpParam(value = "exp", required = false, defaultValue = "*") String exp)
			throws Exception {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<String, ObjectNode> sessionMap = SocketServer.getAllSessionInfo();
		Set<Entry<String, ObjectNode>> sessionEntrySet = sessionMap.entrySet();
		for (Entry<String, ObjectNode> entry : sessionEntrySet) {
			array.add(entry.getValue());
		}
		return array;
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
	@HttpComment(uri = "ajax/client_updata")
	public Integer clientUpdata(
			@HttpParam(value = "file", required = true) HttpFile file,
			@HttpParam(value = "path", required = true) String path)
			throws Exception {
		return SocketServer.clientUpdata(path, file.getData());
	}

	/**
	 * 强制关闭客户端
	 * 
	 * @param exp
	 * @return
	 */
	@HttpComment(uri = "ajax/close_client")
	public Integer closeClient(
			@HttpParam(value = "exp", required = false, defaultValue = "*") String exp) {
		return SocketServer.closeShutdown(exp);
	}

	@Override
	public Object doAfter(Object body) throws Exception {
		ObjectNode root = createAjaxMessage();
		if (body instanceof String) {
			root.put("data", (String) body);
		} else if (body instanceof Integer) {
			root.put("data", (Integer) body);
		} else if (body instanceof Double) {
			root.put("data", (Double) body);
		} else if (body instanceof Float) {
			root.put("data", (Float) body);
		} else if (body instanceof JsonNode) {
			root.put("data", (JsonNode) body);
		} else if (body instanceof Map) {
			ObjectNode map = createAjaxMessage();
			map.putAll((Map) body);
			root.put("data", map);
		} else {
			root.put("data", JsonTool.toJson(body));
		}
		return root;
	}

	@HttpComment(uri = "ajax/get_fail_task")
	public JsonNode getFailTask() throws Exception {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		Set<Entry<Long, ObjectNode>> entrySet = taskMap.entrySet();
		for (Entry<Long, ObjectNode> entry : entrySet) {
			ObjectNode task = entry.getValue();
			int status = task.get("status").asInt();
			if (status == DataManager.TASK_STATUS_FAIL) {
				array.add(task);
			}
		}
		return array;
	}

	/***
	 * 任务head状态
	 * 
	 * @return
	 */
	@HttpComment(uri = "ajax/task_head_list")
	public JsonNode headList() {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<String, Integer> map = DataManager.getHeadStatusClone();
		for (Entry<String, Integer> headStatus : map.entrySet()) {
			ObjectNode head = new ObjectNode(JsonNodeFactory.instance);
			head.put("name", headStatus.getKey());
			head.put("status", headStatus.getValue());
			array.add(head);
		}
		return array;
	}

	@HttpComment(uri = "ajax/task_head_pause")
	public String headPause(
			@HttpParam(value = "head", required = true) String head) {
		if (DataManager.changeHeadStatus(head,
				DataManager.TASK_HEAD_STATUS_PAUSE)) {
			return "true";
		} else {
			return "false";
		}
	}

	@HttpComment(uri = "ajax/task_head_recovery")
	public String headRecovery(
			@HttpParam(value = "head", required = true) String head) {
		if (DataManager.changeHeadStatus(head,
				DataManager.TASK_HEAD_STATUS_NORMAL)) {
			return "true";
		} else {
			return "false";
		}
	}

	/**
	 * 系统信息
	 * 
	 * @return the json node
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/manager_stat")
	public JsonNode managerStat() {
		ObjectNode info = new ObjectNode(JsonNodeFactory.instance);
		info.put("startTime",
				TimeTool.getFormatStringByDate(new Date(HttpManager.startTime)));
		info.putAll(SystemTool.getLocalSystemInfo());
		return info;
	}

	/**
	 * 升级master文件
	 * 
	 * @param file
	 *            the file
	 * @param path
	 *            the path
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/master_save_file")
	public String masterSaveFile(
			@HttpParam(value = "file", required = true) HttpFile file,
			@HttpParam(value = "path", required = true) String path)
			throws Exception {
		File updataFile = FileTool.getClassPathFile(path);
		FileTool.saveByte(updataFile, file.getData());
		return "ok";
	}

	/**
	 * 重新发送任务至客户端
	 * 
	 * @param exp
	 *            the exp
	 * @return the 发送的id列表
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/resend_task")
	public JsonNode reSendTask(@HttpParam(value = "id", required = true) Long id)
			throws Exception {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		TaskManager.sendTask(taskMap.get(id));
		DataManager.taskChange(taskMap.get(id), DataManager.TASK_STATUS_RESEND);
		array.add(id);
		return array;
	}

	@HttpComment(uri = "ajax/restart")
	public String restart() {
		log.info("restart clients");
		SocketServer.sendClientCommand("*", "reboot");
		log.info("restart manager");
		SystemTool.start();
		TaskTool.createTimeTask(1, 1l,
				ReflectTool.getMethod(System.class, "exit"), System.class, 0);
		return "ok";
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
	@HttpComment(uri = "ajax/restart_client")
	public Integer restartClient(
			@HttpParam(value = "exp", required = false, defaultValue = "*") String exp)
			throws Exception {
		return SocketServer.sendClientCommand(exp, "reboot");
	}

	/**
	 * 停止服务
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/shutdown")
	public String shutdown() throws Exception {
		log.info("shutdown manager");
		// 不能直接关闭,会与停机方法死锁
		TaskTool.createTimeTask(1, 10l,
				ReflectTool.getMethod(System.class, "exit"), System.class, 0);
		return "ok";
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
	@HttpComment(uri = "ajax/shutdown_client")
	public Integer shutdownClient(
			@HttpParam(value = "exp", required = false, defaultValue = "*") String exp)
			throws Exception {
		return SocketServer.sendClientCommand(exp, "exit");
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
	@HttpComment(uri = "ajax/start_client")
	public Integer startClient(
			@HttpParam(value = "exp", required = false, defaultValue = "*") String exp)
			throws Exception {
		return SocketServer.sendClientCommand(exp, "start");
	}

	/**
	 * 任务日志
	 * 
	 * @return the json node
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/task_log")
	public String taskLog() throws Exception {
		return PropertyTool.readFileAsString(DataManager.getLogFile());
	}

	/**
	 * 任务状态
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "ajax/task_stat")
	public JsonNode taskStat() throws Exception {
		ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		Set<Entry<Long, ObjectNode>> entrySet = taskMap.entrySet();
		for (Entry<Long, ObjectNode> entry : entrySet) {
			array.add(entry.getValue());
		}
		return array;
	}
}
