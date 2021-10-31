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
import java.io.PrintWriter;

public class CSVWriter extends DataWriter {
	
	private static String BLANK = "";
	private static char QUOTE = '"';
	
	boolean extraInfoCalled;
	
	public CSVWriter(PrintWriter out) {
		super(out, DataWriterType.CSV);
		this.extraInfoCalled = false;
	}
	
	public void headerEntry(String entry, int entryId) {
		this.entry(entry, entryId);
	}
	
	/*public void extraInfo(String key, String value) {
		if(!this.extraInfoCalled) {
			out.print(
				"\n\nStatistics:"
			);
			this.extraInfoCalled = true;
		}
		out.print("\n\t" + key + ": " + value);
	}*/
	
	public void entry(String entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : CSVWriter.BLANK));
		out.print(CSVWriter.QUOTE);
		
		for(int i=0; i<entry.length();i++) {
			if(entry.charAt(i) == CSVWriter.QUOTE) {
				out.print(CSVWriter.QUOTE);
			}
			out.print(entry.charAt(i));
		}
		
		out.print(CSVWriter.QUOTE);
	}
	
	public void entry(double entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : CSVWriter.BLANK));
		out.print(representNull(entry));
	}
	
	public void entry(int entry, int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : CSVWriter.BLANK));
		out.print(entry);
	}
	
	public void nullEntry(int entryId) {
		out.print((entryId > 0 ? DataWriter.FIELD_SEPARATOR : CSVWriter.BLANK));
	}
	
	public void newRow() {
		super.newRow();
		out.print("\n");
	}
}