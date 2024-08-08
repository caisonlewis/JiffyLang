//Interpreter - class version
package class_interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import class_helper.ParseTree;
import class_helper.TreeNode;
import class_parser.JiffyParser;

public class Interpreter {
	private static MemoryManager memMgr;
	
	private static HashMap<String, String> valueTable;
	private static HashMap<String, TreeNode> funcs;
	private static Scanner sysin = new Scanner(System.in);

	public static void interpret(String programText, MemoryManager m, boolean detail) {
		memMgr = m;
		JiffyParser jp = new JiffyParser(programText, detail);
		ParseTree pt = jp.getAST();
		funcs = jp.getFuncs(); // HashMap of function names and corresponding TreeNode

		if (detail)
			System.out.println(pt);
		System.out.println("------------INTERPRETER OUTPUT------------------");

		// Program execution starts with the function main
		// Get the node in the parse tree associated with main
		TreeNode mainNode = funcs.get("main");

		// Evaluate the function represented by that node
		// Second parameter is null because main takes no parameters
		evaluateFunction(mainNode, null);
	}

	private static String evaluate(TreeNode currentNode) throws FunctionReturned {
		String val = "";

		if (currentNode.getSymbol().equals("block")) {
			evaluateBlock(currentNode);
		} else if (currentNode.getSymbol().equals("stmnt_list")) {
			evaluateStatementList(currentNode);
		} else if (currentNode.getSymbol().equals("=")) {
			evaluateAssignmentStatement(currentNode);
		} else if (currentNode.getSymbol().equals("write")) {
			evaluateWriteStatement(currentNode);
		} else if (currentNode.getSymbol().equals("read")) {
			evaluateReadStatement(currentNode);
		} else if (currentNode.getSymbol().equals("while")) {
			evaluateWhileStatement(currentNode);
		} else if (currentNode.getSymbol().equals("if")) {
			evaluateIfStatement(currentNode);
		} else if (currentNode.getSymbol().equals("return")) {
			evaluateReturnStatement(currentNode);
		} else if (currentNode.getSymbol().contains("()")) {
			val = evaluateFunctionCall(currentNode);
		} else if (currentNode.isLeaf()) { // leaf node - numeric constant, string literal, or identifier
			val = evaluateLeafNode(currentNode);
		} else { // current node corresponds to arithmetic operator
			val = evaluateArithmeticOperator(currentNode);
		}

		if (isDouble(val))
			return simplify(val);
		else
			return val;
	}

	private static void evaluateBlock(TreeNode current) throws FunctionReturned {
		evaluate(current.getLeft());
	}

	private static void evaluateStatementList(TreeNode current) throws FunctionReturned {
		ArrayList<TreeNode> children = current.getChildren();
	
		for (TreeNode t : children) {
			if (t != null)
				evaluate(t);
		}
	}

	private static void evaluateAssignmentStatement(TreeNode current) throws FunctionReturned {
		String left;
		String right;
		left = current.getLeft().getSymbol();
		right = evaluate(current.getRight());
		valueTable.put(left, right);
	}

	private static void evaluateWriteStatement(TreeNode current) throws FunctionReturned {
		TreeNode t = current.getLeft();
		String s = evaluate(t);
		if (isStringLiteral(s))
			s = stripEnds(s);
		System.out.println(s);
	}

	private static void evaluateReadStatement(TreeNode current) {
		TreeNode t = current.getLeft();
		String varName = t.getSymbol();
		String input = sysin.nextLine();
		valueTable.put(varName, input);
	}

	private static boolean evaluateBooleanExpression(TreeNode t) throws NumberFormatException, FunctionReturned {
		// TODO Auto-generated method stub
		boolean result = true;
		TreeNode leftExp = t.getLeft(); // left expression
		double left = Double.parseDouble(evaluate(leftExp));
		TreeNode rightExp = t.getRight(); // right expression
		double right = Double.parseDouble(evaluate(rightExp));
	
		if (t.getSymbol().equals("?"))
			result = left == right;
		else if (t.getSymbol().equals(">"))
			result = left > right;
		else if (t.getSymbol().equals("<"))
			result = left < right;
	
		return result;
	}

	private static void evaluateWhileStatement(TreeNode currentNode) throws FunctionReturned {
		//Complete this method
		ArrayList<TreeNode> children = currentNode.getChildren();
		TreeNode boolExp = children.get(0);
		boolean result = evaluateBooleanExpression(boolExp);
		while (result) {
			evaluateBlock(children.get(1));
			result = evaluateBooleanExpression(boolExp);
			
		}
	}

	private static void evaluateIfStatement(TreeNode currentNode) throws FunctionReturned {
		//Complete this method
		ArrayList<TreeNode> children = currentNode.getChildren();
		TreeNode boolExp = children.get(0);
		boolean result = evaluateBooleanExpression(boolExp);
		if (result) {
			evaluateBlock(children.get(1));
		}
		else {
			if (children.size() > 2) {
				evaluate(children.get(2));
			}
		}
	
	}

	private static void evaluateReturnStatement(TreeNode currentNode) throws FunctionReturned {
		String result = evaluate(currentNode.getLeft());

		// Before returning, pop activation record from stack
		// Set currentActivationRecord to activation record on top of stack
		valueTable = memMgr.restoreActivationRecord();

		throw new FunctionReturned(result);
	}

