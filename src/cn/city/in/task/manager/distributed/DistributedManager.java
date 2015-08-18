package cn.city.in.task.manager.distributed;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.ReflectTool;
import cn.city.in.common.DistributedComputationInterface;
import cn.city.in.task.manager.data.DataManager;
import cn.city.in.task.manager.socket.SocketServer;
import cn.city.in.task.manager.task.TaskManager;

public class DistributedManager {

	private static Logger log = Logger.getLogger(TaskManager.class);
	/**
	 * 任务失败，重新计算三次
	 * 
	 * @param session
	 *            the session
	 * @param dis
	 *            the dis
	 * @author 黄林 Do distributed fail.
	 */
	public static void doDistributedFail(IoSession session, ObjectNode dis) {
		log.info("revice fail dis from:"
				+ session.getRemoteAddress().toString());
		int failCount = dis.get("fail_count").asInt();
		failCount++;
		if (failCount < 3) {
			Collection<IoSession> allExecutes = SocketServer
					.getSessionsByClientType(SocketServer.CLIENT_TYPE_EXECUTE);
			for (IoSession ioSession : allExecutes) {
				if (session.getId() != ioSession.getId()) {
					synchronized (ioSession) {
						ioSession.write(dis);
					}
				}
			}
		} else {
			// 记录失败的计算
			long id = dis.get("id").asLong();
			ObjectNode oldDis = DataManager.getDistributed(id);
			((ArrayNode) oldDis.get("fails")).add(dis);
		}
	}

	/**
	 * 任务执行成功
	 * 
	 * @param task
	 *            the task
	 * @author 黄林 Do task ok.
	 * @throws Exception
	 */
	public static void doDistributedOk(IoSession session, ObjectNode dis)
			throws Exception {
		log.info("revice ok dis from:" + session.getRemoteAddress().toString());
		// 判断任务是否已经完成
		long id = dis.get("id").asLong();
		ObjectNode oldDis = DataManager.getDistributed(id);
		int allCount = oldDis.get("split_count").asInt();
		Integer dealCount = oldDis.get("deal_count").asInt();
		dealCount++;
		((ArrayNode) oldDis.get("results")).add(dis.get("result"));
		dis = null;
		if (allCount > dealCount) {
			oldDis.put("deal_count", dealCount);
		} else {
			// 已经处理完毕，首先清理一遍内存
			System.gc();
			// 取出计算结果,并解压
			ArrayNode array = (ArrayNode) oldDis.get("results");
			Object[] resultObjects = new Object[array.size()];
			for (int i = 0; i < array.size(); i++) {
				resultObjects[i] = ReflectTool.getObjectByZipBytes(array.get(i)
						.getBinaryValue());
			}
			// 再次清理内存
			oldDis.remove("results");
			array = null;
			System.gc();
			// 取出DistributedComputationInterface
			byte[] objectData = oldDis.get("obj").getBinaryValue();
			DistributedComputationInterface dci = (DistributedComputationInterface) ReflectTool
					.getObjectByBytes(objectData);
			Object result = dci.merge(resultObjects);
			// 压缩参数
			byte[] resultData = ReflectTool.getZipBytesByObject(result);
			oldDis.put("result", resultData);
			long sourceSessionId = oldDis.get("source_id").asLong();
			// 将对象传输回始发地
			Collection<IoSession> allExecutes = SocketServer
					.getSessionsByClientType(SocketServer.CLIENT_TYPE_COMMIT);
			for (IoSession ioSession : allExecutes) {
				if (ioSession.getId() == sourceSessionId) {
					ObjectNode transferMessage = JsonTool.createNewObjectNode();
					transferMessage.put("type", "mds");
					transferMessage.put("data", oldDis);
					synchronized (ioSession) {
						ioSession.write(transferMessage);
					}
				}
			}
			// 标识计算已经处理完毕
			DataManager.removeDistributed(id);
			// 再再次清理内存-.-
			resultObjects = null;
			objectData = null;
			result = null;
			resultData = null;
			System.gc();
		}
	}

	/**
	 * 发送分布式任务至客户端
	 * 
	 * @param dis
	 *            the dis
	 * @author 黄林 Send distributed.
	 * @throws Exception
	 */
	public static void sendDistributed(ObjectNode dis) throws Exception {
		// 取出DistributedComputationInterface
		byte[] objectData = dis.get("obj").getBinaryValue();
		byte[] clazzData = dis.get("clazz").getBinaryValue();
		Long id = dis.get("id").asLong();
		// 动态保存class文件
		String className = dis.get("clazz_name").asText();
		String fileName = FileTool.BAST_CLASS_PATH.toString() + "/"
				+ className.replace('.', '/') + ".class";
		File file = new File(fileName);
		FileTool.saveByte(file, clazzData);
		DistributedComputationInterface dci = (DistributedComputationInterface) ReflectTool
				.getObjectByBytes(objectData);
		// 取出参数,并解压
		byte[] argsData = dis.get("args").getBinaryValue();
		Object[] args = (Object[]) ReflectTool.getObjectByZipBytes(argsData);
		// 列出所有的客户端ip
		// 计算每个服务器拥有的cpu数量
		int allCpuCount = 0;
		Map<String, List<IoSession>> ipMap = new HashMap<String, List<IoSession>>();
		Collection<IoSession> allExecutes = SocketServer
				.getSessionsByClientType(SocketServer.CLIENT_TYPE_EXECUTE);
		for (IoSession ioSession : allExecutes) {
			String ip = ioSession.getRemoteAddress().toString();
			ip = ip.substring(1);
			ip = ip.substring(0, ip.indexOf(":"));
			if (ipMap.containsKey(ip)) {
				ipMap.get(ip).add(ioSession);
				continue;
			} else {
				allCpuCount += ((Integer) ioSession.getAttribute("cpu_count") - 1);
				ArrayList<IoSession> list = new ArrayList<IoSession>();
				list.add(ioSession);
				ipMap.put(ip, list);
			}
		}
		// 存入拆分数量
		dis.put("split_count", allCpuCount);
		// 存入已处理量
		dis.put("deal_count", 0);
		dis.put("results", new ArrayNode(JsonNodeFactory.instance));
		dis.put("fails", new ArrayNode(JsonNodeFactory.instance));
		// 拆分参数
		Object[] clientArgs = dci.splitArgs(allCpuCount, args);
		// 给每个服务器发送和cpu数相匹配的任务
		int index = 0;
		for (IoSession ioSession : allExecutes) {
			int cpus = (Integer) ioSession.getAttribute("cpu_count") - 1;
			for (int i = 0; i < cpus; i++) {
				log.debug("send ids to client:"
						+ ioSession.getRemoteAddress().toString());
				ObjectNode clientData = JsonTool.createNewObjectNode();
				// 压缩参数
				byte[] clientArgsData = ReflectTool
						.getZipBytesByObject(clientArgs[index]);
				clientData.put("id", id);
				clientData.put("args", clientArgsData);
				clientData.put("obj", objectData);
				clientData.put("clazz", clazzData);
				clientData.put("clazz_name", className);
				clientData.put("fail_count", 0);
				ObjectNode transferMessage = JsonTool.createNewObjectNode();
				transferMessage.put("type", "mds");
				transferMessage.put("data", clientData);
				synchronized (ioSession) {
					ioSession.write(transferMessage);
				}
				index++;
			}
			if (index >= clientArgs.length) {
				break;
			}
		}
		// 发送完毕,清理占用的内存数据
		dis.remove("args");// 移除庞大的初始参数
		argsData = null;
		args = null;
		clientArgs = null;
		ipMap = null;

		System.gc();

	}
}
