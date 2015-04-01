package tools;

import java.util.Date;
import org.apache.log4j.Logger;

public class Debug {
	public static boolean LogSwitch = true;
	public static boolean LoggerSwitch = true;
	static Logger logger = Logger.getLogger(Debug.class);

	public static void init(){
		if(logger == null){
			logger = Logger.getLogger(Debug.class);
		}
	}
	
	public static void log(String messsage) {
		init();
		if (LogSwitch)
			System.out.println(getTime() + messsage);
		
		if (LoggerSwitch)
			logger.debug(messsage);
		
//			System.out.println(getTime() + messsage);
	}
	
	
	public static void log(String head, String messsage) {
		init();
		if (LogSwitch)
			System.out.println(getTime() + head + " : " + messsage);
		
		
		if (LoggerSwitch) 
			logger.debug(head + " : " + messsage);
	}

	public static void log(String[] heads, String messsage) {
		init();
		if (LogSwitch) {
			System.out.print(getTime());
			for (String s : heads)
				System.out.print(s + " : ");
			
			System.out.println(messsage);
		}
		
		if (LoggerSwitch) {
			logger.debug(getTime());
			for (String s : heads)
				logger.debug(s + " : ");
			
			logger.debug(messsage);
		}
	}
	
	public static String getTime(){
		Date date = new Date();
		return "[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "]";
	}
}
