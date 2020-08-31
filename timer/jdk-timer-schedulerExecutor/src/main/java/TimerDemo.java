import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class TimerDemo {
	public static void main(String[] args) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("jdk timer" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
			}
		},1000,5000);//延时1s，之后每隔5s运行一次
	}
}
