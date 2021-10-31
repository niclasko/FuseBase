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
public class RoleInverseRestricted extends Role {
	
	public RoleInverseRestricted(String name, boolean undeleteable, boolean unchangeable) {
		super(name, RoleType.ALL_PRIVILEGES_EXCEPT, undeleteable, unchangeable);
	}
	
	public boolean isAuthorized(String privilegeKey) {
		
		if(this.privileges.contains(privilegeKey)) {
			
			return false;
			
		}
		
		return true;
		
	}
	
}