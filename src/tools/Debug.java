package tools;

import java.util.Date;

import org.apache.log4j.Logger;

import test.TestLog1;

public class Debug {
	public static boolean LogSwitch = true;
	static Logger logger = Logger.getLogger(Debug.class);

	public static void init(){
		if(null == logger){
			logger = Logger.getLogger(Debug.class);
		}
	}
	
	public static void log(String messsage) {
		init();
		if (LogSwitch){
			System.out.println(getTime() + messsage);
			logger.debug(messsage);
		}
//			System.out.println(getTime() + messsage);
	}
	
	
	public static void log(String head, String messsage) {
		init();
		if (LogSwitch){
			logger.debug(getTime() + head + " : " + messsage);
			System.out.println(getTime() + head + " : " + messsage);
		}
	}

	public static void log(String[] heads, String messsage) {
		init();
		if (LogSwitch) {
			logger.debug(getTime());
			System.out.println(getTime());
			for (String s : heads) {
				logger.debug(s + " : ");
				System.out.print(s + " : ");
			}
			logger.debug(messsage);
			System.out.println(messsage);
		}
	}
	
	public static String getTime(){
		Date date = new Date();
		return "[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "]";
	}
}
