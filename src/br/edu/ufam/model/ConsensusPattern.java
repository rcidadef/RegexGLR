package br.edu.ufam.model;

import java.util.*;
import java.util.regex.*;

public class ConsensusPattern {
	private int threshold;
	private ArrayList<TokenSet> pattern;

	public ConsensusPattern() {
		threshold = 3;
		pattern = new ArrayList<TokenSet>();
	}

	public ConsensusPattern(String s) {
		this(s, 3);
	}

	public ConsensusPattern(String s, int threshold) {
		this.threshold = threshold;
		tokenize(s);
	}

	// Cria uma sequencia de TokenSets apartir de uma String
	public void tokenize(String s) {
		char c = s.charAt(0);
		int type = Token.mapType(c);
		String token = new String();
		TokenSet tokenSet = new TokenSet();
		pattern = new ArrayList<TokenSet>();

		token += c;

		for (int i = 1; i != s.length(); ++i) {
			c = s.charAt(i);
			if (Token.mapType(c) == type
					&& Token.mapType(c) != Token.SYMBOL) {
				token += c;
			} else {
				tokenSet.add(new Token(token));
				pattern.add(tokenSet);
				token = new String();
				token += c;
				type = Token.mapType(c);
				tokenSet = new TokenSet();
			}
			// Chegou ao final da String
			if (i == s.length() - 1) {
				tokenSet.add(new Token(token));
				pattern.add(tokenSet);
			}
		}
	} /* End tokenize */

	// Verifica se um caractere precisa ser escapado
	private boolean isMeta(String mchar) {
		String[] metaChars = { "*", ".", "+", "?", "^", "$", "\\", "{", "}",
				"|", "(", ")", "[", "]", "-" };
		for (String s : metaChars) {
			if (s.equals(mchar)) {
				return true;
			}
		}
		return false;
	}

	public Pattern inferRegex() {
		String regex = "^";

		for (TokenSet ts : pattern) {
			if (ts.getSize() == 1) { // Token que nao varia
				String tempStr = ts.getSet().get(0).getToken();
				if (isMeta(tempStr)) { // Se for meta char escapa
					tempStr = "\\" + tempStr;
				}
				regex += tempStr;
			} else if (ts.hasGap() == true) { // Tokenset com gap
				if (ts.getSize() == 2) {
					String tempStr;
					Token t;

					// Pega o Token que nao o Gap
					if (ts.getSet().get(0).getType() != Token.GAP) {
						t = ts.getSet().get(0);
						tempStr = t.getToken();
					} else {
						t = ts.getSet().get(1);
						tempStr = t.getToken();
					}

					// Coloca Token entre parenteses caso nao seja um simbolo
					// ou seja maior que 1
					if (t.getType() == Token.SYMBOL || tempStr.length() == 1) {
						// Se for meta char escapa
						if (isMeta(tempStr)) {
							tempStr = "\\" + tempStr;
						}
						regex += tempStr + "?";
					} else {
						regex += '(' + tempStr + ")?";
					}
				} else {
					regex += '(';
					for (Token t : ts.getSet()) {
						String tempStr = t.getToken();
						if (isMeta(tempStr)) { // Se for meta char escapa
							tempStr = "\\" + tempStr;
						}
						regex += tempStr + '|';
					}
					regex = regex.replaceFirst("/gap/\\|?", "");
					regex += ")?";
					regex = regex.replaceFirst("\\|\\)", ")");
				}
			} else {
				regex += '(';
				for (Token t : ts.getSet()) {
					regex += t.getToken() + '|';
				}
				regex += ")";
				regex = regex.replaceFirst("\\|\\)", ")");
			}
		}

		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		return p;
	}

