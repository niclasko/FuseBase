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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class HTTPServer {
	
	public static final int MAX_CLIENT_SOCKETS = 100000;
	
	protected int port;
	private ServerSocket serverSocket;
	protected HttpRequestProcessor httpRequestProcessor;
	protected LinkedBlockingQueue<Socket> clientSocketProcessingQueue;
	protected HTTPServerThread[] httpServerThreads;
	
	public HTTPServer(int port, HttpRequestProcessor httpRequestProcessor, int threadCount) {
		
		this.port = port;
		this.serverSocket = null;
		this.httpRequestProcessor = httpRequestProcessor;
		this.clientSocketProcessingQueue =
			new LinkedBlockingQueue<Socket>(
				HTTPServer.MAX_CLIENT_SOCKETS
			);
		this.httpServerThreads =
			new HTTPServerThread[threadCount];
		
	}
	
	public void runServer() {
		
		// Setup server socket on specified port
		try {
			
			this.serverSocket = new ServerSocket(this.port);
			
		} catch (Exception e) {
			
			System.err.println(
				e.getMessage()
			);
			System.exit(1);
			
		}
		
		// Listen for connections and add to socket processing queue
		try {
			
			// Start worker thread pool
			for(int i=0; i<this.httpServerThreads.length; i++) {

				this.httpServerThreads[i] =
					new HTTPServerThread(
						this,						// HTTPServer instance
						this.httpRequestProcessor	// HttpRequestProcessor instance
					);

			}
			
			while(true) {
				
				this.clientSocketProcessingQueue.put(
					serverSocket.accept()
				);
				
			}
			
		} catch(Exception e) {
			
			System.out.println(
				e.getMessage()
			);
			
		}
		
		// Shutdown server socket
		try {
			
			this.serverSocket.close();
			
		} catch(Exception e) {
			
			System.out.println(
				e.getMessage()
			);
			
		}
		
	}
	
	public synchronized Socket takeFromClientSocketProcessingQueue() throws Exception {
		
		return this.clientSocketProcessingQueue.take();
		
	}
	
	public static void main(String args[]) {
		
		HTTPServer httpServer =
			new HTTPServer(
				1234,
				new HttpApplication(),
				1
			);
		httpServer.runServer();
		
	}
	
}