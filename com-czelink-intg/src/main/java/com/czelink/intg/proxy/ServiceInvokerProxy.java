package com.czelink.intg.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import com.czelink.intg.activators.ServiceActivator;
import com.czelink.intg.messaging.RequestMessage;
import com.czelink.intg.messaging.ResponseMessage;
import com.czelink.intg.utils.DbAccessUtil;

/**
 * A proxy class used to implement and invoke service interface.
 * 
 * @author lenovo
 * 
 */
public class ServiceInvokerProxy implements FactoryBean<Object>,
		InvocationHandler, ServletContextAware, ApplicationContextAware {

	/**
	 * targetContextName
	 */
	private static final String TARGET_CONTEXT_NAME = "/com-czelink-dbaccess-facade";

	/**
	 * spring webContext root.
	 */
	private static final String SPRING_WEBCONTEXT_ROOT = "org.springframework.web.context.WebApplicationContext.ROOT";

	/**
	 * service registry bean name.
	 */
	private static final String SERVICE_REGISTRY_BEAN = "dbAccessServiceRegistry";

	/**
	 * service activator bean name.
	 */
	private static final String SERVICE_ACTIVATOR_BEAN = "serviceActivator";

	/**
	 * Application Context.
	 */
	private ApplicationContext applicationContext;

	/**
	 * service group name.
	 */
	private String serviceGroupName;

	/**
	 * target service interface.
	 */
	private String serviceInterface;

	/**
	 * servlet context.
	 */
	private ServletContext servletContext;

	private Object invokeLocal(Object proxy, Method method, Object[] args)
			throws Throwable {
		final String methodName = method.getName();
		Object result = null;

		final ServletContext dbAccessServletContext = this.servletContext
				.getContext(ServiceInvokerProxy.TARGET_CONTEXT_NAME);

		final ClassLoader currentAppClassLoader = Thread.currentThread()
				.getContextClassLoader();

		try {
			final Object dbAccessAppContext = dbAccessServletContext
					.getAttribute(ServiceInvokerProxy.SPRING_WEBCONTEXT_ROOT);

			final ClassLoader dbAccessClassLoader = dbAccessAppContext
					.getClass().getClassLoader();

			Thread.currentThread().setContextClassLoader(dbAccessClassLoader);

			final Method getBeanMethod = dbAccessAppContext.getClass()
					.getMethod("getBean", String.class);
			final Object serviceRegistryBean = getBeanMethod.invoke(
					dbAccessAppContext,
					ServiceInvokerProxy.SERVICE_REGISTRY_BEAN);

			final Method getComponentClassLoaderMethod = serviceRegistryBean
					.getClass().getMethod("getComponentClassLoader",
							new Class[] { String.class });

			final ClassLoader componentClassLoader = (ClassLoader) getComponentClassLoaderMethod
					.invoke(serviceRegistryBean, this.serviceGroupName);

			final Method getServiceInterfaceImplementMethod = serviceRegistryBean
					.getClass().getMethod("getServiceInterfaceImplement",
							new Class[] { String.class, String.class });

			final Object serviceImpl = getServiceInterfaceImplementMethod
					.invoke(serviceRegistryBean, new Object[] {
							this.serviceGroupName, this.serviceInterface });

			// transform argument type and object
			final int paramNum = (null == args) ? 0 : args.length;
			final Class[] paramterTypesClass = new Class[paramNum];
			final Object[] parameterObjects = new Object[paramNum];
			for (int i = 0; i < paramNum; i++) {
				paramterTypesClass[i] = DbAccessUtil.safeLoadClass(
						args[i].getClass(), componentClassLoader);

				parameterObjects[i] = DbAccessUtil.transformThroughLoader(
						args[i], componentClassLoader);
			}

			final Method targetMethod = serviceImpl.getClass().getMethod(
					methodName, paramterTypesClass);

			result = DbAccessUtil.transformThroughLoader(targetMethod.invoke(
					serviceImpl, parameterObjects), this.getClass()
					.getClassLoader());

			DbAccessUtil.resyncInputParameterStatusAfterProcess(
					parameterObjects, args);

		} catch (Exception e) {
			throw new IllegalStateException("Invokation of service: "
					+ this.serviceInterface + "." + methodName + " fail.", e);
		} finally {
			Thread.currentThread().setContextClassLoader(currentAppClassLoader);
		}
		return result;
	}

	private Object invokeRemote(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;

		ServiceActivator serviceActivator = null;

		try {
			serviceActivator = (ServiceActivator) this.applicationContext
					.getBean(ServiceInvokerProxy.SERVICE_ACTIVATOR_BEAN);
		} catch (final NoSuchBeanDefinitionException e) {
			// suppress the exception.
			serviceActivator = null;
		}

		if (null != serviceActivator) {
			final RequestMessage requestMessage = new RequestMessage();
			requestMessage.setOperationName(method.getName());
			requestMessage.setOperationParameters(args);
			requestMessage.setServiceInterface(this.serviceInterface);
			requestMessage.setServiceName(this.serviceGroupName);

			final ResponseMessage responseMessage = serviceActivator
					.activate(requestMessage);
			if (responseMessage.getStatus()) {
				DbAccessUtil.resyncInputParameterStatusAfterProcess(
						responseMessage.getOperationParameters(), args);

				result = responseMessage.getReturnValue();
			} else {
				// TODO: Log the error message or throw the exception out by
				// wrapper.
			}
		}
		return result;
	}

	/**
	 * get dynamic service interface implementation.
	 */
	public Object getObject() throws Exception {
		final Class interfaceClass = Class.forName(this.getServiceInterface());
		return Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class[] { interfaceClass }, this);
	}

	/**
	 * get interface class definition.
	 */
	public Class<?> getObjectType() {
		Class interfaceClass = null;

		try {
			interfaceClass = Class.forName(this.getServiceInterface());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Cannot find class: "
					+ this.getServiceInterface() + ".", e);
		}

		return interfaceClass;
	}

	/**
	 * singleton bean.
	 */
	public boolean isSingleton() {
		return true;
	}

	public void setServletContext(ServletContext pServletContext) {
		this.servletContext = pServletContext;
	}

	public String getServiceGroupName() {
		return serviceGroupName;
	}

	public void setServiceGroupName(String serviceGroupName) {
		this.serviceGroupName = serviceGroupName;
	}

	public String getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public void setApplicationContext(ApplicationContext pApplicationContext)
			throws BeansException {
		this.applicationContext = pApplicationContext;
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		result = this.invokeRemote(proxy, method, args);
		if (null == result) {
			result = this.invokeLocal(proxy, method, args);
		}
		return result;
	}
}
