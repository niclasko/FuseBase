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
import java.util.HashMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Iterator;
import java.io.Serializable;

public class UserManager implements Serializable {
	
	private HashMap<String, User> users;
	private HashMap<String, UserSession> userSessions;
	private HashMap<String, User> clientKeys;
	
	private HashMap<String, Role> roles;
	
	private transient Thread sessionCleaner;
	
	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "peregrine";
	private static final String ADMIN_ROLENAME = "ADMIN";
	
	public UserManager() throws Exception {
		
		this.users =
			new HashMap<String, User>();
			
		this.userSessions =
			new HashMap<String, UserSession>();
			
		this.clientKeys =
			new HashMap<String, User>();
			
		this.roles =
			new HashMap<String, Role>();
			
		this.init();
		
		this.startSessionCleaner();
		
	}
	
	public void init() {
		
		Role adminRole =
			new RoleInverseRestricted(
				UserManager.ADMIN_ROLENAME,
				true,
				true
			);
			
		this.roles.put(adminRole.name(), adminRole);
			
		User adminUser =
			new User(
				this,
				UserManager.ADMIN_USERNAME,
				UserManager.ADMIN_PASSWORD,
				true,
				false
			);
			
		adminUser.addRoles(new Role[]{adminRole});
		adminUser.setUnchangeable(true);
		
		this.users.put(
			adminUser.username(),
			adminUser
		);
		
	}
	
	public void startSessionCleaner() throws Exception {
		
		final UserManager userManager = this;
		
		this.sessionCleaner =
			new Thread() {

				public void run() {
					
					try {

						userManager.removeExpiredSessions();
						
						this.sleep(60000);
						
					} catch(Exception e) {
						
						e.printStackTrace();
						
					}

				}

			};
		
		this.sessionCleaner.start();
		
	}
	
