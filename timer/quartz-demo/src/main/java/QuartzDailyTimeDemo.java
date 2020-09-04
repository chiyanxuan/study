import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.quartz.DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule;

/**
 * dailyTimeIntervalSchedule可以设置一周的那几天允许，从一天的中几点到几点允许
 */
public class QuartzDailyTimeDemo {
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

			//每天12：29-13：00每2s执行一次
			DailyTimeIntervalScheduleBuilder dti = dailyTimeIntervalSchedule()
					. startingDailyAt(new TimeOfDay(12, 29))
					. endingDailyAt(new TimeOfDay(13, 0))
					. onEveryDay()
					. withIntervalInSeconds(2)
					. withMisfireHandlingInstructionFireAndProceed();

			//每周内 的12：32-13：00每2s执行一次
			DailyTimeIntervalScheduleBuilder dti1 = dailyTimeIntervalSchedule()
					. startingDailyAt(new TimeOfDay(12, 32))
					. endingDailyAt(new TimeOfDay(13, 0))
					.onMondayThroughFriday()
					//. onDaysOfTheWeek(3)  //周二执行了
					. withIntervalInSeconds(2)
					. withMisfireHandlingInstructionFireAndProceed();

			// 创建Trigger触发器设置使用cronSchedule方式调度
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("myTrigger","myTriggerGroup1")
					.usingJobData("job_trigger_param","job_trigger_param")
					.startNow()
					.withSchedule(dti1)
					.build();
			// 注册JobDetail实例到scheduler以及使用对应的Trigger触发时机
			scheduler.scheduleJob(jobDetail,trigger);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
