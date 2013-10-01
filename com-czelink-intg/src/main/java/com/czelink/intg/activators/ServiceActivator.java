package com.czelink.intg.activators;

import com.czelink.intg.messaging.RequestMessage;
import com.czelink.intg.messaging.ResponseMessage;

public interface ServiceActivator {

	public ResponseMessage activate(final RequestMessage requestMessage);
}
