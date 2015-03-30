package test;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class TestLog1 {
	static Logger logger = Logger.getLogger(TestLog1.class);

	public static void main(String args[]) {
		SimpleLayout layout = new SimpleLayout();
		FileAppender appender = null;
		try {
			appender = new FileAppender(layout, "d:/output1.txt", false);
		} catch (Exception e) {
		}
		logger.addAppender(appender);
		logger.setLevel((Level) Level.DEBUG);
		logger.debug("Here is some DEBUG");
		logger.info("Here is some INFO");
		logger.warn("Here is some WARN");
		logger.error("Here is some ERROR");
		logger.fatal("Here is some FATAL");
	}
}
