package class_interpreter;

import java.util.HashMap;

public interface MemoryManager {
	public HashMap<String, String> getActivationRecord(String funcName);
	public HashMap<String, String> restoreActivationRecord();
}
