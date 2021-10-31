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
import java.util.Stack;
import java.util.Date;

public class JSONBuilder {
	
	private StringBuffer json;
	
	private Stack<JSONCounter> counters;
	private JSONCounter currentCounter;
	
	private Stack<JSONBuilderState> jsonBuilderStates;
	private JSONBuilderState currentJsonBuilderState;
	
	private static JSONBuilder jsonBuilder;
	
	public JSONBuilder(StringBuffer stringBuffer) {
		
		this.json = stringBuffer;
		
		this.counters = new Stack<JSONCounter>();
		this.currentCounter = new JSONCounter();
		
		this.jsonBuilderStates = new Stack<JSONBuilderState>();
		this.currentJsonBuilderState = JSONBuilderState.UNSPECIFIED;
		
	}
	
	public JSONBuilder() {
		
		this(
			new StringBuffer(
				10000
			)
		);
		
	}
	
	public StringBuffer getStringBuffer() {
		
		return this.json;
		
	}
	
	public void reset() {
		
		this.json.delete(
			0,
			this.json.length()
		);
		
	}
	
	public static String quote() {
		
		return "\"".intern();
		
	}
	
	private void beginSub() {
		
		this.counters.push(this.currentCounter);
		this.currentCounter = new JSONCounter();
		
	}
	
	private void endSub() {
		
		if(this.counters.size() == 0) {
			return;
		}
		
		this.currentCounter =
			this.counters.pop();
			
	}
	
	private void beginArray() {
		
		this.currentCounter.resetArrayEntries();
		
		json.append(
			(this.currentCounter.incrementArrayCount() > 0 ? ",".intern() : "".intern())
		);
		
		json.append(
			"[".intern()
		);
		
		this.jsonBuilderStates.push(this.currentJsonBuilderState);
		this.currentJsonBuilderState = JSONBuilderState.IN_ARRAY;
		
	}
	
	private void endArray() {
		
		json.append(
			"]"
		);
		
		this.currentJsonBuilderState = this.jsonBuilderStates.pop();
		
	}
	
	private void beginAssociativeArray() {
		
		this.currentCounter.resetAssociativeArrayEntries();
		
		json.append(
			(this.currentCounter.incrementAssociativeArrayCount() > 0 ? ",".intern() : "".intern())
		);
		
		json.append(
			"{".intern()
		);
		
		this.jsonBuilderStates.push(this.currentJsonBuilderState);
		this.currentJsonBuilderState = JSONBuilderState.IN_HASH;
		
	}
	
	private void endAssociativeArray() {
		
		json.append(
			"}".intern()
		);
		
		this.currentJsonBuilderState = this.jsonBuilderStates.pop();
		
	}
	
	private void newArrayEntry() {
		
		json.append(
			(this.currentCounter.incrementArrayEntries() > 0 ? ",".intern() : "".intern())
		);
		
	}
	
	private void newAssociativeArrayEntry() {
		
		json.append(
			(this.currentCounter.incrementAssociativeArrayEntries() > 0 ? ",".intern() : "".intern())
		);
		
	}
	
	private void addKey(String key) {
		
		this.newAssociativeArrayEntry();
		
		json.append(
			JSONBuilder.quote() +
			JSONBuilder.jsonEscape(key) +
			JSONBuilder.quote() + ": "
		);
		
	}
	
	private void addJS(String value) {
		
		json.append(
			JSONBuilder.quote()
		);
		json.append(
			JSONBuilder.jsonEscapeJS(value)
		);
		json.append(
			JSONBuilder.quote()
		);
		
	}
	
	private void addValue(String value, boolean quoteAndEscape) {
		
		if(quoteAndEscape) {
			
			json.append(
				JSONBuilder.quote()
			);
			json.append(
				JSONBuilder.jsonEscape(value)
			);
			json.append(
				JSONBuilder.quote()
			);
			
		} else {
			
			json.append(value);
			
		}
		
	}
	
	private void addValue(String value) {
		
		this.addValue(value, true);
		
	}
	
	private void addValue(int value) {
		
		json.append(
			value
		);
		
	}
	
	private void addValue(double value) {
		
		json.append(
			value
		);
		
	}
	
	private void addValue(long value) {
		
		json.append(
			value
		);
		
	}
	
	private void addValue(boolean value) {
		
		json.append(
			value
		);
		
	}
	
	private void addValue(Date value) {
		
		this.addValue(
			(value != null ? value.toString() : "".intern())
		);	
		
	}
	
	private void addObjectValue(Object o) {
		
		if(o.getClass().equals(Integer.class)) {
			this.addValue((Integer)o);
		} else if(o.getClass().equals(Double.class)) {
			this.addValue((Double)o);
		} else if(o.getClass().equals(Long.class)) {
			this.addValue((Long)o);
		} else if(o.getClass().equals(Boolean.class)) {
			this.addValue((Boolean)o);
		} else if(o.getClass().equals(Date.class)) {
			this.addValue((Date)o);
		} else {
			this.addValue(o.toString());
		}
		
	}
	
	private void addValue(JSONBuilder jb) {
		
		json.append(jb.getJSON());
		
	}
	
	public JSONBuilder ba() {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.beginSub();
		this.beginArray();
		
		return this;
		
	}
	
	public JSONBuilder ea() {
		
		this.endSub();
		this.endArray();
		
		return this;
		
	}
	
	public JSONBuilder entry() {
		
		this.newArrayEntry();
		
		return this;
		
	}
	
	public JSONBuilder bh() {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.beginSub();
		this.beginAssociativeArray();
		
		return this;
		
	}
	
	public JSONBuilder eh() {
		
		this.endSub();
		this.endAssociativeArray();
		
		return this;
		
	}
	
