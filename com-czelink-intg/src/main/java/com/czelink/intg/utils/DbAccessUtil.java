package com.czelink.intg.utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.ClassUtils;

public final class DbAccessUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * private constructor.
	 */
	private DbAccessUtil() {
		// no-op;
	}

	public static void resyncInputParameterStatusAfterProcess(
			final Object[] processed, final Object[] input)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException,
			IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, IntrospectionException,
			InvocationTargetException {
		for (int i = 0; i < processed.length; i++) {
			final Object processedElem = processed[i];
			final Object inputElem = input[i];
			if (ClassUtils.isPrimitiveWrapperArray(processedElem.getClass())
					|| ClassUtils.isPrimitiveArray(processedElem.getClass())) {
				// handle primitive/wrapper array.
				final int length = Array.getLength(processedElem);
				for (int j = 0; j < length; j++) {
					final Object targetValue = Array.get(processedElem, j);
					Array.set(inputElem, j, targetValue);
				}
			} else if (processedElem.getClass().isArray()) {
				// handle custom object array.
				final int length = Array.getLength(processedElem);
				for (int j = 0; j < length; j++) {
					final Object targetValue = DbAccessUtil
							.transformThroughLoader(
									Array.get(processedElem, j),
									DbAccessUtil.class.getClassLoader());
					Array.set(inputElem, j, targetValue);
				}
			} else if (List.class.isAssignableFrom(processedElem.getClass())) {
				// handle list
				final List processedList = (List) processedElem;
				final List inputList = (List) inputElem;
				inputList.clear();
				final int size = processedList.size();
				for (int j = 0; j < size; j++) {
					final Object targetValue = DbAccessUtil
							.transformThroughLoader(processedList.get(j),
									DbAccessUtil.class.getClassLoader());
					inputList.add(targetValue);
				}
				input[i] = inputList;
			} else if (Set.class.isAssignableFrom(processedElem.getClass())) {
				// handle set
				final Set processedSet = (Set) processedElem;
				final Set inputSet = (Set) inputElem;
				inputSet.clear();
				for (final Iterator iterator = inputSet.iterator(); iterator
						.hasNext();) {
					final Object targetValue = DbAccessUtil
							.transformThroughLoader(iterator.next(),
									DbAccessUtil.class.getClassLoader());
					inputSet.add(targetValue);
				}
				input[i] = inputSet;
			} else if (Map.class.isAssignableFrom(processedElem.getClass())) {
				// handle map
				final Map processedMap = (Map) processedElem;
				final Map inputMap = (Map) inputElem;
				inputMap.clear();
				final Set processedEntrySet = processedMap.entrySet();
				for (final Iterator iterator = processedEntrySet.iterator(); iterator
						.hasNext();) {
					final Entry processedEntry = (Entry) iterator.next();
					final Object targetKey = DbAccessUtil
							.transformThroughLoader(processedEntry.getKey(),
									DbAccessUtil.class.getClassLoader());
					final Object targetValue = DbAccessUtil
							.transformThroughLoader(processedEntry.getValue(),
									DbAccessUtil.class.getClassLoader());
					inputMap.put(targetKey, targetValue);
				}
				input[i] = inputMap;
			} else if (ClassUtils
					.isPrimitiveOrWrapper(processedElem.getClass())) {
				// primitive class
				input[i] = DbAccessUtil.transformThroughLoader(inputElem);
			} else {
				// custom class
				input[i] = DbAccessUtil.transformThroughLoader(inputElem,
						DbAccessUtil.class.getClassLoader());
			}
		}
	}

	public static Class safeLoadClass(final Class rawClass,
			final ClassLoader loader) throws ClassNotFoundException {

		Class result = rawClass;
		if (Map.class.isAssignableFrom(rawClass)) {
			result = Map.class;
		} else if (List.class.isAssignableFrom(rawClass)) {
			result = List.class;
		} else if (Set.class.isAssignableFrom(rawClass)) {
			result = Set.class;
		} else if (ClassUtils.isPrimitiveArray(rawClass)) {
			result = rawClass.getComponentType();
		} else if (rawClass.isArray()) {
			result = Array.newInstance(rawClass.getComponentType(), 0)
					.getClass();
		} else if (!ClassUtils.isPrimitiveOrWrapper(rawClass)) {
			result = loader.loadClass(rawClass.getName());
		}

		return result;
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
