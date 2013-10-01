package com.czelink.dbaccess.facade;

import java.io.Serializable;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;

import com.czelink.dbaccess.loader.ReloadableComponentClassLoader;

/**
 * Services holder registry.
 * 
 * @author lambert.
 * 
 */
public class DbAccessServiceRegistry implements ServletContextAware,
		ApplicationContextAware, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * WEB-INF.
	 */
	private static final String WEB_INF_PATH = "WEB-INF";

	/**
	 * servletContext.
	 */
	private transient ServletContext servletContext;

	/**
	 * applicationContext.
	 */
	private transient ApplicationContext appContext;

	/**
	 * component classloader.
	 */
	private ReloadableComponentClassLoader componentClassLoader;

	/**
	 * componentRepositoryPath.
	 */
	private String componentRepositoryPath;

	private void setComponentClassLoader(
			final ReloadableComponentClassLoader componentClassLoader) {
		this.componentClassLoader = componentClassLoader;
	}

	/**
	 * get service interface implementation from serviceRepository.
	 * 
	 * @param serviceInterface
	 * @param serviceMethod
	 * @return
	 */
	public Object getServiceInterfaceImplement(final String serviceName,
			final String serviceInterfaceName) {
		return this.componentClassLoader.loadComponentImplementation(
				serviceName, serviceInterfaceName, this.appContext);
	}

	public ClassLoader getComponentClassLoader(final String serviceName) {
		return this.componentClassLoader.getComponentClassLoader(serviceName);
	}

	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	public void setApplicationContext(
			final ApplicationContext applicationContext) throws BeansException {
		this.appContext = applicationContext;
	}

	public void init() {
		String realPath = this.servletContext.getRealPath("/").replace('\\',
				'/');

		final StringBuilder classpathBuilder = new StringBuilder();
		classpathBuilder.append(realPath);
		if (!realPath.endsWith("/")) {
			classpathBuilder.append("/");
		}
		classpathBuilder.append(DbAccessServiceRegistry.WEB_INF_PATH);
		realPath = classpathBuilder.toString();

		this.componentRepositoryPath = realPath + "/components";

		this.setComponentClassLoader(ReloadableComponentClassLoader
				.getInstance(this.getClass().getClassLoader(),
						new String[] { this.componentRepositoryPath },
						5 * 1000, this.appContext));
	}
}
