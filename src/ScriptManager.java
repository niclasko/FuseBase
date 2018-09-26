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
import java.util.HashMap;
import java.io.Serializable;
import java.net.URLDecoder;

public class ScriptManager implements Serializable, RESTAPIValidValues {
	
	private HashMap<String, Script> scripts;
	
	private String validAPIValues;
	
	public ScriptManager() {
		this.scripts = new HashMap<String, Script>();
		this.setValidAPIValues();
	}
	
	public static String escapeNewLinesInQuotes(String s) {
		
		StringBuilder modified =
			new StringBuilder();
		
		char c = '\0', pc = '\0';
		
		boolean inSingleQuote = false,
			inDoubleQuote = false;
		
		for(int i=0; i<s.length(); i++) {

			c = s.charAt(i);

			if(c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			}
			
			if(c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}

			if((inSingleQuote || inDoubleQuote) && c == '\n' & pc != '\\') {
				modified.append("\\n");
			} else {
				modified.append(c);
			}

		}
		
		return modified.toString();
		
	}
	
	public MapAction addOrUpdateScript(String name, String source) {
		
		if(!this.scripts.containsKey(name)) {
			
			this.scripts.put(
				name,
				new Script(
					name,
					source
				)
			);
			
			this.setValidAPIValues();
			
			return MapAction.ADD;
			
		} else {
			
			Script script =
				this.scripts.get(name);
			
			script.setSource(
				source
			);
			
			return MapAction.UPDATE;
			
		}
		
	}
	
	public MapAction addScript(JSONDataStructure scriptJSON) throws Exception {
		
		String name =
			scriptJSON.get("name").getValue().toString();
		
		if(this.scripts.containsKey(name)) {
		
			return MapAction.ALREADY_EXISTS;
			
		}
		
		return this.addOrUpdateScript(
			name,
			URLDecoder.decode(
				scriptJSON.get("source").getValue().toString()
			)
		);
		
	}
	
	public MapAction deleteScript(String name) {
		
		if(this.scripts.containsKey(name)) {
			
			this.scripts.remove(name);
			
			this.setValidAPIValues();
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public boolean hasScript(String name) {
		return this.scripts.containsKey(name);
	}
	
	public Script getScript(String name) {
		return this.scripts.get(name);
	}
	
	public JSONBuilder getJSONBuilder() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(Script script : this.scripts.values()) {
			jb.v(script.getJSONBuilder());
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(Script script : this.scripts.values()) {
			jb.v(script.getJSONBuilderForExport());
		}
		
		return jb.$(']');
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	public void setValidAPIValues() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(Script script : this.scripts.values()) {
			jb.v(script.name());
		}
		
		this.validAPIValues = jb.$(']').getJSON();
	}
	
	public String validAPIValues() {
		
		return this.validAPIValues;
		
	}
	
}