package tools;

import java.util.Date;

public class Debug {
	public static boolean LogSwitch = true;
	
	
	public static void log(String messsage) {
		if (LogSwitch)
			System.out.println(getTime() + messsage);
	}

	public static void log(String head, String messsage) {
		if (LogSwitch)
			System.out.println(getTime() + head + " : " + messsage);
	}

	public static void log(String[] heads, String messsage) {
		if (LogSwitch) {
			System.out.print(getTime());
			for (String s : heads)
				System.out.print(s + " : ");
			System.out.println(messsage);
		}
	}
	
	public static String getTime(){
		Date date = new Date();
		return "[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "]";
	}
}
