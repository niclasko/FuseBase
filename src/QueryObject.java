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
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class QueryObject {
	
	private Statement statement;
	private ResultSet resultSet;
	private ResultSetMetaData resultSetMetaData;
	
	public QueryObject(Statement statement, ResultSet resultSet, ResultSetMetaData resultSetMetaData) {
		this.statement = statement;
		this.resultSet = resultSet;
		this.resultSetMetaData = resultSetMetaData;
	}
	
	public ResultSet resultSet() {
		return this.resultSet;
	}
	
	public boolean next() throws Exception {
		return this.resultSet.next();
	}
	
	public Object getObject(int column) throws Exception {
		return this.resultSet.getObject(column);
	}
	
	public Object getString(int column) throws Exception {
		return this.resultSet.getString(column);
	}
	
	public ResultSetMetaData resultSetMetaData() {
		return this.resultSetMetaData;
	}
	
	public int getColumnCount() throws Exception {
		return this.resultSetMetaData.getColumnCount();
	}
	
	public String getColumnName(int column) throws Exception {
		return this.resultSetMetaData.getColumnName(column);
	}
	
	public String getColumnClassName(int column) throws Exception {
		return this.resultSetMetaData.getColumnName(column);
	}
	
	public int close() {
		
		try {
			statement.close();
			resultSet.close();
		} catch(Exception e) {
			return -1;
		}
		
		return 1;
		
	}
	
}