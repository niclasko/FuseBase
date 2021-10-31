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
import java.io.Serializable;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.File;
import java.util.jar.JarFile;

public class FuseBase implements Serializable {
	
	private String fuseBaseSerializationFilename;
	
	public DBConnectionManager dbConnectionManager;
	public ScriptManager scriptManager;
	public QueryManager queryManager;
	public FileManager fileManager;
	private Scheduler scheduler;
	public UserManager userManager;
	public transient ExportManager exportManager;
	
	private FuseBase me;
	
	private transient ScriptAPI scriptAPI;

	private transient JarFile jarFile;
	
	public FuseBase(String fuseBaseSerializationFilename) throws Exception {
		
		this.fuseBaseSerializationFilename = fuseBaseSerializationFilename;
		
		this.dbConnectionManager = new DBConnectionManager();
		this.scriptManager = new ScriptManager();
		this.queryManager = new QueryManager(this.dbConnectionManager);
		this.fileManager = new FileManager();
		this.scheduler = new Scheduler(20);
		this.userManager = new UserManager();
		this.exportManager = new ExportManager(this);
		
		this.me = this;
		
		this.addShutdownHook();
		
	}

	public Scheduler scheduler() {
		return this.scheduler;
	}
	
	public ScriptAPI scriptAPI() {
		return this.scriptAPI;
	}
	
	public void createScriptAPI() {
		Script.setFuseBaseInstance(this);
		this.scriptAPI = new ScriptAPI(this);
		this.scheduler.setScriptAPI(this.scriptAPI);
	}
	
	public static FuseBase FuseBaseFactory(String fuseBaseSerializationFilename) throws Exception {
		
		FuseBase fuseBase = null;
		
		File f = new File(fuseBaseSerializationFilename);
		
		if(f.exists()) {
			
			fuseBase =
				FuseBase.deserialize(fuseBaseSerializationFilename);
			
		} else if(!f.exists()) {
			
			f.getParentFile().mkdirs();
			
			fuseBase = new FuseBase(
				fuseBaseSerializationFilename
			);
		}

		fuseBase.setCurrentJarFile();
		fuseBase.createScriptAPI();

		return fuseBase;
		
	}

	public JarFile getJarFile() {
		return this.jarFile;
	}

	private void setCurrentJarFile() {
		try {
			this.jarFile =
				new JarFile(
					(new File(FuseBaseRESTAPI.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath())
				);
			this.fileManager.setJarFile(this.jarFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
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

			File fuseBaseSerializationFile = new File(this.fuseBaseSerializationFilename);
			fuseBaseSerializationFile.createNewFile();

			FileOutputStream fileOut = new FileOutputStream(fuseBaseSerializationFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
			
		} catch(IOException i) {
			// TODO: Add logging
			i.printStackTrace();
		}
	}
	
	private static FuseBase deserialize(String fuseBaseSerializationFilename) {
		FuseBase fuseBase = null;
		
		try {
			FileInputStream fileIn = new FileInputStream(fuseBaseSerializationFilename);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			fuseBase = (FuseBase)in.readObject();
			
			in.close();
			fileIn.close();
			
			//fuseBase.dbConnectionManager.connectAll();
			
			fuseBase.scheduler().initialize();
			fuseBase.scheduler().setScriptAPI(fuseBase.scriptAPI());
			
			fuseBase.userManager.startSessionCleaner();
			
			fuseBase.exportManager = new ExportManager(fuseBase);
			
			fuseBase.addShutdownHook();
			
		} catch(Exception e) {
			// TODO: Add logging
			System.out.println(e.getMessage());
		}
		
		return fuseBase;
	}
	
	/*
		Run it in background:
		
			Unsecured:
				nohup java -jar fusebase.jar -port=80 > log.txt &
			
			Secured:
				nohup java -jar fusebase.jar -useHttps=true -sslCertificateFileName=NameOfCertificate -sslKeyStorePassword=Password
				
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
		
		FuseBase fuseBase =
			FuseBase.FuseBaseFactory(
				parameters.get("fusebase_serialization_filename", Config.FUSEBASE_SERIALIZATION_FILENAME)
			);
			
		FuseBaseWebServer fuseBaseWebServer =
			new FuseBaseWebServer(
				fuseBase
			);
		
		if(!useHttps) {
			
			HTTPServer httpServer =
				new HTTPServer(
					port,
					fuseBaseWebServer,
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
					fuseBaseWebServer,
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