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
import java.util.HashSet;
import java.util.HashMap;
import java.io.Serializable;

public class User implements Serializable {
	
	private UserManager userManager;
	private String username;
	private String password;
	private boolean undeletable;
	private boolean unchangeable;
	
	private HashSet<String> clientKeys;
	private HashSet<String> privileges;
	private HashMap<String, Role> roles;
	
	public User(UserManager userManager, String username, String password, boolean undeletable, boolean unchangeable) {
		
		this.userManager = userManager;
		this.username = username;
		this.password = password;
		this.undeletable = undeletable;
		this.unchangeable = unchangeable;
		
		this.clientKeys = new HashSet<String>();
		this.privileges = new HashSet<String>();
		this.roles = new HashMap<String, Role>();
		
	}
	
	public String username() {
		
		return this.username;
		
	}
	
	public String password() {
		
		return this.password;
		
	}
	
	public boolean undeletable() {
		
		return this.undeletable;
		
	}
	
	public boolean unchangeable() {
		
		return this.unchangeable;
		
	}
	
	public void setUnchangeable(boolean unchangeable) {
		
		this.unchangeable = unchangeable;
		
	}
	
	public void setPassword(String password) {
		
		this.password = password;
		
	}
	
	public int clientKeyCount() {
		
		return this.clientKeys.size();
		
	}
	
	public MapAction addClientKey() {
		
		String clientKey =
			User.newClientKey();
			
		this.clientKeys.add(clientKey);
		
		this.userManager.addClientKey(
			clientKey,
			this
		);
		
		return MapAction.ADD;
		
	}
	
	public MapAction deleteClientKey(String clientKey) {
		
		if(this.clientKeys.contains(clientKey)) {
			
			this.clientKeys.remove(clientKey);

			this.userManager.deleteClientKey(
				clientKey
			);
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
 	
	public MapAction deleteAllClientKeys() {
		
		for(String clientKey : this.clientKeys) {
			
			this.userManager.deleteClientKey(
				clientKey
			);
			
		}
		
		this.clientKeys = new HashSet<String>();
		
		return MapAction.DELETE_ALL;
		
	}
	
	public MapAction addExternalClientKey(String externalClientKey) {
		
		if(!this.clientKeys.contains(externalClientKey)) {
			
			this.clientKeys.add(externalClientKey);

			this.userManager.addClientKey(
				externalClientKey,
				this
			);
			
			return MapAction.ADD;
			
		}
		
		return MapAction.ALREADY_EXISTS;
		
	}
	
	public MapAction addPrivileges(String[] privilegeList) {
		
		if(this.unchangeable()) {
			
			return MapAction.NOT_ALLOWED;
			
		}
		
		int addCount = 0;
		
		for(String privilegeKey : privilegeList) {
			
			if(!this.privileges.contains(privilegeKey)) {

				this.privileges.add(privilegeKey);
				
				addCount++;

			}
			
		}
		
		if(addCount > 0) {
			
			return MapAction.ADD;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deletePrivileges(String[] privilegeList) {
		
		if(this.unchangeable()) {
			
			return MapAction.NOT_ALLOWED;
			
		}
		
		int deleteCount = 0;
		
		for(String privilegeKey : privilegeList) {
		
			if(this.privileges.contains(privilegeKey)) {
			
				this.privileges.remove(privilegeKey);
				
				deleteCount++;
			
			}
			
		}
		
		if(deleteCount > 0) {
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteAllPrivileges() {
		
		if(this.unchangeable()) {
			
			return MapAction.NOT_ALLOWED;
			
		}
		
		this.privileges = new HashSet<String>();
		
		return MapAction.DELETE_ALL;
		
	}
	
	public MapAction addRoles(Role[] roleList) {
		
		if(this.unchangeable()) {
			
			return MapAction.NOT_ALLOWED;
			
		}
		
		int addCount = 0;
		
		for(Role role : roleList) {
			
			if(!this.roles.containsKey(role.name())) {

				this.roles.put(role.name(), role);
				
				addCount++;
				
			}
			
		}
		
		if(addCount > 0) {
			
			return MapAction.ADD;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteRoles(Role[] roleList) {
		
		if(this.unchangeable()) {
			
			return MapAction.NOT_ALLOWED;
			
		}
		
		int deleteCount = 0;
		
		for(Role role : roleList) {
			
			if(this.roles.containsKey(role.name())) {

				this.roles.remove(role.name());
				
				deleteCount++;

			}
			
		}
		
		if(deleteCount > 0) {
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteAllRoles() {
		
		if(this.unchangeable()) {
			
			return MapAction.NOT_ALLOWED;
			
		}
		
		this.roles = new HashMap<String, Role>();
		
		return MapAction.DELETE_ALL;
		
	}
	
	public boolean isAuthorized(String privilegeKey) {
		
		if(this.privileges.contains(privilegeKey)) {
			
			return true;
			
		}
		
		for(Role role : this.roles.values()) {
			
			if(role.isAuthorized(privilegeKey)) {
				
				return true;
				
			}
			
		}
		
		return false;
		
	}
	
	public JSONBuilder getClientKeysJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(String clientKey : this.clientKeys.toArray(new String[0])) {
			
			jb.v(clientKey);
			
		}
		
		jb.$(']');
		
		return jb;
		
	}
	
	public JSONBuilder getRolesAndPrivilegesJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('{');
		
		jb.k("roles");
		jb.$('[');
		
		for(Role role : this.roles.values()) {
			
			jb.v(role.name());
			
		}
		
		jb.$(']');
		
		jb.k("privileges");
		jb.$('[');
		
		for(String privilegeKey : this.privileges.toArray(new String[0])) {
			
			jb.v(privilegeKey);
			
		}
		
		jb.$(']');
		
		jb.$('}');
		
		return jb;
		
	}
	
	public JSONBuilder getJSONBuilder() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('{');
		
		jb.k("username").v(this.username);
		jb.k("undeleteable").v(this.undeletable);
		jb.k("unchangeable").v(this.unchangeable);
		jb.k("roles");
		jb.$('[');
		
		for(Role role : this.roles.values()) {
			
			jb.v(role.name());
			
		}
		
		jb.$(']');
		
		jb.k("privileges");
		jb.$('[');
		
		for(String privilegeKey : this.privileges.toArray(new String[0])) {
			
			jb.v(privilegeKey);
			
		}
		
		jb.$(']');
		
		return jb.$('}');
		
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('{');
		
		jb.k("username").v(this.username);
		jb.k("password").v(this.username);
		jb.k("undeleteable").v(this.undeletable);
		jb.k("unchangeable").v(this.unchangeable);
		jb.k("roles");
		jb.$('[');
		
		for(Role role : this.roles.values()) {
			
			jb.v(role.name());
			
		}
		
		jb.$(']');
		
		jb.k("privileges");
		jb.$('[');
		
		for(String privilegeKey : this.privileges.toArray(new String[0])) {
			
			jb.v(privilegeKey);
			
		}
		
		jb.$(']');
		
		return jb.$('}');
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	/*
	** Static fields and methods below
	*/
	public static String newClientKey() {
		
		return UserSession.newUserSessionKey();
		
	}
	
}