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
public class StringWalker {
	
	private String string;
	private int position;
	private StringBuffer accumulatedString;
	
	public StringWalker(String string) {
		
		this.string = string;
		this.position = -1;
		this.accumulatedString = new StringBuffer(this.string.length());
		
	}
	
	public char previousCharacter() {
		
		if(this.position <= 0) {
			
			return '\0';
			
		}
		
		return this.string.charAt(this.position - 1);
		
	}
	
	public char currentCharacter() {
		
		if(this.position == this.string.length() || this.position == -1) {
			
			return '\0';
						
		}
		
		return this.string.charAt(this.position);
		
	}
	
	public char nextCharacter() {
		
		if(this.position >= this.string.length() - 2) {
			
			return '\0';
						
		}
		
		return this.string.charAt(this.position + 1);
		
	}
	
	public char lookAhead(int offset) {
		
		if(this.position + offset >= this.string.length()) {
			
			return '\0';
			
		}
		
		return this.string.charAt(this.position + offset);
		
	}
	
	public void jumpAhead(int offset) {
		
		if(this.position + offset >= this.string.length()) {
			
			this.position = this.string.length();
			
		}
		
		this.position += offset;
		
	}
	
	public void accumulate() {
		
		this.accumulatedString.append(this.currentCharacter());
		
		this.next();
		
	}
	
	public String getAccumulated() {
		
		return this.accumulatedString.toString();
		
	}
	
	public void resetAccumulated() {
		
		this.accumulatedString.delete(0, this.accumulatedString.capacity());
		
	}
	
	public void reset() {
		
		this.position = 0;
		
	}
	
	public char next() {
		
		if(this.position == this.string.length()) {
			
			return '\0';
			
		}
		
		this.position++;
		
		return this.currentCharacter();
		
	}
	
	public String soFar() {
		
		return this.string.substring(0, this.position);
		
	}
	
	public int position() {
		return this.position;
	}
	
}