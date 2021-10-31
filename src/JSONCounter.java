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
public class JSONCounter {
	
	private int arrayCount;
	private int associativeArrayCount;
	private int arrayEntries;
	private int associativeArrayEntries;
	
	public JSONCounter() {
		this.arrayCount = 0;
		this.associativeArrayCount = 0;
		this.arrayEntries = 0;
		this.associativeArrayEntries = 0;
	}
	
	public int arrayCount() {
		return this.arrayCount;
	}
	
	public int associativeArrayCount() {
		return this.associativeArrayCount;
	}
	
	public int arrayEntries() {
		return this.arrayEntries;
	}
	
	public int associativeArrayEntries() {
		return this.associativeArrayEntries;
	}
	
	public int incrementArrayCount() {
		return this.arrayCount++;
	}
	
	public int incrementAssociativeArrayCount() {
		return this.associativeArrayCount++;
	}
	
	public int incrementArrayEntries() {
		return this.arrayEntries++;
	}
	
	public int incrementAssociativeArrayEntries() {
		return this.associativeArrayEntries++;
	}
	
	public void resetArrayEntries() {
		this.arrayEntries = 0;
	}
	
	public void resetAssociativeArrayEntries() {
		this.associativeArrayEntries = 0;
	}
	
	public String toString() {
		return
			"arrayCount: " + this.arrayCount + "\n" +
			"associativeArrayCount: " + this.associativeArrayCount + "\n" +
			"arrayEntries: " + this.arrayEntries + "\n" +
			"associativeArrayEntries: " + this.associativeArrayEntries + "\n";
	}
}