	private MapAction addUser(String username, String password, boolean undeletable, boolean unchangeable, Role[] roles, String[] privileges) {
		
		if(!this.users.containsKey(username)) {
			
			User user =
				new User(
					this,
					username,
					password,
					undeletable,
					unchangeable
				);
				
			user.addRoles(roles);
			user.addPrivileges(privileges);
			
			this.users.put(
				username,
				user
			);
			
			return MapAction.ADD;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction addUser(JSONDataStructure userJSON) throws Exception {
		
		if(userJSON.get("username") == null) {
			
			return MapAction.NONE;
			
		}
		
		String[] roleNames =
			JSONDataStructure.entriesToStringArray(
				((JSONDataStructure)userJSON.get("roles").getValue()).entries()
			);
			
		Role[] roles = new Role[roleNames.length];
		
		for(int i=0; i<roleNames.length; i++) {
			
			roles[i] =
				this.roles.get(roleNames[i]);
			
		}
		
		return this.addUser(
			userJSON.get("username").getValue().toString(),
			userJSON.get("password").getValue().toString(),
			Boolean.parseBoolean(userJSON.get("undeleteable").getValue().toString()),
			Boolean.parseBoolean(userJSON.get("unchangeable").getValue().toString()),
			roles,
			JSONDataStructure.entriesToStringArray(
				((JSONDataStructure)userJSON.get("privileges").getValue()).entries()
			)
		);
		
	}
	
	public MapAction addUser(String base64EncodedUsernameAndPassword, String[] roleList, String[] privilegeList) {
		
		String usernameAndPasswordTokens[] =
			Utils.Base64Encode.decode(
				base64EncodedUsernameAndPassword
			).split(":");
			
		if(usernameAndPasswordTokens.length != 2) {
			
			return MapAction.NONE;
			
		}
		
		String username = usernameAndPasswordTokens[0],
			password = usernameAndPasswordTokens[1];
			
		Role[] roles =
			this.toRoleArray(roleList);
		
		return this.addUser(
			username,
			password,
			false,
			false,
			roles,
			privilegeList
		);
		
	}
	
	public UserManagerFeedback changeUserPassword(String base64EncodedUsernameAndOldPassword, String base64EncodedUsernameAndNewPassword) {
		
		String usernameAndOldPasswordTokens[] =
			Utils.Base64Encode.decode(
				base64EncodedUsernameAndOldPassword
			).split(":");
			
		String usernameAndNewPasswordTokens[] =
			Utils.Base64Encode.decode(
				base64EncodedUsernameAndNewPassword
			).split(":");
			
		if(usernameAndOldPasswordTokens.length != 2 || usernameAndNewPasswordTokens.length != 2) {
			
			return UserManagerFeedback.WRONG_USERNAME_AND_PASSWORD_INPUT;
			
		}
		
		String
			usernameForOldPassword = usernameAndOldPasswordTokens[0],
			oldPassword = usernameAndOldPasswordTokens[1],
			usernameForNewPassword = usernameAndNewPasswordTokens[0],
			newPassword = usernameAndNewPasswordTokens[1];
			
		if(!usernameForOldPassword.equals(usernameForNewPassword)) {
			
			return UserManagerFeedback.WRONG_USERNAME_INPUT;
			
		}
		
		if(oldPassword.equals(newPassword)) {
			
			return UserManagerFeedback.OLD_PASSWORD_EQUALS_NEW_PASSWORD;
			
		}
		
		if(this.users.containsKey(usernameForOldPassword)) {
			
			User user =
				this.users.get(usernameForOldPassword);
				
			if(!oldPassword.equals(user.password())) {
				
				return UserManagerFeedback.WRONG_PASSWORD;
				
			}
			
			user.setPassword(newPassword);
			
			return UserManagerFeedback.PASSWORD_CHANGED;
			
		}
		
		return UserManagerFeedback.USER_DOES_NOT_EXIST;
		
	}
	
	public MapAction deleteUser(String username) throws Exception {
		
		User user =
			this.users.get(username);
		
		if(user != null) {
			
			if(user.undeletable()) {
				
				return MapAction.NOT_ALLOWED;
				
			}
			
			this.deleteUserSessions(username);
			this.deleteUserClientKeys(username);
			
			this.users.remove(username);
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteUserSession(String sessionKey) {
		
		UserSession userSession =
			this.userSessions.get(sessionKey);
			
		if(userSession != null) {
			
			this.userSessions.remove(sessionKey);
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public void deleteUserSessions(String username) throws Exception {
		
		for(UserSession userSession : this.userSessions.values()) {
			
			if(userSession.user().username().equals(username)) {
				
				this.userSessions.remove(userSession.sessionKey());
				
			}
			
		}
		
	}
	
	public MapAction deleteAllUserSessions() throws Exception {
		
		for(UserSession userSession : this.userSessions.values()) {
			
			this.userSessions.remove(userSession.sessionKey());
			
		}
		
		return MapAction.DELETE_ALL;
		
	}
	
	public MapAction deleteUserClientKeys(String username) {
		
		User user = this.users.get(username);
		
		if(user != null) {
			
			user.deleteAllClientKeys();
			
			return MapAction.DELETE_ALL;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction addClientKey(String clientKey, User user) {
		
		this.clientKeys.put(
			clientKey,
			user
		);
		
		return MapAction.ADD;
		
	}
	
	public MapAction addClientKey(String username) {
		
		if(!this.users.containsKey(username)) {
			
			return MapAction.NONE;
			
		}
		
		this.users.get(username).addClientKey();
		
		return MapAction.ADD;
		
	}
	
	public MapAction deleteClientKey(String username, String clientKey) {
		
		User user = this.users.get(username);
		
		if(user != null) {
			
			return user.deleteClientKey(clientKey);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public void deleteClientKey(String clientKey) {
		
		this.clientKeys.remove(clientKey);
		
	}
	
	public MapAction addExternalClientKey(String username, String externalClientKey) {
		
		User user = this.users.get(username);
		
		if(user != null) {
			
			return user.addExternalClientKey(externalClientKey);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction addUserClientKeys(JSONDataStructure userClientKeysJSON) {
		
		if(userClientKeysJSON.get("username") == null) {
			
			return MapAction.NONE;
			
		}
		
		int clientKeysAdded = 0;
		
		String username =
			userClientKeysJSON.get("username").getValue().toString();
			
		String[] clientKeys =
			JSONDataStructure.entriesToStringArray(
				((JSONDataStructure)userClientKeysJSON.get("clientKeys").getValue()).entries()
			);
			
		for(String clientKey : clientKeys) {
			
			if(this.addExternalClientKey(username, clientKey) == MapAction.ADD) {
				
				clientKeysAdded++;
				
			}
			
		}
		
		if(clientKeysAdded > 0) {
			
			return MapAction.ADD;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction addUserPrivileges(String username, String[] privilegeList) {
		
		if(this.users.containsKey(username)) {
			
			User user =
				this.users.get(username);
				
			return user.addPrivileges(privilegeList);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteUserPrivileges(String username, String[] privilegeList) {
		
		if(this.users.containsKey(username)) {
			
			User user =
				this.users.get(username);
				
			return user.deletePrivileges(privilegeList);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteAllUserPrivileges(String username) {
		
		if(this.users.containsKey(username)) {
			
			User user =
				this.users.get(username);
				
			return user.deleteAllPrivileges();
			
		}
		
		return MapAction.NONE;
		
	}
	
	private Role[] toRoleArray(String[] roleNameList) {
		
		Vector<Role> rolesVector =
			new Vector<Role>();
		
		for(String roleName : roleNameList) {
			
			Role role =
				this.roles.get(roleName);

			if(role != null) {
				
				rolesVector.add(role);

			}
			
		}
		
		return rolesVector.toArray(new Role[0]);
		
	}
	
	public MapAction addUserRoles(String username, String[] roleNameList) {
		
		if(this.users.containsKey(username)) {
			
			User user =
				this.users.get(username);
			
			return user.addRoles(
				this.toRoleArray(roleNameList)
			);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteUserRoles(String username, String[] roleNameList) {
		
		if(this.users.containsKey(username)) {
			
			User user =
				this.users.get(username);
				
			return user.deleteRoles(
				this.toRoleArray(roleNameList)
			);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteAllUserRoles(String username) {
		
		if(this.users.containsKey(username)) {
			
			User user =
				this.users.get(username);
				
			return user.deleteAllRoles();
			
		}
		
		return MapAction.NONE;
		
	}
	
	private String newSession(User user, String ipAddress, long sessionExpiryTimeInMs) {
		
		UserSession userSession =
			new UserSession(
				user,
				ipAddress,
				sessionExpiryTimeInMs
			);
		
		this.userSessions.put(
			userSession.sessionKey(),
			userSession
		);
		
		return userSession.sessionKey();
		
	}
	
	public String authenticateUser(String base64EncodedUsernameAndPassword, String ipAddress) {
		
		return this.authenticateUser(
			base64EncodedUsernameAndPassword,
			ipAddress,
			UserSession.DEFAULT_SESSION_EXPIRY_TIME
		);
		
	}
	
	public String[] getUsernameAndPasswordTokens(String base64EncodedUsernameAndPassword) {
		
		String decoded =
			Utils.Base64Encode.decode(
				base64EncodedUsernameAndPassword
			);
			
		if(decoded == null) {
			
			return null;
			
		}
		
		String usernameAndPasswordTokens[] = decoded.split(":");
		
		if(usernameAndPasswordTokens.length != 2) {
			
			return null;
			
		}
		
		String
			username = usernameAndPasswordTokens[0],
			password = usernameAndPasswordTokens[1];
		
		return new String[] {
			username,
			password
		};
		
	}
	
	public String authenticateUser(String base64EncodedUsernameAndPassword, String ipAddress, long sessionExpiryTimeInMs) {
		
		String usernameAndPasswordTokens[] =
			this.getUsernameAndPasswordTokens(
				base64EncodedUsernameAndPassword
			);
			
		if(usernameAndPasswordTokens == null) {
			
			return null;
			
		}
			
		String
			username = usernameAndPasswordTokens[0],
			password = usernameAndPasswordTokens[1];
		
		User user =
			this.users.get(username);
		
		if(user == null) {
			return null;
		}
		
		if(!user.password().equals(password)) {
			return null;
		}
		
		return this.newSession(
			user,
			ipAddress,
			sessionExpiryTimeInMs
		);
		
	}
	
	public String authenticateClientKey(String clientKey, String ipAddress) {
		
		return this.authenticateClientKey(
			clientKey,
			ipAddress,
			UserSession.DEFAULT_SESSION_EXPIRY_TIME
		);
		
	}
	
	public String authenticateClientKey(String clientKey, String ipAddress, long sessionExpiryTimeInMs) {
		
		User user =
			this.clientKeys.get(clientKey);
		
		if(user == null) {
			
			return null;
			
		}
		
		return this.newSession(
			user,
			ipAddress,
			sessionExpiryTimeInMs
		);
		
	}
	
	public boolean isValidSession(String sessionKey, String ipAddress) {
		
		if(!this.userSessions.containsKey(sessionKey)) {
			return false;
		}
		
		UserSession userSession =
			this.userSessions.get(sessionKey);
			
		if(!userSession.isValidOrigin(ipAddress)) {
			
			return false;
			
		}
		
		if(userSession.isExpired()) {
			
			return false;
			
		}
		
		return true;
		
	}
	
	public UserSession getUserSession(String sessionKey) {
		
		return this.userSessions.get(sessionKey);
		
	}
	
	public MapAction addRolePrivileges(String roleName, String[] privilegeList) {
		
		Role role =
			this.roles.get(roleName);
			
		if(role != null) {
			
			return role.addPrivileges(privilegeList);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteRolePrivileges(String roleName, String[] privilegeList) {
		
		Role role =
			this.roles.get(roleName);
			
		if(role != null) {
			
			return role.deletePrivileges(privilegeList);
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteAllRolePrivileges(String roleName) {
		
		Role role =
			this.roles.get(roleName);
			
		if(role != null) {
			
			return role.deleteAllPrivileges();
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction addRole(String name, RoleType roleType, String[] privilegeList) {
		
		if(this.roles.containsKey(name)) {
			
			return MapAction.ALREADY_EXISTS;
			
		}
		
		Role role =
			Role.factory(
				name,
				roleType,
				false,
				false
			);
			
		if(role != null) {
			
			role.addPrivileges(privilegeList);
			
			this.roles.put(role.name(), role);
			
			return MapAction.ADD;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction addRole(JSONDataStructure roleJSON) throws Exception {
		
		if(roleJSON.get("name") == null) {
			
			return MapAction.NONE;
			
		}
		
		String name =
			roleJSON.get("name").getValue().toString();
		
		if(this.roles.containsKey(name)) {
			
			return MapAction.ALREADY_EXISTS;
			
		}
		
		Role role =
			Role.factory(
				name,
				RoleType.valueOf(
					roleJSON.get("roleType").getValue().toString()
				),
				Boolean.parseBoolean(
					roleJSON.get("undeleteable").getValue().toString()
				),
				Boolean.parseBoolean(
					roleJSON.get("unchangeable").getValue().toString()
				)
			);
			
		if(role != null) {
			
			role.addPrivileges(
				JSONDataStructure.entriesToStringArray(
					((JSONDataStructure)roleJSON.get("privileges").getValue()).entries()
				)
			);
			
			this.roles.put(role.name(), role);
			
			return MapAction.ADD;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteRole(String name) {
		
		if(this.roles.containsKey(name)) {
			
			Role role =
				this.roles.get(name);
				
			if(role.undeleteable()) {
				
				return MapAction.NOT_ALLOWED;
				
			}
			
			for(User user : this.users.values()) {
				
				user.deleteRoles(new Role[]{role});
				
			}
			
			this.roles.remove(name);
			
			return MapAction.DELETE;
			
		}
		
		return MapAction.NONE;
		
	}
	
	public MapAction deleteAllRoles() {
		
		Entry roleEntry = null;
		Role role = null;
		
		Iterator roleIterator =
			this.roles.entrySet().iterator();
			
		while(roleIterator.hasNext()) {
			
			roleEntry =
				(Entry)roleIterator.next();
			role =
				(Role)roleEntry.getValue();
			
			if(role.undeleteable()) {
				
				continue;
				
			}
			
			for(User user : this.users.values()) {
				
				user.deleteRoles(new Role[]{role});
				
			}
			
			roleIterator.remove();
			
		}
		
		return MapAction.DELETE_ALL;
		
	}
	
	public void removeExpiredSessions() throws Exception {
		
		Entry userSessionsEntry = null;
		UserSession userSession = null;
		
		Iterator userSessionsIterator =
			this.userSessions.entrySet().iterator();
			
		while(userSessionsIterator.hasNext()) {
			
			userSessionsEntry =
				(Entry)userSessionsIterator.next();
			userSession =
				(UserSession)userSessionsEntry.getValue();
				
			if(userSession.isExpired()) {
				
				// Avoids java.util.ConcurrentModificationException
				userSessionsIterator.remove();
				
			}
			
		}
		
	}
	
	public JSONBuilder getJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(User user : this.users.values()) {
			jb.v(user.getJSONBuilder());
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getJSONBuilderForExport() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(User user : this.users.values()) {
			
			if(user.username().equals(UserManager.ADMIN_USERNAME)) {
				
				continue;
				
			}
			
			jb.v(user.getJSONBuilderForExport());
		}
		
		return jb.$(']');
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	public JSONBuilder getUserSessionsJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(UserSession userSession : this.userSessions.values()) {
			jb.v(userSession.getJSONBuilder());
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getUserRolesAndPrivilegesJSONBuilder(String username) {
		
		if(this.users.containsKey(username)) {
			
			return this.users.get(username).getRolesAndPrivilegesJSONBuilder();
			
		}
		
		return null;
		
	}
	
	public JSONBuilder getRolesJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(Role role : this.roles.values()) {
			
			jb.v(role.getJSONBuilder());
			
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getRolesJSONBuilderForExport() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(Role role : this.roles.values()) {
			
			if(role.name().equals(UserManager.ADMIN_ROLENAME)) {
				
				continue;
				
			}
			
			jb.v(role.getJSONBuilder());
			
		}
		
		return jb.$(']');
		
	}
	
	public JSONBuilder getUserClientKeys(String userName) {
		
		if(this.users.containsKey(userName)) {
			
			return this.users.get(userName).getClientKeysJSONBuilder();
			
		}
		
		return null;
		
	}
	
	public JSONBuilder getClientKeysJSONBuilderForExport() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('[');
		
		for(User user : this.users.values()) {
			
			if(user.clientKeyCount() > 0) {
				
				jb.v(
					JSONBuilder.f().$('{')
						.k("username").v(user.username())
						.k("clientKeys").v(
							user.getClientKeysJSONBuilder()
						)
					.$('}')
				);
				
			}
			
		}
		
		return jb.$(']');
		
	}
	
}