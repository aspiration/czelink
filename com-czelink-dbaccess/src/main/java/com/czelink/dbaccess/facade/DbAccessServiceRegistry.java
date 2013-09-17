package com.czelink.dbaccess.facade;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.context.ServletContextAware;

import com.czelink.dbaccess.GridFsOperationsAware;
import com.czelink.dbaccess.LdapOperationsAware;
import com.czelink.dbaccess.MongoOperationsAware;
import com.czelink.dbaccess.handler.DbAccessInvocationHandler;
import com.czelink.dbaccess.loader.ReloadableComponentClassLoader;

/**
 * Services holder registry.
 * 
 * @author lambert.
 * 
 */
public class DbAccessServiceRegistry implements ServletContextAware {

	/**
	 * suffix mapping.
	 */
	private static final String SUFFIX_MAPPING = "Mapping.properties";

	/**
	 * WEB-INF.
	 */
	private static final String WEB_INF_PATH = "WEB-INF";

	/**
	 * component classloader.
	 */
	private ClassLoader componentClassLoader;

	/**
	 * componentRepositoryPath.
	 */
	private String componentRepositoryPath;

	/**
	 * service repository.
	 */
	private Map serviceRepository = new HashMap();

	/**
	 * mongoDbFactory.
	 */
	private MongoDbFactory mongoDbFactory;

	/**
	 * ldapTemplate.
	 */
	private LdapTemplate ldapTemplate;

	/**
	 * MongoConverter.
	 */
	private MongoConverter mongoConverter;

	/**
	 * get service interface implementation from serviceRepository.
	 * 
	 * @param serviceInterface
	 * @param serviceMethod
	 * @return
	 */
	public Object getServiceInterfaceImplement(final String serviceName,
			final String serviceInterfaceName) {

		Map serviceInterfaceRepository = (Map) this.serviceRepository
				.get(serviceName);

		Object serviceImplObject = null;

		if (null == serviceInterfaceRepository
				|| serviceInterfaceRepository.size() == 0) {
			serviceInterfaceRepository = this
					.initServiceRepository(serviceName);
			this.serviceRepository.put(serviceName, serviceInterfaceRepository);
		}

		final Object serviceImplementation = serviceInterfaceRepository
				.get(serviceInterfaceName);

		try {

			final Object serviceImplObject_inner = this.componentClassLoader
					.loadClass((String) serviceImplementation).newInstance();

			final MongoTemplate mongoTemplate = new MongoTemplate(
					this.mongoDbFactory);

			// set MongoOperations
			if (serviceImplObject_inner instanceof MongoOperationsAware) {
				final MongoOperationsAware mongoOperator = (MongoOperationsAware) serviceImplObject_inner;
				mongoOperator.setMongoOperations(mongoTemplate);
			}

			// set GridFsOperations
			if (serviceImplObject_inner instanceof GridFsOperationsAware) {
				final GridFsOperationsAware gridFsOperator = (GridFsOperationsAware) serviceImplObject_inner;
				gridFsOperator.setGridFsOperations(new GridFsTemplate(
						this.mongoDbFactory, this.mongoConverter));
			}

			// set LdapTemplate
			if (serviceImplObject_inner instanceof LdapOperationsAware) {
				final LdapOperationsAware ldapTemplateAware = (LdapOperationsAware) serviceImplObject_inner;
				ldapTemplateAware.setLdapOperations(this.ldapTemplate);
			}

			final InvocationHandler dbAccessInvocationHandler = new DbAccessInvocationHandler(
					serviceImplObject_inner);

			final Class serviceInterfaceClass = this.componentClassLoader
					.loadClass(serviceInterfaceName);

			serviceImplObject = Proxy.newProxyInstance(
					serviceInterfaceClass.getClassLoader(),
					new Class[] { serviceInterfaceClass },
					dbAccessInvocationHandler);

		} catch (Exception e) {
			throw new IllegalStateException("fail creating instance of class: "
					+ serviceImplementation + ".", e);
		}

		return serviceImplObject;
	}

	/**
	 * initialize service repository based on [serviceName]Mapping.properties
	 * file.
	 * 
	 * @param serviceName
	 * @return
	 */
	public Map initServiceRepository(final String serviceName) {
		final StringBuilder serviceMappingBuilder = new StringBuilder();
		serviceMappingBuilder.append(serviceName);
		serviceMappingBuilder.append(DbAccessServiceRegistry.SUFFIX_MAPPING);
		final String serviceMappingFile = serviceMappingBuilder.toString();

		final Properties props = new Properties();

		try {

			final InputStream inputStream = new FileInputStream(
					this.componentRepositoryPath + "/" + serviceMappingFile);

			if (null == inputStream) {
				throw new IllegalStateException("missing file: "
						+ serviceMappingFile + ".");
			}

			props.load(inputStream);
		} catch (IOException e) {
			throw new IllegalStateException("fail to load content of file: "
					+ serviceMappingFile + ".", e);
		}

		return props;
	}

	public MongoDbFactory getMongoDbFactory() {
		return mongoDbFactory;
	}

	public void setMongoDbFactory(MongoDbFactory mongoDbFactory) {
		this.mongoDbFactory = mongoDbFactory;
	}

	public MongoConverter getMongoConverter() {
		return mongoConverter;
	}

	public void setMongoConverter(MongoConverter mongoConverter) {
		this.mongoConverter = mongoConverter;
	}

	public void setServletContext(ServletContext servletContext) {
		String realPath = servletContext.getRealPath("/").replace('\\', '/');

		final StringBuilder classpathBuilder = new StringBuilder();
		classpathBuilder.append(realPath);
		if (!realPath.endsWith("/")) {
			classpathBuilder.append("/");
		}
		classpathBuilder.append(DbAccessServiceRegistry.WEB_INF_PATH);
		realPath = classpathBuilder.toString();

		this.componentRepositoryPath = realPath + "/components";

		this.componentClassLoader = ReloadableComponentClassLoader.getInstance(
				this.getClass().getClassLoader(),
				new String[] { this.componentRepositoryPath }, 5 * 1000);
	}

	public LdapTemplate getLdapTemplate() {
		return ldapTemplate;
	}

	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
}
