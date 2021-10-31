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
import java.lang.reflect.Method;
import java.util.Stack;
import java.util.Vector;

public class SQLParser {
	
	private Stack<SQLParseTreeNode> parseTreeRootNodes;
	private Stack<SQLParseTreeNode> openingParentheses;
	private Stack<SQLParseTreeNode> functions;
	private Stack<ExpressionTreeBuilder> expressionTreeBuilders;
	
	private SQLParseTreeNode currentParseTreeRootNode;
	private SQLParseTreeNode currentParseTreeNode;
	
	private SQLToken[] tokens;
	private int currentTokenIndex;
	private SQLToken currentToken;
	
	public SQLParser(String sql) throws Exception {
		
		SQLTokenizer sqlTokenizer =
			new SQLTokenizer(
				sql
			);
		
		this.tokens =
			sqlTokenizer.tokens();
			
		this.parseTreeRootNodes =
			new Stack<SQLParseTreeNode>();
			
		this.openingParentheses =
			new Stack<SQLParseTreeNode>();
			
		this.functions =
			new Stack<SQLParseTreeNode>();
		
		this.expressionTreeBuilders =
			new Stack<ExpressionTreeBuilder>();
			
		this.currentParseTreeRootNode =
			new SQLParseTreeNode(SQLToken.createRootToken(), null);
		
		this.currentTokenIndex = -1;
		this.currentToken = null;
		
		this.BEGIN_SQL_STATEMENT();
		this.END_SQL_STATEMENT();
		
	}
	
	private SQLToken currentToken() {
		return this.currentToken;
	}
	
	/*private SQLToken previousToken() {
		
		if(this.currentToken == null) {
			return null;
		}
		
		return this.currentToken.previousToken();
	}*/
	
	private SQLToken nextToken() {
		
		if(this.currentToken == null) {
			
			if(this.tokens != null && this.tokens.length > 0) {
				return this.tokens[0];
			}
			
			return null;
		}
		
		return this.currentToken.nextToken();
	}
	
	private SQLParseTreeNode currentParseTreeNode() {
		return this.currentParseTreeNode;
	}
	
	private void consume() throws Exception {
			
		this.consume(null);

	}
	
	private void consume(String methodName) throws Exception {
		
		String _methodName = methodName;
		
		if(this.currentTokenIndex == this.tokens.length-1) {
			return;
		}
		
		this.currentToken =
			this.tokens[++this.currentTokenIndex];
		
		if(_methodName == null) {
			_methodName = this.currentToken.sqlKeyWord().toString();
		}
		
		Method method = null;
		
		method =
			this.getClass().getMethod(_methodName);
			
		method.invoke(
			this,
			new Object[]{}
		);

	}
	
	private String getCurrentMethodName() {
		
		String methodName;
		
		StackTraceElement stackTraceElements[] =
			(new Throwable()).getStackTrace();
		
		methodName =
			stackTraceElements[1].toString();
			
		methodName =
			methodName.substring(
				methodName.indexOf(".") + 1,
				methodName.indexOf("(")
			);
		
		return methodName;
	}
	
	private void setCurrentParseTreeRootNode(SQLParseTreeNode sqlParseTreeNode) {
		
		this.parseTreeRootNodes.push(
			currentParseTreeRootNode
		);
		
		this.currentParseTreeRootNode = sqlParseTreeNode;
		
	}
	
	private void setPreviousParseTreeRootNode() {
		
		this.currentParseTreeRootNode =
			this.parseTreeRootNodes.pop();
		
	}
	
	private void beginFunction() {
		
		this.addChildNode();
		
		this.functions.push(
			this.currentParseTreeNode()
		);
		
	}
	
	private void endFunction() {
		
		if(this.functions.size() > 0) {
			
			this.currentParseTreeNode =
				this.functions.pop().getParent();
			
		}
		
	}
	
	private void beginSub() {
		
		this.beginFunction();
		
	}
	
	private void endSub() {
		
		this.endFunction();
		
	}
	
	private void setOpeningParentheses() {
		
		if(this.currentParseTreeNode().sqlKeyWord() == SQLKeyWord.BEGIN_PARANTHESES) {
			
			this.openingParentheses.push(
				this.currentParseTreeNode()
			);
			
		}
		
	}
	
	private void setClosingParentheses() {
		
		if(this.openingParentheses.size() > 0) {
			
			this.currentParseTreeNode =
				this.openingParentheses.pop().getParent();
			
		}
		
	}
	
	private void resetCurrentNode() {
		this.currentParseTreeNode =
			this.currentParseTreeRootNode;
	}
	
	private SQLParseTreeNode createSQLParseTreeNode() {
		
		return new SQLParseTreeNode(
				this.currentToken,
				this.currentParseTreeNode
			);
		
	}
	
	private void addChildNode() {
		
		SQLParseTreeNode childNode =
			this.createSQLParseTreeNode();
		
		if(this.currentParseTreeNode().addNewChildrentToExpression()) {
			
			this.expressionTreeBuilders.peek().addToken(
				childNode
			);
			
		}
		
		this.currentParseTreeNode = 
			this.currentParseTreeNode.addChild(
				childNode
			);
		
	}
	
	private void addSibling() {
		
		this.addSibling(
			this.createSQLParseTreeNode()
		);
		
	}
	
	private void addSibling(SQLParseTreeNode sibling) {
		
		this.currentParseTreeNode.addChild(
			sibling
		);
		
		if(this.currentParseTreeNode().addNewChildrentToExpression()) {
			
			this.expressionTreeBuilders.peek().addToken(
				sibling
			);
			
		}
		
	}
	
	private void addNumber() {
		
		this.addSibling(
			new SQLParseTreeNodeNumber(
				this.currentToken,
				this.currentParseTreeNode
			)
		);
		
	}
	
	private void addLiteral() {
		
		this.addSibling(
			new SQLParseTreeNodeLiteral(
				this.currentToken,
				this.currentParseTreeNode
			)
		);
		
	}
	
	/*
	** Process SQL Key Words below
	*/
	
