package cn.city.in.api.tools.socket;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import cn.city.in.api.mvc.controller.BaseController;
import cn.city.in.api.tools.common.ExceptionTool;
import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.Tool;
import cn.city.in.api.tools.script.MethodScriptTool;
import cn.city.in.api.tools.script.MontitorCommandTool;
import cn.city.in.api.tools.script.TaskCommandTool;
import cn.city.in.api.tools.task.TaskTool;

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
		log.warn(
				"client " + session.getRemoteAddress() + " on error"
						+ e.getMessage(), e);
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		Integer authenticate = (Integer) session.getAttribute("authenticate");
		if (null == authenticate || authenticate == 0) {
			// 登录
			if (PropertyTool.getProperties("socket_pwd").equals(message)) {
				session.write("welcome servie");
				log.info("client :" + session.getRemoteAddress()
						+ " authenticate ok");
				session.setAttribute("authenticate", 1);
				return;
			} else {
				log.debug("client :" + session.getRemoteAddress()
						+ " authenticate fail");
				session.write("非法访问");
				session.close(false);
				return;
			}
		}
		// 在这里进行消息推送，消息来源可以从数据库取或者通过其他方式(例如application)获得，再推送给客户端
		String command = (String) message;
		log.debug("client from" + session.getRemoteAddress()
				+ " execution command:" + command);
		if ("exit".equals(command) || "quit".equals(command)) {
			session.close(true);
			return;
		}
		String[] commands = command.split(":");
		if ("status".equals(commands[0])) {
			String arg = "all";
			if (commands.length > 1) {
				arg = commands[1];
			}
			session.write(MontitorCommandTool.execCommand(arg));
		} else if ("task".equals(commands[0])) {
			if (commands.length < 2) {
				session.write(("task:obj..."));
			} else {
				session.write(TaskCommandTool.execCommand(commands[1]));
			}
		} else if ("debug".equals(commands[0])) {
			session.write(ExceptionTool.getAllStackTraces());
		} else if ("request".equals(commands[0])) {
			session.write(Tool
					.getLocCache(BaseController.REQUEST_COUNT_CACHE_KEY));
		} else if ("live".equals(commands[0])) {
			session.write(Tool
					.getLocCache(BaseController.USER_LIVE_COUNT_CACHE_KEY));
			// } else if ("shutdown".equals(commands[0])) {
			// session.close(false);
			// TaskTool.createTimeTask(1,10l,ReflectTool.getMethod(System.class,
			// "exit"),System.class, 0);
		} else if ("help".equals(commands[0])) {
			session.write(PropertyTool.readFileAsString(FileTool
					.getClassPathFile("classpath:config/socket.message")));
			StringBuffer sb = new StringBuffer();
			Set<Entry<String, String>> entrySet = TaskTool.getShortName()
					.entrySet();
			for (Entry<String, String> entry : entrySet) {
				sb.append(entry.getKey() + "=" + entry.getValue() + "\r\n");
			}
			session.write(sb.toString());
		} else if ("method".equals(commands[0])) {
			if (commands.length < 2) {
				session.write(("method:obj..."));
			} else {
				session.write(MethodScriptTool.execCommand(commands[1]));
			}
		} else {// 默认直接当方法执行
			session.write(MethodScriptTool.execFastCommand(commands[0]));
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		log.info("client from" + session.getRemoteAddress() + " exit");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		session.setAttribute("authenticate", 0);
		log.debug("client " + session.getRemoteAddress() + " connect");
		session.write("password:");
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status)
			throws Exception {
		log.debug("client from" + session.getRemoteAddress() + " idle");
	}
}
