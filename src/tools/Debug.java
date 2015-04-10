package tools;

import java.util.Date;
import org.apache.log4j.Logger;

public class Debug {
	public static enum LogType{NO, ERROR, FAULT, EXCEPTION};
	public static final int ERROR=0, Fault=1, Exception=2;
	
	public static boolean LogSwitch = false;
	public static boolean LoggerSwitch = true;
	static Logger logger = Logger.getLogger(Debug.class);

	public static void init(){
		if(logger == null){
			logger = Logger.getLogger(Debug.class);
		}
	}
	
	public static void log(String messsage) {
		log(LogType.NO, messsage);
	}
	
	public static void log(LogType logType, String messsage) {
		init();
		if (LogSwitch) {
			System.out.print(getTime());
			if (logType != LogType.NO)
				System.out.print(getLogTypeString(logType));
			System.out.println(messsage);
		}
		
		if (LoggerSwitch)
			if (logType != LogType.NO)
				logger.info(getLogTypeString(logType) + messsage);
			else
				logger.info(messsage);
	}
	
	
	public static void log(String head, String messsage) {
		log(LogType.NO, head, messsage);
	}
	
	public static void log(LogType logType, String head, String messsage) {
		init();
		if (LogSwitch) {
			System.out.print(getTime());
			if (logType != LogType.NO)
				System.out.print(getLogTypeString(logType));
			
			System.out.println(head + " : " + messsage);
		}

		if (LoggerSwitch)
			if (logType != LogType.NO)
				logger.info(getLogTypeString(logType) + head + " : " + messsage);
			else
				logger.info(head + " : " + messsage);
	}

	public static void log(String[] heads, String messsage) {
		log(LogType.NO, heads, messsage);
	}

	public static void log(LogType logType, String[] heads, String messsage) {
		init();
		if (LogSwitch) {
			System.out.print(getTime());
			
			if (logType != LogType.NO)
				System.out.print(getLogTypeString(logType));
			
			for (String s : heads)
				System.out.print(s + " : ");
			
			System.out.println(messsage);
		}
		
		if (LoggerSwitch) {
			logger.info(getTime());
			
			String str = getLogTypeString(logType);
			
			for (String s : heads)
				str += s + " : ";
	
			str += messsage;
			
			logger.info(str);
		}
		
	}
	
	public static String getTime(){
		Date date = new Date();
		return "[" + date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "]";
	}
	
	private static String getLogTypeString(LogType logType){
		return "[" + logType.toString() + "]";
	}
}
