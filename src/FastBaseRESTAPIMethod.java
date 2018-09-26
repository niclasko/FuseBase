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
import java.lang.reflect.Method;

public class FastBaseRESTAPIMethod implements Comparable<FastBaseRESTAPIMethod> {
	
	private String apiPath;
	private Method method;
	private boolean requiresAuthentication;
	
	public FastBaseRESTAPIMethod(	String apiPath,
									Method method,
									boolean requiresAuthentication	) {
		
		this.apiPath = apiPath;
		this.method = method;
		this.requiresAuthentication = requiresAuthentication;
		
	}
	
	public String apiPath() {
		return this.apiPath;
	}
	
	public Method method() {
		return this.method;
	}
	
	public boolean requiresAuthentication() {
		return this.requiresAuthentication;
	}
	
	public int compareTo(FastBaseRESTAPIMethod o) {
		
		return this.apiPath.compareTo(o.apiPath());
		
	}
	
}