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

public class CommandLineParameters {
	
	private HashMap<String, String> parameters;
	
	public CommandLineParameters(String arguments[]) {
		this.parameters = new HashMap<String, String>();
		
		String[] parameter;
		
		for(int i=0; i<arguments.length; i++) {
			parameter = arguments[i].split("=");
			
			parameters.put(parameter[0].substring(1), parameter[1]);
		}
	}
	
	public String get(String parameterName, String defaultValue) {
		
		if(this.parameters.containsKey(parameterName)) {
			return this.parameters.get(parameterName);
		}
		
		return defaultValue;
	}
	
	public boolean hasParameter(String parameterName) {
		return this.parameters.containsKey(parameterName);
	}
}