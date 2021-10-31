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
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.net.InetSocketAddress;

public class HTTPServerThread extends Thread implements HttpRequestProcessor {
	
	private Socket socket;
	private HTTPServer parentHTTPServer;
	private int ID;
	private HttpRequestProcessor httpRequestProcessor;
	private DataInputStream input;
	private DataOutputStream output;
	
	private static int threadIDGenerator = 0;
	
	private static final int BYTE_BUFFER_SIZE = 4000;
	
	private boolean headerIsPrinted;
	
	private HttpRequest httpRequest;
	
	private byte[] servedfileBytes;
	
	// Used to hold request data
	private LiteString requestData;
	
	// Used for parsing request data
	private char c;		// Current character
	private char pc;	// Previous character
	private int nc = 0;	// Next character
	
	private byte[] fileBuffer;
	
	public HTTPServerThread(HTTPServer parentHTTPServer, HttpRequestProcessor httpRequestProcessor) {

		this.parentHTTPServer = parentHTTPServer;
		this.ID = HTTPServerThread.threadIDGenerator++;
		this.httpRequestProcessor =
			(httpRequestProcessor == null ? this : httpRequestProcessor);
		this.input = null;
		this.output = null;
		
		this.headerIsPrinted = false;
		
		this.httpRequest =
			new HttpRequest();
			
		this.servedfileBytes =
			new byte[HTTPServerThread.BYTE_BUFFER_SIZE];
			
		this.requestData =
			new LiteString(100000);
		
		this.start();
		
	}
	
	public void run() {
		
		while(true) {

			try {
				
				// This will wait until a socket element becomes available
				this.socket =
					this.parentHTTPServer.takeFromClientSocketProcessingQueue();

				this.headerIsPrinted = false;
				this.httpRequest.reset();
				
				// Setup HTTP I/O for thread
				this.input =
					new DataInputStream(
						this.socket.getInputStream()
					);

				this.output = 
					new DataOutputStream(
						this.socket.getOutputStream()
					);

				this.processHTTPCommunication();

			} catch(Exception e) {
				
				e.printStackTrace();
				
				System.out.println(
					e.getMessage()
				);

			}

			try {

				// Shutdown HTTP I/O
				this.input.close();
				this.output.close();
				this.socket.close();

			} catch(Exception e) {

				System.out.println(
					e.getMessage()
				);

			}

			//System.out.println("\tStopping HTTP Server Thread");

			// Remove this thread from HTTPServer's list of HTTPServerThread's
			//this.remove();
			
		}
		
	}
	
	private void processHTTPCommunication() throws Exception {
		
		this.requestData.clear();
		
		this.c = '\0';
		this.pc = '\0';
		this.nc = 0;
		
		while((this.nc = this.input.read()) != -1) {
			this.pc = c;
			this.c = (char)this.nc;

			if(this.c == '\n' && this.pc == '\r') {

				if(this.httpRequest.isFresh()) {

					this.httpRequest.begin(this.requestData.toString());

				} else {
					
					this.httpRequest.processLine(this.requestData.toString());

				}
				
				switch(this.httpRequest.getHttpRequestState()) {
					case CLIENT_AWAITING_DATA:
						
						// Process httpRequest object
						this.httpRequestProcessor.processHttpRequest(
							this.output,
							this.httpRequest,
							this
						);
						
						return;
						
					case SERVER_AWAITING_DATA:
					
						// Read post data from client
						this.httpRequest.readClientData(this.input);
						
						this.httpRequestProcessor.processHttpRequest(
							this.output,
							this.httpRequest,
							this
						);
						
						return;
						
					case VOID:
					
						// Process httpRequest object
						this.httpRequestProcessor.processHttpRequest(
							this.output,
							this.httpRequest,
							this
						);
						
						return;
						
					case DONE:
						return;
					default:
						break;
				}
				
				this.requestData.clear();

			} else if(this.c != '\n' && this.c != '\r') {

				this.requestData.append(c);

			}
		}
		
		//System.out.println(this.httpRequest.toString());
		
		// End of client HTTP communication
		if(this.nc == -1) {
			return;
		}
		
	}
	
	public void processHttpRequest(DataOutputStream output, HttpRequest httpRequest, HTTPServerThread httpServerThread) throws Exception {
		
		
		
	}
	
	private String stripPort(String ipAddress) {
		
		return ipAddress.substring(0, ipAddress.lastIndexOf(":"));
		
	}
	
	public String getClientIPAddress() {
		
		return this.stripPort(this.socket.getRemoteSocketAddress().toString());
		
	}
	
	public String getClientHostName() {
		
		return ((InetSocketAddress)this.socket.getRemoteSocketAddress()).getAddress().getHostName();
		
	}
	
	public void serveFile(DataOutputStream output, String fileName) throws Exception {
		
		int bytesRead = 0, totalBytesRead = 0;
		
		BufferedInputStream input =
			new BufferedInputStream(
				new FileInputStream(fileName)
			);
	
		while((bytesRead = input.read(this.servedfileBytes, 0, this.servedfileBytes.length)) > -1) {
			
			totalBytesRead += bytesRead;
			
			if(totalBytesRead % this.servedfileBytes.length == 0) {
				
				output.write(this.servedfileBytes, 0, this.servedfileBytes.length);
				
			}
			
		}

		if(totalBytesRead % this.servedfileBytes.length > 0) {
			output.write(this.servedfileBytes, 0, totalBytesRead % this.servedfileBytes.length);
		}
		
		output.flush();

		input.close();
		
	}
	
	public static boolean isFile(String fileName) {
		File f = new File(fileName);
		
		if(!f.exists() || f.isDirectory()) {
			return false;
		}
		
		return true;
	}
	
	public byte[] getFileBuffer() {
		
		return this.servedfileBytes;
		
	}
	
}