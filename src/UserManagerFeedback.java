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
public enum UserManagerFeedback {
	USER_ADDED("Successfully added user."),
	USER_DELETED("Successfully deleted user."),
	NOT_ALLOWED("Not allowed."),
	WRONG_USERNAME_AND_PASSWORD_INPUT("Neither username or password can contain the character ':'. Please check your input."),
	WRONG_USERNAME_INPUT("Username for old and new password must match."),
	OLD_PASSWORD_EQUALS_NEW_PASSWORD("Old password cannot equal new password."),
	WRONG_PASSWORD("Wrong password."),
	PASSWORD_CHANGED("Successfully changed password."),
	USER_DOES_NOT_EXIST("User does not exist.");
	
	private String feedbackText;

	UserManagerFeedback(String feedbackText) {
		this.feedbackText = feedbackText;
	}
	
	public String toString() {
		return this.feedbackText;
	}
}