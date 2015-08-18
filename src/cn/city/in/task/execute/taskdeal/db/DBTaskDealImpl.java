package cn.city.in.task.execute.taskdeal.db;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

import cn.city.in.api.tools.common.PropertyTool;
import cn.city.in.task.execute.db.DataBaseTool;
import cn.city.in.task.execute.taskdeal.BaseTaskDeal;

/**
 * 执行sql任务
 * 
 * @author 黄林 The Class DBTaskDealImpl.
 */
public class DBTaskDealImpl extends BaseTaskDeal {

	/** The log. @author 黄林 The log. */
	protected static Log log = LogFactory.getLog(DBTaskDealImpl.class);

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String... args) throws Exception {
		PropertyTool.init("classpath:serverconf/task.execute.properties");
		System.exit(0);
	}

	/** The conn. @author 黄林 The conn. */
	protected Connection conn;

	/** The idel count. @author 黄林 The idel count. */
	protected int idelCount = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.city.in.task.execute.taskdeal.BaseTaskDeal#accept(java.lang.String)
	 */
	@Override
	public boolean accept(String task) {
		return task.equals("sql");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * cn.city.in.task.execute.taskdeal.BaseTaskDeal#doTask(org.codehaus.jackson
	 * .JsonNode)
	 */
	@Override
	public boolean doTask(JsonNode task) throws Exception {
		String sql = task.get("data").asText();
		if (null == conn) {
			init();
		}
		Statement st = conn.createStatement();
		try {
			return st.execute(sql);
		} catch (Exception e) {
			throw e;
		} finally {
			st.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.city.in.task.execute.taskdeal.BaseTaskDeal#idle()
	 */
	@Override
	public boolean idle() {
		idelCount++;
		if (idelCount > 500) {
			idelCount = 0;
			return stop();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.city.in.task.execute.taskdeal.BaseTaskDeal#init()
	 */
	@Override
	public boolean init() {
		try {
			conn = DataBaseTool.getConnection();
		} catch (Exception e) {
			log.error("init connection fail", e);
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.city.in.task.execute.taskdeal.BaseTaskDeal#stop()
	 */
	@Override
	public boolean stop() {
		try {
			if (null != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			log.error("stop connection fail", e);
			return false;
		}
		return true;
	}
}
