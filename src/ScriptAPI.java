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
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.Serializable;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.DataOutput;
import java.util.HashMap;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.internal.objects.NativeArray;
import java.util.Set;
import java.util.Map;
import java.util.List;

public class ScriptAPI implements Serializable {
	
	private FastBase fastBase;
	
	public ScriptAPI(FastBase fastBase) {
		this.fastBase = fastBase;
	}
	
	public QueryObject getQueryObject(String connectionName, String query) {
		
		try {
			
			return
				this.fastBase.queryManager.getQueryObject(
					connectionName,
					query
				);
			
		} catch(Exception e) {
			
			return null;
			
		}
		
	}
	
	public String query(String connectionName, String query) {
		
		try {
			
			PersistedPrintWriter persistedPrintWriter = new PersistedPrintWriter();
			DataWriter dataWriter = new JSONWriter(persistedPrintWriter);
			
			this.fastBase.queryManager.sql(
				dataWriter,
				connectionName,
				query
			);

			return persistedPrintWriter.getData();
			
		} catch(Exception e) {
			
			return this.error(e);
			
		}
		
	}
	
	public String csvToJson(String url, char delimiter) {
		
		try {

			return CSVReader.csvToJSON(
				this.getURL(url),
				delimiter
			);
			
		} catch(Exception e) {
			
			return this.error(e);
			
		}
		
	}
	
	public String fuse(String sql) {
		
		try {
			
			JSONBuilder jb = JSONBuilder.f();
			
			String modifiedSQL = sql;
			
			SQLParser sp =
				new SQLParser(sql);
				
			SQLParseTreeNode[] qNodes =
				sp.getSQLParseTreeNodesBySQLKeyWord(
					SQLKeyWord.Q
				);
				
			SQLParseTreeNode[] csvNodes =
				sp.getSQLParseTreeNodesBySQLKeyWord(
					SQLKeyWord.CSV
				);
			
			SQLParseTreeNode[] xlNodes =
				sp.getSQLParseTreeNodesBySQLKeyWord(
					SQLKeyWord.XL
				);
			
			SQLParseTreeNode[] jsonNodes =
				sp.getSQLParseTreeNodesBySQLKeyWord(
					SQLKeyWord.JSON
				);
			
			SQLParseTreeNode tmpNode = null;
				
			jb.$('{');
			
			jb.k("FastBaseQueries").$('[');
				
			for(SQLParseTreeNode qNode : qNodes) {
				
				modifiedSQL =
					modifiedSQL.replace(
						sql.substring(
							qNode.sqlToken().position()-1,
							qNode.getLastChild().sqlToken().position() + 1
						),
						"?"
					);
				
				jb.$('{');
				
				jb.k("type").v("q");
				jb.k("sql").v(qNode.getFirstChild().getFirstChild().sqlToken());
				jb.k("connection").v(qNode.getFirstChild().getNthChild(2).sqlToken());
				jb.k("position").v(qNode.sqlToken().position());
				
				jb.$('}');
				
			}
			
			for(SQLParseTreeNode csvNode : csvNodes) {
				
				modifiedSQL =
					modifiedSQL.replace(
						sql.substring(
							csvNode.sqlToken().position(),
							csvNode.getLastChild().sqlToken().position() + 1
						),
						"?"
					);
				
				jb.$('{');
				
				jb.k("type").v("csv");
				jb.k("url").v(csvNode.getFirstChild().getFirstChild().sqlToken());
				jb.k("delimiter").v(csvNode.getFirstChild().getNthChild(2).sqlToken());
				jb.k("position").v(csvNode.sqlToken().position());
				
				jb.$('}');
				
			}
			
			for(SQLParseTreeNode xlNode : xlNodes) {
				
				modifiedSQL =
					modifiedSQL.replace(
						sql.substring(
							xlNode.sqlToken().position(),
							xlNode.getLastChild().sqlToken().position() + 1
						),
						"?"
					);
				
				jb.$('{');
				
				jb.k("type").v("xl");
				jb.k("url").v(xlNode.getFirstChild().getFirstChild().sqlToken());
				jb.k("position").v(xlNode.sqlToken().position());
				
				jb.$('}');
				
			}
			
			for(SQLParseTreeNode jsonNode : jsonNodes) {
				
				modifiedSQL =
					modifiedSQL.replace(
						sql.substring(
							jsonNode.sqlToken().position(),
							jsonNode.getLastChild().sqlToken().position() + 1
						),
						"?"
					);
				
				jb.$('{');
				
				jb.k("type").v("json");
				jb.k("url").v(jsonNode.getFirstChild().getFirstChild().sqlToken());
				jb.k("requestHeaders").v(jsonNode.getFirstChild().getNthChild(2).sqlToken());
				jb.k("position").v(jsonNode.sqlToken().position());
				
				jb.$('}');
				
			}
			
			jb.$(']');
			
			jb.k("sql").v(modifiedSQL);
			
			jb.$('}');
			
			return jb.getJSON();
			
		} catch(Exception e) {
			
			return this.error(e);
			
		}
		
	}
	
