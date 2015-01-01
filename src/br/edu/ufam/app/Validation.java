package br.edu.ufam.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import br.edu.ufam.utility.Utility;

public class Validation {

	public static String normalize(String word) {

		return word.replaceAll("[/ ]", "_");

	}

	public static HashMap<String, Integer> readTemplate(String templatePath) {

		HashMap<String, Integer> template = new HashMap<String, Integer>();

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(templatePath),
						"UTF-8"))) {

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				line = Utility.removeAccents(line.toLowerCase());
				String[] lineArray = line.split("=");
				String key = normalize(lineArray[0]);
				String value = lineArray[1];

				template.put(key, getCategory(value));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return template;

	}

	private static Integer getCategory(String value) {
		String categories[] = new String[] { "categorico", "multicategorico",
				"numerico" };
		for (int i = 0; i < categories.length; ++i) {
			if (value.equals(categories[i]))
				return i + 1;
		}
		return 0;
	}

	public static void main(String[] args) {

		HashMap<String, Integer> tvTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_tvs.txt");
		HashMap<String, Integer> smartphoneTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_smartphones.txt");
		HashMap<String, Integer> notebookTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_notebooks.txt");
		HashMap<String, Integer> cameraTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_cameras.txt");
		HashMap<String, Integer> filmadoraTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_filmadoras.txt");

		String classDirPath = "./redatasetsexpressaoregular/cameras/";

		File file = new File(classDirPath);

		int i = 1;
		for (File f : file.listFiles()) {
			System.out.println(i + " : " + f + "_" + f.getName());
			System.out.println(cameraTemplate.get(f.getName().substring(0,
					f.getName().length() - 4)));
			i++;
			
			System.out.println(RegexGLR.createRegex(f.getAbsolutePath()));
		}

	}

}