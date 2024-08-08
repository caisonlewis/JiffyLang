package class_interpreter;

public class FunctionReturned extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String result;
	
	public FunctionReturned(String result) {
		this.result = result;
	}
	
	public String getResult() {
		return result;
	}
}
