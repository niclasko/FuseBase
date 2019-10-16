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
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.io.File;

class DriverShim implements Driver {
	
	private Driver driver;
	
	DriverShim(Driver d) {
		this.driver = d;
	}
	
	public boolean acceptsURL(String u) throws SQLException {
		return this.driver.acceptsURL(u);
	}
	
	public Connection connect(String u, Properties p) throws SQLException {
		return this.driver.connect(u, p);
	}
	
	public int getMajorVersion() {
		return this.driver.getMajorVersion();
	}
	
	public int getMinorVersion() {
		return this.driver.getMinorVersion();
	}
	
	public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
		return this.driver.getPropertyInfo(u, p);
	}
	
	public boolean jdbcCompliant() {
		return this.driver.jdbcCompliant();
	}
	
	public Logger getParentLogger() {
		return null;
	}
	
	/*
	** Add related libraries needed by JDBC driver
	** All jars in the same directory as JDBC driver path
	*/
	private static void updateClassPath(String jdbcDriverPath) throws SQLException, Exception {
		
		String jdbcDriverFolderPath = 
			jdbcDriverPath.substring(0, jdbcDriverPath.lastIndexOf("/") + 1);

		File[] files = new File(jdbcDriverFolderPath).listFiles();
		
		URLClassLoader classLoader =
			(URLClassLoader)ClassLoader.getSystemClassLoader();
	
		for(int i=0; i<files.length; i++) {
			if(files[i].toString().lastIndexOf(".jar") > 0) {
				
				URL url = files[i].toURI().toURL();
				
				Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				method.invoke(classLoader, url);
				
			}
		}
		
	}

	private static void registerDriver(String jdbcDriverPath, String className) {
		DriverShim.updateClassPath(jdbcDriverPath);
	
		URL u = new URL("jar:file:" + jdbcDriverPath + "!/");
		String classname = className;
		URLClassLoader ucl = new URLClassLoader(new URL[] { u });
		Driver d = (Driver)Class.forName(classname, true, ucl).newInstance();
		DriverManager.registerDriver(new DriverShim(d));
	}

	public static Connection getConnection(String jdbcDriverPath, String className, String connectionString) throws SQLException, Exception {
		DriverShim.registerDriver(jdbcDriverPath, className);
		return DriverManager.getConnection(connectionString);
	}
	
	public static Connection getConnection(String jdbcDriverPath, String className, String connectionString, String user, String passWord) throws SQLException, Exception {
		DriverShim.registerDriver(jdbcDriverPath, className);		
		return DriverManager.getConnection(connectionString, user, passWord);
		
	}
}