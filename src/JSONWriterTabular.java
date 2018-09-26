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

public class JSONWriterTabular extends DataWriter {
	
	public JSONWriterTabular(PrintWriter out) {
		super(out, DataWriterType.JSON_TABULAR);
	}
	
	public void init() {
		out.print("{\"data\": [");
	}
	
	public void finish() {
		out.print("], \"rowCount\": " + super.getRecordCount() + "}");
	}
	
	public void headerEntry(String entry, int entryId) {
		this.entry(entry, entryId);
	}
	
	public void entry(String entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + "\"" + JSONBuilder.jsonEscape(entry) + "\"");
	}
	
	public void entry(double entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + representNull(entry));
	}
	
	public void entry(int entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + entry);
	}
	
	public void nullEntry(int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : "") + "null");
	}
	
	public void newRow() {
		super.newRow();
		out.print(DataWriter.FIELD_SEPARATOR);
	}
	
	public void beginRow() {
		out.print("[");
	}
	
	public void endRow() {
		out.print("]");
	}
}