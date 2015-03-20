package tools;

public class Debug {
	public static boolean LogSwitch = true;
	
	
	public static void log(String messsage) {
		if (LogSwitch)
			System.out.println(messsage);
	}

	public static void log(String head, String messsage) {
		if (LogSwitch)
			System.out.println(head + " : " + messsage);
	}

	public static void log(String[] heads, String messsage) {
		if (LogSwitch) {
			for (String s : heads)
				System.out.print(s + " : ");
			System.out.println(messsage);
		}
	}
}
