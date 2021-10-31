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
import java.util.HashMap;
import java.util.Set;
import java.io.DataInputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;

public class HttpRequest {
	
	private static long time = 0;
	
	public static final String DEFAULT_FILE_NAME = "index.html";
	
	private boolean isFresh;
	private HttpRequestType httpRequestType;
	private String originalFileName;
	private String fileName;
	private AgedStringMap parameters;
	private AgedStringMap headers;
	private AgedStringMap cookies;
	private int lineCount;
	private HTTPRequestState httpRequestState;
	private byte[] clientData;
	
	// Processing fields
	private String verb;
	private String[] requestParts;
	private String queryString;
	private String[] params;
	private String[] param;
	private String headerKey;
	private String headerValue;
	private String cookieList[];
	private String[] cookieParts;
	private String cookieKey;
	private String cookieValue;
	
	private long requestStartTime;
	
	private LiteString toString;
	
	private URLDecoder urlDecoder;
	
	
	public HttpRequest() {
		
		this.parameters = new AgedStringMap();
		this.headers = new AgedStringMap();
		this.cookies = new AgedStringMap();
		this.toString = new LiteString(10000);
		
		this.urlDecoder = new URLDecoder();
		
		this.reset();
		
	}
	
	public void reset() {
		
		this.isFresh = true;
		this.httpRequestType = HttpRequestType.UNKNOWN;
		this.originalFileName = null;
		this.fileName = null;
		this.lineCount = -1;
		this.httpRequestState = HTTPRequestState.UNKNOWN;
		this.clientData = null;
		this.toString.clear();
		
	}
	
	private HttpRequestType getHttpRequestType(String requestString) {
		
		if(	requestString.charAt(0) == 'G' &&
			requestString.charAt(1) == 'E' &&
			requestString.charAt(2) == 'T'		) {
			
			return HttpRequestType.GET;
				
		} else if(	requestString.charAt(0) == 'P' &&
					requestString.charAt(1) == 'O' &&
					requestString.charAt(2) == 'S' &&
					requestString.charAt(3) == 'T'		) {
			
			return HttpRequestType.POST;
			
		} else {
			
			return HttpRequestType.UNSUPPORTED;
			
		}
		
	}
	
	private void age() {
		
		this.requestStartTime =
			HttpRequest.getNextTime();
		
		this.parameters.setLatestAge(this.requestStartTime);
		this.headers.setLatestAge(this.requestStartTime);
		this.cookies.setLatestAge(this.requestStartTime);
		
	}
	
	public void begin(String requestString) {
		
		this.age();
		
		this.httpRequestState = HTTPRequestState.SERVER_AWAITING_REQUEST;
		
		if(requestString != null) {
			
			this.httpRequestType =
				this.getHttpRequestType(
					requestString
				);
			
			if(this.httpRequestType != HttpRequestType.UNSUPPORTED) {
				
				this.requestParts = requestString.split("\\s")[1].split("\\?");
				
				this.originalFileName = this.urlDecoder.decode(requestParts[0]); 
				this.fileName = this.urlDecoder.decode(requestParts[0].substring(1));
				
				/*if(	(	this.fileName.length() > 0 &&
						this.fileName.substring(this.fileName.length()-1).equals("/")	) ||
						this.fileName.length() == 0	) {
						
					this.fileName += HttpRequest.DEFAULT_FILE_NAME;
					
				}*/

				this.queryString = (requestParts.length == 2 ? requestParts[1] : "");
				this.params = queryString.split("&");

				if(this.queryString.length() > 0) {
					for(int i=0; i<this.params.length; i++) {
						
						this.param = this.params[i].split("=");
						
						if(this.param.length == 2) {
							this.parameters.put(
								param[0],
								param[1],
								this.requestStartTime
							);
						}

					}
				}
				
				this.httpRequestState = HTTPRequestState.SERVER_AWAITING_NEXT_HEADER;
				
			}
			
		} else {
			
			this.httpRequestType = HttpRequestType.UNKNOWN;
			this.httpRequestState = HTTPRequestState.VOID;
			
		}
		
		this.lineCount = 0;
		
		this.isFresh = false;
		
	}
	
	public boolean isFresh() {
		return this.isFresh;
	}
	
	public void processLine(String line) {
		
		if(this.httpRequestState == HTTPRequestState.CLIENT_AWAITING_DATA ||
			this.httpRequestState == HTTPRequestState.DONE) { // Only send data to the client once
			
			this.httpRequestState = HTTPRequestState.DONE;
			
			return;
			
		}
		
		// HTTP header
		if(line.indexOf(":") > -1 && this.lineCount >= 0) {
			
			this.headerKey = line.substring(0, line.indexOf(":")).trim().toLowerCase();
			this.headerValue = line.substring(line.indexOf(":")+1).trim();
			
			if(this.headerKey.equals("cookie")) {
				
				this.cookieList =
					this.headerValue.split(";");
					
				for(String cookieEntry : this.cookieList) {
					
					this.cookieParts = cookieEntry.split("=");
					
					this.cookieKey = this.cookieParts[0].trim();
					
					if(this.cookieParts.length == 2) {
						
						this.cookieValue = this.cookieParts[1].trim();
						
					} else {
						
						this.cookieValue = this.cookieKey;
						
					}
					
					this.cookies.put(
						this.cookieKey,
						this.cookieValue,
						this.requestStartTime
					);
					
				}
				
			} else {
				
				this.headers.put(
					this.headerKey,
					this.headerValue,
					this.requestStartTime
				);
				
			}
			
			
		} else if(line.length() == 0) {
			
			if(this.httpRequestState == HTTPRequestState.SERVER_AWAITING_NEXT_HEADER) {
				
				if(this.httpRequestType == HttpRequestType.GET) {

					this.httpRequestState = HTTPRequestState.CLIENT_AWAITING_DATA;
					

				} else if(this.httpRequestType == HttpRequestType.POST) {

					this.httpRequestState = HTTPRequestState.SERVER_AWAITING_DATA;

				}
				
			}
			
		}
		
		this.newLine();
	}
	
