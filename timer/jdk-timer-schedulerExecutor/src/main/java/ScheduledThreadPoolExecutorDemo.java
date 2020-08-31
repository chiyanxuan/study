import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPoolExecutorDemo {
	public static void main(String[] args) {
		ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(5);
		executorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
				System.out.println("ScheduledThreadPoolExecutor1 run:"+now);
			}
		},1,2, TimeUnit.SECONDS);//初次执行延时1s，之后每2s运行一次
	}
}