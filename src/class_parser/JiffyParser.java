//Jiffy Parser - Class version
//
package class_parser;

import java.util.HashMap;

import class_helper.JiffyError;
import class_helper.ParseTree;
import class_helper.TreeNode;

/**
 * JiffyParser parses a program string in the Jiffy language. Program string is
 * a collection of Jiffy statements. This includes read statements, write
 * statements, if statements, while statements, and expressions involving
 * identifiers, numeric constants, parentheses, and the operators =, +, -, *,
 * and / with the usual precedence and associativity.
 */
public class JiffyParser {

	/**
	 * We use a JiffyLexer object to tokenize the input string.
	 */
	private JiffyLexer lexer;

	/**
	 * Our error message or null if there has been no error.
	 */
	private String errorMessage = null;

	/**
	 * The AST corresponding to the statement being parsed (if no error).
	 */
	private ParseTree ast;
	private HashMap<String, TreeNode> funcs = new HashMap<String, TreeNode>();

	private boolean blockResume = false;

	/**
	 * Constructor for JiffyParser. This uses JiffyLexer to tokenize the string and
	 * then parse it. The resulting parse tree is assigned to ast
	 * 
	 * @param s the program string to be parsed
	 */
	public JiffyParser(String s, boolean detail) {

		// First make a JiffyLexer to hold the string. This
		// will get an error immediately if the first token
		// is bad, so check for that.

		try {
			lexer = new JiffyLexer(s, detail);
		} catch (JiffyError e) {
			printError(e);
			return;
		}

		// Now parse the program text and get the result.

		try {
			ast = new ParseTree(parseProgram());
		} catch (JiffyError e) {
			printError(e);
			return;
		}

		// After the program text we should be at the end of
		// the input.

		try {
			match(JiffyLexer.EOLN_TOKEN);
		} catch (JiffyError e) {
			printError(e);
			;
			return;
		}

	}

	private void printError(JiffyError e) {
		System.err.println("\tLine " + lexer.getLineNumber() + ":" + e.getMessage());
		System.exit(0);
	}

	private TreeNode parseProgram() throws JiffyError {
		// <program> ::= <block> | <declarations> <block>
		// Note - nextToken only looks ahead at the next token
		// subsequent calls to nextToken return the same token
		// only advance (which is called in match) actually moves the marker past this
		// token to the next token
		TreeNode result = new TreeNode("program");
		while (lexer.nextToken() == JiffyLexer.FUN_TOKEN) // one or more function declarations exist
			result.add(parseFunctionDeclaration());

		return result;
	}

	private TreeNode parseFunctionDeclaration() throws JiffyError {
		match(JiffyLexer.FUN_TOKEN);
		String funcName = lexer.getTokenString();
		TreeNode result = new TreeNode("fun " + funcName + "()"); // func name
		match(JiffyLexer.IDENTIFIER_TOKEN);
		match('(');

		// Process formal parameters, if any
		if (lexer.nextToken() == JiffyLexer.IDENTIFIER_TOKEN) {
			result.add(new TreeNode(lexer.getTokenString()));
			match(JiffyLexer.IDENTIFIER_TOKEN);

			while (lexer.nextToken() == ',') {
				match(',');
				result.add(new TreeNode(lexer.getTokenString()));
				match(JiffyLexer.IDENTIFIER_TOKEN);
			}
		}

		// End of formal parameter list
		match(')');

		result.add(parseBlockWithReturn());

		// add the funcName,Node pair to the funcs HashMap
		funcs.put(funcName, result);
		return result;
	}

	private TreeNode parseBlock() throws JiffyError {
		// <block> ::= { <statement_list> }

		TreeNode result = new TreeNode("block");
		if (lexer.nextToken() == '{') { // parse a block
			match('{');
			result.add(parseStatementList());

			if (lexer.nextToken() == '}') {
				match('}');
				blockResume = true;
			} else
				throw new JiffyError("Expected a }");
		} else
			throw new JiffyError("Expected a {");

		return result;
	}

	private TreeNode parseBlockWithReturn() throws JiffyError {
		TreeNode block = parseBlock();
		// Add the mandatory return statement in case none exists
		TreeNode ret = new TreeNode("return");
		ret.add(new TreeNode("\"None\""));
		(block.getLeft()).add(ret);
		return block;
	}

	private TreeNode parseStatementList() throws JiffyError {
		// <statement_list> ::= <statement> | <statement> ; <statement_list>
		TreeNode result = new TreeNode("stmnt_list");
		result.add(parseStatement()); // the first statement

		while (true) {
			if (lexer.nextToken() == ';') { // parse subsequent statements
				match(';');
				result.add(parseStatement());
			} else if (blockResume) {
				blockResume = false;
				result.add(parseStatement());
			} else
				return result;
		}
	}

