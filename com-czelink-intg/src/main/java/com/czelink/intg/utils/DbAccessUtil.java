package com.czelink.intg.utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.reflect.FieldUtils;
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

			if (!ClassUtils.isPrimitiveOrWrapper(obj.getClass())) {
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
			} else if (ClassUtils.isPrimitiveArray(obj.getClass())
					|| ClassUtils.isPrimitiveWrapperArray(obj.getClass())) {
				final Object[] targetArray = (Object[]) obj;
				// TODO: to do with primitive array
				return null;
			} else if (obj.getClass().isArray()) {
				// TODO: to do with custom class array type.
				return null;
			} else {
				// work for primitive (or wrapper) types.
				final ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				out.writeObject(obj);

				final ByteArrayInputStream bis = new ByteArrayInputStream(
						bos.toByteArray());
				ObjectInputStream in = new ObjectInputStream(bis);
				return in.readObject();
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
