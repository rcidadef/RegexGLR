package br.edu.ufam;

public class FooClass {
	public static void main(String[] args) {
//		RegexGLRTester.testRegexGenerator("C:\\Users\\Roberto\\SkyDrive\\BDRI\\RegexGLR\\norepetition\\testFile.xml");
//		RegexGLRTester.runExtraction(.8f);
//		RegexGLRTester.runExtraction2(0.8f);
		String res = RegexGLRTester.runFromFile(args[0]);
		System.out.println(res);
	}
}
