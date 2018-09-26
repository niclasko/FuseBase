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
import java.io.DataOutput;

public class PersistedDataOutputStream implements DataOutput {
	
	private StringBuilder data;
	
	public PersistedDataOutputStream() {
		this.data = new StringBuilder();
	}
	
	public int size() {
		return this.data.length();
	}
	
	public void flush() {
		;
	}
	
	public void write(byte[] b) {
		;
	}
	
	public void write(byte[] b, int off, int len) {
		;
	}
	
	public void write(int b) {
		;
	}
	
	public void writeBoolean(boolean v) {
		;
	}
	
	public void writeByte(int v) {
		;
	}
	
	public void writeBytes(String s) {
		data.append(s);
	}
	
	public void writeChar(int v) {
		;
	}
	
	public void writeChars(String s) {
		;
	}
	
	public void writeDouble(double v) {
		;
	}
	
	public void writeFloat(float v) {
		;
	}
	
	public void writeInt(int v) {
		;
	}
	
	public void writeLong(long v) {
		;
	}
	
	public void writeShort(int v) {
		;
	}
	
	public void writeUTF(String str) {
		;
	}
	
	public String getData() {
		return data.toString();
	}
	
	public static void main(String args[]) {
		PersistedDataOutputStream out = new PersistedDataOutputStream();
		
		out.writeBytes("Hello World!");
		
		System.out.println(out.getData());
	}
	
}