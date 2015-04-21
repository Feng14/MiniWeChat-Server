package exception;

public class MyException extends Exception {
	private String message;

	public MyException(String message) {
		super(message);
		this.message = message;
	}
	
	public String toString(){
		return message + "\n" + getStackStr(getStackTrace());
	}
	
	public static String getStackStr(StackTraceElement[] stackTraceElements) {
		String s = "Stack:\n";
		for (StackTraceElement se : stackTraceElements)
			s += se.toString() + "\n";
		return s;
	}
}
