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

public class JSONAssociativeArray extends JSONDataStructure {
	
	private HashMap<String, JSONKeyValue> entries;
	
	public JSONAssociativeArray() {
		
		this.entries =
			new HashMap<String, JSONKeyValue>();
		
	}
	
	public void addKey(String key) {
		
		this.lastEntry =
			new JSONKeyValue(
				key,
				null
			);
		
		this.entries.put(
			key,
			this.lastEntry
		);
		
	}
	
	public void add(String key, Object value) {
		
		this.lastEntry =
			new JSONKeyValue(
				key,
				value
			);
		
		this.entries.put(
			key,
			this.lastEntry
		);
		
	}
	
	public void add(Object value) {
		
		this.setLastEntryValue(
			value
		);
		
	}
	
	public void add(int value) {
		
		this.setLastEntryValue(
			value
		);
		
	}
	
	public void add(double value) {
		
		this.setLastEntryValue(
			value
		);
		
	}
	
	public JSONKeyValue get(String key) {
		
		if(this.entries.containsKey(key)) {
			return this.entries.get(key);
		}
		
		
		return null;
		
	}
	
	public JSONKeyValue[] entries() {
		
		return
			this.entries.values().toArray(
				new JSONKeyValue[this.entries.size()]
			);
		
	}
	
	public JSONKeyValue lastEntry() {
		
		return this.lastEntry;
		
	}
	
	public void toString(StringBuilder stringBuilder) {
		
		int i = 0;
		
		stringBuilder.append("{");
		
		for(JSONKeyValue keyValue : this.entries()) {
			
			if(i++ > 0) {
				stringBuilder.append(", ");
			}
			
			stringBuilder.append(
				keyValue.getJSONFormattedKey()
			);
			
			keyValue.toString(stringBuilder);
			
			
		}
		
		stringBuilder.append("}");
		
	}
	
}