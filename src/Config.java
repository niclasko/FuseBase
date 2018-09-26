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
public class Config {
	public static final int HTTP_PORT = 4444;
	public static final String FASTBASE_SERIALIZATION_FILENAME = "./db/fastbase.ser";
	public static final String LOG_DIRECTORY = "./logs/";
	public static final String APPS_DIRECTORY = "apps";
	public static final String FASTBASE_WEB_GUI_DIRECTORY = "/ui/";
	public static final String FASTBASE_WEB_DEFAULT_REDIRECT_TARGET = "/ui/";
	public static final String FASTBASE_WEB_LOGIN_PATH = "/ui/login/";
	public static final String CLOSEFILE = "__CLOSEFILE__";
	public static final String FASTBASE_SESSION_KEY_COOKIE_KEYNAME = "fastbase_sk";
	public static final String FASTBASE_SESSION_KEY_PARAMETER_KEYNAME = "fastbase_sk";
	public static final String FASTBASE_FEEDBACK_COOKIE_KEYNAME = "fastbase_feedback";
	public static final int DB_CONNECTION_POOL_SIZE = 5;
	public static final int HTTP_SERVER_THREAD_COUNT = 20;
}