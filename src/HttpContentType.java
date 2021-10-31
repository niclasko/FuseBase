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
public enum HttpContentType {
	
	html("text/html; charset=UTF-8"),
	js("text/javascript; charset=UTF-8"),
	json("application/json; charset=UTF-8"),
	css("text/css; charset=UTF-8"),
	txt("text/plain; charset=UTF-8"),
	png("image/png"),
	jpg("image/jpeg"),
	jpeg("image/jpeg"),
	gif("image/gif"),
	svg("image/svg+xml"),
	other("application/octet-stream");
	
	public static final int SIZE = HttpContentType.values().length;

	private String contentType;

	HttpContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String toString() {
		return this.contentType;
	}
	
	public static void main(String args[]) {
		System.out.println(HttpContentType.other);
	}
	
}