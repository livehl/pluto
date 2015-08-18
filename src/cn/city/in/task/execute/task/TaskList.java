package cn.city.in.task.execute.task;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

public class TaskList {
	private static List<JsonNode> taskList = new ArrayList<JsonNode>();

	/**
	 * 添加一个任务
	 * 
	 * @param task
	 *            the task
	 * @author 黄林 Adds the task.
	 */
	public static void addTask(JsonNode task) {
		synchronized (taskList) {
			taskList.add(task);
		}
	}

	/**
	 * 获取一个任务
	 * 
	 * @return the task
	 * @author 黄林
	 */
	public synchronized static JsonNode getTask() {
		if (taskList.size() > 0) {
			synchronized (taskList) {
				JsonNode task = taskList.get(0);
				taskList.remove(0);
				return task;
			}
		}
		return null;

	}

}
