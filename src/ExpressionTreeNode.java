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
public class ExpressionTreeNode {
	
	private Object element;
	private ExpressionTreeNode parent;
	private ExpressionTreeNode leftChild;
	private ExpressionTreeNode rightChild;
	
	public ExpressionTreeNode(Object element, ExpressionTreeNode parent, ExpressionTreeNode rightChild, ExpressionTreeNode leftChild) {
		
		this.element = element;
		this.parent = parent;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		
		if(this.leftChild != null) {
			this.leftChild.setParent(this);
		}
		
		if(this.rightChild != null) {
			this.rightChild.setParent(this);
		}
		
	}
	
	public Object element() {
		return this.element;
	}
	
	public ExpressionTreeNode parent() {
		return this.parent;
	}
	
	public void setParent(ExpressionTreeNode parent) {
		this.parent = parent;
	}
	
	public ExpressionTreeNode leftChild() {
		return this.leftChild;
	}
	
	public void setLeftChild(ExpressionTreeNode leftChild) {
		this.leftChild = leftChild;
	}
	
	public ExpressionTreeNode rightChild() {
		return this.rightChild;
	}
	
	public void setRightChild(ExpressionTreeNode rightChild) {
		this.rightChild = rightChild;
	}
	
	public String toString() {
		
		StringBuilder treeStringBuilder =
			new StringBuilder();
		
		ExpressionTreeNode.print(
			treeStringBuilder,
			this,
			0
		);
		
		return treeStringBuilder.toString();
		
	}
	
	private static String rightPad(String stringToPad, int paddedLength, char padding) {
		
		StringBuilder paddedString = new StringBuilder(stringToPad);
		
		while(paddedString.length() < paddedLength) {
			
			paddedString.append(padding);
			
		}
		
		return paddedString.toString();
		
	}
	
	private static String leftPad(String stringToPad, int paddedLength, char padding) {
		
		StringBuilder paddedString = new StringBuilder(stringToPad);
		
		while(paddedString.length() < paddedLength) {
			
			paddedString.insert(0, padding);
			
		}
		
		return paddedString.toString();
		
	}
	
	private static void print(StringBuilder treeStringBuilder, ExpressionTreeNode node, int depth) {
		
		if(node == null) {
			
			return;
			
		}
		
		if(node.parent() != null) {
		
			treeStringBuilder.append("\n");
		
		}
		
		treeStringBuilder.append(
			ExpressionTreeNode.leftPad(
				" " + node.element(),
				node.element().toString().length() + 1 + depth + 1,
				'-'
			)
		);
		
		ExpressionTreeNode.print(
			treeStringBuilder,
			node.leftChild(),
			depth + 1
		);
		
		ExpressionTreeNode.print(
			treeStringBuilder,
			node.rightChild(),
			depth + 1
		);
		
	}
	
}