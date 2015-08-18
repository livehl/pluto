package cn.city.in.api.tools.script;

import cn.city.in.api.tools.task.TaskTool;

/**
 * 功能:任务远程命令
 * 
 * @author 黄林 2011-11-7
 * @version
 */
public class TaskCommandTool {

	/**
	 * 功能:执行命令 创建者： 黄林 2011-11-7.
	 * 
	 * @param command
	 *            the command
	 * @return string
	 */
	public static String execCommand(String param) {
		String command = param.split(",")[0];
		String result = "";
		if ("reload".equals(command)) {
			result = result + "reload task count" + TaskTool.reload();
		} else if ("create".equals(command)) {
			String[] params = new String[param.split(",").length - 1];
			for (int i = 1; i < param.split(",").length; i++) {
				params[i - 1] = param.split(",")[i];
			}
			result = result + "create task " + TaskTool.createTask(params)
					+ " ok";
		} else {
			result = "unknown command:" + command;
		}
		if ("".equals(result)) {
			result = "No data";
		}
		return result;
	}
}
