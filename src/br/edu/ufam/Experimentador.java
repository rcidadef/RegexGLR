package br.edu.ufam;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Experimentador {
	int times;
	float percentage;
	boolean print;
	String strings[];
	BufferedWriter bw;
	ArrayList<ArrayList<String>> stringMat;

	// Construtores para experimentos simples
	public Experimentador(String in[], float perc, int times, boolean print) {
		strings = in;
		this.percentage = perc;
		this.times = times;
		this.print = print;
	}
	
	public Experimentador(ArrayList<String> in, float perc, int times, boolean print) {
		this(in.toArray(new String[0]), perc, times, print);
	}

	// Construtor para experimentos cruzados
	public Experimentador(ArrayList<ArrayList<String>> in, float perc,
			int times, boolean print, String out) {
		stringMat = in;
		this.percentage = perc;
		this.times = times;
		this.print = print;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(out), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Seleciona uma porcao aleatoria da colecao sem repeticao
	private int[] selectStrings(ArrayList<ConsensusPattern> csList) {
		int amount = (int) (strings.length * percentage);
		int randomNumber;
		int randomNumbers[] = new int[strings.length];
		for (int i = 0; i < amount; ++i) {
			randomNumber = (int) (strings.length * Math.random());
			if (randomNumbers[randomNumber] == 1) {
				while (randomNumbers[randomNumber] == 1) {
					randomNumber = (int) (amount * Math.random());
				}
			}
			csList.add(new ConsensusPattern(strings[randomNumber]));
			randomNumbers[randomNumber] = 1;
		}
		return randomNumbers;
	}

	// Seleciona uma das metades da colecao
	private int[] selectStrings(ArrayList<ConsensusPattern> csList, boolean half) {
		if (percentage != .5) {
			return selectStrings(csList);
		}
		int begin;
		int end;
		int selectedStrings[] = new int[strings.length];
		if (half == true) {
			begin = 0;
			end = (int) (strings.length / 2);
		} else {
			begin = (int) (strings.length / 2);
			end = strings.length;
		}
		for (int i = begin; i < end; ++i) {
			csList.add(new ConsensusPattern(TokenSet.stripAccents(strings[i])));
			selectedStrings[i] = 1;
		}
		return selectedStrings;
	}

	public float experimentar() {
		int numberOfMatches = 0;
		int selectedStrings[] = null;
		long avgTime = 0;
		long executionTimes[] = new long[times];
		float success = 0;
		float avgSuccess = 0;
		float results[] = new float[times];
		ConsensusPattern cs = null;
		ArrayList<ConsensusPattern> csList = new ArrayList<ConsensusPattern>(
				strings.length);
		DistanceMatrix dm = null;

		for (int i = 0; i < times; ++i) {
			// Escolha de porcentagem de colecao a ser usada como treinamento
			// case 1: primeira metade da colecao
			// case 2: segunda metade da colecao
			// padrao: metades aleatorias sem repeticao
			switch (i) {
			case 0:
				selectedStrings = selectStrings(csList, true);
				break;
			case 1:
				selectedStrings = selectStrings(csList, false);
				break;
			default:
				selectedStrings = selectStrings(csList);
			}

			// Calculo de matrix de distancias, de arvore guia e de
			// ConsensusPattern final
			dm = new DistanceMatrix(csList);
			System.out.print("learning regex... ");
			cs = dm.getConsensusPattern();

			// Gera expressao regular
			Pattern p = cs.inferGeneralRegex();
			System.out.print("regex ready! ");
			numberOfMatches = 0;

			// Testa expressao regular para toda a colecao
			// e conta numero de casamentos
			System.out.print("testing... ");
			executionTimes[i] = System.currentTimeMillis();
			if (print) {
				System.out.println("Using regex:");
				System.out.println(p.toString());
				for (int j = 0; j < strings.length; ++j) {
					if (selectedStrings[j] == 0) {
						if (p.matcher(TokenSet.stripAccents(strings[j]))
								.matches()) {
							System.out
									.println("Matched String : " + strings[j]);
							numberOfMatches++;
						} else {
							System.out.println("Did not match String : "
									+ strings[j]);
						}
					}
				}
				System.out.println("Fim loop " + (i + 1) + "...\n\r\n\r");
			} else {
				for (int j = 0; j < strings.length; ++j) {
					if (selectedStrings[j] == 0) {
						if (p.matcher(TokenSet.stripAccents(strings[j]))
								.matches()) {
							numberOfMatches++;
						}
					}
				}
			}
			executionTimes[i] = System.currentTimeMillis() - executionTimes[i];
			avgTime += executionTimes[i];
			System.out.println("finished tests!");

			// Porcentagem de sucesso
			float testAmount = strings.length
					- ((int) (strings.length * percentage));
			success = (float) ((numberOfMatches * 1.) / testAmount) * 100;
			avgSuccess += success;
			results[i] = success;
		}
		// Calcula taxa de sucesso médio
		avgSuccess /= times;
		avgTime /= times;

		// Impressao de resultados e de valores dos parametros
		System.out.println("Percentual de treinamento : "
				+ (int) (percentage * 100) + "%");
		System.out.println("Tamanho de treinamento : "
				+ ((int) (strings.length * percentage)));
		System.out.println("Numero de testes : " + times);
		// Taxa de sucesso de cada amostra
		for (int i = 0; i < times; ++i) {
			System.out.println("Sucesso amostra " + (i + 1) + " : "
					+ results[i] + "% tempo: " + executionTimes[i] + " ms");
		}
		// Taxa de sucesso médio
		System.out.println("Sucesso médio : " + avgSuccess + "% tempo médio: "
				+ avgTime + " ms");

		return avgSuccess;
	}

	public static String removeTags(String s) {
		return s.split("<|>")[2];
	}

	public static String removeTags(String[] strs) {
		String tag = strs[0].split("<|>")[1];
		for (int i = 0; i < strs.length; ++i) {
			strs[i] = removeTags(strs[i]);
		}
		return tag;
	}

	public static String removeTags(ArrayList<String> strs) {
		String tag = strs.get(0).split("<|>")[1];
		for (int i = 0; i < strs.size(); ++i) {
			strs.set(i, removeTags(strs.get(i)));
		}
		return tag;
	}

	// Treina em porcentagem dos atributos em att1 e testa expressao regular
	// aprendida em att2
	public float cruzar(String[] att1, String[] att2, String name1, String name2) {
		int numberOfMatches = 0;
		long avgTime = 0;
		long executionTimes[] = new long[times];
		float success = 0;
		float avgSuccess = 0;
		float results[] = new float[times];
		ConsensusPattern cs = null;
		ArrayList<ConsensusPattern> csList = new ArrayList<ConsensusPattern>(
				att1.length);
		DistanceMatrix dm = null;

		// Atributo strings recebe o vetor que sera usado para treinamento
		strings = att1;
		// Remove tags de todos as Strings no vetor
		// removeTags(att1);
		// removeTags(att2);

		for (int i = 0; i < times; ++i) {
			// Escolha de porcentagem de colecao a ser usada como treinamento
			// i = 0: primeira metade da colecao
			// i = 1: segunda metade da colecao
			// caso contrario: metades aleatorias sem repeticao
			if (i > 1) {
				selectStrings(csList);
			} else {
				selectStrings(csList, (i == 0) ? true : false);
			}

			// Calculo de matrix de distancias, de arvore guia e de
			// ConsensusPattern final
			dm = new DistanceMatrix(csList);
			System.out.print("learning regex... ");
			cs = dm.getConsensusPattern();

			// Gera expressao regular
			Pattern p = cs.inferGeneralRegex();
			System.out.print("regex ready! ");
			numberOfMatches = 0;
			if (bw != null) {
				try {
					bw.write("regex used: " + p.toString() + '\n');
				} catch(IOException e) {
					e.printStackTrace();
				}
			}

			// Testa expressao regular para att2
			// e conta numero de casamentos
			System.out.print("testing... ");
			executionTimes[i] = System.currentTimeMillis();
			if (print) {
				System.out.println("Using regex:");
				System.out.println(p.toString());
				for (int j = 0; j < att2.length; ++j) {
					if (p.matcher(TokenSet.stripAccents(att2[j])).matches()) {
						System.out.println("Matched String : " + att2[j]);
						numberOfMatches++;
					} else {
						System.out.println("Did not match String : " + att2[j]);
					}
				}
				System.out.println("Fim loop " + (i + 1) + "...\n\r\n\r");
			} else {
				for (int j = 0; j < att2.length; ++j) {
					if (p.matcher(TokenSet.stripAccents(att2[j])).matches()) {
						numberOfMatches++;
					}
				}
			}
			executionTimes[i] = System.currentTimeMillis() - executionTimes[i];
			avgTime += executionTimes[i];
			System.out.println("finished tests!");

			// Porcentagem de sucesso
			float testAmount = att2.length;
			success = (float) ((numberOfMatches * 1.) / testAmount) * 100;
			avgSuccess += success;
			results[i] = success;
		}
		// Calcula taxa de sucesso médio
		avgSuccess /= times;
		avgTime /= times;

		// Impressao de resultados e de valores dos parametros
		System.out.println("Percentual de treinamento : "
				+ (int) (percentage * 100) + "%");
		System.out.println("Tamanho de treinamento : "
				+ ((int) (strings.length * percentage)));
		System.out.println("Numero de testes : " + times);
		System.out.println("Atributo de treino : " + name1);
		System.out.println("Atributo de teste : " + name2);
		// Taxa de sucesso de cada amostra
		for (int i = 0; i < times; ++i) {
			System.out.println("Sucesso amostra " + (i + 1) + " : "
					+ results[i] + "% tempo: " + executionTimes[i] + " ms");
		}
		// Taxa de sucesso médio
		System.out.println("Sucesso médio : " + avgSuccess + "% tempo médio: "
				+ avgTime + " ms");
		
		if (bw != null) {
			try {
				bw.write("Percentual de treinamento : "
						+ (int) (percentage * 100) + "%\n");
				bw.write("Tamanho de treinamento : "
						+ ((int) (strings.length * percentage)) + "\n");
				bw.write("Numero de testes : " + times + "\n");
				bw.write("Atributo de treino : " + name1 + "\n");
				bw.write("Atributo de teste : " + name2 + "\n");
				// Taxa de sucesso de cada amostra
				for (int i = 0; i < times; ++i) {
					bw.write("Sucesso amostra " + (i + 1) + " : "
							+ results[i] + "% tempo: " + executionTimes[i] + " ms\n");
				}
				// Taxa de sucesso médio
				bw.write("Sucesso médio : " + avgSuccess + "% tempo médio: "
						+ avgTime + " ms\n\n\r");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		strings = null;
		return avgSuccess;
	}

	public void experimentarCruzado() {
		String[] names = new String[stringMat.size()];
		String[] attribs;

		for (int i = 0; i < stringMat.size(); ++i) {
			names[i] = removeTags(stringMat.get(i));
		}

		for (int i = 0; i != stringMat.size(); ++i) {
			attribs = new String[stringMat.get(i).size()];
			stringMat.get(i).toArray(attribs);
			for (int j = 0; j != i; ++j) {
				String[] tattribs = new String[stringMat.get(j).size()];
				stringMat.get(j).toArray(tattribs);
				cruzar(attribs, tattribs, names[i], names[j]);
			}
			for (int j = i + 1; j != stringMat.size(); ++j) {
				String[] tattribs = new String[stringMat.get(j).size()];
				stringMat.get(j).toArray(tattribs);
				cruzar(attribs, tattribs, names[i], names[j]);
			}
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}