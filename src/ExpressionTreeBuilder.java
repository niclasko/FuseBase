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
import java.util.Stack;
import java.util.Arrays;

public class ExpressionTreeBuilder {
	
	private Stack<ExpressionToken> output;
	private Stack<ExpressionToken> operators;
	private ExpressionTreeNode expressionTreeRoot;
	
	public ExpressionTreeBuilder() {
		
		this.output = new Stack<ExpressionToken>();
		this.operators = new Stack<ExpressionToken>();
		this.expressionTreeRoot = null;
		
	}
	
	private boolean precedenceConditionIsMet(ExpressionToken token1, ExpressionToken token2) {
		
		OperatorType o1 = token1.operatorType(), o2 = token2.operatorType();
		
		if(o1.leftAssociativity() && o1.precedence() <= o2.precedence()) {
			return true;
		} else if(o1.rightAssociativity() && o1.precedence() < o2.precedence()) {
			return true;
		}
		
		return false;
		
	}
	
	// Shunting Yard algorithm for converting an infix expression to postfix notation (Reverse Polish Notation)
	public void addToken(ExpressionToken token) {
		
		if(token.isOperator()) {
			
			if(operators.size() == 0) {
				
				operators.push(token);
				
			} else if(operators.size() > 0) {
				
				while(!operators.isEmpty() && operators.peek().isOperator() && precedenceConditionIsMet(token, operators.peek())) {
					
					output.push(operators.pop());
					
				}
				
				operators.push(token);
				
			}
			
		} else if(token.sqlKeyWord() == SQLKeyWord.BEGIN_PARANTHESES) {
				
			operators.push(token);
				
		} else if(token.sqlKeyWord() == SQLKeyWord.END_PARANTHESES) {
			
			while(!operators.isEmpty() && operators.peek().sqlKeyWord() != SQLKeyWord.BEGIN_PARANTHESES) {
				
				output.push(operators.pop());
				
			}
			
			if(operators.isEmpty() || operators.peek().sqlKeyWord() != SQLKeyWord.BEGIN_PARANTHESES) {
				
				System.out.println("Mismatched parantheses. Missing left parantheses.");
				
				return;
				
			}
			
			operators.pop(); // Remove the left paranthesis
			
		} else {
			
			output.push(token);
			
		}
		
	}
	
	public void finish() {
		
		while(!operators.isEmpty()) {
			
			if(operators.peek().sqlKeyWord() == SQLKeyWord.BEGIN_PARANTHESES ||
				operators.peek().sqlKeyWord() == SQLKeyWord.END_PARANTHESES) {
				
				System.out.println("Mismatched parantheses");
				
				return;
				
			}
			
			output.push(operators.pop());
			
		}
		
		this.expressionTreeRoot =
			this.buildExpressionTree(
				output.toArray(new ExpressionToken[output.size()])
			);
		
	}
	
	public ExpressionTreeNode expressionTreeRoot() {
		
		return this.expressionTreeRoot;
		
	}
	
	private ExpressionTreeNode buildExpressionTree(ExpressionToken[] postFixExpression) {
		
		Stack<ExpressionTreeNode> expressionTreeNodes =
			new Stack<ExpressionTreeNode>();
			
		for(int i=0; i<postFixExpression.length; i++) {
			
			if(postFixExpression[i].isOperator()) {
				
				expressionTreeNodes.push(
					new ExpressionTreeNode(
						postFixExpression[i],
						null,
						expressionTreeNodes.pop(),
						expressionTreeNodes.pop()
					)
				);
				
			} else {
				
				expressionTreeNodes.push(
					new ExpressionTreeNode(
						postFixExpression[i],
						null,
						null,
						null
					)
				);
				
			}
			
		}
		
		if(expressionTreeNodes.isEmpty()) {
			return null;
		}
		
		return expressionTreeNodes.pop();
		
	}
	
	public static ExpressionToken[] tokenizeExpression(String expression) throws Exception {
		
		SQLTokenizer st =
			new SQLTokenizer(
				expression
			);
		
		SQLToken[] sqlTokens =
			st.tokens();
		
		ExpressionToken[] expressionTokens =
			new ExpressionToken[sqlTokens.length];
		
		int i = 0;
		
		for(SQLToken sqlToken : st.tokens()) {
			
			expressionTokens[i++] =
				new ExpressionToken(sqlToken);
			
		}
		
		return expressionTokens;
		
	}
	
	public static void main(String args[]) throws Exception {
		
		String expression =
			"21+3*(4+5)/6*7 OR 1 AND 2^4 AND 1=1";
		
		String expression2 = "1 or 2";
		
		ExpressionToken[] expressionTokens =
			ExpressionTreeBuilder.tokenizeExpression(
				expression
			);
		
		ExpressionTreeBuilder etb = 
			new ExpressionTreeBuilder();
			
		for(ExpressionToken expressionToken : expressionTokens) {
			
			System.out.println(expressionToken);
			
			etb.addToken(expressionToken);
			
		}
		
		etb.finish();
		
		System.out.println(
			etb.expressionTreeRoot().toString()
		);
		
	}
}