package com.ktcloudware.crams.consumer.clients;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UcloudDBSelector {

	private String driver;
	private String url;
	private String user;
	private String pass;
	private ConnectionManager connectionManager;
	private String getVmNameQuery = "SELECT VM_NM FROM VWCS_CDP_OVERALL_VM_INFO_IS WHERE  VM_ID = ?";

	public UcloudDBSelector(){
		this.driver = "com.mysql.jdbc.Driver";
        this.url = "jdbc:mysql://10.2.8.131/KTCP_EPC_DB_NEW";
        this.user = "kthdev";
        this.pass = "kthdev1@";

        this.connectionManager = new ConnectionManager();
        initPool();
	}

	private void initPool(){
         assert(null != this.driver);
         assert(null != this.url);
         assert(null != this.user);
         assert(null != this.pass);
         assert(null != this.connectionManager);
         this.connectionManager.setDriverClassName(this.driver);
         this.connectionManager.setURL(this.url);
         this.connectionManager.setUser(this.user);
         this.connectionManager.setPassword(this.pass);
 }

	public void connect(){
		
	}
	
	public String getVmNameByVmId(String vmUuid){
        assert(null != vmUuid);
        if(null == vmUuid)
                throw new NullPointerException("vmUuid should not be null");
        assert(null != this.connectionManager);
        Connection conn = this.connectionManager.getConnection();
        assert(null != conn);
        try{
                PreparedStatement stmt = conn.prepareStatement(getVmNameQuery);
                stmt.setString(1, vmUuid);
                ResultSet rs = stmt.executeQuery();
                String secretKey = null;
                while(rs.next()){
                        secretKey = rs.getString(1);
                }
                return secretKey;
        }catch(SQLException se){
        	//TODO make error log
                se.printStackTrace();
                return null;
        }finally{
                connectionManager.releaseConnection(conn);
        }
	}
	
	private static class ConnectionManager {

        private ConnectionManager(){

        }

        String driverClass;
        String url;
        String user;
        String password;

        public void setDriverClassName(String driverClass){
                this.driverClass = driverClass;
        }

        public void setURL(String url){
                // TODO Auto-generated method stub
                this.url = url;
        }

        public void setUser(String user){
                // TODO Auto-generated method stub
                this.user = user;
        }

        public void setPassword(String password){
                // TODO Auto-generated method stub
                this.password = password;
        }

        public Connection getConnection(){
                Connection conn = generateConnection();
                return conn;
        }

        public void releaseConnection(Connection conn){
                // TODO Auto-generated method stub
                try{
                        conn.close();
                }catch(Exception e){

                }
        }

        private Connection generateConnection(){
                try{
                        Class.forName(this.driverClass);
                        Connection conn = DriverManager.getConnection(this.url, this.user, this.password);
                        return conn;
                }catch(Exception e){
                        e.printStackTrace();
                        return null;
                }
        }
}

}
