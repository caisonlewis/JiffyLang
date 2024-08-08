package class_interpreter;

import java.util.HashMap;

public class BasicMemoryManager implements MemoryManager {
	private HashMap<String, String> valueTable;

	public BasicMemoryManager() {
		valueTable = new HashMap<>();
	}

	@Override
	public HashMap<String, String> getActivationRecord(String funcName) {
		return valueTable;
	}

	@Override
	public HashMap<String, String> restoreActivationRecord() {
		return valueTable;
	}

}