	/*
	 * Gera uma express�o regular generalizada a partir da sequ�ncia de
	 * Tokensets que comp�em o ConsensusPattern. A generaliza��o se d� da
	 * seguinte forma: 1: Tokenset de um �nico tipo se torna: Alpha: \w+
	 * Numeric: \d+ Symbol: os pr�prios s�mbolos separados por | 2: Com mais de
	 * um tipo: Todos os Tokens separados por | 3: Alinhado com gap Pode ser um
	 * dos casos acima, mas com um ? ap�s o grupo 4: Tokenset de tamanho 1:
	 * Repete o Token que o forma
	 */
	public Pattern inferGeneralRegex() {
		String regex = new String();
		for (TokenSet ts : pattern) {
			// Repete Token
			if (ts.getSize() == 1) {
				String tempStr = ts.getSet().get(0).getToken();
				// Caso seja um s�mbolo
				if (isMeta(tempStr)) {
					tempStr = "\\" + tempStr;
				}
				regex += tempStr;
			} else if (ts.hasGap()) { // Tokenset com gap
				// Tokenset apenas com um Token opcional
				if (ts.getSize() - 1 == 1) {
					// Pega o Token, n�o o gap
					String temp = (ts.getSet().get(0).getType() == Token.GAP) ? ts
							.getSet().get(1).getToken()
							: ts.getSet().get(0).getToken();
					// Se o Token for maior que 1, � colocada num grupo
					if (temp.length() > 1) {
						temp = "(?:" + temp + ")?";
					} else {
						// Se for metacaractere escapar
						if (isMeta(temp)) {
							temp = "\\" + temp;
						}
						temp = temp + "?";
					}
					regex += temp;
				} else if (ts.getSet().size() > threshold) {
					// Tamanho de Tokenset maior que limiar, ent�o � necess�rio
					// generalizar Tokenset
					int op = ts.getTypeAmount();
					switch (op) {
					case 1: { // Caso em que Tokenset tem apenas 1 tipo de Token
						// Descobre qual e muda a expressao de acordo
						int ty = ts.getSetType();
						if (ty == Token.ALPHA) {
							regex += "[a-zA-Z]*";
						} else if (ty == Token.NUMERIC) {
							regex += "[\\d]*";
						} else { // ty == Token.SYMBOL
							regex += "[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr;
							}
							regex += "]?";
						}
						break;
					} // fim case 1
					case 2: { // Caso com 2 tipos de Token distintos
						int types = ts.getTypes();
						switch (types) {
						case 3: // ALPHA e NUMERIC
							regex += "[\\w]*";
							break;
						case 4: // ALPHA e SYMBOL
							regex += "(?:[a-zA-Z]*|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.GAP) {
									if (t.getType() != Token.SYMBOL) {
										continue;
									}
									String tempStr = t.getToken();
									// Se for metacaractere escapa
									if (isMeta(tempStr)) {
										tempStr = "\\" + tempStr;
									}
									regex += tempStr + '|';
								}
							}
							regex += "]?)";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						case 5: // NUMERIC e SYMBOL
							regex += "(?:\\d*|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += "]?)";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						} // fim switch
						break;
					} // fim case 2
					case 3: { // Caso com 3 tipos distintos de Tokens
						regex += "(?:\\w*|[";
						// Adiciona Tokens de s�mbolos � express�o
						for (Token t : ts.getSet()) {
							if (t.getType() != Token.SYMBOL) {
								continue;
							}
							String tempStr = t.getToken();
							// Se for metacaractere escapa
							if (isMeta(tempStr)) {
								tempStr = "\\" + tempStr;
							}
							regex += tempStr + '|';
						}
						regex += "]?)";
						regex = regex.replaceFirst("\\|\\]", "]");
						break;
					} // fim case 3
					} // fim switch
				} else {
					// N�o h� necessidade de generalizar, apenas se repete cada
					// Token
					regex += "(";
					for (Token t : ts.getSet()) {
						if (t.getType() != Token.GAP) {
							String tempStr = t.getToken();
							// Se for metacaractere escapa
							if (isMeta(tempStr)) {
								tempStr = "\\" + tempStr;
							}
							regex += tempStr + '|';
						}
					}
					regex += ")?";
					regex = regex.replaceFirst("\\|\\)", ")");
				} // fim if-else
			} else { // Tokenset sem gap
				// Tamanho de Tokenset maior que limiar, ent�o � necess�rio
				// generalizar Tokenset.
				if (ts.getSet().size() > threshold) {
					int op = ts.getTypeAmount();
					switch (op) {
					case 1: { // Caso em que Tokenset tem apenas 1 tipo de Token
						// Descobre qual e muda a expressao de acordo
						int ty = ts.getSetType();
						if (ty == Token.ALPHA) {
							regex += "[a-zA-Z]+";
						} else if (ty == Token.NUMERIC) {
							regex += "[\\d]+";
						} else { // ty == Token.SYMBOL
							regex += "[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += ']';
							regex = regex.replaceFirst("\\|\\]", "]");
						}
						break;
					} // fim case 1
					case 2: { // Caso com 2 tipos de Token distintos
						int types = ts.getTypes();
						switch (types) {
						case 3: // ALPHA e NUMERIC
							regex += "[\\w]+";
							break;
						case 4: // ALPHA e SYMBOL
							regex += "(?:[a-zA-Z]+|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += "])";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						case 5: // NUMERIC e SYMBOL
							regex += "(?:\\d+|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += "])";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						} // fim switch
						break;
					} // fim case 2
					case 3: { // Caso com 3 tipos distintos de Tokens
						regex += "(?:\\w+|[";
						// Adiciona Tokens de s�mbolos � express�o
						for (Token t : ts.getSet()) {
							if (t.getType() != Token.SYMBOL) {
								continue;
							}
							String tempStr = t.getToken();
							// Se for metacaractere escapa
							if (isMeta(tempStr)) {
								tempStr = "\\" + tempStr;
							}
							regex += tempStr + '|';
						}
						regex += "])";
						regex = regex.replaceFirst("\\|\\]", "]");
						break;
					} // fim case 3
					} // fim switch
				} else {
					// N�o h� necessidade de generalizar, apenas se repete cada
					// Token
					regex += "(";
					for (Token t : ts.getSet()) {
						String tempStr = t.getToken();
						// Se for metacaractere escapa
						if (isMeta(tempStr)) {
							tempStr = "\\" + tempStr;
						}
						regex += tempStr + '|';
					}
					regex += ')';
					regex = regex.replaceFirst("\\|\\)", ")");
				} // fim if-else
			} // fim else
		} // fim for
		regex = '^' + regex;
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		return p;
	} // fim inferGeneralRegex

	public Pattern inferGeneralRegexBounds() {
		String regex = new String();
		for (TokenSet ts : pattern) {
			// Repete Token
			if (ts.getSize() == 1) {
				String tempStr = ts.getSet().get(0).getToken();
				// Caso seja um s�mbolo
				if (isMeta(tempStr)) {
					tempStr = "\\" + tempStr;
				}
				regex += tempStr;
			} else if (ts.hasGap()) { // Tokenset com gap
				// Tokenset apenas com um Token opcional
				if (ts.getSize() - 1 == 1) {
					// Pega o Token, n�o o gap
					String temp = (ts.getSet().get(0).getType() == Token.GAP) ? ts
							.getSet().get(1).getToken()
							: ts.getSet().get(0).getToken();
					// Se o Token for maior que 1, � colocada num grupo
					if (temp.length() > 1) {
						temp = "(?:" + temp + ")?";
					} else {
						// Se for metacaractere escapar
						if (isMeta(temp)) {
							temp = "\\" + temp;
						}
						temp = temp + "?";
					}
					regex += temp;
				} else if (ts.getSet().size() > threshold) {
					// Tamanho de Tokenset maior que limiar, ent�o � necess�rio
					// generalizar Tokenset
					int op = ts.getTypeAmount();
					switch (op) {
					case 1: { // Caso em que Tokenset tem apenas 1 tipo de Token
						// Descobre qual e muda a expressao de acordo
						int ty = ts.getSetType();
						if (ty == Token.ALPHA) {
							regex += "[a-zA-Z]{0," + ts.getMaxLength() + "}";
						} else if (ty == Token.NUMERIC) {
							regex += "[\\d]{0," + ts.getMaxLength() + "}";
						} else { // ty == Token.SYMBOL
							regex += "[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr;
							}
							regex += "]?";
						}
						break;
					} // fim case 1
					case 2: { // Caso com 2 tipos de Token distintos
						int types = ts.getTypes();
						switch (types) {
						case 3: // ALPHA e NUMERIC
							regex += "[\\w]{0," + ts.getMaxLength() + "}";
							break;
						case 4: // ALPHA e SYMBOL
							regex += "(?:[a-zA-Z]{0," + ts.getMaxLength()
									+ "}|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.GAP) {
									if (t.getType() != Token.SYMBOL) {
										continue;
									}
									String tempStr = t.getToken();
									// Se for metacaractere escapa
									if (isMeta(tempStr)) {
										tempStr = "\\" + tempStr;
									}
									regex += tempStr + '|';
								}
							}
							regex += "]?)";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						case 5: // NUMERIC e SYMBOL
							regex += "(?:\\d{0," + ts.getMaxLength() + "}|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += "]?)";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						} // fim switch
						break;
					} // fim case 2
					case 3: { // Caso com 3 tipos distintos de Tokens
						regex += "(?:\\w{0," + ts.getMaxLength() + "}|[";
						// Adiciona Tokens de s�mbolos � express�o
						for (Token t : ts.getSet()) {
							if (t.getType() != Token.SYMBOL) {
								continue;
							}
							String tempStr = t.getToken();
							// Se for metacaractere escapa
							if (isMeta(tempStr)) {
								tempStr = "\\" + tempStr;
							}
							regex += tempStr + '|';
						}
						regex += "]?)";
						regex = regex.replaceFirst("\\|\\]", "]");
						break;
					} // fim case 3
					} // fim switch
				} else {
					// N�o h� necessidade de generalizar, apenas se repete cada
					// Token
					regex += "(";
					for (Token t : ts.getSet()) {
						if (t.getType() != Token.GAP) {
							String tempStr = t.getToken();
							// Se for metacaractere escapa
							if (isMeta(tempStr)) {
								tempStr = "\\" + tempStr;
							}
							regex += tempStr + '|';
						}
					}
					regex += ")?";
					regex = regex.replaceFirst("\\|\\)", ")");
				} // fim if-else
			} else { // Tokenset sem gap
				// Tamanho de Tokenset maior que limiar, ent�o � necess�rio
				// generalizar Tokenset.
				if (ts.getSet().size() > threshold) {
					int op = ts.getTypeAmount();
					switch (op) {
					case 1: { // Caso em que Tokenset tem apenas 1 tipo de Token
						// Descobre qual e muda a expressao de acordo
						int ty = ts.getSetType();
						if (ty == Token.ALPHA) {
							regex += "[a-zA-Z]{" + ts.getMinLength() + ","
									+ ts.getMaxLength() + "}";
						} else if (ty == Token.NUMERIC) {
							regex += "[\\d]{" + ts.getMinLength() + ","
									+ ts.getMaxLength() + "}";
						} else { // ty == Token.SYMBOL
							regex += "[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += ']';
							regex = regex.replaceFirst("\\|\\]", "]");
						}
						break;
					} // fim case 1
					case 2: { // Caso com 2 tipos de Token distintos
						int types = ts.getTypes();
						switch (types) {
						case 3: // ALPHA e NUMERIC
							regex += "[\\w]{" + ts.getMinLength() + ","
									+ ts.getMaxLength() + "}";
							break;
						case 4: // ALPHA e SYMBOL
							regex += "(?:[a-zA-Z]{" + ts.getMinLength() + ","
									+ ts.getMaxLength() + "}|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += "])";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						case 5: // NUMERIC e SYMBOL
							regex += "(?:\\d{" + ts.getMinLength() + ","
									+ ts.getMaxLength() + "}|[";
							// Adiciona Tokens de s�mbolos � express�o
							for (Token t : ts.getSet()) {
								if (t.getType() != Token.SYMBOL) {
									continue;
								}
								String tempStr = t.getToken();
								// Se for metacaractere escapa
								if (isMeta(tempStr)) {
									tempStr = "\\" + tempStr;
								}
								regex += tempStr + '|';
							}
							regex += "])";
							regex = regex.replaceFirst("\\|\\]", "]");
							break;
						} // fim switch
						break;
					} // fim case 2
					case 3: { // Caso com 3 tipos distintos de Tokens
						regex += "(?:\\w{" + ts.getMinLength() + ","
								+ ts.getMaxLength() + "}|[";
						// Adiciona Tokens de s�mbolos � express�o
						for (Token t : ts.getSet()) {
							if (t.getType() != Token.SYMBOL) {
								continue;
							}
							String tempStr = t.getToken();
							// Se for metacaractere escapa
							if (isMeta(tempStr)) {
								tempStr = "\\" + tempStr;
							}
							regex += tempStr + '|';
						}
						regex += "])";
						regex = regex.replaceFirst("\\|\\]", "]");
						break;
					} // fim case 3
					} // fim switch
				} else {
					// N�o h� necessidade de generalizar, apenas se repete cada
					// Token
					regex += "(";
					for (Token t : ts.getSet()) {
						String tempStr = t.getToken();
						// Se for metacaractere escapa
						if (isMeta(tempStr)) {
							tempStr = "\\" + tempStr;
						}
						regex += tempStr + '|';
					}
					regex += ')';
					regex = regex.replaceFirst("\\|\\)", ")");
				} // fim if-else
			} // fim else
		} // fim for
		regex = '^' + regex;
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		return p;
	} // fim inferGeneralRegexBounds

	public PathMatrix createPathMatrix(ConsensusPattern cp2) {
		PathMatrix pm = new PathMatrix(this, cp2);
		return pm;
	}

	public void print() {
		for (TokenSet t : pattern) {
			t.print(true);
		}
		System.out.println();
	}

	public TokenSet tokenSetAt(int index) {
		return pattern.get(index);
	}

	public ArrayList<TokenSet> getPattern() {
		return pattern;
	}

	// Retorna o comprimento do maior TokenSet em pattern
	public int getBiggestTokenSetSize() {
		int size = 0;
		for (TokenSet ts : pattern) {
			if (ts.getSize() > size)
				size = ts.getSize();
		}
		return size;
	}

	/* Returns longest TokenSet length */
	public int getLongestTokenSetLength() {
		int length = 0;
		for (TokenSet ts : pattern) {
			if (length < ts.getTokensLength())
				length = ts.getTokensLength();
		}
		return length;
	} /* End getLongestTokenSetLength */

	public int size() {
		return pattern.size();
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

}
