package com.czelink.server.base.support;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

public class ConversationManager implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, Conversation> conversationMap = Collections
			.synchronizedMap(new HashMap<String, Conversation>());

	private long maxLivePeriod;

	private TaskScheduler taskScheduler;

	private TaskExecutor taskExecutor;

	public void init() {
		this.taskScheduler.scheduleAtFixedRate(this, this.maxLivePeriod);
	}

	public boolean setConversatioinOnEndTask(final String conversationID,
			final ConversationTask task) {
		boolean result = false;
		final Conversation conversation = this.conversationMap
				.get(conversationID);
		if (null != conversation) {
			conversation.onEnd = task;
			result = true;
		}
		return result;
	}

	public boolean setConversationOnCompleteTask(final String conversationID,
			final ConversationTask task) {
		boolean result = false;
		final Conversation conversation = this.conversationMap
				.get(conversationID);
		if (null != conversation) {
			conversation.onComplete = task;
			result = true;
		}
		return result;
	}

	public String startConversation() {
		final String conversationID = UUID.randomUUID().toString();
		final Conversation conversation = new Conversation();
		conversation.activateTime = new Date().getTime();
		this.conversationMap.put(conversationID, conversation);
		return conversationID;
	}

	public boolean keepConversation(final String conversationID) {
		boolean result = false;
		final Conversation conversation = this.conversationMap
				.get(conversationID);
		if (null != conversation) {
			conversation.activateTime = new Date().getTime();
			result = true;
		}
		return result;
	}

	public void endConversation(final String conversationID) {
		final Conversation conversation = this.conversationMap
				.remove(conversationID);
		if (null != conversation.onEnd) {
			this.taskExecutor.execute(conversation.onEnd);
		}
	}

	public void completeConversation(final String conversationID) {
		final Conversation conversation = this.conversationMap
				.remove(conversationID);
		if (null != conversation.onComplete) {
			this.taskExecutor.execute(conversation.onComplete);
		}
	}

	public boolean putIntoConversation(final String conversationID,
			final String key, final Object value) {
		boolean result = false;
		final Conversation conversation = this.conversationMap
				.get(conversationID);
		if (null != conversation) {
			conversation.put(key, value);
			conversation.activateTime = new Date().getTime();
			result = true;
		}
		return result;
	}

	public boolean removeFromConversation(final String conversationID,
			final String key) {
		boolean result = false;
		final Conversation conversation = this.conversationMap
				.get(conversationID);
		if (null != conversation) {
			conversation.remove(key);
			conversation.activateTime = new Date().getTime();
			result = true;
		}
		return result;
	}

	public void setConversationTaskDefinition(
			final ConversationTaskDefinition conversationTaskDefinition) {
		this.maxLivePeriod = conversationTaskDefinition.getMaxLivePeriod();
		this.taskExecutor = conversationTaskDefinition.getTaskExecutor();
		this.taskScheduler = conversationTaskDefinition.getTaskScheduler();
	}

	@Override
	public void run() {
		final Set<Entry<String, Conversation>> entrySet = this.conversationMap
				.entrySet();
		for (final Iterator<Entry<String, Conversation>> it = entrySet
				.iterator(); it.hasNext();) {
			final Entry<String, Conversation> entry = (Entry<String, Conversation>) it
					.next();
			final Conversation conversation = entry.getValue();
			final long activateTime = conversation.activateTime;
			final long currentTime = new Date().getTime();
			final long interval = currentTime - activateTime;
			if (interval > this.maxLivePeriod) {
				it.remove();
				if (null != conversation.onEnd) {
					this.taskExecutor.execute(conversation.onEnd);
				}
			}
		}
	}

	protected Conversation getConversation(final String conversationID) {
		return this.conversationMap.get(conversationID);
	}

	public static abstract class ConversationTask implements Runnable,
			Serializable {

		private static final long serialVersionUID = 1L;

		private Conversation conversation;

		protected final ConversationManager conversationManager;

		protected final String conversationId;

		public ConversationTask(final String pConversationId,
				final ConversationManager pConversationManager) {
			this.conversationId = pConversationId;
			this.conversationManager = pConversationManager;

			final Conversation pConversation = this.conversationManager
					.getConversation(pConversationId);
			if (null != pConversation) {
				this.conversation = pConversation;
			} else {
				throw new IllegalStateException("invalid conversation ID: "
						+ pConversationId);
			}
		}

		protected final <T> Map<String, T> getConversation(Class<T> className) {
			final Map<String, T> result = new HashMap<String, T>();
			final Set<Entry<String, Object>> entrySet = this.conversation
					.entrySet();
			for (final Iterator<Entry<String, Object>> it = entrySet.iterator(); it
					.hasNext();) {
				final Entry<String, Object> entry = it.next();
				final String key = entry.getKey();
				final T value = (T) entry.getValue();
				result.put(key, value);
			}
			return result;
		}
	}

	public static class Conversation extends HashMap<String, Object> {
		private static final long serialVersionUID = 1L;
		private long activateTime;
		private Runnable onEnd;
		private Runnable onComplete;
	}
}
