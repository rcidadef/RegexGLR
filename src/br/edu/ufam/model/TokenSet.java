package br.edu.ufam.model;

import java.util.*;

public class TokenSet {
	private ArrayList<Token> set;

	public TokenSet() {
		set = new ArrayList<Token>();
	}

	public TokenSet(Token t) {
		set = new ArrayList<Token>();
		set.add(t);
	}

	public boolean add(Token t) {
		set.add(t);
		return false;
	}

	/** Remove caracteres acentuados */
	public static String stripAccents(String s) {
		String ns = new String();
		ns = s.replaceAll("[����]", "a");
		ns = ns.replaceAll("[����]", "A");
		ns = ns.replaceAll("[����]", "e");
		ns = ns.replaceAll("�", "i");
		ns = ns.replaceAll("�", "I");
		ns = ns.replaceAll("[���]", "o");
		ns = ns.replaceAll("[���]", "O");
		ns = ns.replaceAll("�", "u");
		ns = ns.replaceAll("�", "U");
		ns = ns.replaceAll("�", "c");
		return ns;
	}

	public boolean hasGap() {
		for (Token t : set) {
			if (t.getType() == Token.GAP) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Uni�o entre dois Tokensets. Segue a mesma defini��o de uni�o da teoria
	 * dos conjuntos.
	 */
	public static TokenSet tokenSetUnion(TokenSet ts1, TokenSet ts2) {
		TokenSet nts = new TokenSet();

		if (ts1.equals(ts2)) {
			for (Token t : ts1.getSet()) {
				nts.add(t);
			}
		} else {
			boolean in = false;

			for (Token t : ts2.getSet()) {
				nts.add(t);
			}
			for (Token ti : ts1.getSet()) {
				for (Token tj : ts2.getSet()) {
					if (ti.equals(tj) == true) {
						in = true;
					}
				}
				if (in == false) {
					nts.add(ti);
				} else {
					in = false;
				}
			}
		}

		return nts;
	} // fim tokenSetUnion

	/** Retorna a quantidade de tipos distintos no Tokenset. */
	public int getTypeAmount() {
		int sum = 0;
		int[] types = new int[4];
		for (int i = 0; i < set.size() && sum != 3; ++i) {
			types[set.get(i).getType()] = 1;
			sum = types[1] + types[2] + types[3];
		}
		return sum;
	} // fim getTypeAmount

	/** Retorna a quantidade de Tokens de tipo igual a <i>type</i>. */
	public int getAmountOf(int type) {
		int count = 0;
		for (Token t : set) {
			if (type == t.getType()) {
				count++;
			}
		}
		return count;
	} // fim getAmountOf

	/**
	 * Retorna um inteiro que simboliza os tipos presentes no Tokenset.</br> 3:
	 * os tipos ALPHA e NUMERIC.</br> 4: os tipos ALPHA e SYMBOL.</br> 5: os
	 * tipos NUMERIC e SYMBOL.</br> 6: todos os tipos, ALPHA, NUMERIC e SYMBOL.
	 * -1: apenas um tipo.
	 */
	public int getTypes() {
		int sum = -1;
		int type = this.getSetType();
		if (type > 0) {
			int[] types = new int[4];
			for (int i = 0; i < set.size() && sum != 6; ++i) {
				type = set.get(i).getType();
				types[type] = type;
				sum = types[1] + types[2] + types[3];
			}
		}
		return sum;
	}

	/** Retorna o tipo de Token em maior quantidade no Tokenset. */
	public int getLargestType() {
		int type = 1;
		int[] types = new int[4];
		for (int i = 0; i < set.size(); ++i) {
			types[set.get(i).getType()]++;
		}
		if (types[1] > types[2] && types[1] > types[3]) {
			type = Token.ALPHA;
		} else if (types[2] >= types[3]) {
			type = Token.NUMERIC;
		} else {
			type = Token.SYMBOL;
		}
		return type;
	} // fim getLargestType

	/** Retorna o tipo do Tokenset, caso s� tenha um tipo, ou -1 caso contr�rio. */
	public int getSetType() {
		int type = getTypeAmount();
		if (type == 1) {
			type = -1;
			for(Token t : set) {
				if (t.getType() != Token.GAP) {
					type = t.getType();
					break;
				} // fim if
			} // fim for
		} // fim if
		return type;
	} // fim getSetType

	/** Retorna true se Tokenset cont�m s�mbolos, ou false caso contr�rio. */
	public boolean hasSymbol() {
		for (Token t : set) {
			if (t.getType() == Token.SYMBOL) {
				return true;
			}
		}
		return false;
	} // fim hasSymbol
	
	/** Retorna o tamanho do maior Token no Tokenset */
	public int getMaxLength() {
		int max = set.get(0).getToken().length();
		for(Token t : set) {
			if (max < t.getToken().length())
				max = t.getToken().length();
		}
		return max;
	}
	
	/** Retorna o tamanho do menor Token no Tokenset */
	public int getMinLength() {
		int min = set.get(0).getToken().length();
		for(Token t : set) {
			if (min > t.getToken().length())
				min = t.getToken().length();
		}
		return min;
	}

	// Fun��es para impress�o
	public void print(boolean cBraces) {
		if (cBraces)
			System.out.print("{");
		for (Token t : set) {
			System.out.printf("%s ", t.getToken());
		}
		if (cBraces)
			System.out.print("}");
	}

	public void print(int w) {
		for (Token t : set) {
			System.out.printf("%" + w + "s ", t.getToken());
		}
	}

	public void print(Formatter o) {
		for (Token t : set) {
			o.format("%s ", t.getToken());
		}
	}

	public void print(Formatter o, int w) {
		for (Token t : set) {
			o.format("%" + w + "s ", t.getToken());
		}
	}

	// Fim funcoes de impressao

	// Getters e Setters
	public ArrayList<Token> getSet() {
		return set;
	}

	public void setSet(ArrayList<Token> s) {
		this.set = s;
	}

	public int getSize() {
		return set.size();
	}

	// Fim Getters e Setters

	public int getTokensLength() {
		int length = 0;
		for (Token t : set) {
			length += t.getToken().length();
		}
		return length;
	}

	// Verifica se os dois TokenSets sao exatamente iguais
	// Levando em consideracao apenas os elementos, nao sua ordem
	public boolean equals(TokenSet ts) {
		if (ts.getSize() == this.getSize()) {
			for (Token ti : this.set) {
				for (Token tj : ts.getSet()) {
					if (tj.equals(ti) == false) {
						return false;
					}
				}
			}
		}
		return false;
	}

	// Verifica se dois TokenSets tem pelo menos um par de Tokens iguais
	public boolean sameToken(TokenSet ts) {
		for (Token ti : this.set) {
			for (Token tj : ts.getSet()) {
				if (tj.equals(ti) == true) {
					return true;
				}
			}
		}
		return false;
	}

	// Verifica se ha pelo menos um par de Tokens com tipos iguais
	public boolean sameType(TokenSet ts) {
		for (Token ti : this.set) {
			for (Token tj : ts.getSet()) {
				if (tj.getType() == ti.getType()) {
					return true;
				}
			}
		}
		return false;
	}
}