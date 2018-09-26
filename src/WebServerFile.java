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

public class WebServerFile {
	private String fileName;
	private PrintWriter output;
	private long bytesWritten;
	
	public WebServerFile(String fileName, PrintWriter output) {
		this.fileName = fileName;
		this.output = output;
		this.bytesWritten = 0;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public PrintWriter getOutput() {
		return this.output;
	}
	
	public long getBytesWritten() {
		return this.bytesWritten;
	}
	
	public void setBytesWritten(long bytesWritten) {
		this.bytesWritten = bytesWritten;
	}
	
	public void print(String data, boolean flush) {
		this.bytesWritten += data.length();
		output.print(data);
		if(flush) {
			output.flush();
		}
	}
	
	public void close() {
		try {
			output.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}