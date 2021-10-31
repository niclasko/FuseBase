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
public enum QueryType {
	SQL(0),
	DML(1),
	DDL(2);
	
	public static final int SIZE = QueryType.values().length;
	
	public static final QueryType DEFAULT_VALUE =
		QueryType.SQL;
	
	public static final String REST_API_LIST =
		Utils.listOfEnumValuesForReSTAPI(QueryType.class);
		
	public static final JSONBuilder jb = JSONBuilder.f();
	
	static {
		
		QueryType.jb.$('[');
		for(QueryType queryType : QueryType.values()) {
			QueryType.jb.v(queryType);
		}
		QueryType.jb.$(']');
		
	}
	
	private int id;

	QueryType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static JSONBuilder getJSONBuilder() {
		return QueryType.jb;
	}
	
	public static void main(String args[]) {
		System.out.println(QueryType.valueOf(QueryType.DEFAULT_VALUE.toString()));
	}
}