	private static String evaluateFunctionCall(TreeNode currentNode) throws FunctionReturned {
		// TODO Auto-generated method stub
		String funcName = getFuncName(currentNode.getSymbol());
		TreeNode funcNode = funcs.get(funcName); // get the node corresponding to function def

		// Need to pass parameters
		// each parameter is an expression, so evaluate it first
		ArrayList<TreeNode> params = currentNode.getChildren();
		String[] actualParams = new String[params.size()];
		for (int i = 0; i < params.size(); i++)
			actualParams[i] = evaluate(params.get(i));

		// evaluate the node associated with this function with the specified parameters
		return evaluateFunction(funcNode, actualParams);
	}

	private static String evaluateLeafNode(TreeNode current) {
		String val;
		String symbol = current.getSymbol();
		try { // maybe numeric constant
			Double.parseDouble(symbol);
		} catch (Exception e) {
			// oops. Not a double constant. Must be an identifier or string literal
			if (!isStringLiteral(symbol)) // identifier
				symbol = valueTable.get(symbol); // look it up in symbol table
		}
		val = symbol;
		return val;
	}

	private static String evaluateArithmeticOperator(TreeNode current) throws FunctionReturned {
		TreeNode leftChild = current.getLeft();
		TreeNode rightChild = current.getRight();
		double leftVal = Double.parseDouble(evaluate(leftChild));
		double rightVal = Double.parseDouble(evaluate(rightChild));
		
		String op = current.getSymbol();
		double result = 0;
		if(op.equals("+"))
			result = leftVal + rightVal;
		else if (op.equals("*"))
			result = leftVal * rightVal;
		else if (op.equals("-"))
			result = leftVal - rightVal;
		else if (op.equals("/"))
			result = leftVal/rightVal;
		else if (op.equals("^")) //exponential where leftVal is the base and rightVal is the exponent
			result = Math.pow(leftVal,rightVal);
		else if (op.equals("$")) //max function that gives the greater of the two values
			if (leftVal > rightVal)
				result = leftVal;
			else
				result = rightVal;
		else if (op.equals("%")) //modulo function that gives the remainder when one number is divided by another
			result = leftVal % rightVal;
		
		
		
		return Double.toString(result);
	}

	private static String evaluateFunction(TreeNode funcNode, String[] actualParams) {
		// Need to handle function parameters
		ArrayList<TreeNode> children = funcNode.getChildren();

		//Get activation record from memory manager
		valueTable = memMgr.getActivationRecord(getFuncName(funcNode.getSymbol()));

		// Copy values for all parameters - pass by value
		int size = children.size();
		if (actualParams != null) {
			if (actualParams.length == size - 1) {
				for (int i = 0; i < size - 1; i++) {
					TreeNode param = children.get(i);
					String paramSymbol = param.getSymbol();

					String value = actualParams[i];
					valueTable.put(paramSymbol, value);
				}
			} else {
				System.out.println("Mismatched parameters for " + funcNode.getSymbol());
				System.exit(0);
			}
		}
		TreeNode funcBody = children.get(size - 1); // the block associated with this function
		try {
			return evaluate(funcBody);
		} catch (FunctionReturned e) {
			// The return statement in the funcBody throws this exception
			// which is caught here and the result extracted
			// from it
			return e.getResult();
		}
	}

	private static String getFuncName(String symbol) {
		// TODO Auto-generated method stub
		int beginIndex = 0;
		if (symbol.contains("fun"))
			beginIndex = symbol.indexOf(' ') + 1;
		int endIndex = symbol.indexOf('(');
		return symbol.substring(beginIndex, endIndex);
	}

	private static String add(String left, String right) {
		// + is overloaded. Can mean floating point addition or string concatenation
		// figure out which meaning applies and do the corresponding operation
		String val;
		try { 
			// maybe doubles? If so, parse them and do floating point +
			double left_op = Double.parseDouble(left);
			double right_op = Double.parseDouble(right);
			val = Double.toString(left_op + right_op);
		} catch (NumberFormatException e) { 
			// one or both not a double. + means concatenation
			if (isStringLiteral(left))
				left = stripEnds(left);
			if (isStringLiteral(right))
				right = stripEnds(right);
			val = left + right;
		}

		return val;
	}

	private static boolean isStringLiteral(String s) {
		// TODO Auto-generated method stub
		return (s.charAt(0) == '\"');
	}

	private static boolean isDouble(String val) {
		if (val.equals(""))
			return false;
		else {
			try {
				Double.parseDouble(val);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}

	private static String simplify(String val) {
		// if val can be replaced by int, do so, dropping the trailing .0
		double d = Double.parseDouble(val);

		Double dval = d;

		int x = dval.intValue();
		if (d - x == 0.0) {
			return Integer.toString(x);
		} else
			return Double.toString(d);
	}

	private static String stripEnds(String s) {
		// TODO Auto-generated method stub
		String result = "";
		for (int i = 1; i <= s.length() - 2; i++)
			result += s.charAt(i);
		return result;
	}

}