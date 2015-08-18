package cn.city.in.task.manager.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.codehaus.jackson.node.ObjectNode;

import cn.city.in.api.tools.common.FileTool;
import cn.city.in.api.tools.task.TaskTool;
import cn.city.in.task.manager.distributed.DistributedManager;
import cn.city.in.task.manager.socket.SocketServer;
import cn.city.in.task.manager.task.TaskManager;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * 数据管理服务,记录任务执行状态,分布式计算结果
 * 
 * @author 黄林
 * 
 */
public class DataManager {
	private static Logger log = Logger.getLogger(DataManager.class);
	public static final DataManager DATA_MANAGER = new DataManager();
	private static AtomicLong tid = new AtomicLong(0);
	private static HashMap<Long, ObjectNode> taskMap;
	private static File cacheFile;
	private static BufferedWriter logwriter;
	private static Kryo kryo = new Kryo();
	private static File logFile;
	private static HashMap<String, Integer> taskHeadMap = new HashMap<String, Integer>();
	public static final int TASK_STATUS_ADD = 0;
	public static final int TASK_STATUS_SEND = 1;
	public static final int TASK_STATUS_OK = 2;
	public static final int TASK_STATUS_FAIL = 3;
	public static final int TASK_STATUS_REMOVE = 4;
	public static final int TASK_STATUS_RESEND = 5;
	public static final int TASK_HEAD_STATUS_NORMAL = 0;
	public static final int TASK_HEAD_STATUS_PAUSE = 1;
	private static HashMap<Long, ObjectNode> distributedMap;
	private static AtomicLong did = new AtomicLong(0);

	// public static final int DISTRIBUTED_STATUS_ADD=0;
	// public static final int DISTRIBUTED_STATUS_SEND=1;
	// public static final int DISTRIBUTED_STATUS_MERGE=2;
	// public static final int DISTRIBUTED_STATUS_OK=3;

	/**
	 * 添加一个分布式计算队列
	 * 
	 * @param task
	 *            the task
	 * @return the object node
	 * @author 黄林
	 * @throws Exception
	 */
	public static ObjectNode addDistributed(ObjectNode dis) throws Exception {
		Long id = did.addAndGet(1);
		dis.put("id", id);
		synchronized (distributedMap) {
			distributedMap.put(id, dis);
		}
		// 将分布式任务送入管理器
		DistributedManager.sendDistributed(dis);
		return dis;
	}

	public static ObjectNode addTask(ObjectNode task) {
		PlutoObjectNode realTask = new PlutoObjectNode();
		// 加上id作为任务唯一标识
		Long id = tid.addAndGet(1);
		// 防止已经存在过某些task
		while (taskMap.containsKey(id)) {
			id = tid.addAndGet(1);
		}
		realTask.put("id", id);
		realTask.putAll(task);
		// 记录状态改变
		taskChange(realTask, TASK_STATUS_ADD);
		try {
			String head = realTask.get("head").asText();
			if (!taskHeadMap.containsKey(head)) {
				taskHeadMap.put(head, TASK_HEAD_STATUS_NORMAL);
			}
		} catch (Exception e) {
		}
		// 将任务送至TaskManager
		TaskManager.sendTask(realTask);
		return realTask;
	}

