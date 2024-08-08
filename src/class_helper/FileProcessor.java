package class_helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileProcessor {
	public static String readProgramFile(String filename) throws Exception {
		if (isJif(filename)) {
			File infile = new File("class_resources/" + filename);
			String programText = "";
			Scanner fileScan = null;
			try {
				fileScan = new Scanner(infile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (fileScan.hasNext())
				programText += fileScan.nextLine() + "\n";
			return programText;
		} else 
			throw new Exception("Jiffy files must have extension .jif");
	}

	private static boolean isJif(String filename) {
		int dot = filename.indexOf('.');
		String ext = filename.substring(dot + 1);
		return ext.equals("jif");
	}
}
