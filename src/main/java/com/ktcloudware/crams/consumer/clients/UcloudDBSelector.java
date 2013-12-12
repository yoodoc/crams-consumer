package com.ktcloudware.crams.consumer.clients;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ktcloudware.crams.consumer.CramsException;

public class UcloudDBSelector {
	private static final String STAGING = "staging";

	private static final String PRODUCT = "product";

	private String driver;
	private String url;
	private String user;
	private String pass;
	private ConnectionManager connectionManager;
	private String getVmNameQuery = "SELECT VM_NM FROM VWCS_CDP_OVERALL_VM_INFO_IS WHERE VM_ID = ?";

	public UcloudDBSelector() throws CramsException {
			this.driver = "com.mysql.jdbc.Driver";
			this.url = "jdbc:mysql://172.27.115.91:13306/KTCP_EPC_DB_NEW";
			this.user = "cdp";
			this.pass = "cdpadmin";
			this.connectionManager = new ConnectionManager();
		initPool();
	}
	
	public UcloudDBSelector(String dbTarget) throws CramsException {
		if (dbTarget == null || dbTarget.isEmpty()) {
			throw new CramsException("not supported database");
		} else if (dbTarget.equalsIgnoreCase(STAGING)) {
			this.driver = "com.mysql.jdbc.Driver";
			this.url = "jdbc:mysql://172.27.115.91:13306/KTCP_EPC_DB_NEW";
			this.user = "cdp";
			this.pass = "cdpadmin";
			this.connectionManager = new ConnectionManager();
		} else if (dbTarget.equalsIgnoreCase(PRODUCT)) {
			this.driver = "com.mysql.jdbc.Driver";
			this.url = "jdbc:mysql://14.63.254.132:3306/KTCP_EPC_DB_NEW";
			this.user = "selonly";
			this.pass = "vhxkf!1";
			this.connectionManager = new ConnectionManager();
		} else {
			throw new CramsException("not supported database");
		}
		initPool();
	}

	private void initPool() {
		assert (null != this.driver);
		assert (null != this.url);
		assert (null != this.user);
		assert (null != this.pass);
		assert (null != this.connectionManager);
		this.connectionManager.setDriverClassName(this.driver);
		this.connectionManager.setURL(this.url);
		this.connectionManager.setUser(this.user);
		this.connectionManager.setPassword(this.pass);
	}

	public String getVmNameByVmId(String vmUuid) {
		assert (null != vmUuid);
		if (null == vmUuid)
			throw new NullPointerException("vmUuid should not be null");
		assert (null != this.connectionManager);
		Connection conn = this.connectionManager.getConnection();
		assert (null != conn);
		try {
			PreparedStatement stmt = conn.prepareStatement(getVmNameQuery);
			stmt.setString(1, vmUuid);
			ResultSet rs = stmt.executeQuery();
			String secretKey = null;
			while (rs.next()) {
				secretKey = rs.getString(1);
			}
			return secretKey;
		} catch (SQLException se) {
			// TODO make error log
			se.printStackTrace();
			return null;
		} finally {
			connectionManager.releaseConnection(conn);
		}
	}

	private static class ConnectionManager {

		private ConnectionManager() {

		}

		String driverClass;
		String url;
		String user;
		String password;

		public void setDriverClassName(String driverClass) {
			this.driverClass = driverClass;
		}

		public void setURL(String url) {
			// TODO Auto-generated method stub
			this.url = url;
		}

		public void setUser(String user) {
			// TODO Auto-generated method stub
			this.user = user;
		}

		public void setPassword(String password) {
			// TODO Auto-generated method stub
			this.password = password;
		}

		public Connection getConnection() {
			Connection conn = generateConnection();
			return conn;
		}

		public void releaseConnection(Connection conn) {
			// TODO Auto-generated method stub
			try {
				conn.close();
			} catch (Exception e) {

			}
		}

		private Connection generateConnection() {
			try {
				Class.forName(this.driverClass);
				Connection conn = DriverManager.getConnection(this.url,
						this.user, this.password);
				return conn;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
