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
import java.io.Serializable;

public class Role implements Serializable {
	
	private String name;
	protected RoleType roleType;
	private boolean undeleteable;
	private boolean unchangeable;
	protected HashSet<String> privileges;
	
	public Role(String name, RoleType roleType, boolean undeleteable, boolean unchangeable) {
		
		this.name = name;
		this.roleType = roleType;
		this.undeleteable = undeleteable;
		this.unchangeable = unchangeable;
		this.privileges = new HashSet<String>();
		
	}
	
	public static Role factory(String name, RoleType roleType, boolean undeleteable, boolean unchangeable) {
		
		if(roleType == RoleType.ALL_PRIVILEGES_EXCEPT) {
			
			return new RoleInverseRestricted(name, undeleteable, unchangeable);
			
		} else if(roleType == RoleType.NO_PRIVILEGES_EXCEPT) {
			
			return new RoleRestricted(name, undeleteable, unchangeable);
			
		}
		
		return null;
		
	}
	
	public String name() {
		
		return this.name;
		
	}
	
	public RoleType roleType() {
		
		return this.roleType;
		
	}
	
	public boolean undeleteable() {
		
		return this.undeleteable;
		
	}
	
	public boolean unchangeable() {
		
		return this.unchangeable;
		
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
	
	public boolean isAuthorized(String priviligeKey) {
		return true;
	}
	
	public JSONBuilder getJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('{');
		
		jb.k("name").v(this.name);
		jb.k("roleType").v(this.roleType);
		jb.k("undeleteable").v(this.undeleteable);
		jb.k("unchangeable").v(this.unchangeable);
		jb.k("privileges").$('[');
		
		for(String privilegeKey : this.privileges) {
			
			jb.v(privilegeKey);
			
		}
		
		jb.$(']');
		
		jb.$('}');
		
		return jb;
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
}