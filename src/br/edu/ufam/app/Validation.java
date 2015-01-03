package br.edu.ufam.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.edu.ufam.utility.Utility;

public class Validation {

	private static String[] categoriesName = { "multicategorico", "numerico",
			"dimensional" };

	public static String normalize(String word) {

		return word.replaceAll("[/ ]", "_");

	} /* End normalize */

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

	} /* End readTemplate */

	private static Integer getCategory(String value) {
		for (int i = 0; i < categoriesName.length; ++i) {
			if (value.equals(categoriesName[i]))
				return i + 1;
		}
		return 0;
	} /* End getCategory */

	public static void main(String[] args) {

		HashMap<String, Integer> tvTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_tvs.txt");
		HashMap<String, Integer> smartphoneTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_smartphones.txt");
		HashMap<String, Integer> notebookTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_notebooks.txt");
		HashMap<String, Integer> cameraTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_cameras.txt");
		HashMap<String, Integer> filmadoraTemplate = readTemplate("./redatasetsexpressaoregular/gabarito_filmadoras.txt");

		ArrayList<HashMap<String, Integer>> templates = new ArrayList<>();

		templates.add(tvTemplate);
		templates.add(smartphoneTemplate);
		templates.add(notebookTemplate);
		templates.add(cameraTemplate);
		templates.add(filmadoraTemplate);

		String[] classDirPaths = new String[] {
				"./redatasetsexpressaoregular/tvs/",
				"./redatasetsexpressaoregular/smartphones/",
				"./redatasetsexpressaoregular/notebooks/",
				"./redatasetsexpressaoregular/cameras/",
				"./redatasetsexpressaoregular/filmadoras/" };

		for (int i = 0; i < classDirPaths.length; ++i) {
			String classPath = classDirPaths[i];
			String[] regexes = generateRegexes(templates.get(i), classPath);

			System.out.println("Training on " + classPath);
			System.out.println("Generated regexes:");
			System.out.println("Multicategoricos: '" + regexes[0] + "'");
			System.out.println("Numericos: '" + regexes[1] + "'");
			System.out.println("Dimensionais: '" + regexes[2] + "'");

			for (int j = 0; j < classDirPaths.length; j++) {
				if (j != i) {
					validate(templates.get(j), classDirPaths[j], regexes);
				}
			}
		}

		return;

	}

	private static String[] generateRegexes(
			HashMap<String, Integer> trainingTemplate, String classDirPath) {
		File file = new File(classDirPath);

		ArrayList<String> multicategoricosValuesList = new ArrayList<String>();
		ArrayList<String> numericosValuesList = new ArrayList<String>();
		ArrayList<String> dimensionaisValuesList = new ArrayList<String>();

		for (File f : file.listFiles()) {
			String key = f.getName();
			key = key.substring(0, key.length() - 4);

			int category = trainingTemplate.get(key);

			if (category == 1) {
				multicategoricosValuesList.addAll(Utility.readFile(f
						.getAbsolutePath()));
			} else if (category == 2) {
				numericosValuesList
						.addAll(Utility.readFile(f.getAbsolutePath()));
			} else if (category == 3) {
				dimensionaisValuesList.addAll(Utility.readFile(f
						.getAbsolutePath()));
			}

		}

		String[] regexes = new String[3];

		regexes[0] = RegexGLR.createRegex(multicategoricosValuesList);
		regexes[1] = RegexGLR.createRegex(numericosValuesList);
		regexes[2] = RegexGLR.createRegex(dimensionaisValuesList);

		return regexes;
	} /* End generateRegexes */

	private static void validate(HashMap<String, Integer> testingTemplate,
			String attributePath, String[] regexes) {

		String multicategoricosRegex = regexes[0];
		String numericosRegex = regexes[1];
		String dimensionaisRegex = regexes[2];

		File file = new File(attributePath);

		System.out.println("Testing on: " + file.getName());

		int category = 0;

		for (File f : file.listFiles()) {
			String key = f.getName();
			key = key.substring(0, key.length() - 4);

			category = testingTemplate.get(key);

			if (category == 0) {
				System.out.format(
						"Invalid value for category (%1$d) with key '%2$s'.",
						category, key);
				System.exit(1);
			}

			System.out.println(f.getName() + " categoria: "
					+ categoriesName[category - 1]);

			if (category == 1) {
				checkPerformance(multicategoricosRegex, f);
			} else if (category == 2) {
				checkPerformance(numericosRegex, f);
			} else if (category == 3) {
				checkPerformance(dimensionaisRegex, f);
			}
		}
	} /* End validate */

	private static double checkPerformance(String regex, File f) {
		ArrayList<String> valuesList = Utility.readFile(f.getAbsolutePath());

		double precision = 0.0;

		int tp = 0;
		int total = valuesList.size();

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = null;

		for (String string : valuesList) {
			matcher = pattern.matcher(string);
			if (matcher.matches()) {
				tp++;
				System.out.format("Matched: '%1$s'\n", string);
			} else {
				System.out.format("Did not matched: '%1$s'\n", string);
			}
		}

		precision = 1.0 * tp / total;

		System.out.println(f.getName() + " -> " + precision);

		return precision;
	} /* End checkPerformance */
}