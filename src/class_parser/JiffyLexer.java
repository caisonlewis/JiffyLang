package class_parser;

import class_helper.JiffyError;

/**
 * JiffyLexer provides a simple scanner for a JiffyParser. We hold the string
 * being parsed, and JiffyParser uses us to read the string as a sequence of
 * tokens.
 */
public class JiffyLexer {
	/**
	 * The program text being parsed, held in a StringTokenizer.
	 */
	private java.util.StringTokenizer tokens;

	/**
	 * The current token.
	 */
	private int tokenChar;

	private double tokenNum;
	private String tokenString;

	private static int tokenCount = 1;
	private boolean detail = false;

	private int lineNumber = 1;

	/**
	 * Non-character values for tokenChar. By choosing negative values we are
	 * certain not to collide with any char values stored in the int tokenChar.
	 */
	public static final int NUMBER_TOKEN = -1;
	public static final int IDENTIFIER_TOKEN = -2;
	public static final int STRING_TOKEN = -3;
	public static final int WRITE_TOKEN = -4;
	public static final int READ_TOKEN = -5;
	public static final int WHILE_TOKEN = -6;
	public static final int IF_TOKEN = -7;
	public static final int ELSE_TOKEN = -8;
	public static final int EOLN_TOKEN = -9;
	public static final int FUN_TOKEN = -10;
	public static final int RETURN_TOKEN = -11;
	
	public static String [] tokenTypes = {"RETURN", "FUN", "EOLN", "ELSE", "IF", "WHILE","READ"
			, "WRITE", "STRING", "IDENTIFIER", "NUMBER"}; 

	/**
	 * Constructor for a JiffyLexer. Our parameter is the program text to be
	 * tokenized.
	 * 
	 * @param s the String to be tokenized
	 * @throws JiffyError
	 */
	public JiffyLexer(String s, boolean detail) throws JiffyError {

		// We use a StringTokenizer to tokenize the string.
		// Delimiters are shown below.
		// By making the third parameter true,
		// those delimiters are also returned as tokens.

		this.detail = detail;

		s = removeComments(s);

		tokens = new java.util.StringTokenizer(s, " \t\n\r{};+-*/()=\"<>,?^$%", true); //edited in class
		
		// Start by advancing to the first token. Note that
		// this may get an error, which would set our
		// errorMessage instead of setting tokenChar.
		advance();
	}

	private String removeComments(String s) {
		// The parameter s is a string representing the Jiffy source code
		// Comments start with commentChar and go through end of line
		// Remove all comments from the code and
		// RETURN the resulting, uncommented, code
		
		StringBuilder result = new StringBuilder();

        String[] lines = s.split("\n");
        for (String line : lines) {
            int commentIndex = line.indexOf("#");
            if (commentIndex != -1) {
                result.append(line.substring(0, commentIndex)).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }


	/**
	 * Advance to the next token. We don't return anything; the caller must use
	 * nextToken() to see what that token is.
	 * 
	 * @throws JiffyError
	 */
	public void advance() throws JiffyError {

		// White space is returned as a token by our
		// StringTokenizer, but we will loop until something
		// other than white space has been found.

		while (true) {

			// If we're at the end, make it an EOLN_TOKEN.
			if (!tokens.hasMoreTokens()) {
				tokenChar = EOLN_TOKEN;
				return;
			}

			// Get a token
			// Use the first character to figure out whether and what kind of token we have
			String s = tokens.nextToken();
			char c1 = s.charAt(0);

			if (c1 == '\n')
				lineNumber++;

			if (c1 != ' ' && detail) {
				System.out.println("token " + tokenCount + " is " + s);
				tokenCount++;
			}

			if (Character.isDigit(c1)) {
				getNumberToken(s);
				return;
			} else if (c1 == '"') { // string literal
				getStringLiteralToken();
				return;
			} else if (Character.isLetter(c1)) {
				getKeywordToken(s);
				return;
			} else if (!Character.isWhitespace(c1)) {
				// Any other single character that is not
				// white space is considered a token.
				tokenChar = c1;
				tokenString = Character.toString(c1);
				return;
			}
		}
	}

	private void getKeywordToken(String s) {
		tokenString = s;

		// Tag token
		if (s.equals("write"))
			tokenChar = WRITE_TOKEN;
		else if (s.equals("read"))
			tokenChar = READ_TOKEN;
		else if (s.equals("while"))
			tokenChar = WHILE_TOKEN;
		else if (s.equals("if"))
			tokenChar = IF_TOKEN;
		else if (s.equals("else"))
			tokenChar = ELSE_TOKEN;
		else if (s.equals("fun"))
			tokenChar = FUN_TOKEN;
		else if (s.equals("return"))
			tokenChar = RETURN_TOKEN;
		else // default - if it is none of the keywords, treat it as an identifier
			tokenChar = IDENTIFIER_TOKEN;
	}

	private void getStringLiteralToken() throws JiffyError {
		try {
			tokenString = getStringLiteral();
			tokenChar = STRING_TOKEN;
		} catch (JiffyError c) {
			throw c;
		}
	}

	private void getNumberToken(String s) throws JiffyError {
		try {
			tokenNum = Double.valueOf(s).doubleValue();
			tokenString = s;
			tokenChar = NUMBER_TOKEN;
		} catch (NumberFormatException x) {
			throw new JiffyError("Illegal format for a number");
		}
	}

	private String getStringLiteral() throws JiffyError {
		String result = "\"";
		String s = "";
		char c = ' ';
		do {
			s = tokens.nextToken();
			result += s;
			c = s.charAt(0);
		} while (c != '"');

		if (c == '"')
			return result;
		else
			throw new JiffyError("Bad string literal");
	}

	/**
	 * Return the value of a numeric token. This should only be called when
	 * nextToken() reports a NUMBER_TOKEN.
	 *
	 * @return the double value of the number
	 */
	public double getNum() {
		return tokenNum;
	}

	/**
	 * Return the value of a identifier token. This should only be called when
	 * nextToken() reports a IDENTIFIER_TOKEN.
	 *
	 * @return the string value of the token
	 */
	public String getTokenString() {
		return tokenString;
	}

	/**
	 * Return the next token. Repeated calls will return the same token again; the
	 * caller should use advance() to advance to another token.
	 * 
	 * @return the next token as an int
	 */
	public int nextToken() {
		return tokenChar;
	}

	public int getLineNumber() {
		// TODO Auto-generated method stub
		return lineNumber;
	}
}
