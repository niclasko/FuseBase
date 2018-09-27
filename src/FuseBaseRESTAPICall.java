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
import java.io.DataOutputStream;

public class FuseBaseRESTAPICall {
	
	private String jsonCallbackFunction;
	private DataOutputStream output;
	private HttpRequest httpRequest;
	private HTTPServerThread httpServerThread;
	
	public FuseBaseRESTAPICall(	String jsonCallbackFunction,
								DataOutputStream output,
								HttpRequest httpRequest,
								HTTPServerThread httpServerThread	) {
		
		this.jsonCallbackFunction = jsonCallbackFunction;
		this.output = output;
		this.httpRequest = httpRequest;
		this.httpServerThread = httpServerThread;
		
	}
	
	public String jsonCallbackFunction() {
		return this.jsonCallbackFunction;
	}
	
	public DataOutputStream output() {
		return this.output;
	}
	
	public HttpRequest httpRequest() {
		return this.httpRequest;
	}
	
	public HTTPServerThread httpServerThread() {
		return this.httpServerThread;
	}
	
}