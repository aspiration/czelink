package com.czelink.server.base.support;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public abstract class ConversationTask extends RequestAwareRunnable {

	private static final long serialVersionUID = 1L;

	private Conversation conversation;

	protected final String conversationId;

	public ConversationTask(final String pConversationId,
			final ConversationManager pConversationManager) {
		super();
		this.conversationId = pConversationId;
		final Conversation pConversation = pConversationManager
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
