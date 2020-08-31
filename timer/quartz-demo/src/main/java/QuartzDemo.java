import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzDemo {
	public static void main(String[] args) {
		try {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			Scheduler scheduler = schedulerFactory.getScheduler();
			// 启动 scheduler
			scheduler.start();
			// 创建HelloworldJob的JobDetail实例，并设置name/group
			JobDetail jobDetail = JobBuilder.newJob(HelloworldJob.class)
					.withIdentity("myJob","myJobGroup1")
					.usingJobData("job_param","job_param1").build();
			// 创建Trigger触发器设置使用cronSchedule方式调度
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("myTrigger","myTriggerGroup1")
					.usingJobData("job_trigger_param","job_trigger_param")
					.startNow()
					.withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ? 2020"))  //每5s执行一次
					.build();
			// 注册JobDetail实例到scheduler以及使用对应的Trigger触发时机
			scheduler.scheduleJob(jobDetail,trigger);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
