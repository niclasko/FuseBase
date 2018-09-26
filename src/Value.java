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
public class Value<T> {
	protected T value;
	
	public Value(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return this.value;
	}
	
	public static void main(String args[]) {
		/*int[] values = new int[20];
		Value v1 = new ValueInt(10);
		Value v2 = new ValueDouble(0.7);
		
		values[(int)v1.getValue()] = 12;
		
		System.out.println((int)v1.getValue());
		System.out.println((double)v2.getValue());
		
		System.out.println(values[(int)v1.getValue()]);*/
	}
}