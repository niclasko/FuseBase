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
import java.io.DataOutputStream;

public class HttpApplication implements HttpRequestProcessor {
	
	public void processHttpRequest(DataOutputStream output, HttpRequest httpRequest, HTTPServerThread httpServerThread) throws Exception {
		
		/*if(!HTTPServerThread.isFile(httpRequest.getFileName())) {
			
			HttpResponse.fileNotFound(output);
			
			return;
		}*/
		
		HttpResponse.header(
			output,
			HttpResponse.CSV_HEADERS
		);
		
		output.writeBytes(
			httpRequest.toString()
		);
		
		//HTTPServerThread.serveFile(output, httpRequest.getFileName());
		
	}
	
}