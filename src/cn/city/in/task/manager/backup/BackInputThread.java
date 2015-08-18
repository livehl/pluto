package cn.city.in.task.manager.backup;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.api.tools.common.SystemTool;
import cn.city.in.task.manager.data.DataManager;

/**
 * 功能:连接线程，处理每个服务端的请求
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class BackInputThread extends Thread {
	private static Logger log = Logger.getLogger(BackInputThread.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private String serverIP;
	private int port;
	private Socket socket;
	private boolean live = true;

	public BackInputThread(String serverIP, int port) {
		setDaemon(true);
		this.serverIP = serverIP;
		this.port = port;
		log.info("connect main master:" + serverIP + ":" + port);
	}

	/**
	 * 关闭线程
	 * 
	 * @author 黄林 Die.
	 */
	public void die() {
		live = false;
	}

	public JsonNode getClientStatus() {
		ObjectNode status = JsonTool.createNewObjectNode();
		status.put("type", "status");
		ObjectNode data = SystemTool.getLocalSystemInfo();
		data.put("client_type", "backup");
		status.put("data", data);
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			int i = 50;
			String type;
			while (live) {
				try {
					if (null == socket) {
						socket = new Socket(serverIP, port);
						socket.setReceiveBufferSize(1024 * 1024);
					}
					DataInputStream input = new DataInputStream(
							socket.getInputStream());
					i++;
					if (i % 50 == 0) {
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
						type = message.get("type").asText();
						log.debug(type);
						if ("status".equals(type)) {
							// 如果觉得服务器的状态消息有用，可以取出来
							continue;
						} else if (StringTool.isNull(type)) {
							continue;
						}
						log.debug("push data from "
								+ socket.getRemoteSocketAddress() + " :"
								+ message.toString());
						if (type.equals("backup")) {
							// 任务状态变更
							log.info("revice backup data");
							ObjectNode data = (ObjectNode) message.get("data");
							DataManager.taskChange(data,
									data.get("backup_status").asInt());
						} else {
							log.debug("unknow command :" + type);
						}
					}
				} catch (IOException e) {
					socket = null;
					log.warn(e.getMessage(), e);
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			log.error("exit on error" + e.getMessage(), e);
		}
	}

	private synchronized void write(JsonNode context) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		mapper.writeValue(out, context);
		byte[] datas = out.toByteArray();
		int length = datas.length;
		socket.getOutputStream().write((length >>> 24) & 0xFF);
		socket.getOutputStream().write((length >>> 16) & 0xFF);
		socket.getOutputStream().write((length >>> 8) & 0xFF);
		socket.getOutputStream().write((length >>> 0) & 0xFF);
		socket.getOutputStream().write(datas);
		socket.getOutputStream().flush();
	}
}
