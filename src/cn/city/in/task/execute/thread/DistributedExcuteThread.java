package cn.city.in.task.execute.thread;

import java.io.File;

import org.apache.log4j.Logger;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.ExceptionTool;
import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.common.JsonTool;
import cn.city.in.api.tools.common.ReflectTool;
import cn.city.in.common.DistributedComputationInterface;
import cn.city.in.task.execute.socket.SocketClientService;

/**
 * 功能:分布式任务处理线程
 * 
 * @author 黄林 2011-10-27
 * @version
 */
public class DistributedExcuteThread extends Thread {
	private static Logger log = Logger.getLogger(DistributedExcuteThread.class);
	private ObjectNode mdst;

	public DistributedExcuteThread(ObjectNode mdst) {
		setDaemon(true);
		this.mdst = mdst;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		ObjectNode transferMessage = JsonTool.createNewObjectNode();
		transferMessage.put("data", mdst);
		try {
			// 取出数据
			byte[] argsData = mdst.get("args").getBinaryValue();
			byte[] objectData = mdst.get("obj").getBinaryValue();
			byte[] clazzData = mdst.get("clazz").getBinaryValue();
			// 动态保存class文件
			String className = mdst.get("clazz_name").asText();
			String fileName = FileTool.BAST_CLASS_PATH.toString() + "/"
					+ className.replace('.', '/') + ".class";
			File file = new File(fileName);
			FileTool.saveByte(file, clazzData);
			// 反序列化
			DistributedComputationInterface dci = (DistributedComputationInterface) ReflectTool
					.getObjectByBytes(objectData);
			// 解压参数
			Object[] args = (Object[]) ReflectTool
					.getObjectByZipBytes(argsData);
			// 执行
			Object result = dci.execute(args);
			// 压缩并序列化返回
			byte[] data = ReflectTool.getZipBytesByObject(result);
			mdst.put("result", data);
			transferMessage.put("type", "mds");
			transferMessage.put("status", "ok");
			log.debug("do mdst ok");
			// 移除参数
			mdst.remove("args");
			// 标志已成功执行
			SocketClientService.write(transferMessage);
			// 清理内存
			argsData = null;
			args = null;
			result = null;
			data = null;
			System.gc();

		} catch (Throwable e) {
			// 执行失败
			try {
				// log.warn(mdst);
				mdst.put("error", ExceptionTool.getStackTraceString(e));
				transferMessage.put("type", "mds");
				transferMessage.put("status", "fail");
				SocketClientService.write(transferMessage);
			} catch (Exception e1) {
				log.info("send data fail", e1);
			}
			log.info("client mdst error:" + e.getMessage(), e);
		}
	}
}
