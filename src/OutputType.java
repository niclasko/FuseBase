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
public enum OutputType {
	
	CSV(0),
	JSON_TABULAR(1),
	JSON(2);
	
	public static final int SIZE = OutputType.values().length;
	
	public static final OutputType DEFAULT_VALUE =
		OutputType.JSON;
	
	public static final String REST_API_LIST =
		Utils.listOfEnumValuesForReSTAPI(QueryType.class);
	
	private int id;

	OutputType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
}