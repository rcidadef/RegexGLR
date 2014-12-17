package br.edu.ufam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

public class RegexGLR {
	static String[] nrfiles = {
			"C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\norepetition\\corapub.personalBib.noRepetition.kb.xml",
			"C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\norepetition\\ads.folhaOnline.noRepetition.kb.xml",
			"C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\norepetition\\hotels.noRepetition.kb.xml" };

	private static String getTag(String str) {
		return str.substring(0, str.indexOf('>') + 1);
	}

	private static ArrayList<ArrayList<String>> getAttribGroups(String file) {
		String tag = "";
		String text = "";
		BufferedReader br = null;
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<ArrayList<String>> aas = new ArrayList<ArrayList<String>>();

		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file), "UTF-8"));

			text = br.readLine();
			br.mark(100000);
			text = br.readLine();
			tag = getTag(text);
			br.reset();

			while ((text = br.readLine()) != null) {
				if (getTag(text).equals(tag) == false) {
					tag = getTag(text);
					aas.add(as);
					as = new ArrayList<String>();
				}
				as.add(new String(text.substring(0, text.length())));
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return aas;
	}

	// Remove linhas repetidas de uma arquivo
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
	}

	public static void foo(String file, int i) {
		ArrayList<ArrayList<String>> aas = getAttribGroups(file);
		ConsensusPattern p;
		ArrayList<String> a = aas.get(i);
		ArrayList<ConsensusPattern> pl = new ArrayList<ConsensusPattern>(
				a.size());
		DistanceMatrix dm;
		for (String s : a) {
			pl.add(new ConsensusPattern(s));
		}
		dm = new DistanceMatrix(pl);
		p = dm.getConsensusPattern();
		Pattern pat = p.inferGeneralRegex();
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
		System.out.println((n * 1. / a.size()) * 100);
	}

	public static void main(String[] args) {
		Experimentador exp;
		for (String file : nrfiles) {
			ArrayList<ArrayList<String>> aas = getAttribGroups(file);
			exp = new Experimentador(aas, .5f, 5, true, file.substring(0, 40)
					+ file.substring(40, file.indexOf("xml")) + "Results.txt");
			exp.experimentarCruzado();
		}
	}
}