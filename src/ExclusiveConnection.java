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
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class ExclusiveConnection {
	
	private Connection connection;
	private boolean isBusy;
	
	public ExclusiveConnection(Connection connection) {
		
		this.connection = connection;
		this.isBusy = false;
		
	}
	
	public ExclusiveConnection connection() {
		
		return this;
		
	}
	
	public boolean isBusy() {
		
		return this.isBusy;
		
	}
	
	public synchronized Statement createStatement() throws SQLException {
		
		this.isBusy = true;
		
		Statement statement =
			this.connection.createStatement();
			
		this.isBusy = false;
		
		return statement;
		
	}
	
	public synchronized Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		
		this.isBusy = true;
		
		Statement statement =
			this.connection.createStatement(
				resultSetType,
				resultSetConcurrency
			);
			
		this.isBusy = false;
		
		return statement;
		
	}
	
	public synchronized PreparedStatement prepareStatement(String sql) throws SQLException {
		
		this.isBusy = true;
		
		PreparedStatement preparedStatement =
			this.connection.prepareStatement(sql);
		
		this.isBusy = false;
		
		return preparedStatement;
		
	}
	
}