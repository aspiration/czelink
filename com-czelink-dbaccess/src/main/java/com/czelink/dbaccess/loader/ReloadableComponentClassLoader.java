package com.czelink.dbaccess.loader;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ReloadableComponentClassLoader implements Serializable {

	private static final long serialVersionUID = 1L;

	private static ReloadableComponentClassLoader instance;

	private transient ApplicationContext parentAppContext;

	private ClassLoader parent;

	private String componentRepositoryPath;

	private transient Map<String, ComponentClassLoader> coreLoaders = new HashMap<String, ComponentClassLoader>();

	private transient Map<String, ClassPathXmlApplicationContext> componentContexts = new HashMap<String, ClassPathXmlApplicationContext>();

	/**
	 * repository watcher.
	 */
	private List<ComponentRepositoryWatcher> watchers = new ArrayList<ComponentRepositoryWatcher>();

	private ReloadableComponentClassLoader(final ClassLoader pParent,
			final String[] pClasspaths, final long reloadInterval,
			final ApplicationContext parentAppContext) {

		this.parentAppContext = parentAppContext;
		this.parent = pParent;
		this.componentRepositoryPath = pClasspaths[0];

		this.initCoreLoader(pParent);

		if (reloadInterval > 0) {
			for (int i = 0; i < pClasspaths.length; i++) {
				final String classpath = pClasspaths[i];

				final ComponentRepositoryWatcher watcher = new ComponentRepositoryWatcher(
						classpath, reloadInterval);
				this.watchers.add(watcher);
				try {
					watcher.start();
				} catch (Exception e) {
					throw new IllegalStateException(
							"fail watching on the classpath: " + classpath
									+ ".", e);
				}
			}
		}

	}

	private void initCoreLoader(final ClassLoader pParent) {
		synchronized (ReloadableComponentClassLoader.class) {

			final File componentRepository = new File(
					this.componentRepositoryPath);

			final File[] commonAssets = componentRepository
					.listFiles(new FileFilter() {

						// accept jar files only.
						public boolean accept(File file) {
							return file.getName().endsWith(".jar");
						}

					});

			final File[] componentAssets = componentRepository
					.listFiles(new FileFilter() {
						// accept directory only.
						public boolean accept(File file) {
							return file.isDirectory();
						}
					});
			for (int i = 0; i < componentAssets.length; i++) {
				final File componentAsset = componentAssets[i];
				final String directoryName = componentAsset.getName();

				final StringBuilder sb = new StringBuilder();
				sb.append(this.componentRepositoryPath);
				if (!this.componentRepositoryPath.endsWith("/")) {
					sb.append("/");
				}
				sb.append(directoryName);

				final String[] targetClasspath = new String[commonAssets.length + 1];

				for (int j = 0; j < commonAssets.length; j++) {
					final String jarFileName = commonAssets[j].getName();

					final StringBuilder jarSb = new StringBuilder();
					jarSb.append(this.componentRepositoryPath);
					if (!this.componentRepositoryPath.endsWith("/")) {
						jarSb.append("/");
					}
					jarSb.append(jarFileName);

					targetClasspath[j] = jarSb.toString();
				}

				targetClasspath[commonAssets.length] = sb.toString();

				final ComponentClassLoader targetLoader = new ComponentClassLoader(
						this.parent, targetClasspath);
				this.coreLoaders.put(directoryName, targetLoader);
				this.initComponentApplicationContext(directoryName,
						targetLoader);
			}
		}
		System.gc();
	}

	private void initComponentApplicationContext(final String serviceName,
			final ComponentClassLoader targetLoader) {
		final ClassLoader preservedClassLoader = Thread.currentThread()
				.getContextClassLoader();

		Thread.currentThread().setContextClassLoader(targetLoader);

		try {
			final ClassPathXmlApplicationContext componentContext = new ClassPathXmlApplicationContext(
					new String[] { serviceName + "-service.xml" }, false,
					this.parentAppContext);
			componentContext.setClassLoader(targetLoader);
			componentContext.refresh();

			this.componentContexts.put(serviceName, componentContext);
		} catch (BeansException e) {
			throw new IllegalStateException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(preservedClassLoader);
		}
	}

	protected void finalize() throws Throwable {
		for (int i = 0; i < this.watchers.size(); i++) {
			this.watchers.get(i).stop();
		}
		super.finalize();
	}

	public static ReloadableComponentClassLoader getInstance(
			final ClassLoader parent, final String[] pClasspaths,
			final long reloadInterval, final ApplicationContext parentAppContext) {
		if (null != ReloadableComponentClassLoader.instance) {
			return ReloadableComponentClassLoader.instance;
		} else {
			ReloadableComponentClassLoader.instance = new ReloadableComponentClassLoader(
					parent, pClasspaths, reloadInterval, parentAppContext);
			return ReloadableComponentClassLoader.instance;
		}
	}

	public ClassLoader getComponentClassLoader(final String serviceName) {
		return this.coreLoaders.get(serviceName);
	}

	public Object loadComponentImplementation(final String serviceName,
			final String serviceInterfaceName,
			final ApplicationContext parentContext) {
		final ClassLoader loader = this.coreLoaders.get(serviceName);
		final ClassLoader preservedClassLoader = Thread.currentThread()
				.getContextClassLoader();

		Thread.currentThread().setContextClassLoader(loader);

		Object result = null;

		try {
			result = this.componentContexts.get(serviceName).getBean(
					loader.loadClass(serviceInterfaceName));
		} catch (BeansException e) {
			result = null;
			throw new IllegalStateException(e);
		} catch (ClassNotFoundException e) {
			result = null;
			throw new IllegalStateException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(preservedClassLoader);
		}
		return result;
	}

	private class ComponentRepositoryWatcher extends
			FileAlterationListenerAdaptor implements Serializable {

		private static final long serialVersionUID = 1L;

		private final FileAlterationMonitor monitor;

		public ComponentRepositoryWatcher(final String folderPath,
				final long pollingInterval) {
			try {
				final File folder = new File(folderPath);
				this.monitor = new FileAlterationMonitor(pollingInterval);
				final FileAlterationObserver observer = new FileAlterationObserver(
						folder);
				observer.addListener(this);
				monitor.addObserver(observer);
			} catch (Exception e) {
				throw new IllegalStateException(
						"Fail initialize watcher on component repository: "
								+ folderPath + ".", e);
			}
		}

		/**
		 * start monitoring.
		 * 
		 * @throws Exception
		 */
		public void start() throws Exception {
			this.monitor.start();
		}

		/**
		 * stop monitoring.
		 * 
		 * @throws Exception
		 */
		public void stop() throws Exception {
			this.monitor.stop();
		}

		// Is triggered when a file is created in the monitored folder
		@Override
		public void onFileCreate(File file) {
			if (file.isDirectory()) {
				final String directoryName = file.getName();

				final File componentRepository = new File(
						componentRepositoryPath);
				final File[] commonAssets = componentRepository
						.listFiles(new FileFilter() {
							// accept jar files only.
							public boolean accept(File file) {
								return file.getName().endsWith(".jar");
							}
						});

				final StringBuilder sb = new StringBuilder();
				sb.append(componentRepositoryPath);
				if (!componentRepositoryPath.endsWith("/")) {
					sb.append("/");
				}
				sb.append(directoryName);

				final String[] targetClasspath = new String[commonAssets.length + 1];

				for (int j = 0; j < commonAssets.length; j++) {
					final String jarFileName = commonAssets[j].getName();

					final StringBuilder jarSb = new StringBuilder();
					jarSb.append(componentRepositoryPath);
					if (!componentRepositoryPath.endsWith("/")) {
						jarSb.append("/");
					}
					jarSb.append(jarFileName);

					targetClasspath[j] = jarSb.toString();
				}

				targetClasspath[commonAssets.length] = sb.toString();

				final ComponentClassLoader targetLoader = new ComponentClassLoader(
						parent, targetClasspath);
				coreLoaders.put(directoryName, targetLoader);
				initComponentApplicationContext(directoryName, targetLoader);
			} else if (file.getName().endsWith(".jar")) {
				initCoreLoader(parent);
			}
			System.gc();
		}

		// Is triggered when a file is deleted from the monitored folder
		@Override
		public void onFileDelete(File file) {
			if (file.isDirectory()) {
				final String directoryName = file.getName();
				coreLoaders.remove(directoryName);

				componentContexts.get(directoryName).close();
				componentContexts.remove(directoryName);
			} else if (file.getName().endsWith(".jar")) {
				initCoreLoader(parent);
			}
			System.gc();
		}
	}

}
