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
public class ExportManager {
	
	private static final byte[] ENCRYPTION_KEY =
		new byte[] {
			'0','j','l','k','l','2','1','3','4','n','z','3','3','f','e','7'
		};
	
	private FastBase fastBase;
	
	public ExportManager(FastBase fastBase) {
		this.fastBase = fastBase;
	}
	
	public byte[] exportAll() throws Exception {
		
		JSONBuilder exportJSON =
			new JSONBuilder();
			
		exportJSON
			.$('{')
				.k("connections").v(
					this.fastBase.dbConnectionManager.getJSONBuilderForExport()
				)
				.k("queries").v(
					this.fastBase.queryManager.getJSONBuilder()
				)
				.k("scripts").v(
					this.fastBase.scriptManager.getJSONBuilderForExport()
				)
				.k("scheduledJobs").v(
					this.fastBase.scheduler().getJSONBuilderForExport()
				)
				.k("roles").v(
					this.fastBase.userManager.getRolesJSONBuilderForExport()
				)
				.k("users").v(
					this.fastBase.userManager.getJSONBuilderForExport()
				)
				.k("clientKeys").v(
					this.fastBase.userManager.getClientKeysJSONBuilderForExport()
				)
			.$('}');
		
		return
			Utils.Encryptor.encrypt(
				ExportManager.ENCRYPTION_KEY,
				exportJSON.getJSON()
			);
		
	}
	
	public void importAll(byte[] encryptedJsonData) throws Exception {
		
		String jsonText =
			Utils.Encryptor.decrypt(
				ExportManager.ENCRYPTION_KEY,
				encryptedJsonData
			);
			
		JSONParser jsonParser =
			new JSONParser(jsonText);
			
		JSONDataStructure json =
			jsonParser.getJSONDataStructure();
		
		if(json.get("connections") != null) {
			
			for(JSONKeyValue entry : ((JSONDataStructure)json.get("connections").getValue()).entries()) {

				this.fastBase.dbConnectionManager.addConnection(
					(JSONDataStructure)entry.getValue()
				);

			}
			
		}
		
		if(json.get("queries") != null) {
			
			for(JSONKeyValue entry : ((JSONDataStructure)json.get("queries").getValue()).entries()) {

				this.fastBase.queryManager.addQuery(
					(JSONDataStructure)entry.getValue()
				);

			}
			
		}
		
		if(json.get("scripts") != null) {
			
			for(JSONKeyValue entry : ((JSONDataStructure)json.get("scripts").getValue()).entries()) {

				this.fastBase.scriptManager.addScript(
					(JSONDataStructure)entry.getValue()
				);

			}
			
		}
		
		if(json.get("scheduledJobs") != null) {
			
			for(JSONKeyValue entry : ((JSONDataStructure)json.get("scheduledJobs").getValue()).entries()) {

				this.fastBase.scheduler().addJob(
					(JSONDataStructure)entry.getValue(),
					this.fastBase.scriptManager
				);

			}
			
		}
		
		if(json.get("roles") != null) {
			
			for(JSONKeyValue entry : ((JSONDataStructure)json.get("roles").getValue()).entries()) {
				
				this.fastBase.userManager.addRole(
					(JSONDataStructure)entry.getValue()
				);

			}
			
		}
		
		if(json.get("users") != null) {
			
			for(JSONKeyValue entry : ((JSONDataStructure)json.get("users").getValue()).entries()) {
				
				this.fastBase.userManager.addUser(
					(JSONDataStructure)entry.getValue()
				);

			}
			
		}
		
		if(json.get("clientKeys") != null) {
			
			for(JSONKeyValue entry : ((JSONDataStructure)json.get("clientKeys").getValue()).entries()) {
				
				this.fastBase.userManager.addUserClientKeys(
					(JSONDataStructure)entry.getValue()
				);

			}
			
		}
		
	}
	
	public static void main(String args[]) throws Exception {
		
		JSONBuilder jb =
			new JSONBuilder();
			
		jb.$('{');
		jb.k("key").v(1);
		jb.k("value").v("This is the text for key 1.");
		jb.$('}');
		
		java.nio.file.Files.write(
			java.nio.file.Paths.get("test.enc"),
			Utils.Encryptor.encrypt(
				ExportManager.ENCRYPTION_KEY,
				jb.getJSON()
			)
		);
		
		String decrypted =
			Utils.Encryptor.decrypt(
				ExportManager.ENCRYPTION_KEY,
				java.nio.file.Files.readAllBytes(
					java.nio.file.Paths.get("test.enc")
				)
			);
			
		System.out.println(decrypted);
			
		JSONParser jsonParser =
			new JSONParser(decrypted);
			
		JSONDataStructure json =
			jsonParser.getJSONDataStructure();
		
		System.out.println(json.get("key").getValue());
		
	}
	
}