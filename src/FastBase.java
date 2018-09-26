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
import java.io.Serializable;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;
import java.util.jar.JarFile;

public class FastBase implements Serializable {
	
	private String fastBaseSerializationFilename;
	private String fuseDBSerializationFilename;
	
	public DBConnectionManager dbConnectionManager;
	public ScriptManager scriptManager;
	public QueryManager queryManager;
	public FileManager fileManager;
	private Scheduler scheduler;
	public UserManager userManager;
	public transient ExportManager exportManager;
	
	private FastBase me;
	
	private transient ScriptAPI scriptAPI;
	
	public FastBase(String fastBaseSerializationFilename) throws Exception {
		
		this.fastBaseSerializationFilename = fastBaseSerializationFilename;
		
		this.dbConnectionManager = new DBConnectionManager();
		this.scriptManager = new ScriptManager();
		this.queryManager = new QueryManager(this.dbConnectionManager);
		this.fileManager = new FileManager();
		this.scheduler = new Scheduler(20);
		this.userManager = new UserManager();
		this.exportManager = new ExportManager(this);
		
		this.me = this;
		
		this.createScriptAPI();
		
		this.addShutdownHook();
		
	}

	public Scheduler scheduler() {
		return this.scheduler;
	}
	
	public ScriptAPI scriptAPI() {
		return this.scriptAPI;
	}
	
	public void createScriptAPI() {
		this.scriptAPI = new ScriptAPI(this);
		this.scheduler.setScriptAPI(this.scriptAPI);
	}
	
	public static FastBase FastBaseFactory(String fastBaseSerializationFilename) throws Exception {
		
		FastBase fastBase = null;
		
		File f = new File(fastBaseSerializationFilename);
		
		if(f.exists()) {
			
			fastBase =
				FastBase.deserialize(fastBaseSerializationFilename);
			
		} else if(!f.exists()) {
			
			f.getParentFile().mkdirs();
			
			fastBase = new FastBase(
				fastBaseSerializationFilename
			);
		}
		
		fastBase.createScriptAPI();
		
		return fastBase;
		
	}
	
	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				me.serialize();
			}
		}));
	}
	
	private void serialize() {
		try {
			
			this.scheduler().uninitialize();
			
			FileOutputStream fileOut = new FileOutputStream(this.fastBaseSerializationFilename);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			
		} catch(IOException i) {
			// TODO: Add logging
			i.printStackTrace();
		}
	}
	
	private static FastBase deserialize(String fastBaseSerializationFilename) {
		FastBase fastBase = null;
		
		try {
			FileInputStream fileIn = new FileInputStream(fastBaseSerializationFilename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			fastBase = (FastBase)in.readObject();
			
			in.close();
			fileIn.close();
			
			//fastBase.dbConnectionManager.connectAll();
			
			fastBase.scheduler().initialize();
			fastBase.scheduler().setScriptAPI(fastBase.scriptAPI());
			
			fastBase.userManager.startSessionCleaner();
			
			fastBase.exportManager = new ExportManager(fastBase);
			
			fastBase.addShutdownHook();
			
		} catch(Exception e) {
			// TODO: Add logging
			System.out.println(e.getMessage());
		}
		
		return fastBase;
	}
	
	/*
		Run it in background:
		
			Unsecured:
				nohup java -jar fastbase.jar -port=80 > log.txt &
			
			Secured:
				nohup java -jar fastbase.jar -useHttps=true -sslCertificateFileName=NameOfCertificate -sslKeyStorePassword=Password
				
	*/
	public static void main(String args[]) throws Exception {
		
		CommandLineParameters parameters =
			new CommandLineParameters(args);
		
		int port =
			Integer.parseInt(
				parameters.get(
					"port",
					Config.HTTP_PORT + ""
				)
			);
		
		boolean useHttps =
			Boolean.parseBoolean(
				parameters.get("useHttps", "false")
			);

		JDBCDriverInfo.readInfoFromFile(null);
		
		FastBase fastBase =
			FastBase.FastBaseFactory(
				parameters.get("fastbase_serialization_filename", Config.FASTBASE_SERIALIZATION_FILENAME)
			);
			
		FastBaseWebServer fastBaseWebServer =
			new FastBaseWebServer(
				fastBase
			);
		
		if(!useHttps) {
			
			HTTPServer httpServer =
				new HTTPServer(
					port,
					fastBaseWebServer,
					Config.HTTP_SERVER_THREAD_COUNT
				);

			httpServer.runServer();
			
		} else if(useHttps) {
			
			String sslCertificateFileName =
				parameters.get(
					"sslCertificateFileName",
					null
				);
				
			String sslKeyStorePassword =
				parameters.get(
					"sslKeyStorePassword",
					null
				);
			
			SecureHTTPServer httpServer =
				new SecureHTTPServer(
					port,
					fastBaseWebServer,
					new FileInputStream(
						"./ssl/" + sslCertificateFileName
					),
					sslKeyStorePassword,
					Config.HTTP_SERVER_THREAD_COUNT
				);

			httpServer.runServer();
			
		}
		
	}
	
}