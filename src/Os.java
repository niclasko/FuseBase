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
public class Os {
	public static boolean isWindows;
	public static boolean isMac;
	public static boolean isUnix;
	public static boolean isSolaris;
	
	static {
		String OS = System.getProperty("os.name").toLowerCase();
		
		Os.isWindows = (OS.indexOf("win") >= 0);
		Os.isMac = (OS.indexOf("mac") >= 0);
		Os.isUnix = (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
		Os.isSolaris = (OS.indexOf("sunos") >= 0);
	}
}