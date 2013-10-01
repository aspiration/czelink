package com.czelink.dbaccess.handler;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * DbAccessInvocationHandler pre-handle the input parameters before execute on
 * real object.
 * 
 * @author Lambert
 * 
 */
public class DbAccessInvocationHandler implements InvocationHandler,
		Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * target service implementation.
	 */
	private final Object targetServiceImpl;

	/**
	 * constructor.
	 * 
	 * @param pTargetServiceImpl
	 */
	public DbAccessInvocationHandler(Object pTargetServiceImpl) {
		this.targetServiceImpl = pTargetServiceImpl;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		final String methodName = method.getName();

		final Method targetMethod = this.targetServiceImpl.getClass()
				.getMethod(methodName, method.getParameterTypes());

		return targetMethod.invoke(this.targetServiceImpl, args);
	}

}
