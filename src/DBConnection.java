/*
 * Copyright (c) 2018 "Niclas Kjäll-Ohlsson, Bjørnar Fjøren"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Arrays;
import java.io.Serializable;

public class DBConnection implements Serializable {
	
	private String connectionName;
	private String connectString;
	private String user;
	private String passWord;
	private boolean useUserAndPassword = true;
	private String jdbcDriverClass;
	private String validationQuery;
	private boolean isConnected;
	
	private transient Connection[] connections;
	
	private String lastError = "";
	
	private boolean isDefaultConnection = false;
	
	private JDBCDriverInfo jdbcDriverInfo;
	
	public DBConnection(String connectionName, String connectString, String user, String passWord, boolean useUserAndPassword, String jdbcDriverClass, JDBCDriverInfo jdbcDriverInfo) {
		this.connectionName = connectionName;
		this.connectString = connectString;
		this.user = user;
		this.passWord = passWord;
		this.useUserAndPassword = useUserAndPassword;
		this.jdbcDriverClass = jdbcDriverClass;
		this.validationQuery = jdbcDriverInfo.getValidationQuery();
		this.isConnected = false;
		
		this.jdbcDriverInfo = jdbcDriverInfo;
		
		this.connections = null;
	}

	public void setUseUserAndPassword(boolean useUserAndPassword) {
		this.useUserAndPassword = useUserAndPassword;
	}

	public boolean useUserAndPassword() {
		return this.useUserAndPassword;
	}
	
	public boolean isDefaultConnection() {
		return this.isDefaultConnection;
	}
	
	public void setDefaultConnection(boolean isDefaultConnection) {
		this.isDefaultConnection = isDefaultConnection;
	}
	
	public Connection getConnection() throws Exception {
		return this.getFirstAvailableConnection();
	}
	
	public String getConnectionName() {
		return this.connectionName;
	}
	
	public String getConnectString() {
		return this.connectString;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPassword() {
		return this.passWord;
	}
	
	public void setConnectString(String connectString) {
		this.connectString = connectString;
	}
	
	public void setJDBCDriverClass(String jdbcDriverClass) {
		this.jdbcDriverClass = jdbcDriverClass;
	}
	
	public void setUser(String user)  {
		this.user = user;
	}
	
	public void setPassword(String passWord) {
		this.passWord = passWord;
	}
	
	public String getJDBCDriverClass() {
		return this.jdbcDriverClass;
	}
	
	public String getValidationQuery() {
		return this.validationQuery;
	}
	
	public String getLastError() {
		return this.lastError;
	}
	
	private Connection getFirstAvailableConnection() throws Exception {
		
		if(this.connections == null) {
			
			throw new Exception("Not connected. Please connect.");
			
		}
		
		final DBConnection me = this;
		Thread connectionValidationThread = null;
		final DBConnectionValidationThreadSignal threadSignal =
			new DBConnectionValidationThreadSignal();
		
		threadSignal.validationQueryReturned = false;
		
		for(int i=0; i<this.connections.length; i++) {
			
			final int connectionIndex = i;
			
			threadSignal.validationQueryReturned = false;
			
			connectionValidationThread =
				new Thread() {
			    	public void run() {
						
						try {
							
							/*
							Statement statement =
								me.connections[connectionIndex].createStatement();

							statement.executeQuery(
								me.getValidationQuery()
							);
							*/
							
							me.connections[connectionIndex].isClosed();

							threadSignal.validationQueryReturned = true;
							
						} catch(Exception e) {
							
							e.printStackTrace();
							
						}
			        	
			    	}
				};
			
			connectionValidationThread.start();
			// Wait at most 200 milliseconds for connectionValidationThread to finish
			connectionValidationThread.join(5); 
			
			if(threadSignal.validationQueryReturned) {
				
				return this.connections[i];
				
			}
			
		}
		
		throw new Exception("Connection not available. Please try again later.");
		
	}
	
	public String toString() {
		return 
			"connectionName: " + this.connectionName + "\n" +
			"connectString: " + this.connectString + "\n" +
			"user: " + this.user + "\n" +
			"useUserAndPassword: " + this.useUserAndPassword + "\n" +
			"jdbcDriverClass: " + this.jdbcDriverClass + "\n" +
			"JDBCDriverInfo:\n" + jdbcDriverInfo.toString();
	}
	
	public JSONBuilder getJSONBuilder() {
		return
			JSONBuilder.f().
				$('{').
					k("connectionName").v(this.getConnectionName()).
					k("connectString").v(this.getConnectString()).
					k("jdbcDriverInfoName").v(this.jdbcDriverInfo.getName()).
					k("jdbcDriverClass").v(this.getJDBCDriverClass()).
					k("user").v(this.getUser()).
					k("useUserAndPassword").v(this.useUserAndPassword()).
					k("isConnected").v(this.isConnected).
					k("Default").v(this.isDefaultConnection()).
				$('}');
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		return
			JSONBuilder.f().
				$('{').
					k("connectionName").v(this.getConnectionName()).
					k("connectString").v(this.getConnectString()).
					k("jdbcDriverInfoName").v(this.jdbcDriverInfo.getName()).
					k("jdbcDriverClass").v(this.getJDBCDriverClass()).
					k("user").v(this.getUser()).
					k("passWord").v(this.getPassword()).
					k("useUserAndPassword").v(this.useUserAndPassword()).
				$('}');
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	public void connect() throws SQLException, Exception {
		
		this.isConnected = false;
		
		if(this.connections != null) {
			
			for(int i=0; i<Config.DB_CONNECTION_POOL_SIZE; i++) {
				
				try {
					
					this.connections[i].close();
					
				} catch(Exception e) {
					
					;
					
				}
			
			}
			
		} else if(this.connections == null) {
			
			this.connections =
				new Connection[this.jdbcDriverInfo.getConnectionPoolSize()];
			
		}
		
		for(int i=0; i<this.jdbcDriverInfo.getConnectionPoolSize(); i++) {
			
			if(this.useUserAndPassword()) {
				this.connections[i] = DriverShim.getConnection(
					this.jdbcDriverInfo.getJDBCDriverPath(),
					this.jdbcDriverClass,
					this.connectString,
					this.user,
					this.passWord
				);
			} else if(!this.useUserAndPassword()) {
				this.connections[i] = DriverShim.getConnection(
					this.jdbcDriverInfo.getJDBCDriverPath(),
					this.jdbcDriverClass,
					this.connectString
				);
			}
			
		}
		
		this.isConnected = true;
		
	}
}