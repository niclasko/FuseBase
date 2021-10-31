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
import java.util.HashMap;

public enum SQLKeyWord {
	
	SELECT("SELECT", SQLKeyWordType.CLAUSE),
	TOP("TOP", SQLKeyWordType.MODIFIER),
	DISTINCT("DISTINCT", SQLKeyWordType.MODIFIER),
	STAR("STAR", SQLKeyWordType.STAR),
	FROM("FROM", SQLKeyWordType.CLAUSE),
	JOIN("JOIN", SQLKeyWordType.JOIN),
	INNER_JOIN("INNER JOIN", SQLKeyWordType.JOIN),
	LEFT_JOIN("LEFT JOIN", SQLKeyWordType.JOIN),
	RIGHT_JOIN("RIGHT JOIN", SQLKeyWordType.JOIN),
	FULL_OUTER_JOIN("FULL OUTER JOIN", SQLKeyWordType.JOIN),
	UNION("UNION", SQLKeyWordType.SET_OPERATION),
	UNION_ALL("UNION ALL", SQLKeyWordType.SET_OPERATION),
	INTERSECT("INTERSECT", SQLKeyWordType.SET_OPERATION),
	Q("Q", SQLKeyWordType.CLAUSE),
	NQ("NQ", SQLKeyWordType.CLAUSE),
	CSV("CSV", SQLKeyWordType.CLAUSE),
	XL("XL", SQLKeyWordType.CLAUSE),
	JSON("JSON", SQLKeyWordType.CLAUSE),
	GEOCODE("GEOCODE", SQLKeyWordType.FUNCTION),
	FUNCTION_NAME("FUNCTION_NAME", SQLKeyWordType.FUNCTION),
	SET_FUNCTION_NAME("SET_FUNCTION_NAME", SQLKeyWordType.FUNCTION),
	ON("ON", SQLKeyWordType.JOIN_FILTER),
	WITH("WITH", SQLKeyWordType.CLAUSE),
	WHERE("WHERE", SQLKeyWordType.CLAUSE),
	GROUP_BY("GROUP BY", SQLKeyWordType.CLAUSE),
	HAVING("HAVING", SQLKeyWordType.CLAUSE),
	ORDER_BY("ORDER BY", SQLKeyWordType.CLAUSE),
	BEGIN_PARANTHESES("(", SQLKeyWordType.PARANTHESES),
	END_PARANTHESES(")", SQLKeyWordType.PARANTHESES),
	SUB("SUB", SQLKeyWordType.SUB),
	AS("AS", SQLKeyWordType.ALIAS_PREFIX),
	LITERAL("LITERAL", SQLKeyWordType.ATOM),
	SINGLE_QUOTED_LITERAL("SINGLE_QUOTED_LITERAL", SQLKeyWordType.ATOM),
	DOUBLE_QUOTED_LITERAL("SINGLE_QUOTED_LITERAL", SQLKeyWordType.ATOM),
	NUMBER_ATOM("NUMBER_ATOM", SQLKeyWordType.ATOM),
	JSON_DATA_STRUCTURE("JSON_DATA_STRUCTURE", SQLKeyWordType.ATOM),
	COMMA(",", SQLKeyWordType.LIST_DELIMITER),
	PLUS("+", SQLKeyWordType.BINARY_OPERATOR),
	CONCATENATE("||", SQLKeyWordType.BINARY_OPERATOR),
	MINUS("-", SQLKeyWordType.BINARY_OPERATOR),
	MULTIPLY("*", SQLKeyWordType.BINARY_OPERATOR),
	DIVIDE("/", SQLKeyWordType.BINARY_OPERATOR),
	POWER("^", SQLKeyWordType.BINARY_OPERATOR),
	GREATER_THAN(">", SQLKeyWordType.BINARY_OPERATOR),
	LESS_THAN("<", SQLKeyWordType.BINARY_OPERATOR),
	GREATER_THAN_OR_EQUALS(">=", SQLKeyWordType.BINARY_OPERATOR),
	LESS_THAN_OR_EQUALS("<=", SQLKeyWordType.BINARY_OPERATOR),
	EQUALS("=", SQLKeyWordType.BINARY_OPERATOR),
	NOT_EQUALS("!=", SQLKeyWordType.BINARY_OPERATOR),
	JSON_MEMBER("->", SQLKeyWordType.JSON_OPERATOR),
	LIKE("LIKE", SQLKeyWordType.BINARY_OPERATOR),
	NOT_LIKE("NOT LIKE", SQLKeyWordType.BINARY_OPERATOR),
	IS("IS", SQLKeyWordType.BINARY_OPERATOR),
	AND("AND", SQLKeyWordType.BINARY_OPERATOR),
	OR("OR", SQLKeyWordType.BINARY_OPERATOR),
	IN("IN", SQLKeyWordType.LIST_OPERATOR),
	NOT_IN("NOT IN", SQLKeyWordType.LIST_OPERATOR),
	NOT("NOT", SQLKeyWordType.NEGATOR),
	NULL("NULL", SQLKeyWordType.NULL),
	NOT_NULL("NOT NULL", SQLKeyWordType.NULL),
	COUNT("COUNT", SQLKeyWordType.AGGREGATE_OPERATOR),
	SUM("SUM", SQLKeyWordType.AGGREGATE_OPERATOR),
	MIN("MIN", SQLKeyWordType.AGGREGATE_OPERATOR),
	MAX("MAX", SQLKeyWordType.AGGREGATE_OPERATOR),
	AVG("AVG", SQLKeyWordType.AGGREGATE_OPERATOR),
	STDDEV("STDDEV", SQLKeyWordType.AGGREGATE_OPERATOR),
	NTILE("NTILE", SQLKeyWordType.AGGREGATE_OPERATOR),
	LAG("LAG", SQLKeyWordType.AGGREGATE_OPERATOR),
	LEAD("LEAD", SQLKeyWordType.AGGREGATE_OPERATOR),
	OVER("OVER", SQLKeyWordType.WINDOW_SPECIFIER),
	PARTITION_BY("PARTITION BY", SQLKeyWordType.WINDOW_SPECIFIER),
	ASC("ASC", SQLKeyWordType.SORT_ORDER),
	DESC("DESC", SQLKeyWordType.SORT_ORDER),
	CASE("CASE", SQLKeyWordType.LOGIC),
	WHEN("WHEN", SQLKeyWordType.LOGIC),
	THEN("THEN", SQLKeyWordType.LOGIC),
	ELSE("ELSE", SQLKeyWordType.LOGIC),
	END("END", SQLKeyWordType.LOGIC),
	CAST("CAST", SQLKeyWordType.FUNCTION),
	NUMBER("NUMBER", SQLKeyWordType.DATATYPE),
	TRIM("TRIM", SQLKeyWordType.FUNCTION),
	COMMENT("COMMENT", SQLKeyWordType.COMMENT),
	UNKNOWN("UNKNOWN", SQLKeyWordType.UNKNOWN),
	END_OF_FILE("END OF FILE", SQLKeyWordType.END_OF_FILE);
	
	private static final HashMap<String, SQLKeyWord> displayNameMap;
	
	static {
		
		displayNameMap = new HashMap<String, SQLKeyWord>();
		
		for (SQLKeyWord kw : SQLKeyWord.values()) {
			displayNameMap.put(kw.displayName, kw);
		}
	}
	
	public static SQLKeyWord findByDisplayName(String displayName) {
		
		SQLKeyWord sqlKeyWord =
			displayNameMap.get(displayName);
			
		if(sqlKeyWord == null) {
			sqlKeyWord = SQLKeyWord.UNKNOWN;
		}
		
		return sqlKeyWord;
	}
	
	private String displayName;
	private SQLKeyWordType sqlKeyWordType;

	SQLKeyWord(String displayName, SQLKeyWordType sqlKeyWordType) {
		this.displayName = displayName;
		this.sqlKeyWordType = sqlKeyWordType;
	}

	public String displayName() {
		return displayName;
	}
	
	public SQLKeyWordType sqlKeyWordType() {
		return this.sqlKeyWordType;
	}
	
}