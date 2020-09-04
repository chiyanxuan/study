import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.AnnualCalendar;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * calendarIntervalSchedule是根据日历来的，主要用来排除，排除那些天不需要执行
 */
public class QuartzCalendarDemo {
	public static void main(String[] args) {
		try {
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			Scheduler scheduler = schedulerFactory.getScheduler();
			// 启动 scheduler
			scheduler.start();

			// 定义日历
			AnnualCalendar holidays = new AnnualCalendar();

			// 排除生日
			Calendar birthDay =  new GregorianCalendar(2002, 8, 8);
			holidays.setDayExcluded(birthDay, true);
			// 排除中秋节
			Calendar midAutumn = new GregorianCalendar(2020, 10, 1);
			holidays.setDayExcluded(midAutumn, true);
			// 排除圣诞节
			Calendar christmas = new GregorianCalendar(2020, 12, 25);
			holidays.setDayExcluded(christmas, true);

			// 调度器添加日历
			scheduler.addCalendar("holidays", holidays, false, false);

			// 创建HelloworldJob的JobDetail实例，并设置name/group
			JobDetail jobDetail = JobBuilder.newJob(HelloworldJob.class)
					.withIdentity("myJob","myJobGroup1")
					.usingJobData("job_param","job_param1").build();

			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity("myTrigger","myTriggerGroup1")
					.usingJobData("job_trigger_param","job_trigger_param")
					.startNow()
					.modifiedByCalendar("holidays")
					.withSchedule(SimpleScheduleBuilder.simpleSchedule()
							.withIntervalInSeconds(2)
							.repeatForever())
					.build();

			Date firstRunTime = scheduler.scheduleJob(jobDetail, trigger);
			System.out.println(jobDetail.getKey() + " 第一次触发： " + firstRunTime);

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
