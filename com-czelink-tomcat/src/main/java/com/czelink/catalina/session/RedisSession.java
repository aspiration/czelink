package com.czelink.catalina.session;

import java.security.Principal;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RedisSession extends StandardSession {

	private static final long serialVersionUID = 1L;

	protected static Boolean manualDirtyTrackingSupportEnabled = true;

	public static void setManualDirtyTrackingSupportEnabled(Boolean enabled) {
		manualDirtyTrackingSupportEnabled = enabled;
	}

	protected static String manualDirtyTrackingAttributeKey = "__changed__";

	public static void setManualDirtyTrackingAttributeKey(String key) {
		manualDirtyTrackingAttributeKey = key;
	}

	protected HashMap<String, Object> changedAttributes;
	protected Boolean dirty;

	public RedisSession(Manager manager) {
		super(manager);
		resetDirtyTracking();
	}

	public Boolean isDirty() {
		return dirty || !changedAttributes.isEmpty();
	}

	public HashMap<String, Object> getChangedAttributes() {
		return changedAttributes;
	}

	public void resetDirtyTracking() {
		changedAttributes = new HashMap<String, Object>();
		dirty = false;
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (manualDirtyTrackingSupportEnabled
				&& manualDirtyTrackingAttributeKey.equals(key)) {
			dirty = true;
			return;
		}

		Object oldValue = getAttribute(key);

		if (null == oldValue) {
			changedAttributes.put(key, value);
		} else if (null == value) {
			changedAttributes.put(key, value);
		} else if (!value.getClass().isInstance(oldValue)) {
			changedAttributes.put(key, value);
		} else if (Collection.class.isAssignableFrom(value.getClass())
				&& Collection.class.isAssignableFrom(oldValue.getClass())) {
			final Collection<?> oldC = (Collection<?>) oldValue;
			final Collection<?> newC = (Collection<?>) value;
			if (!oldC.containsAll(newC) || !newC.containsAll(oldC)) {
				changedAttributes.put(key, value);
			}
		} else if (Map.class.isAssignableFrom(value.getClass())
				&& Map.class.isAssignableFrom(oldValue.getClass())) {
			final Collection<?> oldC = ((Map<?, ?>) oldValue).values();
			final Collection<?> newC = ((Map<?, ?>) value).values();
			if (!oldC.containsAll(newC) || !newC.containsAll(oldC)) {
				changedAttributes.put(key, value);
			}
		} else if (!value.equals(oldValue)) {
			changedAttributes.put(key, value);
		}

		super.setAttribute(key, value);
	}

	@Override
	public void removeAttribute(String name) {
		dirty = true;
		super.removeAttribute(name);
	}

	@Override
	public void setId(String id) {
		// Specifically do not call super(): it's implementation does unexpected
		// things
		// like calling manager.remove(session.id) and manager.add(session).

		this.id = id;
	}

	@Override
	public void setPrincipal(Principal principal) {
		dirty = true;
		super.setPrincipal(principal);
	}

}
