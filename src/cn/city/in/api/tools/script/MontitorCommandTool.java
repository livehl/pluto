package cn.city.in.api.tools.script;

import cn.city.in.api.tools.common.StringTool;
import cn.city.in.api.tools.monitor.MonitorTool;

/**
 * 功能:状态远程命令
 * 
 * @author 黄林 2011-11-7
 * @version
 */
public class MontitorCommandTool {

	private static String addContext(String key, Long value) {
		return value + "\t=" + key + "\r\n";
	}

	/**
	 * 功能:执行命令 创建者： 黄林 2011-11-7.
	 * 
	 * @param command
	 *            the command
	 * @return string
	 */
	public static String execCommand(String command) {
		String result = "";
		if ("all".equals(command)) {
			StringBuffer sb = new StringBuffer(result);
			for (String key : MonitorTool.getAllKeys()) {
				sb.append(addContext(key, MonitorTool.getValue(key)));
			}
			result = sb.toString();
		} else if (MonitorTool.monitorMap.containsKey(command)) {
			result = addContext(command, MonitorTool.getValue(command));
		} else {
			StringBuffer sb = new StringBuffer();
			for (String key : MonitorTool.getAllKeys()) {
				if (key.indexOf(command) != -1
						|| StringTool.matche(key, command, true, true)) {
					sb.append(addContext(key, MonitorTool.getValue(key)));
				}
			}
			result = sb.toString();
			if (result.equals("")) {
				result = "unknown command:" + command;
			}
		}
		return result;
	}
}
