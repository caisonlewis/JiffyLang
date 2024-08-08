package class_interpreter;

import java.util.Scanner;

import class_helper.FileProcessor;

public class TestInterpreter {
	public static void interpretFile(String filename) throws Exception {
		String programText = FileProcessor.readProgramFile(filename);
		interpretText(programText);
	}

	private static void interpretText(String programText) {
		Interpreter.interpret(programText, new BasicMemoryManager(), true);
	}

	public static void main(String[] args) throws Exception {
		Scanner s = new Scanner(System.in);
		System.out.println("Enter program, or program filename, to INTERPRET: ");
		String input = s.nextLine();

		if (input.startsWith("fun "))
			interpretText(input);
		else
			interpretFile(input);

		s.close();
	}

}
