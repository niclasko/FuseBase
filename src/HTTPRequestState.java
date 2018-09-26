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
public enum HTTPRequestState {
	SERVER_AWAITING_REQUEST(0),
	SERVER_AWAITING_NEXT_HEADER(1),
	CLIENT_AWAITING_NEW_LINE(2),
	SERVER_AWAITING_DATA(3),
	CLIENT_AWAITING_DATA(4),
	DONE(5),
	VOID(6),
	UNKNOWN(7);
	
	public static final int SIZE = HTTPRequestState.values().length;
	
	private int id;

	HTTPRequestState(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
}