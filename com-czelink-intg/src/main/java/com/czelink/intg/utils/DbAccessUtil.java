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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
			} else if (List.class.isAssignableFrom(obj.getClass())) {
				// handle List type.
				final List sourceList = (List) obj;
				final List targetList = (List) obj.getClass().newInstance();
				final int length = sourceList.size();
				for (int i = 0; i < length; i++) {
					final Object targetObj = DbAccessUtil
							.transformThroughLoader(sourceList.get(i), loader);
					targetList.add(targetObj);
				}
				return targetList;
			} else if (Set.class.isAssignableFrom(obj.getClass())) {
				final Set sourceSet = (Set) obj;
				final Set targetSet = (Set) obj.getClass().newInstance();
				for (final Iterator it = targetSet.iterator(); it.hasNext();) {
					final Object targetObj = DbAccessUtil
							.transformThroughLoader(it.next(), loader);
					targetSet.add(targetObj);
				}
				return targetSet;
			} else if (Map.class.isAssignableFrom(obj.getClass())) {
				// handle map type.
				final Map sourceMap = (Map) obj;
				final Map targetMap = (Map) obj.getClass().newInstance();
				final Set sourceSet = sourceMap.entrySet();
				for (final Iterator it = sourceSet.iterator(); it.hasNext();) {
					final Entry entry = (Entry) it.next();
					final Object targetKey = DbAccessUtil
							.transformThroughLoader(entry.getKey(), loader);
					final Object targetValue = DbAccessUtil
							.transformThroughLoader(entry.getValue(), loader);
					targetMap.put(targetKey, targetValue);
				}
				return targetMap;
			} else {
				// work for custom class type - which should have loader
				// provided.
				final Class transformedClass = loader.loadClass(obj.getClass()
						.getName());

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
