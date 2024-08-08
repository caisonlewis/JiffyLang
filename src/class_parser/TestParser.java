package class_parser;

import java.util.Scanner;

import class_helper.FileProcessor;
import class_helper.ParseTree;

public class TestParser {
	public static void parseFile(String filename) {
		String programText;
		try {
			programText = FileProcessor.readProgramFile(filename);
			parseText(programText);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void parseText(String programText) {
		JiffyParser jp = new JiffyParser(programText, true);

		ParseTree pt = jp.getAST();
		System.out.println(programText + " parses as " + pt);
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		System.out.println("Enter program text, or program filename, to PARSE: ");
		String input = s.nextLine();

		if (input.startsWith("fun ") )
			parseText(input);
		else
			parseFile(input);

		s.close();
	}

}
