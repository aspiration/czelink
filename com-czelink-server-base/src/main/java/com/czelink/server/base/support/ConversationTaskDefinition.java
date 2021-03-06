package com.czelink.server.base.support;

import java.io.Serializable;
import java.util.Map;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

public class ConversationTaskDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private long maxLivePeriod;

	private Map<String, Map<String, Conversation>> conversationMap;

	private transient TaskScheduler taskScheduler;

	private transient TaskExecutor taskExecutor;

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

	public Map<String, Map<String, Conversation>> getConversationMap() {
		return conversationMap;
	}

	public void setConversationMap(
			Map<String, Map<String, Conversation>> conversationMap) {
		this.conversationMap = conversationMap;
	}

}
