package tools;

import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContext;

public class MyTask extends TimerTask {

	private static boolean isRunning = false;
	private ServletContext context = null;
	private Timer timer;

	public MyTask(ServletContext context, Timer timer) {
		this.context = context;
		this.timer = timer;
	}

	@Override
	public void run() {

		if (!isRunning) {
			System.out.println("开始执行指定任务.");
			// if (C_SCHEDULE_HOUR == c.get(Calendar.HOUR_OF_DAY)) {
			isRunning = true;
			context.log("开始执行指定任务.");
			// TODO 添加自定义的详细任务，以下只是示例
			int i = 0;
			while (i++ < 10) {
				context.log("已完成任务的" + i + "/" + 10);
				// System.out.println("已完成任务的" + i + "/" + 1000) ;
			}

			isRunning = false;
			context.log("指定任务执行结束");
			System.out.println("指定任务执行结束");
			timer.cancel();
			// }
		} else {
			context.log("上一次任务执行还未结束");
		}
	}

}