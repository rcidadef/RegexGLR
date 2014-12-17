package br.edu.ufam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexGLRTester {
	// Retorna a tag de um atributo
	private static String getTag(String str) {
		return str.substring(0, str.indexOf('>') + 1);
	}

	// Retorna um ArrayList de ArrayLists de strings contendo as ocorrencias de
	// cada atributo distinto
	@SuppressWarnings("unused")
	private static ArrayList<ArrayList<String>> getAttribGroups(String file) {
		return getAttribGroups(file, false);
	}

	// Retorna ArrayList de ArrayLists de strings contendos os valores de
	// cada atributo distinto numa KB
	private static ArrayList<ArrayList<String>> getAttribGroups(String file,
			boolean noTags) {
		String tag = "";
		String text = "";
		BufferedReader br = null;
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<ArrayList<String>> aas = new ArrayList<ArrayList<String>>();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));

			// Descarta <kb>
			text = br.readLine();
			br.mark(100000);
			text = br.readLine();
			tag = getTag(text);
			br.reset();

			if (noTags) {
				// Le linhas do arquivo
				while ((text = br.readLine()) != null
						&& (text.equals("</kb>") == false)) {
					// Aloca um novo ArrayList caso seja novo atributo
					if (getTag(text).equals(tag) == false) {
						tag = getTag(text);
						aas.add(as);
						as = new ArrayList<String>();
					}
					// Retira tags
					String[] strs = text.split("<[^>]*>");
					text = strs[1];
					as.add(new String((String) text.toString()));
				}
			} else {
				// Le linhas do arquivo
				while ((text = br.readLine()) != null
						&& (text.equals("</kb>") == false)) {
					// Aloca um novo ArrayList caso seja novo atributo
					if (getTag(text).equals(tag) == false) {
						tag = getTag(text);
						aas.add(as);
						as = new ArrayList<String>();
					}
					as.add(new String((String) text.toString()));
				}
			}
			aas.add(as);
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return aas;
	} // fim getAttribGroups
	
	private static ArrayList<ConsensusPattern> getAttribFile(String file) {
		String text = "";
		BufferedReader br = null;
		ArrayList<ConsensusPattern> as = new ArrayList<ConsensusPattern>();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));
			while ((text = br.readLine()) != null) {
				as.add(new ConsensusPattern(new String((String) text.toString())));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return as;
	} // fim getAttribGroups
	
	public static String runFromFile(String file) {
		ArrayList<ConsensusPattern> cal = null;
		DistanceMatrix dm = null;
		ConsensusPattern p = null;
		cal = getAttribFile(file);
		dm = new DistanceMatrix(cal);
		p = dm.getConsensusPattern();
		p.print();
		Pattern pat = p.inferGeneralRegex();
		return pat.toString();
	}

	@SuppressWarnings("unused")
	private static String getPosAnchor(String file, String tag) {
		String text = "";
		BufferedReader br = null;
		BufferedWriter bw = null;
		ArrayList<String> as = new ArrayList<String>();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));

			// Le linhas do arquivo
			while ((text = br.readLine()) != null
					&& (text.equals("</kb>") == false)) {
				if (text.matches("</pos>")) {
					br.readLine();
					as.add(br.readLine().split("<[^>]*>")[1]);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			file = "\\." + tag + ".txt";
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));
			bw.write("<kb>");
			for (String s : as) {
				bw.write('<' + tag + '>' + s + "</" + tag + ">\n");
			}
			bw.write("</kb>");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		removeRepeatitedValues(".\\" + file);

		return file;
	} // fim getPosAnchor

	// Remove linhas repetidas de um arquivo
	private static void removeRepeatitedValues(String file) {
		String buffer = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		HashSet<String> lines = new HashSet<String>();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));
			while ((buffer = br.readLine()) != null) {
				lines.add(buffer);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.substring(0, file.length() - 7)
							+ ".noRepetition.kb.xml"), "UTF-8"));
			for (String line : lines) {
				bw.write(line + '\n');
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // fim removeRepeatedValues

	// Remove linhas repetidas de uma arquivo
	@SuppressWarnings("unused")
	private static void removeRepetitions(String file) {
		String tag = "";
		String buffer = null;
		BufferedReader br = null;
		BufferedWriter bw = null;
		HashSet<String> lines = null;
		ArrayList<HashSet<String>> atts = new ArrayList<HashSet<String>>();
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));
			while ((buffer = br.readLine()) != null) {
				if (tag.equals(getTag(buffer)) == false) {
					tag = getTag(buffer);
					lines = new HashSet<String>();
					atts.add(lines);
				}
				lines.add(buffer);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file.substring(0, file.length() - 7)
							+ ".noRepetition.kb.xml"), "UTF-8"));
			for (HashSet<String> hs : atts) {
				for (String line : hs) {
					bw.write(line + '\n');
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // fim removeRepetitions

	// Recebe ArrayList de strings e retorna uma expressao regular resultante do
	// alinhamento multiplo dessas strings
	public static String getRegexString(ArrayList<String> stringArray) {
		ConsensusPattern p;
		DistanceMatrix dm;
		ArrayList<ConsensusPattern> patternArray = new ArrayList<ConsensusPattern>(
				stringArray.size());
		for (String s : stringArray) {
			patternArray.add(new ConsensusPattern(s));
		}
		dm = new DistanceMatrix(patternArray);
		p = dm.getConsensusPattern();
		Pattern pat = p.inferGeneralRegex();
		return pat.toString();
	} // fim getRegexString

	// Retorna uma expressao regular resultante do alinhamento das Strings em
	// stringArray
	public static Pattern getRegexPattern(ArrayList<String> stringArray) {
		return getRegexPattern(stringArray, 3);
	} // fim getRegexPattern

	public static Pattern getRegexPattern(ArrayList<String> stringArray,
			int threshold) {
		ConsensusPattern p;
		DistanceMatrix dm;
		ArrayList<ConsensusPattern> patternArray = new ArrayList<ConsensusPattern>(
				stringArray.size());
		for (String s : stringArray) {
			patternArray.add(new ConsensusPattern(s));
		}
		dm = new DistanceMatrix(patternArray);
		p = dm.getConsensusPattern();
		p.setThreshold(threshold);
		Pattern pat = p.inferGeneralRegexBounds();
		return pat;
	} // fim getRegexPattern

	// Retorna expressao regular de arquivo
	public static Pattern getRegexPatternFile(String file) {
		ConsensusPattern p;
		ArrayList<ArrayList<String>> aas = getAttribGroups(file, true);
		ArrayList<String> a = aas.get(0);
		ArrayList<ConsensusPattern> pl = new ArrayList<ConsensusPattern>(
				a.size());
		DistanceMatrix dm;
		for (String s : a) {
			pl.add(new ConsensusPattern(s));
		}
		dm = new DistanceMatrix(pl);
		p = dm.getConsensusPattern();
		return p.inferGeneralRegex();
	} // fim getRegexPatternFile

	// Escolha porcentagem perc de Strings de stringArray e os retorna
	public static ArrayList<String> getNRandom(ArrayList<String> stringArray,
			float perc) {
		int randomIndex = 0;
		int amount = (int) (stringArray.size() * perc);
		HashSet<String> selectedStringsSet = new HashSet<String>(amount);
		ArrayList<String> selectedStrings = new ArrayList<String>(amount);
		while (selectedStringsSet.size() < amount) {
			randomIndex = (int) (Math.random() * (stringArray.size()));
			selectedStringsSet.add(stringArray.get(randomIndex));
		}
		selectedStrings.addAll(selectedStringsSet);
		return selectedStrings;
	} // fim getNRandom

	// Testa algoritmo de geracao de expressoes. Verifica se ele casa com o
	// conjunto de atributos que foi usado para treina-lo
	public static void testRegexGenerator(String file) {
		ArrayList<ArrayList<String>> stringArrayArray = getAttribGroups(file,
				true);
		ConsensusPattern p;
		for (ArrayList<String> a : stringArrayArray) {
			ArrayList<ConsensusPattern> pl = new ArrayList<ConsensusPattern>(
					a.size());
			DistanceMatrix dm;
			for (String s : a) {
				pl.add(new ConsensusPattern(s));
			}
			dm = new DistanceMatrix(pl);
			p = dm.getConsensusPattern();
			Pattern pat = p.inferGeneralRegexBounds();
			int n = 0;
			System.out.println("Using regex: " + pat.toString());
			for (String s : a) {
				if (pat.matcher(TokenSet.stripAccents(s)).matches()) {
					n++;
					System.out.println("Matched string: " + s);
				} else {
					System.out.println("Did not match string: " + s);
				}
			}
			System.out.println((n * 1. / a.size()) * 100 + " %");
		}
	} // testRegexGenerator

	// Roda experimento, porem sem gabarito
	public static void runExperiment(String file, float perc) {
		runExperiment(file, null, perc);
	}

	// Roda experimento de IETS em file, treinando com perc da KB do atributo
	// que se quer extrair
	public static void runExperiment(String file, String gabarito, float perc) {
		int correctMatches = 0;
		int incorrectMatches = 0;
		int tNumberOfMatches = 0;
		String emailsPath = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\straw\\";
		ArrayList<String> gabArray = null;

		if (gabarito != null) {
			gabArray = getAttribGroups(gabarito, true).get(0);
		}

		File emails = new File(emailsPath);

		ArrayList<String> a = getAttribGroups(file, true).get(0);
		a = getNRandom(a, perc);
		Pattern p = getRegexPattern(a);
		p = Pattern.compile(p.toString().substring(1));

		System.out.println("Regex used: " + p.toString());
		for (String f : emails.list()) {
			Scanner scanner = null;
			try {
				scanner = new Scanner(new File(emailsPath + f), "UTF-8");
				String text = scanner.useDelimiter("\\A").next();

				Matcher matcher = p.matcher(text);
				System.out.println("Matches on: " + f);
				while (matcher.find()) {
					System.out.print("\t"
							+ text.substring(matcher.start(), matcher.end()));
					if (gabArray != null) {
						if (gabArray.indexOf(text.substring(matcher.start(),
								matcher.end())) > -1) {
							System.out.println("  ->  correct");
							correctMatches++;
						} else {
							System.out.println("  ->  incorrect");
							incorrectMatches++;
						}
					} else {
						System.out.println();
					}
					tNumberOfMatches++;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				scanner.close();
			}
		}

		if (gabArray != null) {
			System.out.println("Numero de casamentos corretos: "
					+ correctMatches);
			System.out.println("Numero de casamentos corretos: "
					+ incorrectMatches);
			System.out.printf("Precisao %.4f %%\n",
					(correctMatches * 100. / tNumberOfMatches));
			System.out.printf("Revocacao %.4f %%\n",
					(correctMatches * 100. / gabArray.size()));
		}
		System.out.println("Total de casamentos : " + tNumberOfMatches);
	} // fim runExperiment

	// Roda experimento de IETS em file, treinando com perc da KB do atributo
	// que se quer extrair
	public static void runExperiment2(String trainingFile, String targetFile,
			float perc) {
		int correctMatches = 0;
		int tNumberOfMatches = 0;
		int tCorrectMatches = 0;
		String line = null;
		String anchor = null;
		String left = null;
		String right = null;
		String segment = null;
		BufferedReader br = null;
		Formatter formatter = null;

		// Treina utilizando arquivo de treino e gera expressao regular
		ArrayList<String> a = getAttribGroups(trainingFile, true).get(0);
		a = getNRandom(a, perc);
		Pattern p = getRegexPattern(a);
		p = Pattern.compile(p.toString().substring(1));
		System.out.println("Regex used: " + p.toString());

		try {
			String match = null;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					targetFile), "UTF-8"));

			while ((line = br.readLine()) != null) {
				if (line.matches("^<pos>$")) {
					left = br.readLine().split("</?left>")[1];
					right = br.readLine().split("</?right>")[1];
					br.readLine();
					br.readLine();
					anchor = br.readLine().split("</?anchor>")[1];
					segment = left + anchor + right;

					Matcher matcher = p.matcher(segment);
					System.out.println("Matches on: " + segment);
					while (matcher.find()) {
						match = segment.substring(matcher.start(),
								matcher.end());
						if (match.equals(anchor)) {
							System.out.println("\t" + "correct: " + match);
							correctMatches++;
						} else {
							System.out.println("\t" + "incorrect: " + match);
						}
						tNumberOfMatches++;
					}
					tCorrectMatches++;
				}
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			formatter = new Formatter("C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\Results_" + trainingFile.split("experiment\\\\|\\.xml")[1]);
			formatter.format("Precisao: %.4f %%\n", correctMatches * 1.f
					/ tNumberOfMatches);
			formatter.format("Revocacao : %.4f %%\n", correctMatches * 1.f / tCorrectMatches);
			formatter.format("Total de casamentos : %d", tNumberOfMatches);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		formatter.close();

		System.out.printf("Precisao: %.4f\n", correctMatches * 1.f
				/ tNumberOfMatches);
		System.out.printf("Revocacao : %.4f\n", correctMatches * 1.f / tCorrectMatches);
		System.out.println("Total de casamentos : " + tNumberOfMatches);
	} // fim runExperiment2

	// Roda todos as tarefas de extracao coursenum, phonenum, softwarename e
	// urls
	public static void runExtraction(float perc) {
		String file;
		String gabarito = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\software_experiment\\gabarito.txt";

		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\software_experiment\\software.xml";
		System.out.println("\nSoftware name");
		runExperiment(file, gabarito, perc);

		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\phonenum_experiment\\phonenum.xml";
		System.out.println("\nPhone number");
		runExperiment(file, perc);

		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\coursenum_experiment\\coursenum.xml";
		System.out.println("\nCourse Number");
		runExperiment(file, perc);

		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\urls_experiment\\urls.xml";
		System.out.println("\nURLs");
		runExperiment(file, perc);
	} // fim runExtraction

	public static void runExtraction2(float perc) {
		String file = null;
		String tFile = null;

		tFile = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\softwarename\\softwarename.label";
		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\software_experiment\\software.xml";
		System.out.println("\nSoftware name");
		runExperiment2(file, tFile, perc);

		tFile = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\phonenum\\phonenum.label";
		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\phonenum_experiment\\phonenum.xml";
		System.out.println("\nPhone number");
		runExperiment2(file, tFile, perc);

		tFile = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\coursenum\\coursenum.label";
		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\coursenum_experiment\\coursenum.xml";
		System.out.println("\nCourse Number");
		runExperiment2(file, tFile, perc);

		tFile = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\urls\\urls.label";
		file = "C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\tasks\\urls_experiment\\urls.xml";
		System.out.println("\nURLs");
		runExperiment2(file, tFile, perc);
	} // fim runExtraction2
}