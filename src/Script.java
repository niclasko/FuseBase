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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptContext;
import javax.script.Bindings;
import javax.script.SimpleScriptContext;
import java.io.Serializable;
import java.io.DataOutput;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.jar.JarEntry;

/*

	Testing bindings:
	
		var queryResults = FuseBase.query('UOVDEV', 'select * from dummy');

		output.writeBytes(queryResults);

*/

public class Script implements Serializable {

	private static ScriptEngine scriptEngine =
		(new ScriptEngineManager()).getEngineByName("JavaScript");
	private static FuseBase fuseBase;

	public static void setFuseBaseInstance(FuseBase _fuseBase) {
		fuseBase = fuseBase;
	}

	public static void loadGlobalScripts(ScriptContext scriptContext) {
		try {

			JarEntry[] jarEntries = fuseBase.fileManager.getJarEntriesFromJarFileDirectory(
				"scripts/"
			);

			for(JarEntry jarEntry : jarEntries) {
				scriptEngine.eval(
					Utils.readInputStreamToString(
						fuseBase.fileManager.getFileInputStreamFromJarFile(
							jarEntry.getRealName()
						)
					),
					scriptContext
				);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String name;
	private String source;
	
	private transient boolean initialized = false;
	
	private transient ScriptContext scriptContext;
	private transient Bindings scriptBindings;
	
	public Script(String name, String source) {
		
		this.name = name;
		this.setSource(source);
		
	}
	
	public void init(ScriptAPI scriptAPI) {
		
		if(this.initialized) {
			return;
		}

		this.scriptContext = new SimpleScriptContext();
		this.scriptBindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

		this.scriptBindings.put(
			"FuseBase",
			scriptAPI
		);
		
		this.initialized = true;
	}
	
	public String name() {
		return this.name;
	}
	
	public String source() {
		return this.source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public void addBinding(String key, Object value) {
		
		this.scriptBindings.put(
			key,
			value
		);
		
	}
	
	public void run(DataOutput output) throws Exception {
		
		if(!this.initialized) {
			return;
		}
		
		this.scriptBindings.put(
			"output",
			output
		);

		this.scriptEngine.eval(
			this.source,
			this.scriptContext
		);
		
	}
	
	public JSONBuilder getJSONBuilder() {
		return
			JSONBuilder.f().
				$('{').
					k("name").v(this.name()).
				$('}');
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		return
			JSONBuilder.f().
				$('{').
					k("name").v(this.name()).
					k("source").v(URLEncoder.encode(this.source())).
				$('}');
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	public static void runAnonymousScript(ScriptAPI scriptAPI, String[][] parameters, String[][] cookies, PersistedDataOutputStream output, String scriptSource) throws Exception {

		ScriptContext scriptContext = new SimpleScriptContext();
		Bindings scriptBindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
		
		scriptBindings.put(
			"FuseBase",
			scriptAPI
		);
		
		scriptBindings.put(
			"parameters",
			parameters
		);
		
		scriptBindings.put(
			"cookies",
			cookies
		);
		
		scriptBindings.put(
			"output",
			output
		);

		scriptEngine.eval(
			scriptSource,
			scriptContext
		);
		
	}
	
	public static void main(String args[]) {
		
		Script script =
			new Script(
				"Test",
				"var test = {'k1': 1, 'k2': 2}; Test.call(test);"
			);
		
		try {
			
			script.init(
				null
			);
			
			PersistedDataOutputStream persistedOutput =
				new PersistedDataOutputStream();
			
			script.run(persistedOutput);
			
			System.out.println(persistedOutput.getData());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}