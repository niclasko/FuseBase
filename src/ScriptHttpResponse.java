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
import java.util.Map;
import java.util.List;

public class ScriptHttpResponse {
	
	private Map<String, List<String>> responseHeaderFields;
	private String responseBody;
	
	private String responseHeadersJSON;
	
	public ScriptHttpResponse(Map<String, List<String>> responseHeaderFields, String responseBody) {
		
		this.responseHeaderFields = responseHeaderFields;
		this.responseBody = responseBody;
		
		this.setResponseHeaders();
		
	}
	
	private void setResponseHeaders() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('{');
		
		for(Map.Entry<String, List<String>> entry : responseHeaderFields.entrySet()) {
			
			jb.k(entry.getKey());
			
			jb.$('[');
			
			for(String value : entry.getValue()) {
				
				jb.v(value);
				
			}
			
			jb.$(']');
			
		}
		
		jb.$('}');
		
		this.responseHeadersJSON = jb.getJSON();
		
	}
	
	public String responseHeaders() {
		return this.responseHeadersJSON;
	}
	
	public String responseBody() {
		return this.responseBody;
	}
	
}