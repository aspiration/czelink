package com.czelink.dbaccess.loader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
	 * constructor.
	 * 
	 * @param pClasspaths
	 */
	protected ComponentClassLoader(final ClassLoader parent,
			final String[] pClasspaths) {

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
		}
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

				final JarFile jarFile = new JarFile(jarPath);

				final Enumeration<JarEntry> emumeration = jarFile.entries();
				while (emumeration.hasMoreElements()) {
					final JarEntry jarEntry = emumeration.nextElement();
				}

				final JarEntry jarEntry = (JarEntry) jarFile
						.getEntry(classFileRelativePath);

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
			bis.read(clazzbytes);
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

}
