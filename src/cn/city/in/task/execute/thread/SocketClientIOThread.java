package cn.city.in.task.execute.thread;

import java.io.DataInputStream;
import java.io.File;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.ReflectTool;
import cn.city.in.api.tools.common.StringTool;
import cn.city.in.api.tools.common.SystemTool;
import cn.city.in.api.tools.task.TaskTool;
import cn.city.in.task.execute.ExecuteMain;
import cn.city.in.task.execute.distributed.DistributedList;
import cn.city.in.task.execute.socket.SocketClientService;
import cn.city.in.task.execute.task.TaskList;

/**
 * 功能:连接线程，处理每个服务端的请求
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class SocketClientIOThread extends Thread {
	private static Logger log = Logger.getLogger(SocketClientIOThread.class);
	private static ObjectMapper mapper = new ObjectMapper();
	private boolean live = true;

	public SocketClientIOThread() {
		setDaemon(true);
	}

	/**
	 * 关闭线程
	 * 
	 * @author 黄林 Die.
	 */
	public void die() {
		live = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			Socket socket = SocketClientService.socket;
			DataInputStream input = new DataInputStream(socket.getInputStream());
			int i = 0;
			String type;
			while (live) {
				i++;
				// 刷入一次缓存
				SocketClientService.flush();
				if (i % 500 == 0) {
					// 每隔5秒发送一个状态，防止连接断开
					SocketClientService.write(SocketClientService
							.getClientStatus());
				}
				if (i == 6000) {
					// 每隔分钟发送一个日志记录
					SocketClientService.write(SocketClientService
							.getClientLog());
					i = 0;
				}
				// 至少要有长度和时间
				if (input.available() > 4) {
					// 读取长度
					// byte[] lengthBytes = new byte[4];
					// input.read(lengthBytes);
					int length = input.readInt();
					// (int) (lengthBytes[0] & 0xff
					// | (lengthBytes[1] & 0xff) << 8
					// | (lengthBytes[2] & 0xff) << 16 | (lengthBytes[3] & 0xff)
					// << 24);
					// 时间戳已经从协议中移除
					// //读取时间
					// byte[] timeBytes=new byte[8];
					// input.read(timeBytes);
					// //艹蛋的货，没直接转的
					// long time=(int)(timeBytes[0]&0xff |
					// (timeBytes[1]&0xff)<<8 | (timeBytes[2]&0xff)<<16 |
					// (timeBytes[3]&0xff)<<24|timeBytes[4]<<32|timeBytes[5]<<40|timeBytes[6]<<48|timeBytes[7]<<56);
					// 读取所有数据
					byte[] dataBytes = new byte[length];
					for (int j = 0; j < dataBytes.length; j++) {
						dataBytes[j] = input.readByte();
					}
					JsonNode message = mapper.readValue(dataBytes,
							JsonNode.class);
					log.debug(message);
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
					if ("control".equals(type)) {
						JsonNode data = message.get("data");
						String command = data.get("command").asText();
						// 重启
						if ("reboot".equals(command)
								|| "restart".equals(command)) {
							ExecuteMain.setReboot(true);
						}
						if ("exit".equals(command) || "quit".equals(command)
								|| "reboot".equals(command)
								|| "restart".equals(command)) {
							log.info("push " + command + " from "
									+ socket.getRemoteSocketAddress());
							socket.close();
							// 不能直接关闭
							TaskTool.createTimeTask(
									1,
									10l,
									ReflectTool.getMethod(System.class, "exit"),
									System.class, 0);
							return;
						}
						if ("start".equals(command)) {
							log.info("push " + command + " from "
									+ socket.getRemoteSocketAddress());
							SystemTool.start();
							continue;
						}
						if ("flushLog".equals(command)) {
							SocketClientService.write(SocketClientService
									.getClientLog());
							i = 0;
						}
						if ("updata".equals(command)
								|| "saveFile".equals(command)) {
							// 升级文件
							String path = data.get("path").asText();

							File updataFile = FileTool.getClassPathFile(path);
							FileTool.saveByte(updataFile, data.get("fileData")
									.getBinaryValue());
						}
						if ("updata".equals(command)) {
							// 重启
							ExecuteMain.setReboot(true);
							log.info("updata... ");
							socket.close();
							// 不能直接关闭
							TaskTool.createTimeTask(
									1,
									10l,
									ReflectTool.getMethod(System.class, "exit"),
									System.class, 0);
						}
					}
					if (type.equals("task")) {
						// 添加新的任务
						TaskList.addTask(message.get("data"));
					} else if (type.equals("mds")) {
						// 添加新的分布式任务
						DistributedList.addTask(message.get("data"));
					} else {
						log.debug("unknow command :" + type);
					}
				}
				Thread.sleep(10);
			}
		} catch (Exception e) {
			log.error("exit on error" + e.getMessage(), e);
			if (live) {
				// 尝试重新连接服务器
				try {
					TaskTool.createTimeTask("reconnect",
							SocketClientService.class);
					SocketClientIOThread clientThread = new SocketClientIOThread();
					TaskTool.createTimeTask(100L, "run", clientThread,
							new Object[0]);
					ExecuteMain.setClientThread(clientThread);
				} catch (Exception e1) {
					log.error("fail to reconnect service", e1);
				}
			}
		}
	}
}
