package cn.city.in.api.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.api.tools.common.Tool;
/**
 * 分布式任务系统工具类
 * 
 * @author 黄林 The Class PlutoTool.
 */
public class PlutoTool {
	private static Logger log = Logger.getLogger(PlutoTool.class);
	private static ObjectMapper mapper = new ObjectMapper();
	public static Socket socket;
	private static String serviceIp;
	private static int servicePort;
	private static PlutoReceiveThread prt;

	/**
	 * 发送任务
	 * 
	 * @param fast
	 *            是否为快速发送模式
	 * @param task
	 *            the task
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	private static String sendTask(ObjectNode task)
			throws Exception {
			sendDataBySocket(task);
			return null;
	}

	/**
	 * 发送任务
	 * 
	 * @param fast
	 *            the fast
	 * @param head
	 *            the head
	 * @param data
	 *            the data
	 * @return the string
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static String sendTask(String head, ObjectNode data)
			throws Exception {
		ObjectNode task = JsonTool.createNewObjectNode();
		task.put("head", head);
		task.put("data", data);
		return sendTask(task);
	}

	/**
	 * Send data by socket. 注意，此方法是给定时任务调用的，请使用 public String
	 * sendTask(boolean,String,ObjectNode) 方法发送任务
	 * 
	 * @param task
	 *            the task
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Send data by socket.
	 */
	private static void sendDataBySocket(ObjectNode task) throws Exception {
		ObjectNode transferMessage = new ObjectNode(JsonNodeFactory.instance);
		transferMessage.put("type", "task");
		transferMessage.put("data", task);
		transferMessage.put("status", "add");
		if (null == socket) {
			init();
		}
		try {
			write(transferMessage);
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
			reconnect();
			write(transferMessage);
		}

	}

	/**
	 * 功能:初始化socket服务器，监听端口 创建者： 黄林 2011-11-4.
	 */
	public static void init() {
		try {
			serviceIp = PropertyTool.getProperties("pluto_ip");
			servicePort = PropertyTool.getNumProperties("pluto_port")
					.intValue();
			socket = new Socket(serviceIp, servicePort);
			socket.setReceiveBufferSize(1024 * 1024);
			write(getClientStatus());
			//启动读取线程
			prt=new PlutoTool().new PlutoReceiveThread();
			prt.start();
			log.info(" connect service ok");
		} catch (IOException e) {
			log.warn(
					"connect server:" + serviceIp + " faile:" + e.getMessage(),
					e);
		}
	}

	/**
	 * 刷出所有缓存并关闭连接
	 * 
	 * @author 黄林 Shutdown.
	 */
	public static void shutdown() {
		try {
			socket.getOutputStream().flush();
			socket.close();
		} catch (IOException e) {
		}
	}

	/**
	 * 重新连接服务器
	 * 
	 * @author 黄林 Reconnect.
	 */
	public static boolean reconnect() {
		try {
			try {
				socket.close();
			} catch (Exception e) {
			}
			socket = new Socket(serviceIp, servicePort);
			socket.setReceiveBufferSize(2048);
			write(getClientStatus());
			log.info("reconnect service ok");
			return true;
		} catch (IOException e) {
			log.warn("reconnect server:" + socket.getInetAddress() + " faile:"
					+ e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 发送数据至服务器
	 * 
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 * @author 黄林 Write.
	 */
	public static synchronized void write(JsonNode context) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, context);
		byte[] datas = out.toByteArray();
		int length = datas.length;
		// 我了个艹
		socket.getOutputStream().write((length >>> 24) & 0xFF);
		socket.getOutputStream().write((length >>> 16) & 0xFF);
		socket.getOutputStream().write((length >>> 8) & 0xFF);
		socket.getOutputStream().write((length >>> 0) & 0xFF);
		socket.getOutputStream().write(datas);
		socket.getOutputStream().flush();
	}

	/**
	 * 获取客户端状态信息
	 * 
	 * @return the client status
	 * @author 黄林
	 */
	public static JsonNode getClientStatus() {
		ObjectNode status = new ObjectNode(JsonNodeFactory.instance);
		status.put("type", "status");
		ObjectNode data = new ObjectNode(JsonNodeFactory.instance);
		data.put("client_type", "commit");
		data.put("cpuCount", Runtime.getRuntime().availableProcessors());
		data.put("path",System.getProperty("user.dir"));
		status.put("data", data);
		return status;
	}
	protected class PlutoReceiveThread extends Thread {
		
		public PlutoReceiveThread() {
			super();
		}

		private boolean live = true;

		@Override
		public void run() {
			int i = 0;
			while (live) {
				try {
					Socket socket = PlutoTool.socket;
					if (null==socket) {
						if (!reconnect()) {
							Thread.sleep(500);
							continue;
						}
					}
					DataInputStream input = new DataInputStream(
							socket.getInputStream());
					i++;
					if (i % 500 == 0) {
						// 每隔5秒发送一个状态，防止连接断开
						write(getClientStatus());
					}
					// 至少要有长度和时间
					if (input.available() > 4) {
						// 读取长度
						int length = input.readInt();
						// 读取所有数据
						byte[] dataBytes = new byte[length];
						for (int j = 0; j < dataBytes.length; j++) {
							dataBytes[j] = input.readByte();
						}
						JsonNode message = mapper.readValue(dataBytes,
								JsonNode.class);
						String type = message.get("type").asText();
						log.debug(type);
						if ("status".equals(type)) {
							continue;
						} else if (Tool.isNull(type)) {
							continue;
						}
						log.debug("push data from "
								+ socket.getRemoteSocketAddress() + " :"
								+ message.toString());
						if ("control".equals(type)) {
							// 屏蔽控制指令
						} else if (type.equals("mds")) {
							// 屏蔽分布式相关的东西							
						} else {
							log.debug("unknow command :" + type);
						}
					}
					Thread.sleep(10);

				} catch (IOException ex) {
					if (live) {
						PlutoTool.socket=null;
					}
				} catch (Exception e) {
					log.error("exit on error" + e.getMessage(), e);
				}
			}
		}

	}
}
