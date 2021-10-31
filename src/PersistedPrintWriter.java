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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;

public class PersistedPrintWriter extends PrintWriter  {
	
	public PersistedPrintWriter() {
		
		super(new StringWriter());
		
	}
	
	public void print(String text) {
		try {
			out.write(text);
		} catch (Exception e) {
			;
		}
	}
	
	public String getData() {
		return out.toString();
	}
	
	public static void main(String args[]) {
		PersistedPrintWriter ppw;
		
		try {
			ppw = new PersistedPrintWriter();
			ppw.print("Hello");
			System.out.println(ppw.getData());
		} catch(Exception e) {
			;
		}
	}
	
}