package br.edu.ufam.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Utility {

	public static ArrayList<String> readFile(String filePath) {

		ArrayList<String> lines = new ArrayList<String>();

		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath), "UTF-8"))) {

			String buffer = null;

			while ((buffer = br.readLine()) != null) {

				lines.add(buffer);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return lines;

	} /* End readFile */

	public static String removeAccents(String word) {
		String[] regexes = new String[] { "[áàâãä]", "[éèêẽë]", "[íìîĩï]",
				"[óòôõö]", "[úùûũü]", "[ç]" };
		String[] replacements = new String[] { "a", "e", "i", "o", "u", "c" };

		for (int i = 0; i < regexes.length; ++i) {
			word = word.replaceAll(regexes[i], replacements[i]);
		}

		return word;
	} /* End removeAccents */

}