	private TreeNode parseStatement() throws JiffyError {
		if (lexer.nextToken() == JiffyLexer.IDENTIFIER_TOKEN) {
			return parseAssignmentStatement();
		} else if (lexer.nextToken() == JiffyLexer.WRITE_TOKEN) {
			return parseWriteStatement();
		} else if (lexer.nextToken() == JiffyLexer.READ_TOKEN) {
			return parseReadStatement();
		} else if (lexer.nextToken() == JiffyLexer.WHILE_TOKEN) {
			return parseWhileStatement();
		} else if (lexer.nextToken() == JiffyLexer.IF_TOKEN) {
			return parseIfElseStatement();
		} else if (lexer.nextToken() == JiffyLexer.RETURN_TOKEN) {
			return parseReturnStatement();
		} else if (lexer.nextToken() == '{') { // nested block
			return parseBlock();
		}

		return null;
	}

	private TreeNode parseReturnStatement() throws JiffyError {
		TreeNode result = new TreeNode("return");
		match(JiffyLexer.RETURN_TOKEN);
		result.add(parseArithmeticExpression());
		return result;
	}

	private TreeNode parseAssignmentStatement() throws JiffyError {
		// <assignment_statement> ::= <identifier> = <exp>

		TreeNode result = new TreeNode(lexer.getTokenString()); // identifier
		match(JiffyLexer.IDENTIFIER_TOKEN);

		if (lexer.nextToken() == '=') {
			match('=');
			result = new TreeNode("=", result, parseArithmeticExpression());
			return result;
		} else
			throw new JiffyError("Malformed assignment statement");
	}

	private TreeNode parseWriteStatement() throws JiffyError {
		// <write_statement> ::= write ( <exp> )

		match(JiffyLexer.WRITE_TOKEN);
		TreeNode result = new TreeNode("write");
		match('(');
		result.add(parseArithmeticExpression());
		match(')');
		return result;
	}

	private TreeNode parseReadStatement() throws JiffyError {
		// <read_statement> ::= read ( <identifier> )

		match(JiffyLexer.READ_TOKEN);
		TreeNode result = new TreeNode("read");
		match('(');
		if (lexer.nextToken() == JiffyLexer.IDENTIFIER_TOKEN) {
			result.add(new TreeNode(lexer.getTokenString())); // identifier name
			match(JiffyLexer.IDENTIFIER_TOKEN);
		}
		match(')');
		return result;
	}

	private TreeNode parseWhileStatement() throws JiffyError {
		// <whilestatement> ::= while ( <bexp> ) <block>

		match(JiffyLexer.WHILE_TOKEN);
		TreeNode result = new TreeNode("while");
		// Complete this method
		match('(');
		result.add(parseBooleanExpression());
		match(')');
		
		result.add(parseBlock());

		return result;
	}

	private TreeNode parseIfElseStatement() throws JiffyError {
		// <ifstatement> ::= if (<bexp>) <block> else <block>| if (<bexp>) <block>

		match(JiffyLexer.IF_TOKEN);
		TreeNode result = new TreeNode("if");
		// Complete this method
		
		match('(');
		result.add(parseBooleanExpression());
		match(')');
		
		result.add(parseBlock());
		
		//deal with else statements
		// try to see if there is an else after the if, if not jump into the catch block
		try {
			match(JiffyLexer.ELSE_TOKEN);
			result.add(parseBlock()); 
		
		}
		catch(JiffyError e){
			 

		}

		return result;

	}

	private TreeNode parseBooleanExpression() throws JiffyError {
		// TODO Auto-generated method stub
		TreeNode result = parseArithmeticExpression();

		if (lexer.nextToken() == '>') {
			match('>');
			result = new TreeNode(">", result, parseArithmeticExpression());
		} else if (lexer.nextToken() == '<') {
			match('<');
			result = new TreeNode("<", result, parseArithmeticExpression());
		} else if (lexer.nextToken() == '?') { //edited in class
			match('?'); //edited in class
			result = new TreeNode("?", result, parseArithmeticExpression()); //edited in class
		}

		return result;

	}

	/**
	 * Parse an arithmetic expression. If any error occurs we return immediately.
	 *
	 * @return the parse tree corresponding to the expression or garbage in case of
	 *         errors.
	 * @throws JiffyError
	 */
	private TreeNode parseArithmeticExpression() throws JiffyError {
		return parseExp();
	}

	/**
	 * Parse a $ expression. If any error occurs we return immediately.
	 *
	 * @return a TreeNode corresponding to the addexp or garbage in case of
	 *         errors.
	 * @throws JiffyError
	 */
	
	private TreeNode parseExp() throws JiffyError {
		// <exp> ::= <exp> $ <addexp> | <addexp>
		
		TreeNode result = parseAddExp();
		
		while (true) {
			if (lexer.nextToken() == '$') {
				match('$');
				result = new TreeNode("$", result, parseAddExp());
			} else
				return result;
		}
	}
	
	/**
	 * Parse an add/sub expression at the precedence level of + and - . If any error occurs we return immediately.
	 *
	 * @return a TreeNode corresponding to the expression or garbage in case of
	 *         errors.
	 * @throws JiffyError
	 */
	
