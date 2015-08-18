package cn.city.in.api.tools;

import java.sql.SQLException;

/**
 * 包含Sql语句的异常
 *
 * @author 黄林
 * The Class DetailSQLException.
 */
public class DetailSQLException extends SQLException {
	
	/** The Constant serialVersionUID. @author 黄林 The Constant serialVersionUID. */
	private static final long serialVersionUID = 3143366921143087876L;
	
	public DetailSQLException() {
		super();
	}

	public DetailSQLException(String reason, String sqlState, int vendorCode,
			Throwable cause) {
		super(reason, sqlState, vendorCode, cause);
	}

	public DetailSQLException(String reason, String SQLState, int vendorCode) {
		super(reason, SQLState, vendorCode);
	}

	public DetailSQLException(String reason, String sqlState, Throwable cause) {
		super(reason, sqlState, cause);
	}

	public DetailSQLException(String reason, String SQLState) {
		super(reason, SQLState);
	}

	public DetailSQLException(String reason, Throwable cause) {
		super(reason, cause);
	}

	public DetailSQLException(String reason) {
		super(reason);
	}

	public DetailSQLException(Throwable cause) {
		super(cause);
	}
	public DetailSQLException(Throwable cause,String sql) {
		super(cause);
		this.sql=sql;
	}

	/** The sql. @author 黄林 The sql. */
	private String sql;

	/**
	 * Gets the sql.
	 *
	 * @return the sql
	 * @author 黄林
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Sets the sql.
	 *
	 * @param sql the new sql
	 * @author 黄林
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	public String getMessage()
	{
		return sql+"\r\n"+super.getMessage();
	}
	
	public String toString()
	{
		return sql+"\r\n"+super.toString();
	}
	
}
