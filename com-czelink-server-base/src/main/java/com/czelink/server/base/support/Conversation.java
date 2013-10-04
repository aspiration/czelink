package com.czelink.server.base.support;

import java.util.concurrent.ConcurrentHashMap;

public class Conversation extends ConcurrentHashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	private long activateTime;

	private String groupName;

	private String id;

	private ConversationTask onEnd;

	private ConversationTask onComplete;

	protected Conversation() {
	}

	protected long getActivateTime() {
		return activateTime;
	}

	protected void setActivateTime(long activateTime) {
		this.activateTime = activateTime;
	}

	protected ConversationTask getOnEnd() {
		return onEnd;
	}

	protected void setOnEnd(ConversationTask onEnd) {
		this.onEnd = onEnd;
	}

	protected ConversationTask getOnComplete() {
		return onComplete;
	}

	protected void setOnComplete(ConversationTask onComplete) {
		this.onComplete = onComplete;
	}

	protected String getGroupName() {
		return groupName;
	}

	protected void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	protected String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}
}
