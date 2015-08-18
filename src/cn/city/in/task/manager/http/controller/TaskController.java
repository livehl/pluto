package cn.city.in.task.manager.http.controller;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.task.manager.data.DataManager;
import cn.city.in.task.manager.data.PlutoObjectNode;
import cn.city.in.task.manager.http.comment.HttpComment;
import cn.city.in.task.manager.http.comment.HttpParam;

/**
 * 任务相关
 * 
 * @author 黄林 The Class TaskController.
 */
public class TaskController extends BaseController {

	/**
	 * 添加任务
	 * 
	 * @param task
	 *            the task
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "task/addtask")
	public String addtask(
			@HttpParam(value = "task", required = true) String taskString)
			throws Exception {
		ObjectNode taskObject = JsonTool.readJson(taskString);
		PlutoObjectNode task = new PlutoObjectNode();
		;
		task.putAll(taskObject);
		return DataManager.addTask(task).get("id").asText();
	}

	/***
	 * 添加批量任务
	 * 
	 * @param taskString
	 * @return
	 * @throws Exception
	 */
	@HttpComment(uri = "task/addtask_batch")
	public Integer addTaskBatch(
			@HttpParam(value = "task", required = true) String taskString)
			throws Exception {
		ArrayNode arrayNode = JsonTool.jsonToType(taskString, ArrayNode.class);
		for (JsonNode node : arrayNode) {
			PlutoObjectNode task = new PlutoObjectNode();
			task.putAll((ObjectNode) node);
			DataManager.addTask(task);
		}
		return arrayNode.size();
	}

	/**
	 * 查询任务状态
	 * 
	 * @param taskid
	 *            the task
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "task/query")
	public String querytask(
			@HttpParam(value = "taskid", required = true) Long taskid)
			throws Exception {
		ObjectNode task = DataManager.getTaskMapClone().get(taskid);
		if (null != task) {
			return task + ","
					+ DataManager.getStatString(task.get("status").asInt());
		}
		return "-1:" + "ok OR remove";
	}

	/**
	 * 列出所有的任务
	 * 
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	@HttpComment(uri = "task/")
	public String stat() throws Exception {
		StringBuffer sb = new StringBuffer("<pre>");
		Map<Long, ObjectNode> taskMap = DataManager.getTaskMapClone();
		Set<Entry<Long, ObjectNode>> entrySet = taskMap.entrySet();
		for (Entry<Long, ObjectNode> entry : entrySet) {
			ObjectNode task = entry.getValue();
			sb.append("\t" + entry.getKey() + "\t" + task + " \t"
					+ DataManager.getStatString(task.get("status").asInt())
					+ "\r\n");
		}
		sb.append("</pre>");
		return sb.toString();
	}
}