	/*private String getSQLParseTreeJSON(SQLParseTreeNode root) {
		
		JSONBuilder json =
			new JSONBuilder();
			
		this.getSQLParseTreeJSON(
			root,
			json
		);
		
	}
	
	private String getSQLParseTreeJSON(SQLParseTreeNode parseTreeNode, JSONBuilder json) {
		
		
		
		for(SQLParseTreeNode childParseTreeNode : parseTreeNode.getChildren()) {
			
			this.getSQLParseTreeJSON(
				childParseTreeNode,
				json
			);
			
		}
		
	}*/
	
	public void queryToFile(String connectionName, String query, String fileName, String outputTypeText) throws Exception {
		
		File file = new File(fileName);
		
		file.getParentFile().mkdirs();
		
		OutputType outputType =
			OutputType.valueOf(outputTypeText);
		
		PrintWriter printWriter = new PrintWriter(file);
		
		DataWriter dataWriter = null;
			
		if(outputType == OutputType.JSON) {
			
			dataWriter =
				new JSONWriter(printWriter);
			
		} else if(outputType == OutputType.JSON_TABULAR) {
			
			dataWriter =
				new JSONWriterTabular(printWriter);
			
		} else if(outputType == OutputType.CSV) {
			
			dataWriter =
				new CSVWriter(printWriter);
			
		}
		
		this.fastBase.queryManager.sql(
			dataWriter,
			connectionName,
			query
		);
		
		printWriter.flush();
		
		printWriter.close();
		
	}
	
	public PrintWriter getFileForWriting(String fileName) throws Exception {
		
		return this.getFileForWriting(fileName, false);
		
	}
	
	public PrintWriter getFileForWriting(String fileName, boolean append) throws Exception {
		
		File file = new File(fileName);
		
		file.getParentFile().mkdirs();
		
		FileWriter fileWriter =
			new FileWriter(
				file,
				append
			);
		
		return new PrintWriter(fileWriter);
		
	}
	
	public BufferedReader getFileForReading(String fileName) throws Exception {
		
		return new BufferedReader(new FileReader(fileName));
		
	}
	
	public String ddl(String connectionName, String ddl) {
		
		try {
			
			int ddlStatus = this.fastBase.queryManager.ddl(connectionName, ddl);
				
			return this.success("DDL status: " + ddlStatus);
			
		} catch(Exception e) {
			
			return this.error(e);
			
		}
		
	}
	
	public String dml(String connectionName, String dml) {
		
		try {
			
			int recordCount = this.fastBase.queryManager.dml(connectionName, dml);
				
			return this.success(recordCount + " rows affected.");
			
		} catch(Exception e) {
			
			return this.error(e);
			
		}
		
	}
	
	public PreparedStatement getPreparedStatement(String connectionName, String query) {
		
		try {
				
			return
				this.fastBase.queryManager.getPreparedStatement(
					connectionName,
					query
				);
			
		} catch(Exception e) {

			return null;

		}
		
	}
	
	public CallableStatement getCallableStatement(String connectionName, String query) {
		
		try {
				
			return
				this.fastBase.queryManager.getCallableStatement(
					connectionName,
					query
				);
			
		} catch(Exception e) {

			return null;

		}
		
	}
	
	public String getURL(String urlAddress) {
		try {
			
			URL url = new URL(urlAddress);
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			char[] buffer = new char[4000];
			int bytesRead = 0;
			
			StringBuilder responseBody = new StringBuilder();
			
			while((bytesRead = br.read(buffer, 0, buffer.length)) != -1) {
				
				responseBody.append(
					buffer,
					0,
					bytesRead
				);
				
			}
			
			is.close();
			
			return responseBody.toString();
			
		} catch (Exception e) {
			return this.error(e);
		}
	}
	
	public byte[] getURLAsBytes(String urlAddress) throws Exception {
		
		URL url = new URL(urlAddress);
		InputStream is = url.openStream();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] buffer = new byte[4000];
		int bytesRead = 0;
		
		while((bytesRead = is.read(buffer, 0, buffer.length)) != -1) {
			
			baos.write(
				buffer,
				0,
				bytesRead
			);
			
		}
		
		is.close();
		
