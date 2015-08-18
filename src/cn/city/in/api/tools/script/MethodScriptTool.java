package cn.city.in.api.tools.script;

import cn.city.in.api.tools.common.ReflectTool;
import cn.city.in.api.tools.task.TaskTool;

/**
 * 功能:脚本转方法，并执行
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class MethodScriptTool {
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
		if ("run".equals(command)) {
			String[] oldParams = param.split(",");
			if (oldParams.length<3){
				result=oldParams[1]+"no equal min command:  class,method";
			}else{
				String[] params = new String[oldParams.length - 3];
				for (int i = 3; i < oldParams.length; i++) {
					params[i - 3] = oldParams[i];
				}
				try {
					if (null == TaskTool.dealShortName(oldParams[1])) {
						result = result
								+ ReflectTool.execMethodScript(oldParams[1],
										oldParams[2], params);
					} else {
						result = result
								+ ReflectTool.execMethodScript(
										TaskTool.dealShortName(oldParams[1]),
										oldParams[2], params);
					}
				} catch (Exception e) {
					result = "exec method:" + oldParams[1] + " fail:"
							+ e.getMessage() + " error:" + e.getClass().getName();
				}
			}
		} else if ("show".equals(command)) {
			String[] oldParams = param.split(",");
			try {
				Class clazz = null;
				if (null == TaskTool.dealShortName(oldParams[1])) {
					clazz = Class.forName(oldParams[1]);
				} else {
					clazz = Class.forName(TaskTool.dealShortName(oldParams[1]));
				}
				if (oldParams.length > 2) {// 获取详细方法
					result = ReflectTool.getMethod(clazz, oldParams[2])
							.toGenericString();
				} else {
					String[] methodarray = ReflectTool.getGetMethodNames(clazz);
					StringBuffer sb = new StringBuffer();
					for (String string : methodarray) {
						sb.append(string + "\r\n");
					}
					result = sb.toString();
				}
			} catch (Exception e) {
				result = "show method:" + oldParams[1] + " fail:"
						+ e.getMessage() + " error:" + e.getClass().getName();
			}
		} else {
			result = "unknown command:" + command;
		}
		if (null==result||"".equals(result)) {
			result = "No data return";
		}
		return result;
	}

	/**
	 * 执行快速命令
	 * 
	 * @param param
	 *            the param
	 * @return the string
	 * @author 黄林
	 */
	public static String execFastCommand(String param) {
		String[] oldParams = param.split(",");
		if ("show".equals(oldParams[0])) {
			return execCommand(param);
		} else {
			return execCommand("run," + param);
		}
	}
}
