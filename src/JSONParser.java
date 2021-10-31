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
public class JSONParser {

	private JSONBuilderDynamic json;
	private StringWalker stringWalker;

	public JSONParser(String unparsedJson) throws Exception {
		
		this.json = new JSONBuilderDynamic();
		this.stringWalker =
			new StringWalker(
				unparsedJson
			);
		
		this.parse();
		
	}
	
	private char pc() {
		
		return this.stringWalker.previousCharacter();
		
	}
	
	private char cc() {
		
		return this.stringWalker.currentCharacter();
		
	}
	
	private char nc() {
		
		return this.stringWalker.nextCharacter();
		
	}
	
	private char lookAhead(int offset) {
		
		return this.stringWalker.lookAhead(offset);
		
	}
	
	private void jumpAhead(int offset) {
		
		this.stringWalker.jumpAhead(offset);
		
	}
	
	private void next() {
		
		this.stringWalker.next();
		
		while(whiteSpace()) {
			
			this.stringWalker.next();
			
		}
		
	}
	
	private void resetAccumulated() {
		
		this.stringWalker.resetAccumulated();
		
	}
	
	public void accumulate() {
		
		this.stringWalker.accumulate();
		
	}
	
	public String getAccumulated() {
		
		return this.stringWalker.getAccumulated();
		
	}
	
	private void parse() throws Exception {
		
		this.next();
		
		if(!(array() || associativeArray())) {
			
			error("Expected array or associative array.");
			
		}
		
	}
	
	private boolean array() throws Exception {
		
		if(openingBracket()) {
			
			this.next();
			
			arrayEntry();
			
			if(!closingBracket()) {
				
				error("Expected closing bracket.");
				
			}
			
		} else {
			
			return false;
			
		}
		
		this.next();
		
		return true;
		
	}
	
	private boolean associativeArray() throws Exception {
		
		if(openingCurlyBracket()) {
			
			this.next();
			
			associativeArrayEntry();
			
			if(!closingCurlyBracket()) { 
				
				error("Expected closing curly bracket.");
				
			}
			
		} else {
			
			return false;
			
		}
		
		this.next();
		
		return true;
		
	}
	
	private boolean arrayEntry() throws Exception {
		
		if(entry()) {
			
			skipWhiteSpace();
			
			if(comma()) {
				
				this.next();
				
				arrayEntry();
								
			}
			
		} else {
			
			return false;
			
		}
		
		return true;
		
	}
	
	private boolean associativeArrayEntry() throws Exception {
		
		if(keyValuePair()) {
			
			skipWhiteSpace();
			
			if(comma()) {
				
				this.next();
				
				associativeArrayEntry();
				
			}
			
		} else {
			
			return false;
			
		}
		
		return true;
		
	}
	
	public boolean keyValuePair() throws Exception {
		
		if(key()) {
			
			this.next();
			
			if(colon()) {
				
				this.next();
				
				if(!entry()) {
					
					error("Expected value entry.");
					
				}
				
			} else {
				
				error("Expected colon.");
				
			}
			
		} else {
			
			return false;
			
		}
		
		return true;
		
	}
	
	private boolean key() throws Exception {
		
		if(string()) {
			
			this.json.k(this.getAccumulated());
			
			return true;
			
		} else if(!closingCurlyBracket()) {
			
			error("Associative array key must be a string.");
			
		}
		
		return false;
		
	}
	
	private boolean entry() throws Exception {
		
		if(string()) {
			
			this.json.v(this.getAccumulated());
			
			this.next();
			
			return true;
			
		} else if(booleanValue()) {
			
			this.json.v(
				Boolean.parseBoolean(
					this.getAccumulated()
				)
			);
			
			return true;
			
		} else if(value()) {
			
			try {
				
				this.json.v(
					Integer.parseInt(this.getAccumulated())
				);
				
			} catch (Exception e) {
				
				this.json.v(
					Double.parseDouble(this.getAccumulated())
				);
				
			}
			
			return true;
			
		} else if(nullValue()) {
			
			this.json.v((Object)null);
			
			return true;
			
		} else if(array() || associativeArray()) {
			
			return true;
			
		}
		
		return false;
		
	}
	
	private boolean openingBracket() {
		
		if(this.cc() == '[') {
			
			this.json.$('[');
			
			return true;
			
		}
		
		return false;
		
	}
	
	private boolean closingBracket() {
		
		if(this.cc() == ']') {
			
			this.json.$(']');
			
			return true;
			
		}
		
		return false;
		
	}
	
