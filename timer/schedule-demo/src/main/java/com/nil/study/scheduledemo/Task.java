package com.nil.study.scheduledemo;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class Task {
	//@Scheduled 注解开启一个定时任务
	//fixedRate 表示任务执行之间的时间间隔，具体是指两次任务的开始时间间隔，即第二次任务开始时，第一次任务可能还没结束
	//所有时间的单位都是毫秒。
	@Scheduled(fixedRate = 2000)
	public void fixRate(){
		System.out.println("fixedRate:每隔2s开始执行一次>>>"+new Date());
	}

	//fixedDelay 表示任务执行之间的时间间隔，具体是指本次任务结束到下次任务开始之间的时间间隔
	@Scheduled(fixedDelay = 2000)
	public void fixedDelay() {
		System.out.println("fixedDelay：结束2s后开始下一次任务--开始>>>"+new Date());
		try {
			// 睡1s
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("fixedDelay：结束2s后开始下一次任务---结束>>>"+new Date());
	}

	//initialDelay 表示首次任务启动的延迟时间。
	@Scheduled(initialDelay = 2000,fixedDelay = 2000)
	public void initialDelay() {
		System.out.println("initialDelay：首次任务从启动2s后开始，结束2s后开始下一次任务>>>"+new Date());
	}

	@Scheduled(cron = "0/5 * * * * *")
	public void cron() {
		System.out.println("cron：每隔5s执行一次>>>" + new Date());
	}
}
