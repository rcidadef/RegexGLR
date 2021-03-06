package br.edu.ufam.model;

public class Token {

	private int type;
	private String token;

	public static final int GAP = 0;
	public static final int ALPHA = 1;
	public static final int NUMERIC = 2;
	public static final int SYMBOL = 3;

	/**
	 * Standard constructor.
	 */
	public Token() {
	}

	/**
	 * Constructor. Defines Token type and value
	 * 
	 * @param tk
	 *            Token's token attribute.
	 */
	public Token(String tk) {
		token = tk;
		if (token == "/gap/") {
			type = GAP;
		} else {
			type = mapType(token.charAt(0));
		}
	}

	/**
	 * Maps a Java character type to one the three token types used by RegexGLR
	 * (SYMBOL, NUMERIC, ALPHA).
	 * 
	 * @param t
	 * @return int thats represents a RegexGLR token type (ALPHA - 1, NUMERIC -
	 *         2, SYMBOL - 3).
	 */
	public static int mapType(char ch) {
		int t = Character.getType(ch);
		int res = SYMBOL;
		switch (t) {
		case 1:
		case 2:
			res = ALPHA;
			break;
		case 9:
			res = NUMERIC;
			break;
		}
		return res;
	} /* End mapType */

	/**
	 * @return String that represents the Token.
	 */
	public String getToken() {
		return token;
	} /* End getToken */

	public int getType() {
		return type;
	} /* End getType */

	public void setToken(String t) {
		token = t;
	} /* End setToken */

	/**
	 * Sets Token's type to t.
	 * 
	 * @param t
	 *            The type corresponding int value.
	 * @throws Exception
	 */
	public void setType(int t) throws Exception {
		if (t != Token.SYMBOL && t != Token.ALPHA && t != Token.NUMERIC
				&& t != Token.GAP) {
			throw new Exception("Invalid Token type: " + t);
		}
		type = t;
	} /* End setType */

	/**
	 * Tokens need to be the same type and have the same value.
	 * 
	 * @param t
	 *            Token to compare with
	 * @return True if Tokens are equal. False otherwise.
	 */
	public boolean equals(Token t) {
		return (type == t.getType()) ? token.equalsIgnoreCase(t.token) : false;
	} /* End equals */

	public String toString() {
		return token;
	} /* End toString */

}