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
import java.io.PrintWriter;

public class DataWriter {
	public static final String FIELD_SEPARATOR = ",";
	protected PrintWriter out;
	private DataWriterType dataWriterType;
	protected int recordCount;
	
	public DataWriter(PrintWriter out, DataWriterType dataWriterType) {
		this.out = out;
		this.dataWriterType = dataWriterType;
		this.recordCount = 0;
	}
	
	public int getRecordCount() {
		return this.recordCount;
	}
	
	public DataWriterType getDataWriterType() {
		return this.dataWriterType;
	}
	
	public void init() {
		;
	}
	
	public void extraInfo(String key, String value) {
		;
	}
	
	public void finish() {
		;
	}
	
	public void headerEntry(String entry, int entryId) {
		;
	}
	
	public void entry(String entry, int entryId) {
		;
	}
	
	public void entry(double entry, int entryId) {
		;
	}
	
	public void entry(int entry, int entryId) {
		;
	}
	
	public void nullEntry(int entryId) {
		;
	}
	
	public void newRow() {
		this.recordCount++;
	}
	
	public void beginRow() {
		;
	}
	
	public void endRow() {
		;
	}
	
	public String representNull(double entry) {
		return (!Double.isNaN(entry) ? entry + "" : "null");
	}
}