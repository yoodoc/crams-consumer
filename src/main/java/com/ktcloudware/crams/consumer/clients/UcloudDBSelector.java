package com.ktcloudware.crams.consumer.clients;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;

@Deprecated
public class UcloudDBSelector {
    private static final String STAGING = "staging";
    private static final String PRODUCT = "product";

    private static Logger logger = LogManager.getLogger("DB_SELECTOR");

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

    /**
     * supported dbTargetType: STAGING, PRODUCT 
     * @param dbTarget
     * @throws CramsException
     */
    public UcloudDBSelector(String dbTarget) throws CramsException {
        if (dbTarget == null || dbTarget.isEmpty()) {
            throw new CramsException("not supported database");
        } else if (dbTarget.equalsIgnoreCase(STAGING)) {
            this.driver = "com.mysql.jdbc.Driver";
            this.url = "jdbc:mysql://172.27.115.91:3306/KTCP_EPC_DB_NEW";
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
        this.connectionManager.setDriverClassName(this.driver);
        this.connectionManager.setURL(this.url);
        this.connectionManager.setUser(this.user);
        this.connectionManager.setPassword(this.pass);
    }

    public String getVmNameByVmId(String vmUuid) {
        if (null == vmUuid) {
            throw new CramsRuntimeException("vmUuid should not be null");
        }

        Connection conn = this.connectionManager.getConnection();

        try {
            PreparedStatement stmt = conn.prepareStatement(getVmNameQuery);
            stmt.setString(1, vmUuid);
            ResultSet rs = stmt.executeQuery();
            String vmName = null;
            while (rs.next()) {
                vmName = rs.getString(1);
            }
            return vmName;
        } catch (SQLException se) {
            // TODO make error log
            logger.error(se.getMessage(), se);
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
            return generateConnection();
        }

        public void releaseConnection(Connection conn) {
            try {
                conn.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        private Connection generateConnection() {
            try {
                Class.forName(this.driverClass);
                Connection conn = DriverManager.getConnection(this.url,
                        this.user, this.password);
                return conn;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

}
