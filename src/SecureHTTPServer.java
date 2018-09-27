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
import java.util.LinkedList;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

/*
** Generate certificate:
**	keytool -genkey -alias alias -keypass fusebase -keystore fusebase.keystore -storepass fusebase -keyalg RSA
*/

public class SecureHTTPServer extends HTTPServer {
	
	private SSLServerSocket serverSocket;
	private SSLServerSocketFactory sslServerSocketFactory;
	
	public SecureHTTPServer(	int port,
								HttpRequestProcessor httpRequestProcessor,
								InputStream certificateFileInputStream,
								String sslKeyStorePassword,
								int threadCount								) throws Exception {
		
		super(port, httpRequestProcessor, threadCount);
		
		this.serverSocket = null;
		
		this.setupSSL(
			certificateFileInputStream,
			sslKeyStorePassword
		);
		
	}
	
	private void setupSSL(InputStream certificateFileInputStream, String sslKeyStorePassword) throws Exception {
		
		KeyStore keyStore =
			KeyStore.getInstance(
				KeyStore.getDefaultType()
			);
			
		keyStore.load(
			certificateFileInputStream,
			sslKeyStorePassword.toCharArray()
		);
		
		KeyManagerFactory keyManagerFactory = 
        	KeyManagerFactory.getInstance(
				KeyManagerFactory.getDefaultAlgorithm()
			);

		keyManagerFactory.init(
			keyStore,
			sslKeyStorePassword.toCharArray()
		);
		
		SSLContext sslContext =
			SSLContext.getInstance("SSL");
		
		sslContext.init(
			keyManagerFactory.getKeyManagers(),
			null,
			null
		);
		
		this.sslServerSocketFactory =
			sslContext.getServerSocketFactory();
		
	}
	
	public void runServer() {
		
		// Setup server socket on specified port
		try {
			
			this.serverSocket =
				(SSLServerSocket)this.sslServerSocketFactory.createServerSocket(this.port);
			
		} catch (Exception e) {
			
			System.err.println(
				e.getMessage()
			);
			System.exit(1);
			
		}
		
		// Listen for connections and create server thread when accepting a connection
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
					(SSLSocket)this.serverSocket.accept()
				);
				
				/*this.serverThreads.add(
					new HTTPServerThread(
						(SSLSocket)this.serverSocket.accept(),		// Socket
						this,										// SecureHTTPServer instance
						this.httpRequestProcessor					// HttpRequestProcessor instance
					)
				);*/
				
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
	
}