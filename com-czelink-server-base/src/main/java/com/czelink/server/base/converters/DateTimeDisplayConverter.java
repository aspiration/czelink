package com.czelink.server.base.converters;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;
import org.springframework.core.convert.converter.Converter;

public class DateTimeDisplayConverter implements Converter<Date, String> {

	/**
	 * expected output format.
	 */
	private String expectedOutputFormat;

	@Override
	public String convert(Date inputDate) {
		final FastDateFormat fastDateFormat = FastDateFormat
				.getInstance(this.expectedOutputFormat);
		return fastDateFormat.format(inputDate);
	}

	public String getExpectedOutputFormat() {
		return expectedOutputFormat;
	}

	public void setExpectedOutputFormat(String expectedOutputFormat) {
		this.expectedOutputFormat = expectedOutputFormat;
	}

}
