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
import java.util.jar.JarFile;

public class FuseBaseWebServer implements HttpRequestProcessor {
	
	private FuseBase fuseBase;
	private FuseBaseRESTAPI fuseBaseRESTAPI;
	private JarFile currentJarFile;
	private String[] unrestrictedResourceFilePaths;
	
	public FuseBaseWebServer(FuseBase fuseBase) throws Exception {
		
		this.fuseBase = fuseBase;
		
		this.fuseBaseRESTAPI =
			new FuseBaseRESTAPI(
				fuseBase
			);
			
		try {
			
			this.currentJarFile =
				new JarFile(
					FuseBaseRESTAPI.class.getProtectionDomain().getCodeSource().getLocation().getFile()
				);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		this.unrestrictedResourceFilePaths =
			new String[] {
				"web/js",
				"web/css",
				"ui/login"
			};
		
	}
	
	public void processHttpRequest(DataOutputStream output, HttpRequest httpRequest, HTTPServerThread httpServerThread) throws Exception {
		
		String fileName =
			httpRequest.getFileName();
		
		boolean routeFound =
			this.fuseBaseRESTAPI.hasRoute(
				fileName
			);
		
		if(routeFound) {
			
			this.fuseBaseRESTAPI.route(
				fileName,
				output,
				httpRequest,
				httpServerThread
			);
			
			return;
			
		} else if(!routeFound) {
			
			if(httpRequest.getOriginalFileName().equals("/")) {
				
				HttpResponse.redirect(
					output,
					Config.FUSEBASE_WEB_GUI_DIRECTORY
				);
				
				return;
				
			}
			
			if(fileName.indexOf(".") == -1) {
				
				if(fileName.substring(fileName.length()-1).equals("/")) {
						
					fileName += HttpRequest.DEFAULT_FILE_NAME;
					
				} else {
					
					HttpResponse.redirect(
						output,
						httpRequest.getOriginalFileName() + "/"
					);
					
					return;
					
				}
				
			}
			
			
			boolean
				isFile = HTTPServerThread.isFile(fileName),
				isResourceFile = this.fuseBase.fileManager.isResourceFile(this.currentJarFile, fileName);
			
			// Every access to a fusebase.jar resource file must be authenticated
			if(	isResourceFile &&
				!isUnrestrictedResourceFilePath(fileName) &&
				!this.isAuthenticated(httpRequest, httpServerThread)	) {
				
				HttpResponse.redirect(
					output,
					Config.FUSEBASE_WEB_LOGIN_PATH
				);
				
				return;
				
			}
			
			if(isFile || isResourceFile) {
				
				HttpResponse.header(
					output,
						HttpResponse.contentType(
							fileName
						)
					,
					-1
				);
				
			}
			
			if(isFile) {
				
				httpServerThread.serveFile(
					output,
					fileName
				);
				
			} else if(isResourceFile) {
				
				this.fuseBase.fileManager.serveResourceFile(
					output,
					this.currentJarFile,
					fileName,
					httpServerThread.getFileBuffer()
				);
				
			} else {
				
				HttpResponse.fileNotFound(output);
				
			}
			
		}
		
	}
	
	private boolean isUnrestrictedResourceFilePath(String path) {
		
		for(String unrestrictedResourceFilePath : this.unrestrictedResourceFilePaths) {
			
			if(path.startsWith(unrestrictedResourceFilePath)) {
				
				return true;
				
			}
			
		}
		
		return false;
		
	}
	
	private boolean isAuthenticated(HttpRequest httpRequest, HTTPServerThread httpServerThread) {
		
		String sessionKey =
			httpRequest.cookie(Config.FUSEBASE_SESSION_KEY_COOKIE_KEYNAME);
		
		if(sessionKey != null) {
			
			if(fuseBase.userManager.isValidSession(sessionKey, httpServerThread.getClientIPAddress())) {
				
				return true;
				
			} else {
				
				return false;
				
			}
			
		}
		
		return false;
		
	}
	
	public JarFile currentJarFile() {
		return this.currentJarFile;
	}
	
}