	private boolean openingCurlyBracket() {
		
		if(this.cc() == '{') {
			
			this.json.$('{');
			
			return true;
			
		}
		
		return false;
		
	}
	
	private boolean closingCurlyBracket() {
		
		if(this.cc() == '}') {
			
			this.json.$('}');
			
			return true;
			
		}
		
		return false;
		
	}
	
	private boolean doubleQuote() {
		
		return this.cc() == '"';
		
	}
	
	private boolean singleQuote() {
		
		return this.cc() == '\'';
		
	}
	
	private boolean comma() {
		
		return this.cc() == ',';
		
	}
	
	private void skipWhiteSpace() {
		
		while(whiteSpace(this.cc())) {
			
			this.next();
			
		}
		
	}
	
	private boolean colon() {
		
		return this.cc() == ':';
		
	}
	
	private boolean dot() {
		
		return this.cc() == '.';
		
	}
	
	private boolean escape() {
		
		return this.cc() == '\\';
		
	}
	
	private boolean previousEscape() {
		
		return this.pc() == '\\';
		
	}
	
	private boolean whiteSpace(char c) {
		
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
		
	}
	
	private boolean whiteSpace() {
		
		return whiteSpace(this.cc());
		
	}
	
	private boolean string() {
		
		if(!doubleQuote()) {
			
			return false;
			
		}
		
		this.next();
		
		this.resetAccumulated();
		
		while( !(doubleQuote() && !previousEscape()) ) {
			
			this.accumulate();
			
		}
		
		return true;
		
	}
	
	private boolean numeric() {
		
		switch(this.cc()) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return true;
		}
		
		return false;
		
	}
	
	private boolean value() throws Exception {
		
		if(!numeric()) {
			
			return false;
			
		}
		
		this.resetAccumulated();
		
		while(numeric()) {
			
			this.accumulate();
			
		}
		
		if(dot()) {
			
			this.accumulate();
			
			if(!numeric()) {
				
				error("Expected number after dot.");
				
			}
			
			while(numeric()) {
				
				this.accumulate();
				
			}
			
			return true;
			
		} else {
			
			return true;
			
		}
		
	}
	
	private boolean nullValue() {
		
		if(	lookAhead(0) == 'n' &&
			lookAhead(1) == 'u' &&
			lookAhead(2) == 'l' &&
			lookAhead(3) == 'l' ) {
			
			jumpAhead(4);
			
			return true;
				
		}
		
		return false;
		
	}
	
	private boolean booleanValue() {
		
		if(	(lookAhead(0) == 't' || lookAhead(0) == 'T') &&
			(lookAhead(1) == 'r' || lookAhead(1) == 'R') &&
			(lookAhead(2) == 'u' || lookAhead(2) == 'U') &&
			(lookAhead(3) == 'e' || lookAhead(3) == 'E') ) {
			
			this.resetAccumulated();
			
			this.accumulate();
			this.accumulate();
			this.accumulate();
			this.accumulate();
			
			return true;
				
		} else if (	(lookAhead(0) == 'f' || lookAhead(0) == 'F') &&
					(lookAhead(1) == 'a' || lookAhead(1) == 'A') &&
					(lookAhead(2) == 'l' || lookAhead(2) == 'L') &&
					(lookAhead(3) == 's' || lookAhead(3) == 'S') &&
					(lookAhead(4) == 'e' || lookAhead(4) == 'E') ) {
			
			this.resetAccumulated();
			
			this.accumulate();
			this.accumulate();
			this.accumulate();
			this.accumulate();
			this.accumulate();
			
			return true;
			
		}
		
		return false;
		
	}
	
	public void error(String msg) throws Exception {
		
		throw new Exception(
			msg + " Got '" + this.cc() + "'. Parsed so far: '" + this.stringWalker.soFar() + "'"
		);
		
	}
	
	public String toString() {
		
		return this.json.toString();
		
	}
	
	public int jsonEndsAtPosition() {
		return this.stringWalker.position();
	}
	
	public JSONDataStructure getJSONDataStructure() {
		
		return this.json.getJSONStructure();
		
	}
	
	public static void main(String args[]) {
		
		JSONParser jsonParser = null;
		
		try {
			
			String fileData =
				new String(
					java.nio.file.Files.readAllBytes(
						java.nio.file.Paths.get("query.json")
					)
				);
			
			String s = "{\"Authorization\": \"Basic\"}) sq inner join table as t2 on (sq.id = t2.id)";
			
			jsonParser =
				new JSONParser(
					s
				);
			
			System.out.println(jsonParser.toString() + ". Remaining text: " + s.substring(jsonParser.jsonEndsAtPosition()));
			
		} catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
}