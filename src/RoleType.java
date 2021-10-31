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
public enum RoleType {
	
	ALL_PRIVILEGES_EXCEPT,
	NO_PRIVILEGES_EXCEPT;
	
	public static final String REST_API_LIST =
		Utils.listOfEnumValuesForReSTAPI(RoleType.class);
		
	public static final JSONBuilder jb = JSONBuilder.f();
	
	static {
		
		RoleType.jb.$('[');
		for(RoleType roleType : RoleType.values()) {
			RoleType.jb.v(roleType);
		}
		RoleType.jb.$(']');
		
	}
	
	public static JSONBuilder getJSONBuilder() {
		return RoleType.jb;
	}
	
}