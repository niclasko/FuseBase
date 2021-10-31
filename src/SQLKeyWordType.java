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
public enum SQLKeyWordType {
	CLAUSE,
	MODIFIER,
	STAR,
	JOIN,
	JOIN_FILTER,
	SET_OPERATION,
	PARANTHESES,
	SUB,
	ALIAS_PREFIX,
	ATOM,
	LIST_DELIMITER,
	BINARY_OPERATOR,
	LIST_OPERATOR,
	NEGATOR,
	JSON_OPERATOR,
	NULL,
	AGGREGATE_OPERATOR,
	WINDOW_SPECIFIER,
	SORT_ORDER,
	LOGIC,
	FUNCTION,
	DATATYPE,
	COMMENT,
	UNKNOWN,
	END_OF_FILE;
}