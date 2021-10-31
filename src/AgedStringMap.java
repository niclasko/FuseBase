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
import java.util.HashMap;
import java.util.HashSet;

public class AgedStringMap {
	
	private HashMap<String, AgedString> map;
	private long latestAge;
	
	public AgedStringMap() {
		
		this.map =
			new HashMap<String, AgedString>();
		this.latestAge = -1;
		
	}
	
	public void setLatestAge(long latestAge) {
		
		this.latestAge = latestAge;
		
	}
	
	public void put(String key, String value, long age) {
		
		if(!this.map.containsKey(key)) {
			
			this.map.put(
				key,
				new AgedString(
					value,
					age
				)
			);
			
		} else {
			
			this.map.get(key).setValue(
				value,
				age
			);
			
		}
		
	}
	
	public boolean containsKey(String key) {
		
		return (
			this.map.containsKey(key) &&
			this.map.get(key).value(this.latestAge) != null
		);
		
	}
	
	public String get(String key) {
		
		return this.get(
			key,
			null
		);
		
	}
	
	public String get(String key, String defaultValue) {

		AgedString agedString =
			this.map.get(key);
			
		if(agedString != null) {
			
			return agedString.value(
				this.latestAge,
				defaultValue
			);
			
		}
		
		return defaultValue;
		
	}
	
	public int size() {
		
		int size = 0;
		
		for(AgedString agedString : this.map.values()) {
			
			if(agedString.age() == this.latestAge) {
				size++;
			}
			
		}
		
		return size;
		
	}
	
	public HashSet<String> keySet() {
		
		HashSet<String> keys =
			new HashSet<String>();
			
		for(String key : this.map.keySet()) {
			
			if(this.map.get(key).value(this.latestAge) != null) {
				
				keys.add(key);
				
			}
			
		}
		
		return keys;
		
	}
	
}