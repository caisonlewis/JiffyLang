package class_helper;
@SuppressWarnings("serial")
public class JiffyError extends Exception {
	private String message;

	public JiffyError(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
