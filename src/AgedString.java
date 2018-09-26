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
public class AgedString {
	
	private String value;
	private long age;
	
	public AgedString(String value, long age) {
		this.value = value;
		this.age = age;
	}
	
	public void setValue(String value, long age) {
		this.value = value;
		this.age = age;
	}
	
	public String value(long age) {
		if(age == this.age) {
			return this.value;
		}
		return null;
	}
	
	public String value(long age, String defaultvalue) {
		if(age == this.age) {
			return this.value;
		}
		return defaultvalue;
	}
	
	public long age() {
		return this.age;
	}
	
	public String toString() {
		return this.value;
	}
	
}