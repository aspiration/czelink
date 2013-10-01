package com.czelink.dbaccess.activator;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.czelink.dbaccess.facade.DbAccessServiceRegistry;
import com.czelink.intg.activators.ServiceActivator;
import com.czelink.intg.exceptions.ServiceInvocationException;
import com.czelink.intg.messaging.RequestMessage;
import com.czelink.intg.messaging.ResponseMessage;
import com.czelink.intg.utils.DbAccessUtil;

public class RemoteServiceActivator implements ServiceActivator, Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory
			.getLog(RemoteServiceActivator.class);

	private DbAccessServiceRegistry dbAccessServiceRegistry;

	public ResponseMessage activate(final RequestMessage requestMessage) {

		final ResponseMessage responseMessage = new ResponseMessage();

		try {
			final Object serviceImplementation = this.dbAccessServiceRegistry
					.getServiceInterfaceImplement(
							requestMessage.getServiceName(),
							requestMessage.getServiceInterface());
			final ClassLoader componentClassLoader = this.dbAccessServiceRegistry
					.getComponentClassLoader(requestMessage.getServiceName());

			// transform argument type and object
			final Object[] parameters = requestMessage.getOperationParameters();

			final int paramNum = (null == parameters) ? 0 : parameters.length;
			final Class[] paramterTypesClass = new Class[paramNum];
			final Object[] parameterObjects = new Object[paramNum];
			for (int i = 0; i < paramNum; i++) {
				paramterTypesClass[i] = DbAccessUtil.safeLoadClass(
						parameters[i].getClass(), componentClassLoader);

				parameterObjects[i] = DbAccessUtil.transformThroughLoader(
						parameters[i], componentClassLoader);
			}

			final Method targetMethod = serviceImplementation.getClass()
					.getMethod(requestMessage.getOperationName(),
							paramterTypesClass);

			final Object rawResult = DbAccessUtil.transformThroughLoader(
					targetMethod
							.invoke(serviceImplementation, parameterObjects),
					this.getClass().getClassLoader());

			responseMessage.setOperationParameters(parameterObjects);
			responseMessage.setReturnValue(rawResult);

		} catch (SecurityException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (NoSuchMethodException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (IllegalArgumentException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (IllegalAccessException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (InvocationTargetException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (ClassNotFoundException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (IOException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (InstantiationException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		} catch (IntrospectionException e) {
			final String correlationID = UUID.randomUUID().toString();
			final ServiceInvocationException exception = new ServiceInvocationException(
					correlationID, e);
			responseMessage.setException(exception);
			if (RemoteServiceActivator.LOGGER.isErrorEnabled()) {
				RemoteServiceActivator.LOGGER.error("[CorrelationID: "
						+ correlationID + "]", e);
			}
		}

		return responseMessage;
	}

	public DbAccessServiceRegistry getDbAccessServiceRegistry() {
		return dbAccessServiceRegistry;
	}

	public void setDbAccessServiceRegistry(
			DbAccessServiceRegistry dbAccessServiceRegistry) {
		this.dbAccessServiceRegistry = dbAccessServiceRegistry;
	}
}
