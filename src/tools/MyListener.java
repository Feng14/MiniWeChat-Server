package tools;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MyListener implements ServletContextListener {

	private java.util.Timer timer = null;

	public void contextDestroyed(ServletContextEvent event) {
		// TODO Auto-generated method stub

	}

	public void contextInitialized(ServletContextEvent event) {

		timer = new java.util.Timer(true);
		event.getServletContext().log("定时器已启动。");
		timer.schedule(new MyTask(event.getServletContext(), timer), 0, 5000);
		event.getServletContext().log("已经添加任务调度表。");

	}

}