	public void BEGIN_SQL_STATEMENT() throws Exception {
		
		if(this.with()) {
			
			this.consume();
			
			if(!this.parseWithList()) {
				
				this.error("Expected aliased parentheses-enclosed sub-query, alias as (sub-query).");
				
			}
			
		}
		
		if(this.select()) {

			this.currentParseTreeNode =
				this.currentParseTreeRootNode;
			
			this.consume();
			
			if(this.top()) {
				
				this.consume();
				
				if(this.number()) {
					
					this.consume();
					
				} else {
					
					this.error("Expected " + SQLKeyWord.NUMBER);
					
				}
				
			}
			
			if(this.distinct()) {
				
				this.consume();
				
			}
			
			if(this.multiply()) {
				
				this.consume();
				
			} else if(!this.multiply()) {
				
				this.parseSelectElement();
				
			}
			
			if(this.from()) {
				
				this.consume();
				
				this.parseFromElement();
				
				if(this.where()) {
					
					this.consume();
					
					this.parseWhereClause();
					
				}
				
				if(this.groupBy()) {

					this.consume();

					this.parseField();

				}

				if(this.orderBy()) {

					this.consume();

					this.parseOrderByField();

				}
				
			}
			
			if(this.setOperation()) {

				this.consume();

				this.BEGIN_SQL_STATEMENT();

			}
			
		} else {
			
			this.error("Expected " + SQLKeyWord.SELECT);
			
		}
		
	}
	
	public void SELECT() { this.resetCurrentNode(); this.addChildNode(); }
	
	public void TOP() { this.addSibling(); }
	
	public void DISTINCT() { this.addSibling(); }
	
	public void STAR() { this.addLiteral(); }
	
	public void FROM() { this.resetCurrentNode(); this.addChildNode(); }
	public void JOIN() {
		if(this.currentToken().previousToken().sqlKeyWord() == SQLKeyWord.JSON_MEMBER) {
			this.beginFunction();
			return;
		}
		this.addSibling();
	}
	public void INNER_JOIN() { this.addSibling(); }
	public void LEFT_JOIN() { this.addSibling(); }
	public void RIGHT_JOIN() { this.addSibling(); }
	public void FULL_OTER_JOIN() { this.addSibling(); }
	public void ON() { this.addSibling(); }
	public void WITH() { this.resetCurrentNode(); this.addChildNode(); }
	public void WHERE() { this.resetCurrentNode(); this.addChildNode(); }
	public void GROUP_BY() { this.resetCurrentNode(); this.addChildNode(); }
	public void HAVING() { this.resetCurrentNode(); this.addChildNode(); }
	public void ORDER_BY() { this.resetCurrentNode(); this.addChildNode(); }
	
	public void UNION() { this.resetCurrentNode(); this.addLiteral(); }
	public void UNION_ALL() { this.resetCurrentNode(); this.addLiteral(); }
	public void INTERSECT() { this.resetCurrentNode(); this.addLiteral(); }
	
	public void BEGIN_PARANTHESES() {
		/*this.addChildNode();
		this.setOpeningParentheses();*/
		this.addSibling();
	}
	
	public void END_PARANTHESES() {
		//this.setClosingParentheses();
		this.addSibling();
	}
	
	public void BEGIN_FUNCTION_PARANTHESES() {
		this.addChildNode();
		this.setOpeningParentheses();
	}
	
	public void END_FUNCTION_PARANTHESES() {
		this.setClosingParentheses();
		this.addSibling();
	}
	
	public void BEGIN_SUBSET_PARANTHESES() {
		this.addChildNode();
		this.setOpeningParentheses();
	}
	
	public void END_SUBSET_PARANTHESES() {
		this.setClosingParentheses();
		this.addSibling();
	}
	
	public void AS() { this.addSibling(); }
	public void LITERAL() { this.addLiteral(); }
	public void SINGLE_QUOTED_LITERAL() { this.addLiteral(); }
	public void DOUBLE_QUOTED_LITERAL() { this.addLiteral(); }
	public void NUMBER_ATOM() { this.addNumber(); }
	public void JSON_DATA_STRUCTURE() { this.addLiteral();}
	public void COMMA() { this.addSibling(); }
	
	public void JSON_MEMBER() { this.addSibling(); }
	
	// Special function to allow for cross-system
	// sub-queries
	public void Q() { this.beginFunction(); }
	// Special function to allow for named queries
	public void NQ() { this.beginFunction(); }
	// Special function to allow for querying CSV
	// files from file system or web URL
	public void CSV() { this.beginFunction(); }
	// Special function to allow for querying Excel
	// files from file system or web URL
	public void XL() { this.beginFunction(); }
	// Special function to allow for querying JSON
	// files from file system or web URL
	public void JSON() { this.beginFunction(); }
	
	public void GEOCODE() { this.beginFunction(); }
	
	public void CAST() { this.beginFunction(); }
	
	public void NUMBER() { this.addLiteral(); }
	
	public void TRIM() { this.beginFunction(); }
	
	public void SET_FUNCTION_NAME() {
		this.currentToken.overrideSQLKeyWord(SQLKeyWord.SET_FUNCTION_NAME);
		this.currentToken.makeUpperCase();
		this.beginFunction(); 
	}
	
	public void PLUS() { this.addSibling(); }
	public void CONCATENATE() { this.addSibling(); }
	public void MINUS() { this.addSibling(); }
	public void MULTIPLY() { this.addSibling(); }
	public void DIVIDE() { this.addSibling(); }
	public void POWER() { this.addSibling(); }
	public void GREATER_THAN() { this.addSibling(); }
	public void LESS_THAN() { this.addSibling(); }
	public void GREATER_THAN_OR_EQUALS() { this.addSibling(); }
	public void LESS_THAN_OR_EQUALS() { this.addSibling(); }
	public void EQUALS() { this.addSibling(); }
	public void NOT_EQUALS() { this.addSibling(); }
	public void LIKE() { this.addSibling(); }
	public void NOT_LIKE() { this.addSibling(); }
	public void IS() { this.addSibling(); }
	public void IN() { this.addSibling(); }
	public void NOT_IN() { this.addSibling(); }
	public void NOT() { this.addSibling(); }
	public void NULL() { this.addSibling(); }
	public void NOT_NULL() { this.addSibling(); }
	
