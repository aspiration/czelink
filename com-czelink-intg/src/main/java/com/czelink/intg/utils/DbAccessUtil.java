package com.czelink.intg.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class DbAccessUtil {

	/**
	 * private constructor.
	 */
	private DbAccessUtil() {
		// no-op;
	}

	/**
	 * transform object through classloader.
	 * 
	 * @param obj
	 *            - only accept Object type and Object array, no collection type
	 *            is acceptable.
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object transformThroughLoader(final Object obj)
			throws IOException, ClassNotFoundException {
		if (null == obj) {
			return obj;
		} else {
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(obj);

			final ByteArrayInputStream bis = new ByteArrayInputStream(
					bos.toByteArray());
			ObjectInputStream in = new ObjectInputStream(bis);
			return in.readObject();
		}
	}
}
