package com.example.batch.file;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

public class OutputFileColumnLineAggregator<T> implements LineAggregator<T> {

	private final BeanWrapperImpl beanWrapper;

	private final Field[] annotatedFields;

	private final Map<Field, OutputFileColumn> fieldAnnotations = new ConcurrentHashMap<>();

	private final String delimiter;

	public OutputFileColumnLineAggregator(Class<T> targetType) {
		this(targetType, ",");
	}

	public OutputFileColumnLineAggregator(Class<T> targetType, String delimiter) {
		this.beanWrapper = new BeanWrapperImpl();
		// Initializes the extractor by finding and sorting all fields with the
		// OutputFileColumn annotation by their columnIndex.

		// Find all fields with OutputFileColumn annotation
		Field[] allFields = targetType.getDeclaredFields();

		// Filter and collect fields with the annotation
		annotatedFields = Arrays.stream(allFields)
			.filter(field -> field.isAnnotationPresent(OutputFileColumn.class))
			.toArray(Field[]::new);

		// Check if any annotated fields were found
		Assert.notEmpty(annotatedFields,
				"No fields with OutputFileColumn annotation found in class " + targetType.getName());

		// Store annotations for quick access
		for (Field field : annotatedFields) {
			OutputFileColumn annotation = field.getAnnotation(OutputFileColumn.class);
			fieldAnnotations.put(field, annotation);
		}

		// Sort fields by columnIndex
		Arrays.sort(annotatedFields,
				Comparator.comparingInt(field -> field.getAnnotation(OutputFileColumn.class).columnIndex()));

		// Validate that column indices are unique
		int[] indices = Arrays.stream(annotatedFields)
			.mapToInt(field -> field.getAnnotation(OutputFileColumn.class).columnIndex())
			.toArray();

		for (int i = 0; i < indices.length; i++) {
			for (int j = i + 1; j < indices.length; j++) {
				if (indices[i] == indices[j]) {
					throw new IllegalStateException(
							"Duplicate columnIndex " + indices[i] + " found in class " + targetType.getName());
				}
			}
		}
		this.delimiter = delimiter;
	}

	@Override
	public String aggregate(T item) {
		return Arrays.stream(extract(item)).map(x -> Objects.toString(x, "")).collect(Collectors.joining(delimiter));
	}

	/**
	 * Extracts field values from the provided item according to the OutputFileColumn
	 * annotation specifications.
	 * @param item The object to extract field values from
	 * @return An array of field values in the order specified by columnIndex
	 */
	protected Object[] extract(T item) {
		beanWrapper.setWrappedInstance(item);
		Object[] values = new Object[annotatedFields.length];

		for (int i = 0; i < annotatedFields.length; i++) {
			Field field = annotatedFields[i];
			ReflectionUtils.makeAccessible(field);

			OutputFileColumn annotation = fieldAnnotations.get(field);
			String fieldName = field.getName();
			Object value = beanWrapper.getPropertyValue(fieldName);

			// Apply formatting and processing based on annotation
			if (value != null) {
				// Format Date values
				if (value instanceof Date && !annotation.columnFormat().isEmpty()) {
					DateFormatter formatter = new DateFormatter(annotation.columnFormat());
					value = formatter.print((Date) value, Locale.getDefault());
				}
				// Format BigDecimal values
				else if (value instanceof BigDecimal && !annotation.columnFormat().isEmpty()) {
					DecimalFormat formatter = new DecimalFormat(annotation.columnFormat());
					value = formatter.format(value);
				}

				// Apply string conversion if applicable
				var stringConverter = annotation.stringConverter();
				if (value instanceof String && !stringConverter.equals(NoOpConverter.class)) {
					try {
						var converter = stringConverter.getDeclaredConstructor().newInstance();
						value = converter.apply((String) value);
					}
					catch (Exception e) {
						throw new RuntimeException("Error applying string converter", e);
					}
				}

				// Apply trimming if applicable
				if (value instanceof String strValue && annotation.trimType() != TrimType.NONE) {
					char trimChar = annotation.trimChar();
					TrimType trimType = annotation.trimType();
					if (trimType == TrimType.LEFT || trimType == TrimType.BOTH) {
						strValue = trimLeft(strValue, trimChar);
					}
					if (trimType == TrimType.RIGHT || trimType == TrimType.BOTH) {
						strValue = trimRight(strValue, trimChar);
					}
					value = strValue;
				}

				// Apply padding if applicable
				PaddingType paddingType = annotation.paddingType();
				if (annotation.bytes() > 0 && paddingType != PaddingType.NONE) {
					String strValue = value.toString();
					int length = strValue.length();
					int targetLength = annotation.bytes();

					if (length < targetLength) {
						char padChar = annotation.paddingChar();

						if (paddingType == PaddingType.LEFT) {
							// Left pad (right-aligned)
							strValue = padLeft(strValue, targetLength, padChar);
						}
						if (paddingType == PaddingType.RIGHT) {
							// Right pad (left-aligned)
							strValue = padRight(strValue, targetLength, padChar);
						}
						value = strValue;
					}
				}
			}

			// Apply enclosing characters
			if (annotation.columnEncloseChar() != Character.MIN_VALUE) {
				char encloseChar = annotation.columnEncloseChar();
				value = encloseChar + (value == null ? "" : value.toString()) + encloseChar;
			}

			values[i] = value;
		}

		return values;

	}

	/**
	 * Helper method to left-trim the specified character from a string.
	 */
	private String trimLeft(String str, char trimChar) {
		int i = 0;
		while (i < str.length() && str.charAt(i) == trimChar) {
			i++;
		}
		return str.substring(i);
	}

	/**
	 * Helper method to right-trim the specified character from a string.
	 */
	private String trimRight(String str, char trimChar) {
		int i = str.length() - 1;
		while (i >= 0 && str.charAt(i) == trimChar) {
			i--;
		}
		return str.substring(0, i + 1);
	}

	/**
	 * Helper method to left-pad a string with the specified character.
	 */
	private String padLeft(String str, int length, char padChar) {
		return String.valueOf(padChar).repeat(Math.max(0, length - str.length())) + str;
	}

	/**
	 * Helper method to right-pad a string with the specified character.
	 */
	private String padRight(String str, int length, char padChar) {
		return str + String.valueOf(padChar).repeat(Math.max(0, length - str.length()));
	}

}
