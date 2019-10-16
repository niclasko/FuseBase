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
import java.util.HashMap;
import java.io.Serializable;
import java.sql.SQLException;

public class DBConnectionManager implements Serializable, RESTAPIValidValues {
	
	private HashMap<String, DBConnection> connections;
	
	private String validAPIValues;
	
	public DBConnectionManager() {
		this.connections = new HashMap<String, DBConnection>();
		this.validAPIValues = this.validAPIValues();
	}
	
	public boolean hasConnection(String connectionName) {
		return this.connections.containsKey(connectionName);
	}
	
	public boolean connect(String connectionName) throws SQLException, Exception {
		
		if(this.hasConnection(connectionName)) {
			
			DBConnection dbConnection =
				this.connections.get(connectionName);
			
			dbConnection.connect();
			
			return true;
			
		}
		
		return false;
		
	}
	
	public void connectAll() {
		
		for(DBConnection dbConnection : this.connections.values()) {
			
			try {
				dbConnection.connect();
			} catch(Exception e) {
				// TODO: Add logging
				;
			}
			
		}
			
	}
	
	public boolean setDefaultConnection(String connectionName) {
		
		if(this.connections.containsKey(connectionName)) {
			
			for(DBConnection dbConnection :this.connections.values()) {
				
				dbConnection.setDefaultConnection(
					dbConnection.getConnectionName().equals(connectionName)
				);
				
			}
			
			return true;

		} else {
			
			return false;
			
		}
		
	}
	
	public MapAction addOrUpdateConnection(	String connectionName,
											String connectString,
											String user,
											String passWord,
											boolean useUserAndPassword,
											String jdbcDriverClass,
											String jdbcDriverInfoName	) {
		
		if(!this.connections.containsKey(connectionName)) {
			
			JDBCDriverInfo jdbcDriverInfo =
				JDBCDriverInfo.getJDBCDriverInfo(
					jdbcDriverInfoName
				);
			
			this.connections.put(
				connectionName,
				new DBConnection(
					connectionName,
					connectString,
					user,
					passWord,
					useUserAndPassword,
					jdbcDriverClass,
					jdbcDriverInfo
				)
			);
			
			this.setValidAPIValues();
			
			return MapAction.ADD;
			
		} else {
			
			DBConnection dbConnection =
				this.connections.get(connectionName);
			
			dbConnection.setConnectString(connectString);
			dbConnection.setUser(user);
			
			if(passWord != null) {
				dbConnection.setPassword(passWord);
			}
			
			dbConnection.setJDBCDriverClass(jdbcDriverClass);
			
			return MapAction.UPDATE;
			
		}
		
	}
	
	public MapAction addConnection(JSONDataStructure connectionJSON) throws Exception {
		
		String connectionName =
			connectionJSON.get("connectionName").getValue().toString();
		
		if(this.connections.containsKey(connectionName)) {
			
			return MapAction.ALREADY_EXISTS;
			
		}
		
		return this.addOrUpdateConnection(
			connectionName,
			connectionJSON.get("connectString").getValue().toString(),
			connectionJSON.get("user").getValue().toString(),
			connectionJSON.get("passWord").getValue().toString(),
			Boolean.parseBoolean(connectionJSON.get("useUserAndPassword").getValue().toString()),
			connectionJSON.get("jdbcDriverClass").getValue().toString(),
			connectionJSON.get("jdbcDriverInfoName").getValue().toString()
		);
		
	}
	
	public MapAction deleteConnection(String connectionName) {
		
		if(this.connections.containsKey(connectionName)) {
			
			this.connections.remove(connectionName);
			
			this.setValidAPIValues();
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public DBConnection getConnection(String connectionName) {
		return this.connections.get(connectionName);
	}
	
	public JSONBuilder getJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(DBConnection dbConnection : this.connections.values()) {
			jb.v(dbConnection.getJSONBuilder());
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(DBConnection dbConnection : this.connections.values()) {
			jb.v(dbConnection.getJSONBuilderForExport());
		}
		
		return jb.$(']');
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	public void setValidAPIValues() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(DBConnection dbConnection : this.connections.values()) {
			jb.v(dbConnection.getConnectionName());
		}
		
		this.validAPIValues = jb.$(']').getJSON();
	}
	
	public String validAPIValues() {
		
		return this.validAPIValues;
		
	}
	
}