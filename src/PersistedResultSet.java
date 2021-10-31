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
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class PersistedResultSet {
	private LinkedHashMap<String, Integer> fields;
	private LinkedList<LinkedList<Value>> records;
	private LinkedList<Value> currentRecord;
	private int recordIndex;
	
	public PersistedResultSet() {
		this.fields = new LinkedHashMap<String, Integer>();
		this.records = new LinkedList<LinkedList<Value>>();
		this.currentRecord = null;
		this.recordIndex = -1;
	}
	
	public void addField(String fieldName) {
		this.fields.put(fieldName, this.fields.size());
	}
	
	public void addRecord() {
		this.currentRecord = new LinkedList<Value>();
		for(int i=0; i<this.fields.size(); i++) {
			this.currentRecord.add(null);
		}
		this.records.add(this.currentRecord);
	}
	
	public void setValue(int fieldIndex, Value value) {
		this.currentRecord.add(fieldIndex, value);
	}
	
	public void setFirst() {
		this.recordIndex = 0;
		this.currentRecord = this.records.get(this.recordIndex);
	}
	
	public boolean nextRecord() {
		if(this.recordIndex+1 >= this.records.size()) {
			return false;
		}
		this.currentRecord = this.records.get(++this.recordIndex);
		return true;
	}
	
	public Value getValue(int fieldIndex) {
		return this.currentRecord.get(fieldIndex);
	}
	
	public Value getValue(String fieldName) {
		return this.getValue(this.fields.get(fieldName));
	}
	
	public int getRecordCount() {
		return this.records.size();
	}
	
	public void print() {
		this.setFirst();
		for(String field : this.fields.keySet()) {
			System.out.print(field + ";");
		}
		System.out.println();
		while(this.nextRecord()) {
			for(String field : this.fields.keySet()) {
				System.out.print(this.getValue(field).getValue() + ";");
			}
			System.out.println();
		}
	}
}