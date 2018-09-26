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
import java.util.Vector;

public class JSONArray extends JSONDataStructure {
	
	private Vector<JSONKeyValue> entries;
	
	public JSONArray() {
		
		this.entries =
			new Vector<JSONKeyValue>();
		
	}
	
	public void add(int value) {
		
		this.add(new Integer(value));
		
	}
	
	public void add(double value) {
		
		this.add(new Double(value));
		
	}
	
	public void add(Object value) {
		
		this.lastEntry =
			new JSONKeyValue(
				null,
				value
			);
		
		this.entries.add(
			this.lastEntry
		);
		
	}
	
	public JSONKeyValue get(int index) {
		return this.entries.get(index);
	}
	
	public JSONKeyValue[] entries() {
		
		return
			this.entries.toArray(
				new JSONKeyValue[this.entries.size()]
			);
		
	}
	
	public void toString(StringBuilder stringBuilder) {
		
		int i = 0;
		
		stringBuilder.append("[");
		
		for(JSONKeyValue keyValue : this.entries()) {
			
			if(i++ > 0) {
				stringBuilder.append(", ");
			}
			
			keyValue.toString(stringBuilder);
			
		}
		
		stringBuilder.append("]");
		
	}
	
}