package com.czelink.dbaccess.activator;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.czelink.dbaccess.exceptions.ServiceInvocationException;
import com.czelink.dbaccess.facade.DbAccessServiceRegistry;
import com.czelink.intg.activators.ServiceActivator;
import com.czelink.intg.messaging.RequestMessage;
import com.czelink.intg.messaging.ResponseMessage;
import com.czelink.intg.utils.DbAccessUtil;

public class RemoteServiceActivator implements ServiceActivator {

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
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (IOException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (InstantiationException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
		} catch (IntrospectionException e) {
			e.printStackTrace();
			responseMessage.addToExceptionList(e);
			throw new ServiceInvocationException(e);
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
