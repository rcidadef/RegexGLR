package br.edu.ufam;

public class Token {
	private String token;
	private int type;

	public static final int GAP = 0;
	public static final int ALPHA = 1;
	public static final int NUMERIC = 2;
	public static final int SYMBOL = 3;

	// Construtores
	public Token() {
	}

	public Token(String tk) {
		token = tk;
		if (token == "/gap/") {
			type = GAP;
		} else {
			type = mapType(Character.getType(token.charAt(0)));
		}
	}

	// Funcao que mapeia um tipo caractere em Java para um tipo do alfabeto 
	// aceita pela linguagem do programa
	public static int mapType(int t) {
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
	} // fim mapType

	public String getToken() {
		return token;
	}

	public int getType() {
		return type;
	}

	public void setToken(String t) {
		token = t;
	}

	public void setType(int t) {
		type = t;
	}

	public boolean equals(Token t) {
		if (type == t.getType()) {
			return token.equalsIgnoreCase(t.token);
		} else {
			return false;
		}
	}
}