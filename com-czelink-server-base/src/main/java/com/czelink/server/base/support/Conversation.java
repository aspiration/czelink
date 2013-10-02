package com.czelink.server.base.support;

import java.util.concurrent.ConcurrentHashMap;

public class Conversation extends ConcurrentHashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	private long activateTime;

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
}
