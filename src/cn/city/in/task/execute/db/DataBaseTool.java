package cn.city.in.task.execute.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.city.in.api.tools.DetailSQLException;
import cn.city.in.api.tools.common.PropertyTool;

/**
 * 数据库定时任务父类，提供必要方法支持
 * 
 * @author 黄林 The Class DataBaseTask.
 */
public class DataBaseTool {
	protected static Log log = LogFactory.getLog(DataBaseTool.class);
	private static boolean init = false;

	/**
	 * 从配置文件获取数据库连接
	 * 
	 * @return the connection
	 * @throws SQLException
	 *             the sQL exception
	 * @author 黄林
	 */
	public static Connection getConnection() throws Exception {
		if (!init) {
			Class.forName(PropertyTool.getProperties("jdbc.driver"));
			init = true;
		}
		return DriverManager.getConnection(
				PropertyTool.getProperties("jdbc.url"),
				PropertyTool.getProperties("jdbc.username"),
				PropertyTool.getProperties("jdbc.password"));
	}

	/**
	 * 获取数据库连接
	 * 
	 * @return the dB conn
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	protected static Connection getDBConn() throws Exception {
		Connection conn = null;
		conn = getConnection();
		while (null == conn || conn.isClosed()) {
			conn = getDBConn();
		}
		return conn;
	}

	/**
	 * 查询数据库
	 * 
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the clazz
	 * @param sql
	 *            the sql
	 * @return the list
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static <E> List<E> queryDataBase(Class<E> clazz, String sql)
			throws Exception {
		ResultSetHandler<List<E>> rsh = new BeanListHandler<E>(clazz);
		QueryRunner run = new QueryRunner();
		Connection conn = getDBConn();
		try {
			List<E> results = run.query(conn, sql, rsh);
			conn.close();
			conn = null;
			return results;
		} catch (SQLException e) {
			throw new DetailSQLException(e,sql);
		}
	}

	/**
	 * 查询数据库
	 * 
	 * @param <E>
	 *            the element type
	 * @param clazz
	 *            the clazz
	 * @param sql
	 *            the sql
	 * @return the list
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static <E> List<E> queryDataBase(Connection conn, Class<E> clazz,
			String sql) throws Exception {
		ResultSetHandler<List<E>> rsh = new BeanListHandler<E>(clazz);
		QueryRunner run = new QueryRunner();
		try {
			List<E> results = run.query(conn, sql, rsh);
			return results;
		} catch (SQLException e) {
			throw new DetailSQLException(e,sql);
		}
	}
	
	/**
	 * 查询并返回map
	 *
	 * @param conn the conn
	 * @param sqlQuery the sql query
	 * @return the list
	 * @throws Exception the exception
	 * @author 黄林
	 */
	public static List<Map<String,Object>> queryDataBaseMap(Connection conn, String sqlQuery)
			throws Exception {
		MapListHandler rsh = new MapListHandler();
		QueryRunner run = new QueryRunner();
		try {
			List<Map<String,Object>> list = run.query(conn, sqlQuery, rsh);
			conn.close();
			return list;
		} catch (SQLException e) {
			throw new DetailSQLException(e,sqlQuery);
		}
	}

	/**
	 * 执行更新sql
	 * 
	 * @param conn
	 *            the conn
	 * @param sql
	 *            the sql
	 * @return the int
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static int update(Connection conn, String sql) throws Exception {
		Statement st = conn.createStatement();
		int resultcount = 0;
			try {
				if (st.execute(sql)) {
					resultcount = st.getUpdateCount();
				}
			} catch (SQLException e) {
				throw new DetailSQLException(e,sql);
			}
		return resultcount;
	}
	
	/**
	 * 插入，返回id
	 *
	 * @param conn the conn
	 * @param sql the sql
	 * @return the int
	 * @throws Exception the exception
	 * @author 黄林
	 */
	@Deprecated
	public static int insert(Connection conn, String sql)throws Exception{
		Statement st = conn.createStatement();
		int insertId = 0;
			try {
				
				if (st.execute(sql)) {
					ResultSet rs=st.getGeneratedKeys();
					insertId=rs.next()?rs.getInt(1):-1;
					log.info(insertId);
				}
			} catch (SQLException e) {
				throw new DetailSQLException(e,sql);
			}
		return insertId;
	}

	// /**
	// * 单独启动入口
	// *
	// * @param args the arguments
	// */
	// public static void main(String[] args)throws
	// ClassNotFoundException,SQLException
	// {
	// //初始化配置文件
	// PropertyConfigurator.configure(FileTool.getClassPathFile("log4j.properties").getAbsolutePath());
	// PropertyTool.init("classpath:config/*.conf");
	// //初始化数据源
	// Class.forName(PropertyTool.getProperties("jdbc.driver"));
	// getConnection();
	// //启动定时任务
	// TaskTool.putAllTaskByPropertyFile("taskPropertyFile",false);
	// if ("true".equals(PropertyTool.getProperties("use_socket"))) {
	// //启动远程终端
	// SocketService.init();
	// }
	// new ShutdownThread(true);
	// }

	/**
	 * 更新数据
	 * 
	 * @param sql
	 *            the sql
	 * @return the int
	 * @throws Exception
	 *             the exception
	 * @author 黄林
	 */
	public static int update(String sql) throws Exception {
		Connection conn = getDBConn();
		int result = update(conn, sql);
		conn.close();
		return result;
	}

}
