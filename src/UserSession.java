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
import java.security.SecureRandom;
import java.math.BigInteger;
import java.io.Serializable;

public class UserSession implements Serializable {
	
	private User user;
	private String sessionKey;
	private String ipAddress;
	private long startTime;
	private long endTime;
	
	public UserSession(User user, String ipAddress, long expiryTimeInMs) {
		
		this.user = user;
		
		this.sessionKey =
			UserSession.newUserSessionKey();
			
		this.ipAddress = ipAddress;
			
		this.startTime =
			Utils.timeInMs();
			
		if(expiryTimeInMs >= 0) {
			
			this.endTime =
				this.startTime + expiryTimeInMs;
			
		} else if(expiryTimeInMs == -1)  {
			
			this.endTime = -1;
			
		} else {
			
			this.endTime = -2;
			
		}
		
	}
	
	public User user() {
		
		return this.user;
		
	}
	
	public String sessionKey() {
		
		return this.sessionKey;
		
	}
	
	public boolean isValidOrigin(String ipAddress) {
		
		return this.ipAddress.equals(ipAddress);
		
	}
	
	public boolean isExpired() {
		
		if(this.endTime >= 0) {
			
			return Utils.timeInMs() > this.endTime;
			
		} else if(this.endTime == -1) {
			
			return false;
			
		}
		
		return true;
		
	}
	
	public JSONBuilder getJSONBuilder() {
		
		JSONBuilder jb = JSONBuilder.f();
		
		jb.$('{');
		
		jb.k("username").v(this.user.username());
		jb.k("sessionKey").v(this.sessionKey);
		jb.k("ipAddress").v(this.ipAddress);
		jb.k("startTime").v(Utils.convertTimeInMsToDate(this.startTime));
		jb.k("endTime").v(Utils.convertTimeInMsToDate(this.endTime));
		
		return jb.$('}');
		
	}
	
	public String toJSON() {
		return this.getJSONBuilder().getJSON();
	}
	
	/*
	** Static fields and methods below
	*/
	private static SecureRandom secureRandom;
	public static final long DEFAULT_SESSION_EXPIRY_TIME = 60*60*1000;
	
	static {
		
		UserSession.secureRandom =
			new SecureRandom();
		
	}
	
	public static String newUserSessionKey() {
		
		return new BigInteger(130, UserSession.secureRandom).toString(32);
		
	}
	
	public static void main(String args[]) {
		System.out.println(UserSession.newUserSessionKey());
	}
	
}