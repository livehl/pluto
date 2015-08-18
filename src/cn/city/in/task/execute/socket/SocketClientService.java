package cn.city.in.task.execute.socket;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.SystemTool;
import cn.city.in.task.execute.log.ExcuteMemAppender;

/**
 * 静态类，提供服务器socket对象，连接socket对象组，相应线程对象
 * 
 * @author 黄林
 * 
 */
public class SocketClientService {
	private static ObjectMapper mapper = new ObjectMapper();
	private static Logger log = Logger.getLogger(SocketClientService.class);
	public static Socket socket;
	private static String serviceIp;
	private static int servicePort;
	private static BufferedOutputStream out;
	private static int writeCount = 0;

	public static boolean connect() {
		try {
			socket = new Socket(serviceIp, servicePort);
			// 1M的缓冲区
			socket.setReceiveBufferSize(1024 * 1024);
			// 1M的写缓冲
			out = new BufferedOutputStream(socket.getOutputStream(),
					1024 * 1024);
			write(getClientStatus());
			log.info("connect service ok:" + System.currentTimeMillis());
			return true;
		} catch (IOException e) {
			log.warn("connect server:" + socket.getInetAddress() + " faile:"
					+ e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 刷入缓冲的数据
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @author 黄林 Flush.
	 */
	public static synchronized void flush() throws IOException {
		out.flush();
		writeCount = 0;
	}

	/**
	 * 获取客户端日志
	 * 
	 * @return the client log
	 * @author 黄林
	 */
	public static JsonNode getClientLog() {
		ObjectNode status = JsonTool.createNewObjectNode();
		status.put("type", "log");
		status.put("data", ExcuteMemAppender.getLogString());
		return status;
	}

	/**
	 * 获取客户端状态信息
	 * 
	 * @return the client status
	 * @author 黄林
	 */
	public static JsonNode getClientStatus() {
		ObjectNode status = JsonTool.createNewObjectNode();
		status.put("type", "status");
		ObjectNode data = SystemTool.getLocalSystemInfo();
		data.put("client_type", "execute");
		status.put("data", data);
		return status;
	}

	/**
	 * 功能:初始化socket服务器，监听端口 创建者： 黄林 2011-11-4.
	 */
	public static void init(String ip, int port) {
		serviceIp = ip;
		servicePort = port;
		if (connect()) {
			log.info(" connect service ok");
		} else {
			log.warn("connect server:" + ip + " faile");
		}
	}

	/**
	 * 重新连接服务器
	 * 
	 * @author 黄林 Reconnect.
	 */
	public static boolean reconnect() {
		try {
			socket.close();
		} catch (Exception e) {
		}
		return connect();
	}

	/**
	 * 刷出所有缓存并关闭连接
	 * 
	 * @author 黄林 Shutdown.
	 */
	public static void shutdown() {
		try {
			flush();
			socket.close();
		} catch (IOException e) {
		}
	}

	/**
	 * 发送数据至服务器缓存
	 * 
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Write.
	 */
	public static synchronized void write(JsonNode context) throws IOException {
		ByteArrayOutputStream baout = new ByteArrayOutputStream();
		mapper.writeValue(baout, context);
		byte[] datas = baout.toByteArray();
		int length = datas.length;
		// 我了个艹
		out.write((length >>> 24) & 0xFF);
		out.write((length >>> 16) & 0xFF);
		out.write((length >>> 8) & 0xFF);
		out.write((length >>> 0) & 0xFF);
		out.write(datas);
		// IO线程每10ms刷入一次，如果写入超过线程设置也刷入
		writeCount++;
		if (writeCount >= 20*Runtime.getRuntime().availableProcessors()) {
			flush();
		}
	}
}
