package com.czelink.dbaccess.loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class ComponentClassLoader extends ClassLoader {

	/**
	 * scattered classes in classPath directory.
	 */
	private final List<String> scatteredClasses = new ArrayList<String>();

	/**
	 * classPath as jar file.
	 */
	private final List<String> classpathJars = new ArrayList<String>();

	/**
	 * repository watcher.
	 */
	private List<ComponentRepositoryWatcher> watchers = new ArrayList<ComponentRepositoryWatcher>();

	/**
	 * internal instance.
	 */
	private static ComponentClassLoader instance;

	/**
	 * getInstance.
	 * 
	 * @param pClasspaths
	 * @param reloadInterval
	 * @return
	 */
	public static ComponentClassLoader getInstance(final ClassLoader parent,
			final String[] pClasspaths, final long reloadInterval) {
		if (null != ComponentClassLoader.instance) {
			return ComponentClassLoader.instance;
		} else {
			ComponentClassLoader.instance = new ComponentClassLoader(parent,
					pClasspaths, reloadInterval);
			return ComponentClassLoader.instance;
		}
	}

	/**
	 * constructor.
	 * 
	 * @param pClasspaths
	 */
	private ComponentClassLoader(final ClassLoader parent,
			final String[] pClasspaths, final long reloadInterval) {

		super(parent);

		if (null == pClasspaths) {
			throw new IllegalArgumentException("Classpath cannot be null!");
		} else if (0 == pClasspaths.length) {
			throw new IllegalArgumentException(
					"Cannot specify emtpy classpath!");
		}

		for (int i = 0; i < pClasspaths.length; i++) {
			final String classpath = pClasspaths[i];
			this.resolveClasspaths(classpath);

			if (reloadInterval > 0) {
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

		System.out.println("scatteredClasses: " + this.scatteredClasses);
		System.out.println("classpathJars: " + this.classpathJars);
	}

	/**
	 * searching for jar files under the classPath directory.
	 * 
	 * @param classpath
	 */
	private void resolveClasspaths(final String classpath) {
		if (classpath.endsWith(".jar")) {
			this.classpathJars.add(classpath);
		} else if (classpath.endsWith(".class")) {
			this.scatteredClasses.add(classpath);
		} else {
			final File classpathFile = new File(classpath);
			if (classpathFile.isDirectory()) {
				for (File subFile : classpathFile.listFiles()) {
					this.resolveClasspaths(subFile.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * remove classpath.
	 * 
	 * @param classpath
	 */
	private void removeClasspaths(final String classpath) {
		if (classpath.endsWith(".jar")) {
			this.classpathJars.remove(classpath);
		} else if (classpath.endsWith(".class")) {
			this.scatteredClasses.remove(classpath);
		} else {
			final File classpathFile = new File(classpath);
			if (classpathFile.isDirectory()) {
				for (File subFile : classpathFile.listFiles()) {
					this.removeClasspaths(subFile.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * load classFile bytes.
	 * 
	 * @param className
	 * @return bytes array - nullable.
	 */
	private byte[] loadClassFileBytes(String className) {
		byte[] classbytes = null;
		final String classFileRelativePath = className.replace('.', '/')
				.concat(".class");

		try {
			// search in scattered classes.
			final int scatteredClassesSize = this.scatteredClasses.size();
			for (int i = 0; null == classbytes && i < scatteredClassesSize; i++) {
				final String scatteredClass = this.scatteredClasses.get(i);

				System.out.println("scatteredClass: " + scatteredClass);
				System.out.println("classFileRelativePath: "
						+ classFileRelativePath);

				if (scatteredClass.endsWith(classFileRelativePath)) {
					final File classFile = new File(scatteredClass);
					final FileInputStream classFileInputStream = new FileInputStream(
							classFile);
					classbytes = this.readClassBytes(classFileInputStream);
				}
			}

			// search in jar files.
			final int jarsCounts = this.classpathJars.size();
			for (int i = 0; null == classbytes && i < jarsCounts; i++) {
				final String jarPath = this.classpathJars.get(i);

				System.out.println("jarPath: " + jarPath);
				System.out.println("classFileRelativePath: "
						+ classFileRelativePath);

				final JarFile jarFile = new JarFile(jarPath);

				final Enumeration<JarEntry> emumeration = jarFile.entries();
				while (emumeration.hasMoreElements()) {
					final JarEntry jarEntry = emumeration.nextElement();
					System.out.println("Introspect jarEntry: " + jarEntry);
				}

				final JarEntry jarEntry = (JarEntry) jarFile
						.getEntry(classFileRelativePath);
				System.out.println("found jarEntry: " + jarEntry);

				if (null != jarEntry) {
					final InputStream classFileInputStream = jarFile
							.getInputStream(jarEntry);
					classbytes = this.readClassBytes(classFileInputStream);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return classbytes;
	}

	/**
	 * read bytes from class file input stream.
	 * 
	 * @param classFileInputStream
	 * @return byte array - nullable.
	 */
	private byte[] readClassBytes(final InputStream classFileInputStream) {
		final BufferedInputStream bis = new BufferedInputStream(
				classFileInputStream);
		byte[] clazzbytes = null;
		try {
			clazzbytes = new byte[bis.available()];
			int result = bis.read(clazzbytes);

			System.out.println("read result: " + result);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				classFileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return clazzbytes;
	}

	@Override
	protected Class<?> findClass(String className)
			throws ClassNotFoundException {
		Class<?> result = null;
		final byte[] classBytes = this.loadClassFileBytes(className);
		if (null != classBytes) {
			result = this.defineClass(className, classBytes, 0,
					classBytes.length);
		} else {
			throw new ClassNotFoundException(
					"No class defined in classpath as: " + className);
		}
		return result;
	}

	@Override
	protected void finalize() throws Throwable {
		for (int i = 0; i < this.watchers.size(); i++) {
			this.watchers.get(i).stop();
		}
		super.finalize();
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
			try {
				// "file" is the reference to the newly created file
				System.out.println("File created: " + file.getCanonicalPath());
				resolveClasspaths(file.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}

		// Is triggered when a file is deleted from the monitored folder
		@Override
		public void onFileDelete(File file) {
			try {
				// "file" is the reference to the removed file
				System.out.println("File removed: " + file.getCanonicalPath());
				// "file" does not exists anymore in the location
				System.out.println("File still exists in location: "
						+ file.exists());
				removeClasspaths(file.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}

}
