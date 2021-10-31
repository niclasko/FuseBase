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
import java.util.LinkedList;

public class SQLParseTreeNode extends ExpressionToken {
	
	private SQLToken sqlToken;
	private SQLParseTreeNode parent;
	private LinkedList<SQLParseTreeNode> children;
	
	private boolean addNewChildrentToExpression;
	
	private static SQLParseTreeNode nullNode =
		new SQLParseTreeNode(
			SQLToken.createRootToken(),
			null
		);
	
	public SQLParseTreeNode(SQLToken sqlToken, SQLParseTreeNode parent) {
		
		super(sqlToken);
		
		this.sqlToken = sqlToken;
		this.parent = parent;
			
		this.children = new LinkedList<SQLParseTreeNode>();
		
		this.addNewChildrentToExpression = false;
		
	}
	
	public SQLToken sqlToken() {
		return this.sqlToken;
	}
	
	public SQLKeyWord sqlKeyWord() {
		return this.sqlToken.sqlKeyWord();
	}
	
	public SQLParseTreeNode getParent() {
		return this.parent;
	}
	
	public SQLParseTreeNode addChild(SQLParseTreeNode child) {
		
		this.children.add(child);
		
		return child;
	}
	
	public LinkedList<SQLParseTreeNode> getChildren() {
		return this.children;
	}
	
	public SQLParseTreeNode getNthChild(int n) {
		if(n > this.childCount()) {
			return SQLParseTreeNode.nullNode;
		}
		return this.children.get(n);
	}
	
	public SQLParseTreeNode getFirstChild() {
		return this.children.peekFirst();
	}
	
	public SQLParseTreeNode getLastChild() {
		return this.children.peekLast();
	}
	
	public int childCount() {
		return this.children.size();
	}
	
	public void setAddNewChildrentToExpression(boolean addNewChildrentToExpression) {
		this.addNewChildrentToExpression = addNewChildrentToExpression;
	}
	
	public boolean addNewChildrentToExpression() {
		return this.addNewChildrentToExpression;
	}
	
	public String toString() {
		
		if(this.sqlToken == null) {
			
			return "[ROOT]";
			
		}
		
		if(this.sqlKeyWord() == SQLKeyWord.SET_FUNCTION_NAME) {
			return this.sqlToken().toString() +
					" (" + this.sqlKeyWord().sqlKeyWordType() + ")";
		}
		
		return this.sqlKeyWord().toString() +
				" (" + this.sqlKeyWord().sqlKeyWordType() + ")";
	}
}