package br.edu.ufam.app;

import java.util.ArrayList;
import java.util.regex.Pattern;

import br.edu.ufam.model.ConsensusPattern;
import br.edu.ufam.model.DistanceMatrix;
import br.edu.ufam.utility.Utility;

public class RegexGLR {

	public static void main(String args[]) {

		if (args.length == 1) {

			String regex = RegexGLR.createRegex(args[0]);

			System.out.println("Regex generated:");
			System.out.print("\t");
			System.out.println("\"" + regex + "\"");

		} else {

			System.out.println("Usage:");
			System.out.println("\t\tjava -jar RegexGLR.jar \"file name\"");

		}

		/*
		 * Experimentador exp; for (String file : nrfiles) {
		 * ArrayList<ArrayList<String>> aas = getAttribGroups(file); exp = new
		 * Experimentador(aas, .5f, 5, true, file.substring(0, 40) +
		 * file.substring(40, file.indexOf("xml")) + "Results.txt");
		 * exp.experimentarCruzado(); }
		 */

		/*
		 * ArrayList alo = new ArrayList(4); Object objs[] = new Object[4];
		 * 
		 * System.out.println(alo.size()); System.out.println(objs.length);
		 * 
		 * for (Object o : objs) { o = new Integer(1); alo.add(o);
		 * System.out.println(o); }
		 * 
		 * for (Object o : alo) { System.out.println(o); }
		 * 
		 * for (Object o : objs) { System.out.println(o); }
		 * 
		 * for (Object o : alo) { System.out.println(o); }
		 */

	}

	public static String createRegex(String filePath) {
		ArrayList<String> stringList = Utility.readFile(filePath);

		ConsensusPattern consensusPattern;
		DistanceMatrix distanceMatrix;
		ArrayList<ConsensusPattern> patternArray = new ArrayList<ConsensusPattern>(
				stringList.size());

		for (String s : stringList) {
			patternArray.add(new ConsensusPattern(s));
		}

		distanceMatrix = new DistanceMatrix(patternArray);
		consensusPattern = distanceMatrix.getConsensusPattern();
		Pattern pattern = consensusPattern.inferGeneralRegex();

		return pattern.toString();
	}

}