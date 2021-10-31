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
import java.io.Serializable;

public class Query implements Serializable {
	private String queryId;
	private String query;
	private String connectionName;
	
	public Query(String queryId, String query, String connectionName) {
		this.queryId = queryId;
		this.query = query;
		this.connectionName = connectionName;
	}
	
	public String getQueryId() {
		return this.queryId;
	}
	
	public String getQuery() {
		return this.query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getConnectionName() {
		return this.connectionName;
	}
	
	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}
	
	public JSONBuilder getJSONBuilder() {
		return
			JSONBuilder.f().
				$('{').
					k("queryId").v(this.getQueryId()).
					k("connectionName").v(this.getConnectionName()).
					k("query").v(this.getQuery()).
				$('}');
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
}