package class_parser;

import java.util.Scanner;

import class_helper.FileProcessor;
import class_helper.JiffyError;

public class TestLexer {
	public static void tokenizeFile(String filename) {
		String programText;
		try {
			programText = FileProcessor.readProgramFile(filename);
			tokenizeText(programText);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void tokenizeText(String programText) {
		JiffyLexer jlex;
		try {
			jlex = new JiffyLexer(programText, true);
			while (true) {
				try {
					jlex.advance();
				} catch (JiffyError e) {
					// TODO Auto-generated catch block
					System.exit(0);
				}
			}
		} catch (JiffyError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		System.out.println("Enter program text, or program filename, to TOKENIZE: ");
		String input = s.nextLine();

		if (input.startsWith("fun ") )
			tokenizeText(input);
		else
			tokenizeFile(input);

		s.close();
	}

}