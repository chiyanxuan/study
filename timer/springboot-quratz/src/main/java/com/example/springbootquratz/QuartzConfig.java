package com.example.springbootquratz;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
	@Bean
	public JobDetail myJobDetail(){
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("jobDataMapKey","jobDataMapVal");
		JobDetail jobDetail = JobBuilder.newJob(CustomJob.class)
				.withIdentity("myJob","myJobGroup")
				.usingJobData(jobDataMap)
				.storeDurably()
				.build();
		return jobDetail;
	}

	@Bean
	public Trigger myTrigger(){
		Trigger trigger = TriggerBuilder.newTrigger()
				.forJob(myJobDetail())
				.withIdentity("myTrigger","myTriggerGroup")
				.usingJobData("jobTriggerParam","jobTriggerVal")
				.startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(2).repeatForever())
				.build();
		return trigger;
	}
}