	public void COUNT() { this.beginFunction(); }
	public void SUM() { this.beginFunction(); }
	public void MIN() { this.beginFunction(); }
	public void MAX() { this.beginFunction(); }
	public void AVG() { this.beginFunction(); }
	public void STDDEV() { this.beginFunction(); }
	public void NTILE() { this.beginFunction(); }
	public void LAG() { this.beginFunction(); }
	public void LEAD() { this.beginFunction(); }
	public void OVER() { this.beginFunction(); }
	public void PARTITION_BY() { this.addChildNode(); }
	
	public void ASC() { this.addLiteral(); }
	public void DESC() { this.addLiteral(); }
	
	public void CASE() { this.addSibling(); }
	public void WHEN() { this.beginSub(); }
	public void THEN() { this.endSub(); this.beginSub(); }
	public void ELSE() { this.endSub(); this.beginSub(); }
	public void END() { this.endSub(); this.addSibling(); }
	
	public void AND() { this.addSibling(); }
	public void OR() { this.addSibling(); }
	
	public void COMMENT() { this.addSibling(); }
	
	public void END_SQL_STATEMENT() {  }
	
	/*
	** Parser helper methods
	*/
		
	private boolean parseWithList() throws Exception {
		
		boolean found = false;
		
		if(this.literal()) {
			
			this.consume();
			
			if(this.as()) {
				
				this.consume();
				
				if(this.beginParentheses()) {
		
					this.consume("BEGIN_SUBSET_PARANTHESES");
		
					if(this.begin_query()) {
			
						this.setCurrentParseTreeRootNode(
							this.currentParseTreeNode()
						);
			
						this.BEGIN_SQL_STATEMENT();
			
						this.setPreviousParseTreeRootNode();
			
					}
		
					if(this.endParentheses()) {
			
						this.consume("END_SUBSET_PARANTHESES");
			
					} else {
			
						this.error("Expected " + SQLKeyWord.END_PARANTHESES);
			
					}
		
				}
				
			} else {
				
				this.error("Expected " + SQLKeyWord.AS);
				
			}
			
			if(this.comma()) {
				
				this.consume();
				
				if(!this.parseWithList()) {
					
					this.error("Expected another aliased sub-query.");
					
				}
				
			}
			
			found = true;
			
		} else {
			
			this.error("Expected alias for with sub-query.");
			
		}
		
		return found;
		
	}
	
	private void parseSelectElement() throws Exception {
		
		if(!this.parseExpressionAndBuildTree()) {
			
			this.error("Expected expression or *.");
			
		}
		
		this.parseAlias();

		if(this.comma()) {

			this.consume();

			this.parseSelectElement();

		}
	
	}
	
	private boolean parseAlias() throws Exception {
		
		boolean foundAlias = false;
		
		// Alias with AS
		if(this.as()) {
			
			this.consume();
			
			if(this.literal()) {
				
				this.consume();
				
			} else {
				
				this.error("Expected literal.");
				
			}
			
			foundAlias = true;
			
		// Alias without as
		} else if(this.literal()) {
			
			this.consume();
			
			foundAlias = true;
			
		}
		
		return foundAlias;
		
	}
	
	private void parseFromElement() throws Exception {
		this.parseFromElement(true);
	}
	
	private void parseFromElement(boolean required) throws Exception {
		
		if(!this.parseFromElementSet() && required) {
				
			this.error("Expected data set.");
				
		}
		
		if(this.comma()) {
			
			this.consume();
			
			this.parseFromElement(true);
			
		} else if(this.join()) {
			
			this.consume();
			
			if(!this.parseFromElementSet()) {
				
				this.error("Expected data set.");
				
			}
			
			if(this.on()) {

				this.consume();
				
				this.parseJoinCondition();

			} else {

				this.error("Expected ON-keyword.");

			}
			
			this.parseFromElement(false);
			
		}
		
	}
	
	private boolean parseFromElementSet() throws Exception {
		
		boolean found = false;
		
		if(this.beginParentheses()) {
			
			this.consume("BEGIN_SUBSET_PARANTHESES");
			
			if(this.begin_query()) {
				
				this.setCurrentParseTreeRootNode(
					this.currentParseTreeNode()
				);
				
				this.BEGIN_SQL_STATEMENT();
				
				this.setPreviousParseTreeRootNode();
				
			}
			
			if(this.endParentheses()) {
				
				this.consume("END_SUBSET_PARANTHESES");
				
			} else {
				
				this.error("Expected " + SQLKeyWord.END_PARANTHESES);
				
			}
			
			if(!this.parseAlias()) {
				
				this.error("Expected alias for sub-query.");
				
			}
			
			found = true;
			
		} else if(this.q()) { 
			
			this.consume();
			
			this.parseQ();
			
			if(!this.parseAlias()) {
				
				this.error("Expected alias for sub-query.");
				
			}
			
			found = true;
			
		} else if(this.nq()) {
		
			this.consume();
			
			this.parseNQ();
			
			if(!this.parseAlias()) {
				
				this.error("Expected alias for sub-query.");
				
			}
			
			found = true;
			
		} else if(this.csv()) {
		
			this.consume();
			
			this.parseCSV();
			
			if(!this.parseAlias()) {
				
				this.error("Expected alias for sub-query.");
				
			}
			
			found = true;
			
		} else if(this.xl()) {
		
			this.consume();
			
			this.parseXL();
			
			if(!this.parseAlias()) {
				
				this.error("Expected alias for sub-query.");
				
			}
			
			found = true;
			
		} else if(this.json()) {
			
			this.consume();
			
			this.parseJSON();
			
			if(!this.parseAlias()) {
				
				this.error("Expected alias for sub-query.");
				
			}
			
			found = true;
			
		} else if(this.set_function_name()) {
			
			found = this.parseFunction();
			
			if(!this.parseAlias()) {
				
				this.error("Expected alias for function call.");
				
			}
			
			if(found) {
				found = true;
			}
			
		} else if(this.literal()) {
			
			this.consume();
			
			this.parseAlias();
			
			found = true;
			
		}
		
		return found;
		
	}
	