	public int getLineCount() {
		return this.lineCount;
	}
	
	public void newLine() {
		this.lineCount++;
	}
	
	public String getOriginalFileName() {
		return this.originalFileName;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public HttpRequestType getHttpRequestType() {
		return this.httpRequestType;
	}
	
	public HTTPRequestState getHttpRequestState() {
		return this.httpRequestState;
	}
	
	public String parameter(String key) {
		return this.parameters.get(key);
	}
	
	public String parameter(String key, String defaultValue) {
		return this.parameters.get(key, defaultValue);
	}
	
	public boolean hasParameterKey(String key) {
		return this.parameters.containsKey(key);
	}
	
	public Set<String> getParameterKeys() {
		
		return this.parameters.keySet();
		
	}
	
	public String[][] getParameters() {
		
		String[][] parameterList =
			new String[this.parameters.size()][];
		int index = 0;
		
		for(String key : this.getParameterKeys()) {
			
			parameterList[index++] =
				new String[]{key, this.parameters.get(key)};
			
		}
		
		return parameterList;
		
	}
	
	public String[][] getCookies() {
		
		String[][] cookieList =
			new String[this.cookies.size()][];
		int index = 0;
		
		for(String key : this.getCookieKeys()) {
			
			cookieList[index++] =
				new String[]{key, this.cookies.get(key)};
			
		}
		
		return cookieList;
		
	}
	
	public String cookie(String key) {

		return this.cookies.get(key);
		
	}
	
	public Set<String> getCookieKeys() {
		return this.cookies.keySet();
	}
	
	public String header(String key) {
		return this.headers.get(key);
	}
	
	public String header(String key, String defaultValue) {
		
		return this.headers.get(
			key.toLowerCase(),
			defaultValue
		);
		
	}
	
	public boolean hasHeaderKey(String key) {
		return this.headers.containsKey(key.toLowerCase());
	}
	
	public Set<String> getHeaderKeys() {
		return this.headers.keySet();
	}
	
	public void readClientData(DataInputStream input) throws Exception {
		
		if(!this.hasHeaderKey("content-length")) {
			return;
		}
		
		int contentLengthExpected = Integer.parseInt(this.header("content-length"));
		this.clientData = new byte[contentLengthExpected];
		int bytesRead = 0;
		
		while(bytesRead < contentLengthExpected) {
			bytesRead +=
				input.read(
					this.clientData,
					bytesRead,
					contentLengthExpected - bytesRead
				);
		}
		
		this.httpRequestState = HTTPRequestState.CLIENT_AWAITING_DATA;
		
	}
	
	public byte[] getClientData() {
		return this.clientData;
	}
	
	public String toString() {
		
		this.toString.clear();
			
		this.toString.append("Request Type: ".intern());
		this.toString.append(this.httpRequestType.toString().intern());
		this.toString.append("\n".intern());
		this.toString.append("Unprocessed Resource: ".intern());
		this.toString.append((this.originalFileName != null ? this.originalFileName : "".intern()));
		this.toString.append("\n".intern());
		this.toString.append("Processed Resource: ".intern());
		this.toString.append(this.fileName);
		
		if(this.parameters != null) {
			this.toString.append("\nParameters:\n".intern());
			for(String key : this.getParameterKeys()) {
				this.toString.append("  ".intern());
				this.toString.append(key.intern());
				this.toString.append(": ".intern());
				this.toString.append(this.parameters.get(key).intern());
				this.toString.append("\n".intern());
			}
		}
		
		if(this.headers != null) {
			this.toString.append("Headers:\n".intern());
			for(String key : this.getHeaderKeys()) {
				this.toString.append("  ".intern());
				this.toString.append(key.intern());
				this.toString.append(": ".intern());
				this.toString.append(this.headers.get(key).intern());
				this.toString.append("\n".intern());
			}
		}
		
		if(this.cookies != null) {
			this.toString.append("Cookies:\n".intern());
			for(String key : this.getCookieKeys()) {
				this.toString.append("  ".intern());
				this.toString.append(key.intern());
				this.toString.append(": ".intern());
				this.toString.append(this.cookies.get(key).intern());
				this.toString.append("\n".intern());
			}
		}
		
		if(this.clientData != null) {
			this.toString.append("Post Data:\n".intern());
			this.toString.append(
				this.clientData
			);
		}
		
		return this.toString.toString();
		
	}
	
	private synchronized static long getNextTime() {
		
		try {
			
			return HttpRequest.time++;
			
		} catch(Exception e) {
			
			HttpRequest.time = 0;
			
		}
		
		return HttpRequest.time++;
		
	}
	
}