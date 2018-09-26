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
import java.util.Stack;
import java.util.Date;

public class JSONBuilderDynamic extends JSONBuilder {
	
	private JSONDataStructure jsonRootDataStructure;
	private Stack<JSONDataStructure> jsonDataStructures;
	private JSONDataStructure currentJsonDataStructure;
	
	public JSONBuilderDynamic() {
		
		this.jsonRootDataStructure = null;
		this.jsonDataStructures = new Stack<JSONDataStructure>();
		this.currentJsonDataStructure = null;
		
	}
	
	private void beginJSONDataStructure(JSONDataStructure jsonDataStructure) {
		
		if(this.jsonRootDataStructure == null) {
			
			this.jsonRootDataStructure = jsonDataStructure;
			this.currentJsonDataStructure = this.jsonRootDataStructure;
			
			
		} else if(this.jsonRootDataStructure != null) {
			
			this.currentJsonDataStructure.add(jsonDataStructure);
			
			this.jsonDataStructures.push(this.currentJsonDataStructure);
			
			this.currentJsonDataStructure = jsonDataStructure;
			
		}
		
	}
	
	private void endJSONDataStructure() {
		
		if(this.jsonDataStructures.size() == 0) {
			return;
		}
		
		this.currentJsonDataStructure =
			this.jsonDataStructures.pop();
		
	}
	
	private void beginArray() {
		
		beginJSONDataStructure(new JSONArray());
		
	}
	
	private void endArray() {
		
		this.endJSONDataStructure();
		
	}
	
	private void beginAssociativeArray() {
		
		beginJSONDataStructure(new JSONAssociativeArray());
		
	}
	
	private void endAssociativeArray() {
		
		this.endJSONDataStructure();
		
	}
	
	private void addKey(String key) {
		
		this.currentJsonDataStructure.addKey(
			key
		);
		
	}
	
	private void addValue(String value) {
		
		this.currentJsonDataStructure.add(
			value
		);
		
	}
	
	private void addValue(int value) {
		
		this.currentJsonDataStructure.add(
			value
		);
		
	}
	
	private void addValue(double value) {
		
		this.currentJsonDataStructure.add(
			value
		);
		
	}
	
	private void addValue(long value) {
		
		this.currentJsonDataStructure.add(
			value
		);
		
	}
	
	private void addValue(boolean value) {
		
		this.currentJsonDataStructure.add(
			value
		);
		
	}
	
	private void addValue(Date value) {
		
		this.currentJsonDataStructure.add(
			value
		);
		
	}
	
	private void addObjectValue(Object o) {
		
		this.currentJsonDataStructure.add(
			o
		);
		
	}
	
	private void addValue(JSONBuilderDynamic jb) {
		
		this.currentJsonDataStructure.add(
			jb.getJSONStructure()
		);
		
	}
	
	public JSONBuilderDynamic ba() {

		this.beginArray();
		
		return this;
		
	}
	
	public JSONBuilderDynamic ea() {

		this.endArray();
		
		return this;
		
	}
	
	public JSONBuilderDynamic bh() {

		this.beginAssociativeArray();
		
		return this;
		
	}
	
	public JSONBuilderDynamic eh() {

		this.endAssociativeArray();
		
		return this;
		
	}
	
	public JSONBuilderDynamic $(char c) {
		
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
	
	public JSONBuilderDynamic k(String key) {
		
		this.addKey(key);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(String v) {

		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(int v) {

		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(double v) {

		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(long v) {
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(boolean v) {

		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(Date v) {
		
		this.addValue(v);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(Object v) {

		this.addObjectValue(v);
		
		return this;
		
	}
	
	public JSONBuilderDynamic v(JSONBuilderDynamic v) {
		
		this.addValue(v);
		
		return this;
		
	}
	
	public static JSONBuilderDynamic f() {
		return new JSONBuilderDynamic();
	}
	
	public JSONDataStructure getJSONStructure() {
		return this.jsonRootDataStructure;
	}
	
	public String toString() {
		return this.jsonRootDataStructure.toString();
	}
	
	public static void main(String args[]) {
		
		JSONBuilderDynamic jb1 = new JSONBuilderDynamic();
		
		jb1.$('{').k("Hello").v("World").$('}');
		
		JSONBuilderDynamic jb = new JSONBuilderDynamic();
		
		jb
			.$('{')
				.k("Family Members")
					.$('[')
						.$('{')
							.k("id").v(1)
							.k("name").v("Sondre")
							.k("age").v(6)
						.$('}')
						.$('{')
							.k("id").v(2)
							.k("name").v("Sebastian")
							.k("age").v(3)
							.k("hungry").v(true)
							.k("playing").v((Boolean)null)
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
			.$('}');
		
		jb.getJSONStructure().print();
		
	}
	
}