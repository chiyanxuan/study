package com.example.springbootquratz.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SysJob implements Serializable {

	/**
	 * 任务调度类
	 */
	private static final long serialVersionUID = 3746569356086283114L;

	private Integer id; //任务ID
	private String jobName; //任务名称
	private String jobGroup; //任务组别
	private String jobCron; //任务表达式
	private String jobClassPath; //类路径
	private Integer jobStatus; //任务状态 1启用 0停用
	private String jobStatusStr; //任务状态 1启用 0停用
	private String jobDescribe; // 任务具体描述
	private String jobDataMap;//传递map参数

	public void setJobClassPath(String jobClassPath) {
		this.jobClassPath = jobClassPath == null ? null : jobClassPath.trim();
	}
	public String getJobStatusStr() {
		if("1".equals(jobStatus.toString())){
			return "运行中";
		}else if ("0".equals(jobStatus.toString())){
			return "已停止";
		}else{
			return "未知";
		}

	}
}