package com.czelink.intg.utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ClassUtils;

public final class DbAccessUtil {

	/**
	 * private constructor.
	 */
	private DbAccessUtil() {
		// no-op;
	}

	/**
	 * transform object through classLoader.
	 * 
	 * @param obj
	 *            - only accept Object type, no Object array, no collection type
	 *            is acceptable.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 */
	public static Object transformThroughLoader(final Object obj,
			final ClassLoader loader) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			IntrospectionException, InvocationTargetException {
		if (null == obj) {
			return obj;
		} else if (null != loader) {

			if (ClassUtils.isPrimitiveOrWrapper(obj.getClass())
					|| StringUtils.equals(String.class.getName(), obj
							.getClass().getName())) {
				// work for primitive (or wrapper) types.
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(obj);

				final ByteArrayInputStream bis = new ByteArrayInputStream(
						bos.toByteArray());
				ObjectInputStream in = new ObjectInputStream(bis);

				return in.readObject();
			} else if (ClassUtils.isPrimitiveArray(obj.getClass())
					|| ClassUtils.isPrimitiveWrapperArray(obj.getClass())) {

				// handle with primitive array type.
				final int length = Array.getLength(obj);
				final Object newArray = Array.newInstance(obj.getClass()
						.getComponentType(), length);
				for (int i = 0; i < length; i++) {
					final Object arrayElement = Array.get(obj, i);
					Array.set(newArray, i,
							DbAccessUtil.transformThroughLoader(arrayElement));
				}

				return newArray;
			} else if (obj.getClass().isArray()) {

				// handle with custom class element array type.
				final int length = Array.getLength(obj);
				final Class targetClass = loader.loadClass(obj.getClass()
						.getComponentType().getName());
				final Object newArray = Array.newInstance(targetClass, length);
				for (int i = 0; i < length; i++) {
					final Object arrayElement = Array.get(obj, i);
					Array.set(newArray, i, DbAccessUtil.transformThroughLoader(
							arrayElement, loader));
				}

				return newArray;
			} else {
				// work for custom class type - which should have loader
				// provided.
				final Class transformedClass = loader.loadClass(obj.getClass()
						.getName());

				System.out.println("transformedClass: " + transformedClass);

				final Object result = transformedClass.newInstance();

				for (PropertyDescriptor pd : Introspector.getBeanInfo(
						obj.getClass()).getPropertyDescriptors()) {
					if (pd.getReadMethod() != null
							&& !"class".equals(pd.getName())) {
						final Object targetValue = DbAccessUtil
								.transformThroughLoader(pd.getReadMethod()
										.invoke(obj), loader);
						new PropertyDescriptor(pd.getName(), transformedClass)
								.getWriteMethod().invoke(result, targetValue);
					}
				}

				return result;
			}
		} else {
			if (ClassUtils.isPrimitiveOrWrapper(obj.getClass())) {
				// work for primitive (or wrapper) types.
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(obj);

				final ByteArrayInputStream bis = new ByteArrayInputStream(
						bos.toByteArray());
				ObjectInputStream in = new ObjectInputStream(bis);
				return in.readObject();
			} else {
				throw new IllegalStateException(
						"ClassLoader should be provided for custom classes.");
			}
		}
	}

	/**
	 * This method is only work for primitive type.
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 */
	public static Object transformThroughLoader(final Object obj)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			IntrospectionException, InvocationTargetException {
		return DbAccessUtil.transformThroughLoader(obj, null);
	}
}
