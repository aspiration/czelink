package com.czelink.server.base.support;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;

public class ConversationManager extends TimerTask {

	private final Map<String, Conversation> conversationMap = new HashMap<String, Conversation>();

	public String startConversation() {
		final String conversationID = UUID.randomUUID().toString();
		final Conversation conversation = new Conversation();
		conversation.setActivateTime((new Date().getTime()));
		conversationMap.put(conversationID, conversation);
		return conversationID;
	}

	public boolean keepConversation(final String conversationID) {
		boolean result = false;
		final Conversation conversation = this.conversationMap
				.get(conversationID);
		if (null != conversation) {
			conversation.setActivateTime((new Date().getTime()));
			result = true;
		}
		return result;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	private class Conversation extends HashMap {

		private long activateTime;

		public long getActivateTime() {
			return activateTime;
		}

		public void setActivateTime(long activateTime) {
			this.activateTime = activateTime;
		}
	}
}
