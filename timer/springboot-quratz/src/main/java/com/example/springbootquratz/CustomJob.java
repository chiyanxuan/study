package com.example.springbootquratz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class CustomJob extends QuartzJobBean {
	@Autowired
	private MyService myService;
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		myService.sayHi();
		System.out.println("Hi:"+ jobExecutionContext.getJobDetail().getKey());
	}
}
