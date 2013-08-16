package com.czelink.dbaccess.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class ReloadableComponentClassLoader extends ClassLoader {

	private static ClassLoader instance;

	private ClassLoader parent;

	private String[] classpaths;

	private ComponentClassLoader core;

	/**
	 * repository watcher.
	 */
	private List<ComponentRepositoryWatcher> watchers = new ArrayList<ComponentRepositoryWatcher>();

	private ReloadableComponentClassLoader(final ClassLoader pParent,
			final String[] pClasspaths, final long reloadInterval) {

		this.parent = pParent;
		this.classpaths = pClasspaths;

		this.initCoreLoader(pParent, pClasspaths);

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

	private void initCoreLoader(final ClassLoader pParent,
			final String[] pClasspaths) {
		synchronized (ReloadableComponentClassLoader.class) {
			this.core = new ComponentClassLoader(this.parent, this.classpaths);
		}
		System.gc();
	}

	protected void finalize() throws Throwable {
		for (int i = 0; i < this.watchers.size(); i++) {
			this.watchers.get(i).stop();
		}
		super.finalize();
	}

	public static ClassLoader getInstance(final ClassLoader parent,
			final String[] pClasspaths, final long reloadInterval) {
		if (null != ReloadableComponentClassLoader.instance) {
			return ReloadableComponentClassLoader.instance;
		} else {
			ReloadableComponentClassLoader.instance = new ReloadableComponentClassLoader(
					parent, pClasspaths, reloadInterval);
			return ReloadableComponentClassLoader.instance;
		}
	}

	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return this.core.loadClass(name);
	}

	private class ComponentRepositoryWatcher extends
			FileAlterationListenerAdaptor {

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
			initCoreLoader(parent, classpaths);
		}

		// Is triggered when a file is deleted from the monitored folder
		@Override
		public void onFileDelete(File file) {
			initCoreLoader(parent, classpaths);
		}
	}

}
