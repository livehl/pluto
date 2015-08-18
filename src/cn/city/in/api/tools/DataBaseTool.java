package cn.city.in.api.tools;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cn.city.in.api.common.APIInitialize;

public class DataBaseTool {
	/**
	 * 获取当前API所使用的数据库连接
	 * 
	 * @return the connection
	 * @throws Exception
	 *             the exception
	 */
	public static Connection getConnection() throws Exception {
		return APIInitialize.getConnection();
	}

	/**
	 * 功能:查询数据库 创建者： 黄林 2011-10-8.
	 * 
	 * @param conn
	 *            连接
	 * @param sqlQuery
	 *            sql语句
	 * @return object[] 结果集
	 * @throws Exception
	 *             the exception
	 */
	public static List<Object[]> queryDataBase(Connection conn, String sqlQuery)
			throws Exception {
		ArrayListHandler rsh = new ArrayListHandler();
		QueryRunner run = new QueryRunner();
		try {
			List<Object[]> list = run.query(conn, sqlQuery, rsh);
			conn.close();
			return list;
		} catch (SQLException e) {
			throw new DetailSQLException(e,sqlQuery);
		}
	}

	/**
	 * 功能:根据指定类型查询数据库 创建者： 黄林 2011-10-8.
	 * 
	 * @param conn
	 *            连接
	 * @param sqlQuery
	 *            sql语句
	 * @param clazz
	 *            class
	 * @return object[] 结果集
	 * @throws Exception
	 *             the exception
	 */
	public static <T> List<T> queryDataBase(Connection conn, String sqlQuery,
			Class<T> clazz) throws Exception {
		ResultSetHandler<List<T>> rsh = new BeanListHandler<T>(clazz);
		QueryRunner run = new QueryRunner();
		try{
			List<T> results = run.query(conn, sqlQuery, rsh);
			return results;
		} catch (SQLException e) {
			throw new DetailSQLException(e,sqlQuery);
		}
	}

	/**
	 * 功能:根据指定类型查询数据库 创建者： 黄林 2012-1-14.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param sqlQuery
	 *            the sql query
	 * @param clazz
	 *            the clazz
	 * @return list
	 * @throws Exception
	 *             the exception
	 */
	public static <T> List<T> queryDataBase(String sqlQuery, Class<T> clazz)
			throws Exception {
		Connection conn = getConnection();
		try{
			List<T> list = queryDataBase(conn, sqlQuery, clazz);
			conn.close();
			conn = null;
			return list;
		} catch (SQLException e) {
			throw new DetailSQLException(e,sqlQuery);
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

}
