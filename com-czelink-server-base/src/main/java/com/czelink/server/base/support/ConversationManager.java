package com.czelink.server.base.support;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;

public class ConversationManager extends RequestAwareRunnable implements
		Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, Map<String, Conversation>> conversationMap;

	private long maxLivePeriod;

	private transient TaskScheduler taskScheduler;

	private transient TaskExecutor taskExecutor;

	public void init() {
		this.taskScheduler.scheduleAtFixedRate(this, this.maxLivePeriod);
	}

	protected Conversation getConversation(final String conversationGroup,
			final String conversationID) {
		Conversation result = null;
		final Map<String, Conversation> group = this.conversationMap
				.get(conversationGroup);
		if (null != group) {
			result = group.get(conversationID);
		}
		return result;
	}

	public boolean setConversatioinOnEndTask(final String conversationGroup,
			final String conversationID, final ConversationTask task) {
		boolean result = false;
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation) {
			conversation.setOnEnd(task);
			result = true;
		}
		return result;
	}

	public boolean setConversationOnCompleteTask(
			final String conversationGroup, final String conversationID,
			final ConversationTask task) {
		boolean result = false;
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation) {
			conversation.setOnComplete(task);
			result = true;
		}
		return result;
	}

	public String startConversation(final String converstionGroup,
			boolean isMutlipleAllowed) {
		final String conversationID = UUID.randomUUID().toString();
		final Conversation conversation = new Conversation();
		conversation.setActivateTime(new Date().getTime());
		conversation.setGroupName(converstionGroup);
		conversation.setId(conversationID);
		Map<String, Conversation> group = this.conversationMap
				.get(converstionGroup);
		if (null == group || !isMutlipleAllowed) {
			group = new ConcurrentHashMap<String, Conversation>();
		}
		group.put(conversationID, conversation);
		this.conversationMap.put(converstionGroup, group);

		return conversationID;
	}

	public boolean keepConversation(final String conversationGroup,
			final String conversationID) {
		boolean result = false;
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation) {
			conversation.setActivateTime(new Date().getTime());
			result = true;
		}
		return result;
	}

	public void endConversation(final String conversationGroup,
			final String conversationID) {
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation.getOnEnd()) {
			this.taskExecutor.execute(conversation.getOnEnd());
		}
		this.conversationMap.get(conversationGroup).remove(conversationID);
	}

	public void completeConversation(final String conversationGroup,
			final String conversationID) {
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation.getOnComplete()) {
			this.taskExecutor.execute(conversation.getOnComplete());
		}
		this.conversationMap.get(conversationGroup).remove(conversationID);
	}

	public boolean putIntoConversation(final String conversationGroup,
			final String conversationID, final String key, final Object value) {
		boolean result = false;
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation) {
			conversation.put(key, value);
			conversation.setActivateTime(new Date().getTime());
			result = true;
		}
		return result;
	}

	public Object getFromConversation(final String conversationGroup,
			final String conversationID, final String key) {
		Object result = null;
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation) {
			result = conversation.get(key);
		}
		return result;
	}

	public boolean removeFromConversation(final String conversationGroup,
			final String conversationID, final String key) {
		boolean result = false;
		final Conversation conversation = this.getConversation(
				conversationGroup, conversationID);
		if (null != conversation) {
			conversation.remove(key);
			conversation.setActivateTime(new Date().getTime());
			result = true;
		}
		return result;
	}

	public void setConversationTaskDefinition(
			final ConversationTaskDefinition conversationTaskDefinition) {
		this.maxLivePeriod = conversationTaskDefinition.getMaxLivePeriod();
		this.taskExecutor = conversationTaskDefinition.getTaskExecutor();
		this.taskScheduler = conversationTaskDefinition.getTaskScheduler();
		this.conversationMap = conversationTaskDefinition.getConversationMap();
	}

	public void onRun() {
		final Set<Entry<String, Map<String, Conversation>>> conversationGroupSet = this.conversationMap
				.entrySet();
		for (final Iterator<Entry<String, Map<String, Conversation>>> converIt = conversationGroupSet
				.iterator(); converIt.hasNext();) {
			final Entry<String, Map<String, Conversation>> converEntry = (Entry<String, Map<String, Conversation>>) converIt
					.next();
			final Map<String, Conversation> conversationGroup = converEntry
					.getValue();
			final Set<Entry<String, Conversation>> entrySet = conversationGroup
					.entrySet();
			for (final Iterator<Entry<String, Conversation>> it = entrySet
					.iterator(); it.hasNext();) {
				final Entry<String, Conversation> entry = (Entry<String, Conversation>) it
						.next();
				final Conversation conversation = entry.getValue();
				final long activateTime = conversation.getActivateTime();
				final long currentTime = new Date().getTime();
				final long interval = currentTime - activateTime;
				if (interval > this.maxLivePeriod) {
					it.remove();
					if (null != conversation.getOnEnd()) {
						this.taskExecutor.execute(conversation.getOnEnd());
					}
				}
			}
		}
	}
}
