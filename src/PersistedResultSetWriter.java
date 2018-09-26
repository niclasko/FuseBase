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
public class PersistedResultSetWriter extends DataWriter {
	
	private PersistedResultSet persistedResultSet;
	
	public PersistedResultSetWriter() {
		super(null, DataWriterType.PERSISTED_RESULTSET);
		this.persistedResultSet = new PersistedResultSet();
	}
	
	public PersistedResultSet getPersistedResultSet() {
		return this.persistedResultSet;
	}
	
	public void init() {
		this.persistedResultSet.addRecord();
	}
	
	public void headerEntry(String entry, int entryId) {
		this.persistedResultSet.addField(entry);
	}
	
	public void entry(String entry, int entryId) {
		this.persistedResultSet.setValue(entryId, new ValueString(entry));
	}
	
	public void entry(double entry, int entryId) {
		this.persistedResultSet.setValue(entryId, new ValueDouble(entry));
	}
	
	public void entry(int entry, int entryId) {
		this.persistedResultSet.setValue(entryId, new ValueInt(entry));
	}
	
	public void newRow() {
		this.recordCount++;
		this.persistedResultSet.addRecord();
	}
}