	private void parseQ() throws Exception { // cross-system sub-query
		
		if(this.beginParentheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The FuseBase connection query

				if(this.comma()) {

					this.consume();

					if(this.literal()) {

						this.consume(); // The FuseBase connection name

					}
					
					if(this.endParentheses()) {
						
						this.consume("END_FUNCTION_PARANTHESES");
						
					} else {
						
						this.error("Expected " + SQLKeyWord.END_PARANTHESES);
						
					}
					

				} else {
					
					this.error("Expected FuseBase connection name.");
					
				}

			} else {
				
				this.error("Expected single- or double-quoted query for a FuseBase connection.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + SQLKeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseNQ() throws Exception { // Named Query
		
		if(this.beginParentheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The query name

				if(this.endParentheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected " + SQLKeyWord.END_PARANTHESES);
					
				}

			} else {
				
				this.error("Expected single- or double-quoted query name.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + SQLKeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseCSV() throws Exception { // CSV file reader
		
		if(this.beginParentheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The file URL

				if(this.comma()) {

					this.consume();

					if(this.literal()) {

						this.consume(); // The field delimiter

					}
					
					if(this.endParentheses()) {
						
						this.consume("END_FUNCTION_PARANTHESES");
						
					} else {
						
						this.error("Expected " + SQLKeyWord.END_PARANTHESES);
						
					}
					

				} else {
					
					this.error("Expected single- or double-quoted file URL.");
					
				}

			} else {
				
				this.error("Expected single- or double-quoted field delimiter.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + SQLKeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseXL() throws Exception { // Excel file reader
		
		if(this.beginParentheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The Excel file URL

				if(this.endParentheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected " + SQLKeyWord.END_PARANTHESES);
					
				}

			} else {
				
				this.error("Expected single- or double-quoted URL or file name.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + SQLKeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseJSON() throws Exception { // Excel file reader
		
		if(this.beginParentheses()) {
			
			this.consume("BEGIN_FUNCTION_PARANTHESES");
			
			if(this.literal()) {

				this.consume(); // The URL, typically a REST API returning JSON
				
				if(this.comma()) {

					this.consume();

					if(this.json_data_structure()) {

						this.consume(); // The HTTP GET request headers as a JSON associative array

					} else {
						
						this.error("Expected single- or double-quoted  URL or file name.");
						
					}

				}

				if(this.endParentheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected " + SQLKeyWord.END_PARANTHESES);
					
				}

			} else {
				
				this.error("Expected single- or double-quoted  URL or file name.");
				
			}
			
			this.endFunction();
			
		} else {
			
			this.error("Expected " + SQLKeyWord.BEGIN_PARANTHESES);
			
		}
		
	}
	
	private void parseJoinCondition() throws Exception {
		
		if(this.beginParentheses()) {

			this.consume();

			if(!this.parseExpression()) {

				this.error("Expected expression.");

			}
			
			if(this.logicalGate()) {
				
				this.consume();
				
				this.parseJoinCondition();
				
			}

			if(this.endParentheses()) {

				this.consume();

			} else {

				this.error("Expected closing parentheses.");

			}

		} else {
			
			if(!this.parseExpression()) {

				this.error("Expected expression.");

			}
			
			if(this.logicalGate()) {
				
				this.consume();
				
				this.parseJoinCondition();
				
			}
			
		}
		
	}
	
	private void parseWhereClause() throws Exception {
		
		if(this.beginParentheses()) {

			this.consume();

			this.parseWhereClause();

			if(this.endParentheses()) {

				this.consume();
				
				if(this.logicalGate()) {
				
					this.consume();
				
					this.parseWhereClause();
				
				}

			} else {

				this.error("Expected closing parentheses.");

			}

		} else {
			
			if(!this.parseExpression()) {

				this.error("Expected expression.");

			}
			
			if(this.logicalGate()) {
				
				this.consume();
				
				this.parseWhereClause();
				
			} else if(this.in()) {
				
				this.consume();
				
				this.parseIn();
				
				if(this.logicalGate()) {
				
					this.consume();
				
					this.parseWhereClause();
				
				}
				
			}
			
		}
		
	}
	
	private void parseIn() throws Exception {
		
		if(this.beginParentheses()) {

			this.consume();
			
			try {
				
				this.parseField();
				
			} catch (Exception fieldException) {
				
				try {
					this.BEGIN_SQL_STATEMENT();
				} catch (Exception sqlStatementException) {
					this.error("Expected list of values or SQL statement.");
				}
				
			}
			

			if(this.endParentheses()) {

				this.consume();

			} else {

				this.error("Expected closing parentheses.");

			}

		} else {
			
			this.error("Expected opening parentheses.");
			
		}
		
	}
	
	private void parseField() throws Exception {
		
		/*SQLParseTreeNode currentExpressionParseTreeNode =
			this.currentParseTreeNode();*/
		
		if(!this.parseExpression()) {
			
			this.error("Expected expression.");
			
		}
		
		if(this.comma()) {
			
			this.consume();
			
			this.parseField();
			
		}
		
	}
	
	private void parseOrderByField() throws Exception {
		
		if(!this.parseExpression()) {
			
			this.error("Expected expression.");
			
		}
		
		if(this.sortOrder()) {
			
			this.consume();
			
		}
		
		if(this.comma()) {
			
			this.consume();
			
			this.parseOrderByField();
			
		}
		
	}
	
	private boolean parseFunction() throws Exception {
		
		if(this.cast()) {
			
			this.consume();
			
			if(this.beginParentheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(this.parseExpression()) {

					if(this.as()) {

						this.consume();

						if(this.datatype()) {

							this.consume();

						} else {

							this.error("Expected datatype.");

						}

					} else {

						this.error("Expected AS-keyword.");

					}

				} else {

					this.error("Expected expression.");

				}
				
				if(this.endParentheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected closing parentheses.");
					
				}
				
			} else {
				
				this.error("Expected opening parentheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		} else if(this.trim()) {
			
			this.consume();
			
			if(this.beginParentheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(!this.parseExpression()) {

					this.error("Expected expression.");

				}
				
				if(this.endParentheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected closing parentheses.");
					
				}
				
			} else {
				
				this.error("Expected opening parentheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		} else if(this.geocode()) {
			
			this.consume();
			
			if(this.beginParentheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(this.parseExpression()) {
					
					if(this.endParentheses()) {
					
						this.consume("END_FUNCTION_PARANTHESES");
					
					} else {
					
						this.error("Expected closing parentheses.");
					
					}
					
				} else {
					
					this.error("Expected address input.");
					
				}
				
			} else {
				
				this.error("Expected opening parentheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		} else if(this.set_function_name()) {
			
			this.consume("SET_FUNCTION_NAME");
			
			if(this.beginParentheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				this.parseFunctionParameters();
				
				if(this.endParentheses()) {
				
					this.consume("END_FUNCTION_PARANTHESES");
				
				} else {
				
					this.error("Expected closing parentheses.");
				
				}
				
			} else {
				
				this.error("Expected opening parentheses.");
				
			}
			
			this.endFunction();
			
			return true;
			
		}
		
		return false;
		
	}
	
	private void parseFunctionParameters() throws Exception {
		
		if(this.parseExpression()) {
			
			if(this.comma()) {
				
				this.consume();
				
				this.parseFunctionParameters();
				
			}
			
		}
		
	}
	
	private boolean parseExpressionAndBuildTree() throws Exception {
		
		this.currentParseTreeNode().setAddNewChildrentToExpression(true);
		
		this.expressionTreeBuilders.push(
			new ExpressionTreeBuilder()
		);
		
		boolean foundExpression =
			this.parseExpression();
		
		this.currentParseTreeNode().setAddNewChildrentToExpression(false);
		
		if(foundExpression) {
			
			ExpressionTreeBuilder expressionTreeBuilder =
				this.expressionTreeBuilders.pop();
			
			expressionTreeBuilder.finish();
			
			/*System.out.println(
				expressionTreeBuilder.expressionTreeRoot().toString()
			);*/
			
		}
		
		/*if(foundExpression) {
			
			SQLParseTreeNode currentExpressionParseTreeNode =
				this.currentParseTreeNode().getFirstChild();
			
			ExpressionTreeBuilder expressionTreeBuilder =
				new ExpressionTreeBuilder();
		
			while(currentExpressionParseTreeNode != null &&
					!currentExpressionParseTreeNode.endOfExpression()) {
			
				expressionTreeBuilder.addToken(
					currentExpressionParseTreeNode
				);
			
				currentExpressionParseTreeNode =
					currentExpressionParseTreeNode.next();
			
			}
		
			expressionTreeBuilder.finish();
		
			System.out.println(
				expressionTreeBuilder.expressionTreeRoot().toString()
			);
			
		}*/
		
		return foundExpression;
		
	}
	
	private boolean parseExpression() throws Exception {
		
		boolean foundExpression = false;
		
		if(this.beginParentheses()) {
			
			this.consume();
			
			if(!this.parseExpression()) {

				this.error("Expected expression.");

			}
			
			if(this.endParentheses()) {
				
				this.consume();
				
			} else {
				
				this.error("Expected closing parentheses.");
				
			}
			
			foundExpression = true;
			
		} else if(this.json_lookup()) {
			
			this.parseJsonLookup();
			
			foundExpression = true;
			
		} else if(this.function() || this.set_function_name()) {
		
			this.parseFunction();
			
			foundExpression = true;
			
		} else if(this.factor()) {
			
			this.consume();
			
			foundExpression = true;
			
		} else if(this.nil()) {
			
			this.consume();
			
			foundExpression = true;
			
		} else if(this.aggregateFunction()) {
		
			this.consume();
			
			boolean isCount = this.currentToken().sqlKeyWord() == SQLKeyWord.COUNT;
			
			if(this.beginParentheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(isCount && this.star()) {
					
					this.consume(SQLKeyWord.STAR.toString());
					
				} else {
					
					this.parseExpressionAndBuildTree();
				
					if(this.binaryOperator()) {

						this.consume();

						this.parseExpression();

					}
					
				}
				
				if(this.endParentheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
					// Window function specifier
					if(this.over()) {
						
						this.consume();
						
						if(this.beginParentheses()) {
							
							this.consume("BEGIN_FUNCTION_PARANTHESES");
							
							if(this.partitionBy()) {
								
								this.consume();
								
								this.parseField();
								
							}
							
							if(this.orderBy()) {
								
								this.consume();
								
								this.parseOrderByField();
								
							}
							
							if(this.endParentheses()) {
								
								this.consume("END_FUNCTION_PARANTHESES");
								
							} else {
								
								this.error("Expected closing parentheses");
								
							}
							
						} else {
							
							this.error("Expected opening parentheses.");
							
						}
						
						this.endFunction();
						
					}
					
				} else {
					
					this.error("Expected closing parentheses");
					
				}
				
				
			} else {
				
				this.error("Expected opening parentheses.");
				
			}
			
			this.endFunction();
			
			foundExpression = true;
		
		} else if(this._case()) {
			
			this.parseCaseStatement();
			
			foundExpression = true;
			
		}
		
		if(this.binaryOperator()) {

			this.consume();

			this.parseExpression();

		}
		
		return foundExpression;
		
	}
	
	private void parseCaseStatement() throws Exception {
		
		if(this._case()) {
			
			this.consume();
			
			if(!this.parseWhen()) {
				
				this.error("Expected when-statement.");
				
			}
			
			if(this._else()) {
				
				this.consume();
				
				if(!this.parseExpression()) {
					this.error("Expected expression.");
				}
				
			} else {
				
				this.error("Expected else-statement.");
				
			}
			
			if(!this.end()) {
				
				this.error("Expected end-statement.");
				
			}
			
			this.consume(); // End-statement
			
		}
		
	}
	
	private boolean parseWhen() throws Exception {
		
		if(this.when()) {
			
			this.consume();
			
			if(!this.parseExpression()) {
				this.error("Expected expression.");
			}
			
			if(this.then()) {
				
				this.consume();
				
				this.parseExpression();
				
			} else {
				
				this.error("Expected then-statement.");
				
			}
			
			if(this.when()) {
				
				this.parseWhen();
				
			}
			
			return true;
			
		}
		
		return false;
		
	}
	
	private boolean parseJsonLookup() throws Exception {
		
		boolean foundMember = false;
		
		if(this.jsonArrayJoin()) { // JSON array join
			
			this.consume();
			
			if(this.beginParentheses()) {
				
				this.consume("BEGIN_FUNCTION_PARANTHESES");
				
				if(this.literal()) {
					
					this.consume();
					
				} else {
					
					this.error("Expected single- or double-quoted field delimiter for joining JSON array.");
					
				}
				
				if(this.endParentheses()) {
					
					this.consume("END_FUNCTION_PARANTHESES");
					
				} else {
					
					this.error("Expected closing parentheses.");
					
				}
				
			} else {
				
				this.error("Expected opening parentheses.");
				
			}
			
			this.endFunction();
			
			foundMember = true;
			
		} else if(this.literal()) {
			
			this.consume();
			
			foundMember = true;
			
		} else if(this.beginParentheses()) {
			
			this.consume();
			
			if(this.number() || this.literal()) {
				
				this.consume();
				
				if(this.endParentheses()) {
					
					this.consume();
					
					foundMember = true;
					
				} else {
					
					this.error("Expected closing parentheses.");
					
				}
				
			} else {
				
				this.error("Expected number.");
				
			}
				
		}
		
		if(this.json_member()) {
			
			this.consume();
			
			if(this.json_member() || !this.parseJsonLookup()) {
				
				this.error("Expected literal or parentheses-enclosed number.");
				
			}
			
		}
		
		return foundMember;
		
	}
	
	private SQLKeyWord nextSQLKeyWord() {
		if(this.nextToken().sqlKeyWord() == SQLKeyWord.COMMENT) {
			try {
				this.consume();
			} catch(Exception e) {
				;
			}
		}
		if(this.end_of_statement()) {
			return SQLKeyWord.END_OF_FILE;
		}
		return this.nextToken().sqlKeyWord();
	}
	
	private boolean select() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.SELECT);
	}
	
	private boolean with() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.WITH);
	}
	
	private boolean begin_query() {
		return (!this.end_of_statement() &&
			nextSQLKeyWord() == SQLKeyWord.SELECT ||
			nextSQLKeyWord() == SQLKeyWord.WITH
		);
	}
	
	private boolean top() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.TOP);
	}
	
	private boolean distinct() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.DISTINCT);
	}
	
	private boolean star() {
		return this.multiply();
	}
	
	private boolean multiply() { // Used to check for select all, i.e. select * from
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.MULTIPLY);
	}
	
	private boolean _case() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.CASE);
	}
	
	private boolean when() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.WHEN);
	}
	
	private boolean then() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.THEN);
	}
	
	private boolean _else() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.ELSE);
	}
	
	private boolean end() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.END);
	}
	
	private boolean from() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.FROM);
	}
	
	private boolean comma() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.COMMA);
	}
	
	private boolean beginParentheses() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.BEGIN_PARANTHESES);
	}
	
	private boolean endParentheses() {
		return (nextSQLKeyWord() == SQLKeyWord.END_PARANTHESES);
	}
	
	private boolean as() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.AS);
	}
	
	private boolean join() {
		return (!this.end_of_statement() && nextSQLKeyWord().sqlKeyWordType() == SQLKeyWordType.JOIN);
	}
	
	private boolean jsonArrayJoin() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.JOIN);
	}
	
	private boolean setOperation() {
		return (!this.end_of_statement() && nextSQLKeyWord().sqlKeyWordType() == SQLKeyWordType.SET_OPERATION);
	}
	
	private boolean on() {
		return (nextSQLKeyWord() == SQLKeyWord.ON);
	}
	
	private boolean q() { // Cross-system Query
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.Q);
	}
	
	private boolean nq() { // Named Query
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.NQ);
	}
	
	private boolean csv() { // CSV file
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.CSV);
	}
	
	private boolean xl() { // Excel file
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.XL);
	}
	
	private boolean json() { // JSON file
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.JSON);
	}
	
	private boolean geocode() { // Geocode function
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.GEOCODE);
	}
	
	private boolean cast() {
		return (nextSQLKeyWord() == SQLKeyWord.CAST);
	}
	
	private boolean trim() {
		return (nextSQLKeyWord() == SQLKeyWord.TRIM);
	}
	
	private boolean set_function_name() {
		return (
			!this.end_of_statement() &&
				nextSQLKeyWord() == SQLKeyWord.LITERAL &&
				this.nextToken().nextToken() != null &&
				this.nextToken().nextToken().sqlKeyWord() == SQLKeyWord.BEGIN_PARANTHESES
		);
	}
	
	private boolean datatype() {
		return (nextSQLKeyWord().sqlKeyWordType() == SQLKeyWordType.DATATYPE);
	}
	
	private boolean literal() {
		return (
			!this.end_of_statement() &&
				(nextSQLKeyWord() == SQLKeyWord.LITERAL ||
				nextSQLKeyWord() == SQLKeyWord.SINGLE_QUOTED_LITERAL ||
				nextSQLKeyWord() == SQLKeyWord.DOUBLE_QUOTED_LITERAL)
		);
	}
	
	private boolean logicalGate() {
		return (
			!this.end_of_statement() &&
			(nextSQLKeyWord() == SQLKeyWord.AND ||
			nextSQLKeyWord() == SQLKeyWord.OR)
		);
	}
	
	private boolean factor() {
		return
			!this.end_of_statement() && 
			(nextSQLKeyWord() == SQLKeyWord.LITERAL ||
			nextSQLKeyWord() == SQLKeyWord.DOUBLE_QUOTED_LITERAL ||
			nextSQLKeyWord() == SQLKeyWord.SINGLE_QUOTED_LITERAL ||
			nextSQLKeyWord() == SQLKeyWord.NUMBER_ATOM);
	}
	
	private boolean nil() {
		return (
			!this.end_of_statement() &&
			(nextSQLKeyWord() == SQLKeyWord.NULL || nextSQLKeyWord() == SQLKeyWord.NOT_NULL)
		);
	}
	
	private boolean in() {
		return (
			!this.end_of_statement() &&
			(nextSQLKeyWord() == SQLKeyWord.IN || nextSQLKeyWord() == SQLKeyWord.NOT_IN)
		);
	}
	
	private boolean json_lookup() {
		return (
			!this.end_of_statement() &&
			(nextSQLKeyWord() == SQLKeyWord.LITERAL ||
			nextSQLKeyWord() == SQLKeyWord.DOUBLE_QUOTED_LITERAL ||
			nextSQLKeyWord() == SQLKeyWord.SINGLE_QUOTED_LITERAL) &&
			this.nextToken().nextToken() != null &&
				this.nextToken().nextToken().sqlKeyWord() == SQLKeyWord.JSON_MEMBER
		);
	}
	
	private boolean json_member() {
		return nextSQLKeyWord() == SQLKeyWord.JSON_MEMBER;
	}
	
	private boolean number() {
		return (
			nextSQLKeyWord() == SQLKeyWord.NUMBER_ATOM
		);
	}
	
	private boolean json_data_structure() {
		return (
			nextSQLKeyWord() == SQLKeyWord.JSON_DATA_STRUCTURE
		);
	}
	
	private boolean aggregateFunction() {
		return (!this.end_of_statement() && nextSQLKeyWord().sqlKeyWordType() == SQLKeyWordType.AGGREGATE_OPERATOR);
	}
	
	private boolean function() {
		return (!this.end_of_statement() && nextSQLKeyWord().sqlKeyWordType() == SQLKeyWordType.FUNCTION);
	}
	
	private boolean binaryOperator() {
		return (!this.end_of_statement() && nextSQLKeyWord().sqlKeyWordType() == SQLKeyWordType.BINARY_OPERATOR);
	}
	
	private boolean over() {
		return (nextSQLKeyWord() == SQLKeyWord.OVER);
	}
	
	private boolean partitionBy() {
		return (nextSQLKeyWord() == SQLKeyWord.PARTITION_BY);
	}
	
	private boolean where() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.WHERE);
	}
	
	private boolean groupBy() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.GROUP_BY);
	}
	
	private boolean orderBy() {
		return (!this.end_of_statement() && nextSQLKeyWord() == SQLKeyWord.ORDER_BY);
	}
	
	private boolean sortOrder() {
		return (!this.end_of_statement() && nextSQLKeyWord().sqlKeyWordType()  == SQLKeyWordType.SORT_ORDER);
	}
	
	private boolean end_of_statement() {
		return this.nextToken() == null;
	}
	
	private void error(String error) throws Exception {
		throw new Exception(error + "\n" + (this.nextToken() != null ? this.nextToken().tokenInSQL() : "Got EOF"));
	}
	
	/*
	** Print methods
	*/
	
	private void print(SQLParseTreeNode parseTreeNode, int depth, JSONBuilder jb, boolean debug) {
		
		if(parseTreeNode == null) {
			return;
		}
		
		if(debug) {
			for(int i=0; i<=depth; i++) {
				System.out.print("-");
			}
		
			System.out.println(
				" " + parseTreeNode.toString()
			);
		}
		
		jb.$('{');
		
		if(parseTreeNode.getParent() == null) {
			
			jb.k("KEYWORD").v("ROOT");
			
		} else {
			
			jb.k("KEYWORD").v(parseTreeNode.sqlKeyWord());
			jb.k("TYPE").v(parseTreeNode.sqlKeyWord().sqlKeyWordType());
			jb.k("TOKEN").v(parseTreeNode.sqlToken());
			
		}
		
		if(parseTreeNode.childCount() > 0) {
			
			jb.k("children");
		
			jb.$('[');
		
			for(SQLParseTreeNode childParseTreeNode : parseTreeNode.getChildren()) {
		
				this.print(
					childParseTreeNode,
					depth + 1,
					jb,
					debug
				);
			
			}
		
			jb.$(']');
			
		}
		
		jb.$('}');
		
	}
	
	public void print() {
		
		JSONBuilder jb = new JSONBuilder();
		
		System.out.println("SQL Parse tree:\n");
		
		this.print(this.currentParseTreeRootNode, 0, jb, true);
		
		System.out.println("\nSQL Parse tree as JSON:\n");
		
		System.out.print(jb.getJSON());
		
	}
	
	public String toJSON() {
		JSONBuilder jb = new JSONBuilder();
		this.print(this.currentParseTreeRootNode, 0, jb, false);
		return jb.getJSON();
	}
	
	/*
	** Helper methods
	*/
	private void getSQLParseTreeNodesBySQLKeyWord(
		SQLParseTreeNode parseTreeNode,
		Vector<SQLParseTreeNode> nodeList,
		SQLKeyWord sqlKeyWord
	) {
		
		if(parseTreeNode == null) {
			return;
		}
		
		for(SQLParseTreeNode childParseTreeNode : parseTreeNode.getChildren()) {
			
			if(childParseTreeNode.sqlKeyWord() == sqlKeyWord) {
				
				nodeList.add(
					childParseTreeNode
				);
				
			}
			
			this.getSQLParseTreeNodesBySQLKeyWord(
				childParseTreeNode,
				nodeList,
				sqlKeyWord
			);
			
		}
		
	}
	
	public SQLParseTreeNode[] getSQLParseTreeNodesBySQLKeyWord(SQLKeyWord sqlKeyWord) {
		
		Vector<SQLParseTreeNode> nodeList =
			new Vector<SQLParseTreeNode>();
		
		this.getSQLParseTreeNodesBySQLKeyWord(
			this.currentParseTreeRootNode,
			nodeList,
			sqlKeyWord
		);
		
		return nodeList.toArray(new SQLParseTreeNode[]{});
		
	}
	
	public SQLParseTreeNode getRootNode() {
		
		return this.currentParseTreeRootNode;
		
	}
	
	public static void main(String[] args) {
		
		try {
			
			String sql =
				"select\n" +
				"	top 10\n" +
				"	name as \"Test Column\",\n" +
				"	1+1+1*2,\n" +
				"	1/4+1*2,\n" +
				"	geocode('Oslo, Norway') as gc," +
				"	sum(value/(23+3)^((2))) over (partition by name)*10^2 sum_value,\n" +
				"	'hello' as text\n" +
				"from\n" +
				"	q('select \"customer_id\" from test', 'ERP') t1\n" +
				"	inner join\n" +
				"	table2 t2\n" +
				"	on (t1.customer_id = t2.id)\n" +
				"group by\n" +
				"	name\n" +
				"order by\n" +
				"	sum_value asc\n" +
				"union " +
				"select\n" +
				"	name,\n" +
				"	1+1+1*2,\n" +
				"	1/4+1*2,\n" +
				"	sum(value/(23+3)^((2))) over (partition by name)*10^2 sum_value,\n" +
				"	'hello' as text\n" +
				"from\n" +
				"	q('select \"customer_id\" from test', 'ERP') t1\n" +
				"	inner join\n" +
				"	xl('/Users/nkjalloh/Desktop/test.xlsx!combined') t2\n" +
				"	on (t1.customer_id = t2.id)\n" +
				"group by\n" +
				"	name\n" +
				"order by\n" +
				"	sum_value asc";
				
			String sql2 =
				"select\n" +
				"	sq.ID, sq.R\n" +
				"from\n" +
				"	q('select * from (select \\'HANA\\' id, round(rand(),1) r from dummy)', 'UOVDEV') sq\n" +
				"union all\n" +
				"select\n" +
				"	sq.ID, sq.R\n" +
				"from\n" +
				"	q('select \\'ORACLE\\' id, round(dbms_random.value(),1) r from dual', 'ERP_STAGE') sq\n" +
				"union all\n" +
				"select\n" +
				"	sq.ID, sq.R\n" +
				"from\n" +
				"	q('return \\'NEO4J\\' as ID, round(rand()*10) as R', 'neo4j') sq";
				
			String sql3 =
				"select\n" +
				"	sq.ID, sq.R, 1+1\n" +
				"from\n" +
				"	q('select * from (select \\'HANA\\' id, round(rand(),1) r from dummy)', 'UOVDEV') sq\n" +
				"union all\n" +
				"select\n" +
				"	sq.ID, sq.R\n" +
				"from\n" +
				"	q('select \\'ORACLE\\' id, round(dbms_random.value(),1) r from dual', 'ERP_STAGE') sq\n" +
				"union all\n" +
				"select\n" +
				"	sq.ID, sq.R\n" +
				"from\n" +
				"	csv('http://fuse.cisco.com:4444/apps/datacafe/files/test.csv', ',') sq";
				
			String sql4 =
				"select 1 as ID, 'Niclas' as NAME, 'Test' as DESCRIPTION " +
				"UNION ALL select 1 from csv('test', ',') as sq " +
				"UNION ALL select 1 from nq(\"Supply Chain Test\") as sq";
				
			String sql5 =
				"select SHIPPING_ROUTE_CODE from csv('http://fuse.cisco.com:4444/apps/datacafe/files/orders.csv', ',') sq group by SHIPPING_ROUTE_CODE";
				
			String sql6 =
				"select count(1) from csv('http://fuse.cisco.com:4444/apps/datacafe/files/orders.csv', ',') sq";
				
			String sql7 =
				"select SUM(CAST(UNIT_SELLING_PRICE_USD as number)), trim('value') from csv('http://fuse.cisco.com:4444/apps/datacafe/files/orders.csv', ',') sq";
			
			String sql8 =
				"select sum(1^2+3*12-30/40)*100 from dual";
			
			String sql9 =
				"select top 10 * from csv('https://cisco.box.com/shared/static/nxa351bcrd0ib046hrbkei01fqyq6qtk.csv', '|') sq where sq.long_lat = \'NA;NA\'";
			
			String sql10 =
				"select * from (select top 10 * from csv('https://cisco.box.com/shared/static/nxa351bcrd0ib046hrbkei01fqyq6qtk.csv', '|') sq) sq";
			
			String sql11 =
				"with sq as (select " +
					"sq.json_array, " +
					"geocode('oslo') as gc, " +
					"sq.json_array_length " +
				"from " +
					"(select " +
						"sq.`1`->`getBom.getBOMResult`->`getBom.row`->(t.idx) as json_array, " +
						"sq.`1`->`getBom.getBOMResult`->`getBom.row`->length as json_array_length " +
					"from " +
						"json( " +
							"'http://dv-tst-02:9400/json/DV_POC_ENV16/getBom/getBOM?item_id=73-13652-02', " +
							"{\"Authorization\": \"Basic ZHZfcG9jX2VudjE2QGNvbXBvc2l0ZTpkdl9wb2NfZW52MTY=\"} " +
						") sq) sq " +
					"inner join " +
					"(select t.index from seq(0,sq.json_array_length,1) i) t " +
					"on (1=1)), sq2 as (select 1 as id) select * from sq";
			
			String sql12 =
				"select * from dual a inner join dual b on (1=1)";
			
			SQLParser sp =
				new SQLParser(
					sql12
				);
			
			sp.print();
			
		} catch(Exception e) {
			
			JSONBuilder jb = new JSONBuilder();
			
			jb.$('{').k("error").v(e.getMessage()).$('}');
			
			System.out.println(jb.getJSON());
			
			e.printStackTrace();
			
		}
		
	}
	
}