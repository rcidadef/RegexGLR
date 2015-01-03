package br.edu.ufam.model;

import java.io.FileNotFoundException;
import java.util.Formatter;

public class PathMatrix {
	private int[][] matrix;
	private ConsensusPattern x;
	private ConsensusPattern y;
	private int score;
	private Formatter output;
	private boolean print;

	// Maior entre tres numeros
	private int max(int a, int b, int c) {
		return Math.max(Math.max(a, b), c);
	}

	// Construtor padrao
	public PathMatrix() {
	};

	// Construtor que mota matrix e calcula pontuacao de similaridade entre
	// dois ConsensusPattern
	public PathMatrix(ConsensusPattern p1, ConsensusPattern p2) {
		this(p1, p2, false);
	}

	// Imprime
	public PathMatrix(ConsensusPattern p1, ConsensusPattern p2, boolean print) {
		x = p1;
		y = p2;
		this.print = print;
		buildMatrix();
	}

	// Score function para Tokensets
	private int sf(TokenSet ts1, TokenSet ts2, int i, int j) {
		int sc = -1;
		if (ts1.sameToken(ts2)) {
			sc = (i == j) ? 7 : 5;
		} else if (ts1.sameType(ts2)) {
			sc = 1;
		}
		return sc;
	}

	// Constroi a matrix de caminhamento para um par de ConsensusPattern
	public void buildMatrix() {
		if (x == null || y == null)
			return;
		matrix = new int[x.size() + 1][y.size() + 1];
		for (int i = 1; i < x.size() + 1; ++i) {
			for (int j = 1; j < y.size() + 1; ++j) {
				matrix[i][j] = max(
						sf(x.tokenSetAt(i - 1), y.tokenSetAt(j - 1), i, j)
								+ matrix[i - 1][j - 1], matrix[i - 1][j],
						matrix[i][j - 1]);
			}
		}
		score = matrix[x.size()][y.size()];
	} /* End buildMatrix */

	public void buildMatrix(ConsensusPattern x, ConsensusPattern y) {
		this.x = x;
		this.y = y;
		buildMatrix();
	} /* End buildMatrix */

	// Funcao que alinha dois ConsensusPattern, inserindo gaps entre eles
	public void align() {
		int i = x.size();
		int j = y.size();

		while (i != 0 || j != 0) {
			if (j != 0 && matrix[i][j] == matrix[i][j - 1]) {
				x.getPattern().add(i, new TokenSet(new Token("/gap/")));
				j--;
			} else if (i != 0 && matrix[i][j] == matrix[i - 1][j]) {
				y.getPattern().add(j, new TokenSet(new Token("/gap/")));
				i--;
			} else {
				i--;
				j--;
			}
		}

		if (print) {
			x.print();
			y.print();
		}
	} /* End align */

	// Funde dois ConsensusPattern de tamanho iguais e o retorna o
	// ConsensusPattern resultante
	public ConsensusPattern merge() throws Exception {
		ConsensusPattern mc = new ConsensusPattern();
		if (x.size() != y.size()) {
			throw new Exception("Sequencias com tamanhos diferentes!");
		}
		for (int i = 0; i < x.size(); ++i) {
			mc.getPattern().add(
					TokenSet.tokenSetUnion(x.getPattern().get(i), y
							.getPattern().get(i)));
		}
		return mc;
	}

	public int[][] getMatrix() {
		return matrix;
	}

	public int getScore() {
		return score;
	}

	public void print() {
		try {
			output = new Formatter("C:\\Users\\Roberto\\Desktop\\RegexOut.txt");
		} catch (FileNotFoundException e) {
			System.out.print("File not found\n");
			e.printStackTrace();
		}

		TokenSet ys = null;
		int fColW = y.getLongestTokenSetLength() + 1;

		// Imprime padrao das colunas
		System.out.printf("%" + fColW + "s    ", ' ');
		output.format("%" + fColW + "s    ", ' ');
		for (TokenSet ts : y.getPattern()) {
			ts.print(ts.getTokensLength() + 1);
			ts.print(output, ts.getTokensLength() + 1);
		}
		System.out.println();
		output.format("\n");

		// Imprime matriz e padrao das linhas
		for (int i = 0; i != matrix.length; ++i) {
			if (i > 0) {
				ys = x.tokenSetAt(i - 1);
				ys.print(fColW);
				ys.print(output, fColW);
			} else {
				System.out.printf("%" + fColW + "s ", ' ');
				output.format("%" + fColW + "s ", ' ');
			}

			for (int j = 0; j != matrix[i].length; j++) {
				if (j > 0) {
					System.out.printf("%"
							+ (y.tokenSetAt(j - 1).getTokensLength() + 1)
							+ "d ", matrix[i][j]);
					output.format("%"
							+ (y.tokenSetAt(j - 1).getTokensLength() + 1)
							+ "d ", matrix[i][j]);
				} else {
					System.out.printf("%2d ", matrix[i][j]);
					output.format("%2d ", matrix[i][j]);
				}
			}
			System.out.println();
			output.format("\n");
		}

		output.close();
	}
}