	/**
	 * 改变head状态
	 * 
	 * @param head
	 * @param status
	 * @return
	 */
	public static boolean changeHeadStatus(String head, int status) {
		if (status == TASK_HEAD_STATUS_NORMAL
				|| status == TASK_HEAD_STATUS_PAUSE) {
			if (taskHeadMap.containsKey(head)) {
				taskHeadMap.put(head, status);
				// 通知TaskManager任务状态改变
				if (status == TASK_HEAD_STATUS_NORMAL) {
					TaskManager.clientOrTaskStatusChange();
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 刷新日志到磁盘
	 * 
	 * @author 黄林 Flush log.
	 */
	public static void flushLog() {
		try {
			logwriter.flush();
		} catch (IOException e) {
			log.error("fail save task change log to disk:", e);
		}
	}

	/**
	 * 获取暂存在这里的分布式任务
	 * 
	 * @param id
	 *            the id
	 * @return the distributed
	 * @author 黄林
	 */
	public static ObjectNode getDistributed(long id) {
		return distributedMap.get(id);
	}

	/**
	 * 获取head状态
	 * 
	 * @param head
	 * @return
	 */
	public static Integer getHeadStatus(String head) {
		if (taskHeadMap.containsKey(head)) {
			return taskHeadMap.get(head);
		} else {
			return TASK_HEAD_STATUS_NORMAL;
		}
	}

	// 返回任务head状态
	public static HashMap<String, Integer> getHeadStatusClone() {
		return (HashMap<String, Integer>) taskHeadMap.clone();
	}

	/**
	 * 获取任务日志文件
	 * 
	 * @return the log file
	 * @author 黄林
	 */
	public static File getLogFile() {
		return logFile;
	}

	/**
	 * 获取任务的字符串表示形式
	 * 
	 * @param stat
	 *            the stat
	 * @return the stat string
	 * @author 黄林
	 */
	public static String getStatString(int stat) {
		switch (stat) {
		case 0:
			return "TASK_STATUS_ADD";
		case 1:
			return "TASK_STATUS_SEND";
		case 2:
			return "TASK_STATUS_OK";
		case 3:
			return "TASK_STATUS_FAIL";
		case 4:
			return "TASK_STATUS_REMOVE";
		case 5:
			return "TASK_STATUS_RESEND";

		default:
			return "UNKNOW";
		}
	}

	/**
	 * 获取任务的克隆实例
	 * 
	 * @return the task map clone
	 * @author 黄林
	 */
	public static Map<Long, ObjectNode> getTaskMapClone() {
		return new HashMap<Long, ObjectNode>(taskMap);
	}

	/**
	 * 初始化任务管理器
	 * 
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static void init(File saveFile, File logSaveFile) throws Exception {
		if (!saveFile.exists()) {
			FileTool.createNewFile(saveFile);
		}
		cacheFile = saveFile;
		if (!logSaveFile.exists()) {
			FileTool.createNewFile(logSaveFile);
		}
		logwriter = new BufferedWriter(new FileWriter(logSaveFile), 8192);
		logFile = logSaveFile;
		// schema=RuntimeSchema.getSchema(HashMap.class);
		HashMap<Long, ObjectNode> map = new HashMap<Long, ObjectNode>();
		if (saveFile.exists()) {
			try {
				Input input = new Input(new FileInputStream(cacheFile));
				map = kryo.readObject(input, HashMap.class);
				input.close();
				// FileInputStream in = new FileInputStream(cacheFile);
				// ProtostuffIOUtil.mergeFrom(in,map,schema);
			} catch (Exception e) {
				log.error("fail load task from disk:", e);
			}
		}
		if (null == map) {
			map = new HashMap<Long, ObjectNode>();
		}
		taskMap = map;
		distributedMap = new HashMap<Long, ObjectNode>();
		log.info("start task manager ok");
	}

	/**
	 * 移除分布式计算
	 * 
	 * @param id
	 *            the id
	 * @author 黄林 Removes the distributed.
	 */
	public static void removeDistributed(long id) {
		synchronized (distributedMap) {
			distributedMap.remove(id);
		}
	}

	/**
	 * 重新发送任务至客户端执行
	 * 
	 * @param task
	 *            the task
	 * @author 黄林 Re send task.
	 */
	public static void reSendTask(ObjectNode task) {
		taskChange(task, TASK_STATUS_RESEND);
		TaskManager.sendTask(task);
	}

	/**
	 * 保存数据
	 * 
	 * @author 黄林 Save task.
	 */
	public static void saveTask() {
		try {
			Output output = new Output(new FileOutputStream(cacheFile));
			kryo.writeObject(output, taskMap);
			output.close();
			// ProtostuffIOUtil.writeTo(out, taskMap, schema,
			// LinkedBuffer.allocate(512));
		} catch (Exception e) {
			log.error("fail save task to disk:", e);
		}

	}

	/**
	 * 停止管理服务器 写入数据、日志
	 */
	public static void shutdown() {
		saveTask();
		flushLog();
	}

	/**
	 * 改变任务状态
	 * 
	 * @param task
	 *            the task
	 * @param status
	 *            the status
	 * @author 黄林 Task change.
	 */
	public static void taskChange(ObjectNode task, int status) {
		long id = task.get("id").asLong();
		synchronized (taskMap) {
			taskChangeLog(id, status, task);
			if (status == TASK_STATUS_OK || status == TASK_STATUS_REMOVE) {
				taskMap.remove(id);
			} else {
				if (!(task instanceof PlutoObjectNode)) {
					PlutoObjectNode plutoObjectNode = new PlutoObjectNode();
					plutoObjectNode.putAll(task);
					task = plutoObjectNode;
				}
				task.put("status", status);
				taskMap.put(id, task);
			}
		}
	}

	/**
	 * 任务变更日志
	 * 
	 * @param task
	 *            the task
	 * @param status
	 *            the status
	 * @author 黄林 Task change log.
	 */
	public static void taskChangeLog(long task, int status, ObjectNode context) {
		try {
			logwriter.newLine();
			// 任务加入后写入日志
			if (status == TASK_STATUS_ADD) {
				logwriter.write(task + ":" + status + ":" + context.toString());
			} else {
				logwriter.write(task + ":" + status);
			}
			// 异步一秒后刷新日志到文件
			TaskTool.createTimeDelayTask(100L, "flushLog", DATA_MANAGER,
					new Object[0]);
			// 异步100毫秒后写入数据至磁盘
			TaskTool.createTimeDelayTask(10L, "saveTask", DATA_MANAGER,
					new Object[0]);
			// 写入备份服务器
			SocketServer.sendBackup(context, status);
		} catch (Exception e) {
			log.error("fail save task change log to disk:", e);
		}

	}

	/**
	 * 任务修正
	 * 
	 * @author 黄林 Task repair.
	 */
	public static void taskRepair() {
		synchronized (taskMap) {
			Collection<ObjectNode> tasks = taskMap.values();
			for (ObjectNode task : tasks) {
				int status = task.get("status").asInt();
				if (status == TASK_STATUS_SEND || status == TASK_STATUS_RESEND
						|| status == TASK_STATUS_ADD) {
					// 记录状态改变
					taskChange(task, TASK_STATUS_RESEND);
					// 将任务送至TaskManager
					TaskManager.sendTask(task);
				}
			}
		}
	}
}