	private TreeNode parseAddExp() throws JiffyError {

		// <addexp> ::= <addexp> + <mulexp> | <addexp> - <mulexp> | <mulexp>

		TreeNode result = parseMulExp();

		while (true) {
			if (lexer.nextToken() == '+') {
				match('+');
				result = new TreeNode("+", result, parseMulExp());
			} else if (lexer.nextToken() == '-') {
				match('-');
				result = new TreeNode("-", result, parseMulExp());
			} else
				return result;
		}
	}

	/**
	 * Parse a mulexp, a subexpression at the precedence level of * and /. If any
	 * error occurs we return immediately.
	 *
	 * @return a TreeNode corresponding to the mulexp or garbage in case of errors.
	 * @throws JiffyError
	 */
	private TreeNode parseMulExp() throws JiffyError {
		// <mulexp> ::= <mulexp> * <rootexp> | <mulexp> / <rootexp> | <rootexp>

		TreeNode result = parsePowExp();

		while (true) {
			if (lexer.nextToken() == '*') {
				match('*');
				result = new TreeNode("*", result, parsePowExp());
			} else if (lexer.nextToken() == '/') {
				match('/');
				result = new TreeNode("/", result, parsePowExp());
			} else if (lexer.nextToken() == '%') {
				match('%');
				result = new TreeNode("%", result, parsePowExp());
			} else
				return result;
		}
	}
	
	/**
	 * Parse a powexp, a subexpression at the precedence level of ^ . If any
	 * error occurs we return immediately.
	 *
	 * @return a TreeNode corresponding to the powexp or garbage in case of errors.
	 * @throws JiffyError
	 */
	
	private TreeNode parsePowExp() throws JiffyError {
		// <powexp> ::= <rootexp> ^ <powexp> | <rootexp>
		
		TreeNode result = parseRootExp();
		
		while (true) {
			if (lexer.nextToken() == '^') {
				match('^');
				result = new TreeNode("^", result, parsePowExp());
			} else
				return result;
		}
	}

	/**
	 * Parse a rootexp, which is a constant or parenthesized subexpression. If any
	 * error occurs we return immediately.
	 *
	 * @return a TreeNode corresponding to the rootexp or garbage in case of errors
	 * @throws JiffyError
	 */
	private TreeNode parseRootExp() throws JiffyError {
		TreeNode result = null;

		// <rootexp> ::= '(' <expression> ')'

		if (lexer.nextToken() == '(') {
			match('(');
			result = parseArithmeticExpression();
			match(')');
		}

		// <rootexp> ::= number

		else if (lexer.nextToken() == JiffyLexer.NUMBER_TOKEN) {
			result = new TreeNode(Double.toString(lexer.getNum()));
			match(JiffyLexer.NUMBER_TOKEN);
		} else if (lexer.nextToken() == JiffyLexer.IDENTIFIER_TOKEN) {
			String symbol = lexer.getTokenString();
			match(JiffyLexer.IDENTIFIER_TOKEN);
			if (lexer.nextToken() == '(') {
				result = parseFuncCall(symbol);
			} else {
				result = new TreeNode(symbol);
			}
		} else if (lexer.nextToken() == JiffyLexer.STRING_TOKEN) {
			result = new TreeNode(lexer.getTokenString());
			match(JiffyLexer.STRING_TOKEN);
		} else {
			errorMessage = "Expected a number, identifier, string literal, or a parenthesis.";
		}

		return result;
	}

	private TreeNode parseFuncCall(String symbol) throws JiffyError {
		// TODO Auto-generated method stub
		TreeNode result = new TreeNode(symbol + "()");
		match('(');
		if (lexer.nextToken() == ')') { // no params
			match(')');
		} else { // one or more params, each of which is an expression
			result.add(parseArithmeticExpression());
			while (lexer.nextToken() == ',') {
				match(',');
				result.add(parseArithmeticExpression());
			}
			match(')');
		}
		return result;
	}

	/**
	 * Match a given token and advance to the next. This utility is used by our
	 * parsing routines. If the given token does not match lexer.nextToken(), we
	 * generate an appropriate error message. Advancing to the next token may also
	 * cause an error.
	 *
	 * @param expectedToken - the token that we expect to find, i.e. the one to
	 *                      match
	 * @throws JiffyError
	 */
	private void match(int expectedToken) throws JiffyError {

		// First check that the current token matches the
		// one we were passed; if not, signal an error.

		if (lexer.nextToken() != expectedToken) {
			if (expectedToken == JiffyLexer.EOLN_TOKEN)
				throw new JiffyError("+++Unexpected text in the program.");
			else
				throw new JiffyError(
						"+++Expected a " + decode(expectedToken) + ". " + "Got " + decode(lexer.nextToken()));
		} else
			lexer.advance();
	}

	private String decode(int token) {
		if (token > 0)
			return Character.toString((char) token);
		else {
			int offset = JiffyLexer.tokenTypes.length;
			return JiffyLexer.tokenTypes[token + offset];
		}
	}

	/**
	 * Get the error message or null if none.
	 *
	 * @return the error message or null
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Get the AST corresponding to the statement parsed
	 *
	 * @return the value of the expression as a String
	 */
	public ParseTree getAST() {
		return ast;
	}

	public HashMap<String, TreeNode> getFuncs() {
		return funcs;
	}

}
