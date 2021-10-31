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
import java.util.Arrays;

public class LiteString {
	
	private char[] data;
	private int length;
	
	private static final char NULL = '\0';
	
	public LiteString(int capacity) {
		
		this.data = new char[capacity];
		this.length = 0;
		
	}
	
	public void append(String s) {
		
		for(int i=0; i<s.length(); i++) {
			
			this.data[this.length++] =
				s.charAt(i);
			
		}
		
		this.data[this.length] = LiteString.NULL;
		
	}
	
	public void append(byte[] bytes) {
		
		for(int i=0; i<bytes.length; i++) {
			
			this.data[this.length++] = (char)bytes[i];
			
		}
		
		this.data[this.length] = LiteString.NULL;
		
	}
	
	public void append(char c) {
		
		this.data[this.length++] = c;
		
		this.data[this.length] = LiteString.NULL;
		
	}
	
	public void clear() {
		
		this.length = 0;
		
	}
	
	public char[] data() {
		
		return this.data;
		
	}
	
	public int length() {
		
		return this.length;
		
	}
	
	public int capacity() {
		
		return this.data.length;
		
	}
	
	public char g(int index) {
		return this.data[index];
	}
	
	public String toString() {
		
		return new String(this.data, 0, this.length);
		
	}
	
	public int indexOf(char characterToFind) {
		
		return this.indexOf(0, characterToFind);
		
	}
	
	public int indexOf(int offset, char characterToFind) {
		
		for(int i=offset; i<this.length; i++) {
			
			if(this.data[i] == characterToFind) {
				
				return i;
				
			}
			
		}
		
		return -1;
		
	}
	
	public int indexOf(String s) {
		
		return this.indexOf(0, s);
		
	}
	
	public int indexOf(int offset, String s) {
		
		int i = 0;
		int j = offset;
		
		for(; j<this.length; j++) {

			if(this.data[j] == s.charAt(i)) {
				i++;
			} else if(this.data[j] != s.charAt(i) && i > 0) {
				return -1;
			}
			
			if(i == s.length()) {
				break;
			}

		}
		
		if(i < s.length()) {
			return -1;
		}
		
		return j;
		
	}
	
	public static void main(String args[]) {
		
		LiteString s =
			new LiteString(10000);
			
		s.append("Hello World!");
		
		System.out.println(s.toString());
		
		s.clear();
		
		System.out.println(s.toString());
		
	}
	
}