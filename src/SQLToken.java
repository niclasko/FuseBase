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
import java.lang.Math;

public class SQLToken {
	private SQLTokenizer sqlTokenizer;
	private String tokenText;
	private String tokenTextUpperCase;
	private SQLQuotation sqlQuotation;
	private SQLToken previousToken;
	private SQLToken nextToken;
	private SQLKeyWord sqlKeyWord;
	
	private int position;
	private int lineNumber;
	
	public SQLToken(SQLTokenizer sqlTokenizer, String tokenText, SQLQuotation sqlQuotation, SQLToken previousToken, int position, int lineNumber) {
		
		this.sqlTokenizer = sqlTokenizer;
		this.tokenText = tokenText;
		this.tokenTextUpperCase = tokenText.toUpperCase();
		this.sqlQuotation = sqlQuotation;
		this.previousToken = previousToken;
		this.nextToken = null;
		
		if(this.previousToken != null) {
			this.previousToken.setNextToken(this);
		}
		
		this.setSQLKeyWord();
		
		this.position = position;
		this.lineNumber = lineNumber;
		
	}
	
	public static SQLToken createRootToken() {
		return new SQLToken(
			null,
			"",
			SQLQuotation.NONE,
			null,
			-1,
			-1
		);
	}
	
	public void overrideSQLKeyWord(SQLKeyWord sqlKeyWord) {
		this.sqlKeyWord = sqlKeyWord;
	}
	
	public void makeUpperCase() {
		this.tokenText = this.tokenTextUpperCase;
	}
	
	private void setSQLKeyWord() {
		
		this.sqlKeyWord =
			SQLKeyWord.findByDisplayName(
				this.tokenTextUpperCase
			);
		
		if(this.sqlKeyWord == SQLKeyWord.UNKNOWN) {
			if(this.tokenText.length() >=2
				&& (this.tokenText.substring(0,2).equals("--")
				|| this.tokenText.substring(0,2).equals("/*"))) {
				this.sqlKeyWord = SQLKeyWord.COMMENT;
			} else if(this.sqlQuotation == SQLQuotation.SINGLE) {
				this.sqlKeyWord = SQLKeyWord.SINGLE_QUOTED_LITERAL;
			} else if(this.sqlQuotation == SQLQuotation.DOUBLE) {
				this.sqlKeyWord = SQLKeyWord.DOUBLE_QUOTED_LITERAL;
			} else if(this.sqlQuotation == SQLQuotation.NONE) {
				if(SQLToken.isNumeric(this.tokenText)) {
					this.sqlKeyWord = SQLKeyWord.NUMBER_ATOM;
				} else {
					this.sqlKeyWord = SQLKeyWord.LITERAL;
				}
			}
		} else if(this.sqlKeyWord == SQLKeyWord.COMMA) {
			if(this.sqlQuotation == SQLQuotation.SINGLE) {
				this.sqlKeyWord = SQLKeyWord.SINGLE_QUOTED_LITERAL;
			} else if(this.sqlQuotation == SQLQuotation.DOUBLE) {
				this.sqlKeyWord = SQLKeyWord.DOUBLE_QUOTED_LITERAL;
			}
		}
		
	}
	
	public String tokenInSQL() {
		return "\"" + this.sqlTokenizer().sql().substring(0, Math.max(1,this.position())-1) + "\".\nGot: " + this.toString();
	}
	
	public SQLTokenizer sqlTokenizer() {
		return this.sqlTokenizer;
	}
	
	public String tokenText() {
		return this.tokenText;
	}
	
	public String tokenTextUpperCase() {
		return this.tokenTextUpperCase;
	}
	
	public void append(String tokenText) {
		
		this.tokenText += tokenText;
		this.tokenTextUpperCase = this.tokenText.toUpperCase();
		
		this.setSQLKeyWord();
		
	}
	
	public SQLQuotation sqlQuotation() {
		return this.sqlQuotation;
	}
	
	public boolean quoted() {
		return (this.sqlQuotation != SQLQuotation.NONE);
	}
	
	public boolean singleQuoted() {
		return (this.sqlQuotation == SQLQuotation.SINGLE);
	}
	
	public boolean doubleQuoted() {
		return (this.sqlQuotation == SQLQuotation.DOUBLE);
	}
	
	public SQLToken previousToken() {
		return this.previousToken;
	}
	
	public SQLToken nextToken() {
		return this.nextToken;
	}
	
	public void setNextToken(SQLToken nextToken) {
		this.nextToken = nextToken;
	}
	
	public SQLKeyWord sqlKeyWord() {
		return this.sqlKeyWord;
	}
	
	public int position() {
		return this.position;
	}
	
	public int lineNumber() {
		return this.lineNumber;
	}
	
	public String toString() {
		return
			this.sqlQuotation.toString() +
			this.tokenText +
			this.sqlQuotation.toString();
	}
	
	public static boolean isNumeric(String s) {  
		try {  
			double d = Double.parseDouble(s);  
		} catch(Exception e) {  
			return false;  
		}  
		return true;  
	}
}