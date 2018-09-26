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
import java.util.Vector;

public class JSONWriter extends DataWriter {
	
	private Vector<String> keys;
	private Vector<KeyValue> extraInfo;
	
	public JSONWriter(PrintWriter out) {
		super(out, DataWriterType.JSON);
		this.keys = new Vector<String>();
		this.extraInfo = new Vector<KeyValue>();
	}
	
	public void init() {
		out.print("{\"data\": [");
	}
	
	public void extraInfo(String key, String value) {
		this.extraInfo.add(new KeyValue(key, value));
	}
	
	public void finish() {
		out.print("]");
		
		for(KeyValue entry : this.extraInfo) {
			out.print(", \"" + entry.getKey() + "\": " + entry.getValue());
		}
		
		out.print("}");
	}
	
	private String getKey(int keyIndex) {
		return "\"" + this.keys.get(keyIndex) + "\": ";
	}
	
	public void headerEntry(String entry, int entryId) {
		this.keys.setSize(entryId+1);
		this.keys.add(
			entryId,
			JSONBuilder.jsonEscape(entry)
		);
	}
	
	public void entry(String entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + this.getKey(entryId) + "\"" + JSONBuilder.jsonEscape(entry) + "\"");
	}
	
	public void entry(double entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + this.getKey(entryId) + super.representNull(entry));
	}
	
	public void entry(int entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + this.getKey(entryId) + entry);
	}
	
	public void nullEntry(int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + this.getKey(entryId) + "null");
	}
	
	public void newRow() {
		super.newRow();
		if(super.getRecordCount() > 1) {
			out.print(DataWriter.FIELD_SEPARATOR);
		}
	}
	
	public void beginRow() {
		if(super.getRecordCount() > 0) {
			out.print("{");
		}
	}
	
	public void endRow() {
		if(super.getRecordCount() > 0) {
			out.print("}");
		}
	}
}