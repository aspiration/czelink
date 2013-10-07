package com.czelink.server.base.support;

import java.io.Serializable;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public abstract class RequestAwareRunnable implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;

	private transient RequestAttributes requestAttributes;

	private transient Thread thread;

	protected abstract void onRun();

	public RequestAwareRunnable() {
		this.thread = Thread.currentThread();
	}

	public void setRequestAttributesBeforeRun(
			final RequestAttributes pRequestAttributes) {
		this.requestAttributes = pRequestAttributes;
	}

	public final void run() {
		try {
			RequestContextHolder.setRequestAttributes(this.requestAttributes);

			if (null != RequestContextHolder.getRequestAttributes()) {
				onRun();
			}
		} finally {
			if (Thread.currentThread() != this.thread) {
				RequestContextHolder.resetRequestAttributes();
			}
			this.thread = null;
		}
	}
}
