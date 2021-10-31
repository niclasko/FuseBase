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

public class HttpResponse {
	
	public static final String NEW_LINE = "\r\n";
	
	public static final String FILE_NOT_FOUND =
		"HTTP/1.0 404 Not Found" + HttpResponse.NEW_LINE;
		
	public static final String SEE_OTHER =
		"HTTP/1.1 303 See Other" + HttpResponse.NEW_LINE;
	
	public static final String PERMANENTLY_MOVED =
		"HTTP/1.1 301 Permanently Moved" + HttpResponse.NEW_LINE;
	
	public static final String TEMPORARY_REDIRECT =
		"HTTP/1.1 307 Temporary Redirect" + HttpResponse.NEW_LINE;
		
	public static final String NO_CACHE_MUST_REVALIDATE =
		"Cache-Control: max-age=0, no-cache, must-revalidate, proxy-revalidate, private" + HttpResponse.NEW_LINE +
		"Edge-Control: no-store" + HttpResponse.NEW_LINE;
	
	public static final String CONNECTION_CLOSE =
		"Connection: close" + HttpResponse.NEW_LINE;
	
	public static final String AGE_ZERO =
		"Age: 0" + HttpResponse.NEW_LINE;
	
	public static final String defaultHeader =
		"HTTP/1.0 200 OK" + HttpResponse.NEW_LINE +
		"Access-Control-Allow-Origin: *" + HttpResponse.NEW_LINE;
	
	public static final String[] defaultHeaders =
		new String[] {
			"HTTP/1.0 200 OK",
			"Access-Control-Allow-Origin: *"
		};
		
	public static final String[] JSON_HEADERS =
		new String[] {
			"Content-Type: application/json; charset=UTF-8",
			"Cache-Control: no-cache, no-store, must-revalidate",
			"Pragma: no-cache",
			"Expires: 0"
		};
		
	public static final String[] CSV_HEADERS =
		new String[] {
			"Content-Type: text/plain; charset=UTF-8",
			"Cache-Control: no-cache, no-store, must-revalidate",
			"Pragma: no-cache",
			"Expires: 0"
		};
	
	public static void header(DataOutputStream output, String contentType, int contentLength) throws Exception {
		
		output.writeBytes(HttpResponse.defaultHeader);
		
		if(contentType != null) {
			output.writeBytes("Content-Type: " + contentType + HttpResponse.NEW_LINE);
		}
		
		if(contentLength > 0) {
			output.writeBytes("Content-Length: " + contentLength + HttpResponse.NEW_LINE);
		}
		
		output.writeBytes(
			 HttpResponse.NEW_LINE
		);
		
	}
	
	public static void header(DataOutputStream output, final String[] headers) throws Exception {
		
		output.writeBytes(HttpResponse.defaultHeader);
		
		for(int i=0; i<headers.length; i++) {
			output.writeBytes(headers[i] + HttpResponse.NEW_LINE);
		}
		
		output.writeBytes(
			 HttpResponse.NEW_LINE
		);
		
	}
	
	private static void dummy(DataOutputStream output) throws Exception {
		output.writeBytes(HttpResponse.defaultHeader);
		output.writeBytes(HttpResponse.NEW_LINE);
		output.writeBytes("Hello World!");
		output.writeBytes(HttpResponse.NEW_LINE);
	}
	
	public static void fileNotFound(DataOutputStream output) throws Exception {
		output.writeBytes(HttpResponse.FILE_NOT_FOUND);
	}
	
	public static void redirect(DataOutputStream output, String path) throws Exception {
		HttpResponse.redirect(
			output,
			path,
			null
		);
	}
	
	public static void redirect(DataOutputStream output, String path, String cookie) throws Exception {
		output.writeBytes(HttpResponse.PERMANENTLY_MOVED);
		output.writeBytes(HttpResponse.NO_CACHE_MUST_REVALIDATE);
		output.writeBytes(HttpResponse.AGE_ZERO);
		output.writeBytes("Date: " + Utils.getSystemTime() + HttpResponse.NEW_LINE);
		if(cookie != null) {
			output.writeBytes("Set-Cookie: " + cookie + HttpResponse.NEW_LINE);
		}
		output.writeBytes("Location: " + path + HttpResponse.NEW_LINE);
		output.writeBytes(HttpResponse.CONNECTION_CLOSE);
		output.flush();
	}
	
	public static String getFileType(String fileName) {
		
		if(fileName.lastIndexOf(".") == -1) {
			return null;
		}
		
		return fileName.substring(fileName.lastIndexOf(".")+1);
	}
	
	public static String contentTypeByFileType(String fileType) {
		
		if(fileType == null) {
			
			return null;
			
		}
		
		try {
			
			return HttpContentType.valueOf(fileType).toString();
			
		} catch(Exception e) {
			
			;
			
		}
		
		return HttpContentType.other.toString();
		
	}
	
	public static String contentType(String fileName) {
		
		return
			HttpResponse.contentTypeByFileType(
				HttpResponse.getFileType(
					fileName
				)
			);
		
	}
	
	public static void main(String args[]) {
		System.out.println(HttpResponse.contentType("web/css/styles.css"));
	}
	
}