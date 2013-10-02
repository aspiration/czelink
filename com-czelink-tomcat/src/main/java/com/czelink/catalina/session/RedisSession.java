package com.czelink.catalina.session;

import java.security.Principal;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;

public class RedisSession extends StandardSession {

	private static final long serialVersionUID = 1L;

	protected Boolean dirty;

	public RedisSession(Manager manager) {
		super(manager);
		resetDirtyTracking();
	}

	public Boolean isDirty() {
		return dirty;
	}

	public void resetDirtyTracking() {
		dirty = false;
	}

	@Override
	public void setAttribute(String key, Object value) {
		super.setAttribute(key, value);
		this.dirty = true;
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
