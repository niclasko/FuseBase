/*
 * Copyright (c) 2018 "Niclas Kjall-Ohlsson, Bjornar Fjoren"
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
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.io.Serializable;

public class QueryManager implements Serializable, RESTAPIValidValues {
	
	private DBConnectionManager dbConnectionManager;
	private HashMap<String, Query> queries;
	
	private String validAPIValues;
	
	public QueryManager(DBConnectionManager dbConnectionManager) {
		this.dbConnectionManager = dbConnectionManager;
		this.queries = new HashMap<String, Query>();
		this.setValidAPIValues();
	}
	
	public Connection getConnection(String connectionName) throws Exception {
		return this.dbConnectionManager.getConnection(connectionName).getConnection();
	}
	
	public void sql(	DataWriter dataWriter,
						String connectionName,
						String query			) throws Exception {
		
		QueryObject queryObject =
			this.getQueryObject(
				connectionName,
				query
			);
		
		this.printResultSet(
			queryObject,
			dataWriter
		);
		
		queryObject.close();
						
	}
	
	public void printResultSet(	QueryObject queryObject,
								DataWriter dataWriter	) throws Exception {
		
		ResultSet rs =
			queryObject.resultSet();
		
		ResultSetMetaData rsmd =
			queryObject.resultSetMetaData();

		Object fieldVal = null;
		String columnClassName = null;
		
		dataWriter.init();
		dataWriter.beginRow();

		for(int i=0; i<rsmd.getColumnCount(); i++) {
			dataWriter.headerEntry(
				rsmd.getColumnName(i+1),
				i
			);
		}
		
		dataWriter.endRow();
		
		while(rs.next()) {
			
			dataWriter.newRow();
			dataWriter.beginRow();
			
			for(int i=0; i<rsmd.getColumnCount(); i++) {
				
				fieldVal = rs.getObject(i + 1);
				
				if(fieldVal == null) {
					dataWriter.nullEntry(i);
					continue;
				}
				
				// Ugly hack :-(
				try {
					columnClassName =
						rsmd.getColumnClassName(i+1);
				} catch(Exception e) {
					columnClassName = "java.lang.String";
				}
				
				
				if(columnClassName.equals("java.lang.String") || columnClassName.equals("java.lang.Boolean")) {
					
					dataWriter.entry(
						fieldVal.toString(),
						i
					);
					
				} else if(columnClassName.equals("java.lang.Integer")) {
					
					dataWriter.entry(
						Integer.parseInt(
							fieldVal.toString()
						),
						i
					);
					
				} else if (columnClassName.equals("java.lang.Double") || columnClassName.equals("java.lang.Float")) {
					
					dataWriter.entry(
						Double.parseDouble(
							fieldVal.toString()
						),
						i
					);
					
				} else if (columnClassName.equals("java.lang.Date")) {
					
					dataWriter.entry(
						((Date)fieldVal).toString(),
						i
					);
					
				} else {
					
					dataWriter.entry(
						fieldVal.toString(),
						i
					);
					
				}
				
			}
			
			dataWriter.endRow();
			
		}
		
		dataWriter.finish();
		
	}
	
	public QueryObject getQueryObject(String connectionName, String query) throws Exception {
		
		Connection connection =
			this.getConnection(connectionName);
		
		Statement statement = connection.createStatement(
			ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY
		);

		// Not supported by all JDBC drivers
		try {
			statement.setMaxRows(0);
		} catch(Exception e) {
			;
		}
		
		ResultSet resultSet =
			statement.executeQuery(query);
		
		return
			new QueryObject(
				statement,
				resultSet,
				resultSet.getMetaData()
			);
		
	}
	
	public int dml(String connectionName, String query) throws Exception {
		
		Statement statement =
			this.getConnection(connectionName).createStatement();
		
		int recordCount = statement.executeUpdate(query);
		
		statement.close();
		
		return recordCount;
		
	}
	
	public int ddl(String connectionName, String query) throws Exception {
		
		Statement statement =
			this.getConnection(connectionName).createStatement();
		
		int ddlStatus = statement.executeUpdate(query);
		
		statement.close();
		
		return ddlStatus;
		
	}
	
	public PreparedStatement getPreparedStatement(String connectionName, String query) throws Exception {
		
		return this.getConnection(connectionName).prepareStatement(query);
		
	}
	
	/*
		How to use:
			callableStatement.setInt(1, 10);
			callableStatement.registerOutParameter(2, java.sql.Types.VARCHAR);
			callableStatement.registerOutParameter(3, java.sql.Types.VARCHAR);
			callableStatement.registerOutParameter(4, java.sql.Types.DATE);
		
			// execute getDBUSERByUserId store procedure
			callableStatement.executeUpdate()
		
			String userName = callableStatement.getString(2);
			String createdBy = callableStatement.getString(3);
			Date createdDate = callableStatement.getDate(4);
	
	*/
	public CallableStatement getCallableStatement(String connectionName, String query) throws Exception {
		
		return this.getConnection(connectionName).prepareCall(query);
		
	}
	
	public MapAction addOrUpdateQuery(String queryId, String query, String connectionName) {
		
		if(!this.queries.containsKey(queryId)) {
			
			this.queries.put(
				queryId,
				new Query(
					queryId,
					query,
					connectionName
				)
			);
			
			this.setValidAPIValues();
			
			return MapAction.ADD;
			
		} else {
			
			Query queryObject =
				this.queries.get(queryId);
				
			queryObject.setQuery(query);
			queryObject.setConnectionName(connectionName);
			
			return MapAction.UPDATE;
			
		}
		
	}
	
	public MapAction addQuery(JSONDataStructure queryJSON) throws Exception {
		
		if(queryJSON.get("queryId") == null) {
			
			return MapAction.NONE;
			
		}
		
		String queryId =
			queryJSON.get("queryId").getValue().toString();
		
		if(this.queries.containsKey(queryId)) {
		
			return MapAction.ALREADY_EXISTS;
			
		}
		
		return this.addOrUpdateQuery(
			queryId,
			queryJSON.get("query").getValue().toString(),
			queryJSON.get("connectionName").getValue().toString()
		);
		
	}
	
	public MapAction deleteQuery(String queryId) {
		
		if(this.queries.containsKey(queryId)) {
			
			this.queries.remove(queryId);
			
			this.setValidAPIValues();
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public boolean hasQuery(String queryId) {
		return this.queries.containsKey(queryId);
	}
	
	public Query getQuery(String queryId) {
		return this.queries.get(queryId);
	}
	
	public JSONBuilder getJSONBuilder() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(Query query : this.queries.values()) {
			jb.v(query.getJSONBuilder());
		}
		
		return jb.$(']');
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	public void setValidAPIValues() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(Query query : this.queries.values()) {
			jb.v(query.getQueryId());
		}
		
		this.validAPIValues = jb.$(']').getJSON();
	}
	
	public String validAPIValues() {
		
		return this.validAPIValues;
		
	}
	
}