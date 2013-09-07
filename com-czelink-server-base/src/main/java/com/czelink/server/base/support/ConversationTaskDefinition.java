package com.czelink.server.base.support;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

public class ConversationTaskDefinition {

	private long maxLivePeriod;

	private TaskScheduler taskScheduler;

	private TaskExecutor taskExecutor;

	public long getMaxLivePeriod() {
		return maxLivePeriod;
	}

	public void setMaxLivePeriod(long maxLivePeriod) {
		this.maxLivePeriod = maxLivePeriod;
	}

	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	public void setTaskScheduler(TaskScheduler taskScheduler) {
		this.taskScheduler = taskScheduler;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

}