		return baos.toByteArray();
		
	}
	
	public ScriptHttpResponse getURL(String urlAddress, ScriptObjectMirror requestPropertyAssociativeArrayObject) throws Exception {
		
		URL url = new URL(urlAddress);
		
		HttpURLConnection httpUrlConnection =
			(HttpURLConnection)url.openConnection();
			
		httpUrlConnection.setRequestMethod("GET");
		
		this.setRequestHeaders(
			httpUrlConnection,
			requestPropertyAssociativeArrayObject
		);
		
		return this.processRequest(
			httpUrlConnection
		);
		
	}
	
	public ScriptHttpResponse postURL(String urlAddress, ScriptObjectMirror requestPropertyAssociativeArrayObject, String postData) throws Exception {
		
		URL url = new URL(urlAddress);
		
		HttpURLConnection httpUrlConnection =
			(HttpURLConnection)url.openConnection();
			
		httpUrlConnection.setRequestMethod("POST");
		
		this.setRequestHeaders(
			httpUrlConnection,
			requestPropertyAssociativeArrayObject
		);
		
		// Send post request
		httpUrlConnection.setDoOutput(true);
		DataOutputStream postDataOutputStream =
			new DataOutputStream(
				httpUrlConnection.getOutputStream()
			);
		postDataOutputStream.writeBytes(postData);
		postDataOutputStream.flush();
		postDataOutputStream.close();
		
		return this.processRequest(
			httpUrlConnection
		);
		
	}
	
	private void setRequestHeaders(HttpURLConnection httpUrlConnection, ScriptObjectMirror requestPropertyAssociativeArrayObject) throws Exception {
		
		for (Map.Entry<String, Object> entry : requestPropertyAssociativeArrayObject.entrySet()) {

			httpUrlConnection.setRequestProperty(
				entry.getKey(),
				entry.getValue().toString()
			);

		}
		
	}
	
	private ScriptHttpResponse processRequest(HttpURLConnection httpUrlConnection) throws Exception {
		
		BufferedReader br =
			new BufferedReader(
				new InputStreamReader(
					httpUrlConnection.getInputStream()
				)
			);
		
		char[] buffer = new char[4000];
		int bytesRead = 0;
		
		StringBuilder responseBody = new StringBuilder();
		
		while((bytesRead = br.read(buffer, 0, buffer.length)) != -1) {
			
			responseBody.append(
				buffer,
				0,
				bytesRead
			);
			
		}
		
		br.close();
		
		return new ScriptHttpResponse(
			httpUrlConnection.getHeaderFields(),
			responseBody.toString()
		);
		
	}
	
	public void callScript(String scriptName, DataOutput output) throws Exception {
		
		try {
			
			Script script =
				this.fastBase.scriptManager.getScript(scriptName);
				
			script.init(this);
			
			script.run(output);
			
		} catch(Exception e) {
			
			output.writeBytes(
				Utils.exceptionToString(e)
			);
			
		}
		
	}
	
	public void callScript(String scriptName, ScriptObjectMirror scriptParameters, DataOutput output) throws Exception {
		
		try {
			
			Script script =
				this.fastBase.scriptManager.getScript(scriptName);
				
			String[][] parameters =
				new String[scriptParameters.entrySet().size()][2];
				
			int i = 0;
				
			for (Map.Entry<String, Object> entry : scriptParameters.entrySet()) {
				
				parameters[i][0] = entry.getKey();
				parameters[i][1] = entry.getValue().toString();
				
				i++;

			}
				
			script.init(this);
			
			script.addBinding(
				"parameters",
				parameters
			);
			
			script.run(output);
			
		} catch(Exception e) {
			
			output.writeBytes(
				Utils.exceptionToString(e)
			);
			
		}
		
	}
	
	public String command(NativeArray commandSpec) throws Exception {
		String[] commandSpecArray = (String[])commandSpec.getArray().asArrayOfType(String.class);
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(commandSpecArray);
		return this.read(pr.getInputStream());
	}
	
	public String command(String command) throws Exception {
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(command);
		return this.read(pr.getInputStream());
	}
	
	private String read(InputStream is) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		char[] buffer = new char[4000];
		int bytesRead = 0;

		StringBuilder responseBody = new StringBuilder();

		while((bytesRead = br.read(buffer, 0, buffer.length)) != -1) {

			responseBody.append(
					buffer,
					0,
					bytesRead
			);

		}

		is.close();

		return responseBody.toString();
	}
	
	private String error(Exception e) {
		return JSONBuilder.f().
			$('{').
				k("status").v("ERROR").
				k("message").v(e.getMessage()).
			$('}').getJSON();
	}
	
	private String success(String message) {
		return JSONBuilder.f().
			$('{').
				k("status").v("OK").
				k("message").v(message).
			$('}').getJSON();
	}
	
}