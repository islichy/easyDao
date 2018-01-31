package com.oracle.jdbc.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

public class Dao {

	private static String driver;
	private static String url;
	private static String userName;
	private static String password;

	static ThreadLocal<Connection> local = new ThreadLocal<Connection>();

	/**
	 * 获得连接
	 * 
	 * @return
	 */
	public static Connection get() {
		if (local.get() == null) {
			local.set(getConnection());
		}
		return local.get();
	}

	/**
	 * 关闭
	 * @return
	 */
	public static void close() {
		if (local.get() != null) {
			try {
				local.get().close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				local.remove();
			}
			System.out.println("关闭连接");
		}
	}

	public static void begin() {
		try {
			get().setAutoCommit(false);
			System.out.println("开始事务");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void commit() {
		try {
			get().commit();
			System.out.println("提交事务");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void rollback() {
		try {
			get().rollback();
			System.out.println("回退事务");
		} catch (SQLException e) {	
			e.printStackTrace();
			throw new RuntimeException("回退失败");
		}
	}

	static {
		Properties p = new Properties();
		try {
			p.load(Dao.class.getResourceAsStream("/db.properties"));
			url = p.getProperty("url");
			driver = p.getProperty("driver");
			userName = p.getProperty("userName");
			password = p.getProperty("password");
			System.out.println(url);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static Connection getConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, userName, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * 执行非查询语句
	 * 
	 * @param sql
	 * @param params
	 * @throws SQLException
	 */
	public static void executeSql(String sql, Object... params) {
		QueryRunner run = new QueryRunner();
		Connection conn =get();
		try {
			run.update(conn, sql, params);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("sql执行失败");			
		}

	}

	/**
	 * 执行任何sql，参数使用数组
	 * 
	 * @param sql
	 * @param params
	 * @throws SQLException
	 */
	public static void execute(String sql, Object[] params){
		QueryRunner run = new QueryRunner();
		Connection conn = get();
		try {
			run.update(conn, sql, params);
		} catch (SQLException e) {
			
			e.printStackTrace();
			throw new RuntimeException("sql执行失败");
		}
	}

	/**
	 * 根据查询语句和类型名称，获得对应的对象集合；
	 * 
	 * @param sql
	 * @param clazz
	 *            ;集合中的类型
	 * @param params
	 * @return
	 * @throws SQLException 
	 */
	public static <T> List<T> query(String sql, Class<T> clazz, Object... params)  {

		QueryRunner run = new QueryRunner();
		Connection conn = get();
		ResultSetHandler<List<T>> h = new BeanListHandler<T>(clazz);
		List<T> p = null;
		try {
			p = run.query(conn, sql, h, params);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("sql执行失败");
		}
		return p;
	}

	/**
	 * 获得唯一的一条记录
	 * 
	 * @param sql
	 * @param clazz，返回的类型
	 * @param params
	 * @return
	 * @throws SQLException 
	 */
	public static <T> T queryOne(String sql, Class<T> clazz, Object... params)  {
		List<T> list = query(sql, clazz, params);
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public static Object[] queryOne(String sql, Object... params)  {
		List<Object[]> list=null;
		try {
			list = query(sql, params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("sql执行失败");
		}
		if (list == null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public static List<Object[]> query(String sql, Object... params) {
		QueryRunner run = new QueryRunner();
		Connection conn = get();

		ResultSetHandler<List<Object[]>> h = new ResultSetHandler<List<Object[]>>() {
			public List<Object[]> handle(ResultSet rs) throws SQLException {
				List<Object[]> list = new ArrayList<Object[]>();

				ResultSetMetaData meta = rs.getMetaData();
				int cols = meta.getColumnCount();
				while (rs.next()) {
					Object[] result = new Object[cols];

					for (int i = 0; i < cols; i++) {
						result[i] = rs.getObject(i + 1);
					}
					list.add(result);
				}
				return list;
			}
		};
		// Execute the query and get the results back from the handler
		List<Object[]> result = null;	
		try {
			result = run.query(conn, sql, h, params);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("sql执行失败");
		}
		return result;
	}

}
