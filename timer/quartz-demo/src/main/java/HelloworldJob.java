import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HelloworldJob implements Job {
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		System.out.println("hello world!:" + jobExecutionContext.getJobDetail().getKey()+ "time:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
	}
}
