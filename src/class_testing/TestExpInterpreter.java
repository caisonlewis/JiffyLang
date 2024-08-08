package class_testing;

import class_helper.FileProcessor;
import class_interpreter.BasicMemoryManager;
import class_interpreter.Interpreter;

public class TestExpInterpreter {

	private static void interpret(String programText) {
		Interpreter.interpret(programText, new BasicMemoryManager(), true);
	}

	public static void main(String[] args) throws Exception {
        interpret(FileProcessor.readProgramFile("whileTest.jif"));
		interpret(FileProcessor.readProgramFile("interp1.jif"));
		interpret(FileProcessor.readProgramFile("interp2.jif"));
		interpret(FileProcessor.readProgramFile("interp3.jif"));
		
		interpret("fun main(){write(\"Test 4. (2+3)*(4-6) should be -10\");write((2+3)*(4-6))}");
		interpret("fun main(){write(\"Test 5. 2^3 should be 8\");write(2^3)}");
		interpret("fun main(){write(\"Test 6. 2^3^2 should be 512\");write(2^3^2)}");
		interpret("fun main(){write(\"Test 7. 2+3^2^2 should be 83\");write(2+3^2^2)}");
		interpret("fun main(){write(\"Test 8. 2^3/2*2 should be 8\");write(2^3/2*2)}");
		interpret("fun main(){write(\"Test 9. 2+3/2^2$3/2*4+2^3 should be 14\");write(2+3/2^2$3/2*4+2^3)}");
		interpret("fun main(){write(\"Test 10. 2^(2^2$3^1) should be 16\");write(2^(2^2$3^1))}");
		interpret("fun main(){write(\"Test 11. 2^1^2$3^1 should be 3\");write(2^1^2$3^1)}");
		interpret("fun main(){write(\"Test 12. (2+3)/2^(2$3)/2*(4+2)^3 should be 67.5\");write((2+3)/2^(2$3)/2*(4+2)^3)}");
		interpret("fun main(){write(\"Test 13. 1+2^3/4-5+6/2+(2-4)^2 should be 5\");write(1+2^3/4-5+6/2+(2-4)^2)}");
		interpret("fun main(){write(\"Test 14. 1+2^3/4-5+6/2+(2-4)^2$6 should be 6\");write(1+2^3/4-5+6/2+(2-4)^2$6)}");
	
		interpret(FileProcessor.readProgramFile("seq_sum_test.jif"));
		interpret(FileProcessor.readProgramFile("square_root_test.jif"));
		interpret(FileProcessor.readProgramFile("gcd_test.jif"));
	}

}