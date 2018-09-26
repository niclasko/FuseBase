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
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Collection;
import java.io.Serializable;

public class JDBCDriverInfo implements Serializable {
	
	private static String defaultJDBCDriverInfoFileName = "./jdbc_drivers/JDBCDrivers.txt";
	private static HashMap<String, JDBCDriverInfo> driverInfoList = new HashMap<String, JDBCDriverInfo>();
	
	private String name;
	private String jdbcDriverPath;
	private String className;
	private String connectStringPattern;
	private String validationQuery;
	private int connectionPoolSize;
	
	public JDBCDriverInfo(String name, String jdbcDriverPath, String className, String connectStringPattern, String validationQuery, int connectionPoolSize) {
		this.name = name;
		this.jdbcDriverPath = jdbcDriverPath;
		this.className = className;
		this.connectStringPattern = connectStringPattern;
		this.validationQuery = validationQuery;
		this.connectionPoolSize = connectionPoolSize;
	}
	
	public String toString() {
		return
			"Name: " + this.name + "\n" +
			"JDBC Driver Path: " + this.jdbcDriverPath + "\n" +
			"Class Name: " + this.className + "\n" +
			"Connect String Pattern: " + this.connectStringPattern;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getJDBCDriverPath() {
		return this.jdbcDriverPath;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public String getConnectStringPattern() {
		return this.connectStringPattern;
	}
	
	public String getValidationQuery() {
		return this.validationQuery;
	}
	
	public int getConnectionPoolSize() {
		return this.connectionPoolSize;
	}
	
	public static Collection<JDBCDriverInfo> getDriverInfoListEntries() {
		return driverInfoList.values();
	}
	
	public static JDBCDriverInfo getJDBCDriverInfo(String name) {
		return driverInfoList.get(name);
	}
	
	public static void printDriverList() {
		for(JDBCDriverInfo i : JDBCDriverInfo.getDriverInfoListEntries()) {
			System.out.println(i.toString());
		}
	}
	
	public static boolean isIdentifier(String line) {
		if(line.equals("")) {
			return false;
		}
		
		if(line.substring(0,1).equals("[") &&
			line.substring(line.length()-1).equals("]")) {
			return true;
		}
		
		return false;
	}
	
	private static void addJDBCDriverInfo(HashMap<String, String> props) {
		driverInfoList.put(
			props.get("Name"),
			new JDBCDriverInfo(
				props.get("Name"),
				props.get("DriverPath"),
				props.get("ClassName"),
				props.get("ConnectStringPattern"),
				props.get("ValidationQuery"),
				(props.get("ConnectionPoolSize") != null ?
					Integer.parseInt(props.get("ConnectionPoolSize")) : Config.DB_CONNECTION_POOL_SIZE)
			)
		);
	}
 	
	public static void readInfoFromFile(String fileName) {
		String _fileName = (fileName == null ? defaultJDBCDriverInfoFileName : fileName);
		HashMap<String, String> props = null;
		String prop[];
		
		try {
			FileReader fr = new FileReader(_fileName);
			BufferedReader br = new BufferedReader(fr);

			String line = "";

			while((line = br.readLine()) != null) {
				if(isIdentifier(line)) {
					
					if(props != null) {
						JDBCDriverInfo.addJDBCDriverInfo(props);
					} 
					
					props = new HashMap<String, String>();
					props.put("Name", line.substring(1,line.length()-1));
				} else if(line.indexOf("=") > -1) {
					
					prop = new String[2];
					
					prop[0] = line.substring(0, line.indexOf("="));
					prop[1] = line.substring(line.indexOf("=")+1);
					
					props.put(prop[0], prop[1]);
					
				}
			}
			
			if(props != null) {
				JDBCDriverInfo.addJDBCDriverInfo(props);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static JSONBuilder getJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(JDBCDriverInfo ji : JDBCDriverInfo.getDriverInfoListEntries()) {
			jb.
				$('{').
					k("name").v(ji.getName()).
					k("className").v(ji.getClassName()).
					k("connectStringPattern").v(ji.getConnectStringPattern()).
					k("className").v(ji.getClassName()).
				$('}');
		}
		
		jb.$(']');
		
		return jb;
		
	}
	
	public static String toJSON() {
		return JDBCDriverInfo.getJSONBuilder().getJSON();
	}
	
	public static void main(String args[]) {
		JDBCDriverInfo.readInfoFromFile(null);
		JDBCDriverInfo.printDriverList();
	}
}