	public JSONBuilder $(char c) {
		
		switch(c) {
			case '{':
				return this.bh();
			case '}':
				return this.eh();
			case '[':
				return this.ba();
			case ']':
				return this.ea();
		}
		
		
		return this;
	}
	
	public JSONBuilder k(String key) {
		
		this.addKey(key);
		
		return this;
		
	}
	
	public JSONBuilder v(String v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilder js(String v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addJS(v);
		
		return this;
		
	}
	
	public JSONBuilder v(String v, boolean quoteAndEscape) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v, quoteAndEscape);
		
		return this;
		
	}
	
	public JSONBuilder v(int v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilder v(double v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilder v(long v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilder v(boolean v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilder v(Date v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilder v(Object v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addObjectValue(v);
		
		return this;
		
	}
	
	public JSONBuilder v(JSONBuilder v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilder v(JSONBuilderDynamic v) {
		
		if(this.currentJsonBuilderState == JSONBuilderState.IN_ARRAY) {
			this.newArrayEntry();
		}
		
		this.addValue(v.toString());
		
		return this;
		
	}
	
	public String getJSON() {
		return this.json.toString();
	}
	
	public static String jsonEscapeJS(String s) {
		
		if(s == null) {
			return s;
		}
		
		StringBuilder escapedString =
			new StringBuilder();
		
		char c = '\0', pc = '\0';
		
		boolean inSingleQuotes = false, inDoubleQuotes = false;
		
		for(int i=0; i<s.length(); i++) {
			
			c = s.charAt(i);
			
			if(c == '"') {
				
				inDoubleQuotes = !inDoubleQuotes;
				
			}
			
			if(c == '\'') {
				
				inSingleQuotes = !inSingleQuotes;
				
			}
			
			if(c == '"' && pc != '\\') {
				
				escapedString.append('\\');
				escapedString.append(c);
				
			} else if(c == '\n') {
				
				escapedString.append("\\n".intern());
				
			} else if(c == '\t') {
				
				escapedString.append("\\t".intern());
				
			} else if(c == '\r') {
				
				escapedString.append("\\r".intern());
				
			} else if(c == '\\' && !(inSingleQuotes || inDoubleQuotes)) {
			
				escapedString.append("\\\\".intern());
				
			} else {
				
				escapedString.append(c);
				
			}
			
			pc = c;
			
		}
		
		return escapedString.toString();
	}
	
	public static String jsonEscape(String s) {
		
		if(s == null) {
			return s;
		}
		
		StringBuilder escapedString =
			new StringBuilder();
		
		char c = '\0', pc = '\0';
		
		boolean inSingleQuotes = false, inDoubleQuotes = false;
		
		for(int i=0; i<s.length(); i++) {
			
			c = s.charAt(i);
			
			if(c == '"') {
				
				inDoubleQuotes = !inDoubleQuotes;
				
			}
			
			if(c == '\'') {
				
				inSingleQuotes = !inSingleQuotes;
				
			}
			
			if(c == '"' && pc != '\\') {
				
				escapedString.append('\\');
				escapedString.append(c);
				
			} else if(c == '\n') {
				
				escapedString.append("\\\\n".intern());
				
			} else if(c == '\t') {
				
				escapedString.append("\\\\t".intern());
				
			} else if(c == '\r') {
				
				escapedString.append("\\\\r".intern());
				
			} else if(c == '\\' && !(inSingleQuotes || inDoubleQuotes)) {
			
				escapedString.append("\\\\".intern());
				
			} else {
				
				escapedString.append(c);
				
			}
			
			pc = c;
			
		}
		
		return escapedString.toString();
	}
	
	public static String jsonUnescape(String s) {
		
		if(s == null) {
			return s;
		}
		
		StringBuilder unescapedString =
			new StringBuilder();
		
		char c = '\0', pc = '\0';
		
		for(int i=0; i<s.length(); i++) {
			
			c = s.charAt(i);
			
			if(c == '\\' && pc != '\\') {
				
				;
				
			} else if(c == '"' && pc == '\\') {
				
				unescapedString.append(c);
				
			} else if(c == 'n' && pc == '\\') {
				
				unescapedString.append('\n');
				
			} else if(c == 't' && pc == '\\') {
				
				unescapedString.append('\t');
				
			} else if(c == 'r' && pc == '\\') {
				
				unescapedString.append('\r');
				
			} else {
				
				unescapedString.append(c);
				
			}
			
			pc = c;
			
		}
		
		return unescapedString.toString();
	}
	
	public static JSONBuilder f() {
		
		return new JSONBuilder();
		
	}
	
	public static JSONBuilder f(JSONBuilder jb) {
		
		return new JSONBuilder(jb.getStringBuffer());
		
	}
	
	public String toString() {
		return this.json.toString();
	}
	
	public static void main(String args[]) {
		
		JSONBuilder jb1 = new JSONBuilder();
		
		jb1.$('{').k("Hello").v("World").$('}');
		
		JSONBuilder jb = new JSONBuilder();
		
		/*jb
			.$('{')
				.k("Family Members")
					.$('[')
						.$('{')
							.k("id").v(1)
							.k("name").v("Sondre\nMjelde-Ohlsson")
							.k("age").v(6)
						.$('}')
						.$('{')
							.k("id").v(2)
							.k("name").v("Sebastian")
							.k("age").v(3)
							.k("json").v("/\\s/g")
							.k("subarray")
							.$('[')
								.v(1)
								.v(2)
								.v(3)
							.$(']')
						.$('}')
						.v(1)
						.v(2)
						.v(jb1)
					.$(']')
			.$('}');*/
			
		jb.$('[').$('[').v("hello").v("world").$('[').$(']').$(']').$(']');
		
		System.out.println(jb.getJSON());
		
	}
}