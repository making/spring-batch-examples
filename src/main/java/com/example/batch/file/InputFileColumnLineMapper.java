package com.example.batch.file;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Implementation of Spring Batch's LineMapper that uses {@link InputFileColumn}
 * annotations to map each line of an input file to a Java object.
 * <p>
 * This LineMapper reads a line from a file, splits it based on the delimiter, and maps
 * each column to the corresponding field in the target class based on the
 * {@link InputFileColumn} annotations. It supports various operations defined in the
 * annotation like trimming, padding, and format conversion.
 * </p>
 *
 * @param <T> The type of object to which each line will be mapped
 */
public class InputFileColumnLineMapper<T> implements LineMapper<T> {

	/**
	 * Target class to map the line data to
	 */
	private final Class<T> targetClass;

	/**
	 * Delimiter used to split the line
	 */
	private final String delimiter;

	/**
	 * Flag indicating whether the input is fixed-length
	 */
	private boolean fixedLength = false;

	/**
	 * Cache for annotated fields to improve performance
	 */
	private final Map<Class<?>, List<FieldInfo>> fieldInfoCache = new ConcurrentHashMap<>();

	/**
	 * Constructs a new InputFileColumnLineMapper with default delimiter (comma)
	 * @param targetClass the class to which each line will be mapped
	 */
	public InputFileColumnLineMapper(Class<T> targetClass) {
		this(targetClass, ",");
	}

	/**
	 * Constructs a new InputFileColumnLineMapper with specified delimiter
	 * @param targetClass the class to which each line will be mapped
	 * @param delimiter the delimiter used to separate columns in the file
	 */
	public InputFileColumnLineMapper(Class<T> targetClass, String delimiter) {
		this.targetClass = targetClass;
		this.delimiter = delimiter;
		// Check if we're dealing with a fixed-length file by examining if all fields
		// with InputFileColumn annotation have the bytes attribute set
		List<FieldInfo> fieldInfos = getFieldInfo(targetClass);
		if (!fieldInfos.isEmpty()) {
			this.fixedLength = fieldInfos.stream().allMatch(fi -> fi.annotation().bytes() > 0);
		}
	}

	/**
	 * {@inheritDoc} Maps a line from the file to an object of the target class based on
	 * {@link InputFileColumn} annotations
	 */
	@Override
	public T mapLine(String line, int lineNumber) throws Exception {
		// Create an instance of the target class
		T item = BeanUtils.instantiateClass(targetClass);

		// Get field info for the target class
		List<FieldInfo> fieldInfos = getFieldInfo(targetClass);

		String[] columns;
		if (line != null) {
			if (fixedLength) {
				// For fixed-length file, extract columns based on byte positions
				columns = extractFixedLengthColumns(line, fieldInfos);
			}
			else {
				// For delimited file, split by delimiter with respect to enclosing
				// characters
				columns = splitLineWithEnclosing(line, fieldInfos);
			}
		}
		else {
			columns = new String[0];
		}

		// Process each field with InputFileColumn annotation
		for (FieldInfo fieldInfo : fieldInfos) {
			Field field = fieldInfo.field();
			InputFileColumn annotation = fieldInfo.annotation();

			int columnIndex = annotation.columnIndex();
			if (columnIndex >= columns.length) {
				// Skip if column index is out of bounds
				continue;
			}

			String columnValue = columns[columnIndex];

			// Process the column value according to annotation settings
			columnValue = processColumnValue(columnValue, annotation);

			// Set the field value
			setFieldValue(item, field, columnValue, annotation);
		}

		return item;
	}

	/**
	 * Extracts columns from a fixed-length line based on the byte positions specified in
	 * annotations
	 * @param line the line to extract columns from
	 * @param fieldInfos the field information with column widths
	 * @return an array of column values
	 */
	protected String[] extractFixedLengthColumns(String line, List<FieldInfo> fieldInfos) {
		String[] columns = new String[fieldInfos.size()];

		int currentPosition = 0;
		for (FieldInfo fieldInfo : fieldInfos) {
			InputFileColumn annotation = fieldInfo.annotation();

			int bytes = annotation.bytes();
			if (bytes <= 0) {
				throw new IllegalArgumentException("Fixed-length field must have a positive 'bytes' value");
			}

			int endPosition = Math.min(currentPosition + bytes, line.length());
			if (currentPosition < line.length()) {
				columns[annotation.columnIndex()] = line.substring(currentPosition, endPosition);
			}
			else {
				columns[annotation.columnIndex()] = "";
			}

			currentPosition += bytes;
		}

		return columns;
	}

	/**
	 * Splits the line into columns based on the delimiter and handles enclosed columns
	 * with various enclosing characters
	 * @param line the line to split
	 * @param fieldInfos the field information with enclosing character settings
	 * @return an array of column values
	 */
	protected String[] splitLineWithEnclosing(String line, List<FieldInfo> fieldInfos) {
		if (line == null || line.isEmpty()) {
			return new String[0];
		}

		// Find the maximum columnIndex to determine the size of the result array
		int maxColumnIndex = -1;
		for (FieldInfo info : fieldInfos) {
			maxColumnIndex = Math.max(maxColumnIndex, info.annotation().columnIndex());
		}

		// Create a map of enclosing characters for each column index
		Map<Integer, Character> encloseChars = new HashMap<>();
		for (FieldInfo info : fieldInfos) {
			char encloseChar = info.annotation().columnEncloseChar();
			if (encloseChar != Character.MIN_VALUE) {
				encloseChars.put(info.annotation().columnIndex(), encloseChar);
			}
		}

		String[] result = new String[maxColumnIndex + 1];
		StringBuilder currentField = new StringBuilder();
		boolean inEnclosedField = false;
		char currentEncloseChar = 0;
		int currentColumnIndex = 0;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (!inEnclosedField) {
				// Check if this is the start of an enclosed field
				Character expectedEncloseChar = encloseChars.get(currentColumnIndex);
				if (expectedEncloseChar != null && c == expectedEncloseChar) {
					inEnclosedField = true;
					currentEncloseChar = c;
					// Keep the enclosing character
					currentField.append(c);
				}
				else if (delimiter.length() == 1 && c == delimiter.charAt(0)) {
					// This is a delimiter, end the current field
					result[currentColumnIndex] = currentField.toString();
					currentField.setLength(0);
					currentColumnIndex++;
				}
				else if (delimiter.length() > 1 && i + delimiter.length() <= line.length()
						&& line.substring(i, i + delimiter.length()).equals(delimiter)) {
					// This is a multi-character delimiter
					result[currentColumnIndex] = currentField.toString();
					currentField.setLength(0);
					currentColumnIndex++;
					i += delimiter.length() - 1; // Skip the rest of the delimiter
				}
				else {
					// Regular character
					currentField.append(c);
				}
			}
			else {
				// We're inside an enclosed field
				if (c == currentEncloseChar) {
					// This might be the end of the enclosed field
					if (i + 1 < line.length() && line.charAt(i + 1) == currentEncloseChar) {
						// This is an escaped enclosing character, include it
						currentField.append(c);
						i++; // Skip the next character
					}
					else {
						// This is the end of the enclosed field
						inEnclosedField = false;
						// Keep the enclosing character
						currentField.append(c);
					}
				}
				else {
					// Regular character inside an enclosed field
					currentField.append(c);
				}
			}
		}

		// Add the last field
		if (currentColumnIndex <= maxColumnIndex) {
			result[currentColumnIndex] = currentField.toString();
		}

		return result;
	}

	/**
	 * Processes the column value according to annotation settings
	 * @param value the column value
	 * @param annotation the InputFileColumn annotation
	 * @return the processed value
	 */
	protected String processColumnValue(String value, InputFileColumn annotation) {
		if (value == null) {
			return null;
		}

		// Remove enclosing characters if specified
		value = removeEnclosingChars(value, annotation);

		// Apply trimming if specified
		value = applyTrimming(value, annotation);

		// Apply padding removal if specified for fixed-length files
		value = removePadding(value, annotation);

		// Apply string conversion if specified
		value = applyStringConversion(value, annotation);

		return value;
	}

	/**
	 * Removes padding from the value according to annotation settings This is mainly used
	 * for fixed-length files
	 * @param value the column value
	 * @param annotation the InputFileColumn annotation
	 * @return the value without padding
	 */
	protected String removePadding(String value, InputFileColumn annotation) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		PaddingType paddingType = annotation.paddingType();
		char paddingChar = annotation.paddingChar();

		if (paddingType == PaddingType.NONE) {
			return value;
		}

		if (paddingType == PaddingType.LEFT) {
			// Remove left padding (right-aligned value)
			int start = 0;
			while (start < value.length() && value.charAt(start) == paddingChar) {
				start++;
			}
			if (start > 0) {
				return value.substring(start);
			}
		}
		else if (paddingType == PaddingType.RIGHT) {
			// Remove right padding (left-aligned value)
			int end = value.length() - 1;
			while (end >= 0 && value.charAt(end) == paddingChar) {
				end--;
			}
			if (end < value.length() - 1) {
				return value.substring(0, end + 1);
			}
		}

		return value;
	}

	/**
	 * Removes enclosing characters from the value if specified in the annotation
	 * @param value the column value
	 * @param annotation the InputFileColumn annotation
	 * @return the value without enclosing characters
	 */
	protected String removeEnclosingChars(String value, InputFileColumn annotation) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		char encloseChar = annotation.columnEncloseChar();

		// If the annotation specifies an enclosing char (not the default MIN_VALUE)
		if (encloseChar != Character.MIN_VALUE) {
			// Normal case: properly enclosed value
			if (value.length() >= 2 && value.charAt(0) == encloseChar
					&& value.charAt(value.length() - 1) == encloseChar) {
				return value.substring(1, value.length() - 1);
			}

			// Handle case where there's only an opening enclosing character
			if (value.length() >= 1 && value.charAt(0) == encloseChar) {
				return value.substring(1);
			}

			// Handle case where there's only a closing enclosing character
			if (value.length() >= 1 && value.charAt(value.length() - 1) == encloseChar) {
				return value.substring(0, value.length() - 1);
			}
		}

		return value;
	}

	/**
	 * Applies trimming to the value according to the annotation settings
	 * @param value the column value
	 * @param annotation the InputFileColumn annotation
	 * @return the trimmed value
	 */
	protected String applyTrimming(String value, InputFileColumn annotation) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		TrimType trimType = annotation.trimType();
		char trimChar = annotation.trimChar();

		if (trimType == TrimType.NONE) {
			return value;
		}

		if (trimType == TrimType.LEFT || trimType == TrimType.BOTH) {
			// Trim left
			int start = 0;
			while (start < value.length() && value.charAt(start) == trimChar) {
				start++;
			}
			if (start > 0) {
				value = value.substring(start);
			}
		}

		if (trimType == TrimType.RIGHT || trimType == TrimType.BOTH) {
			// Trim right
			int end = value.length() - 1;
			while (end >= 0 && value.charAt(end) == trimChar) {
				end--;
			}
			if (end < value.length() - 1) {
				value = value.substring(0, end + 1);
			}
		}

		return value;
	}

	/**
	 * Applies string conversion to the value if specified in the annotation
	 * @param value the column value
	 * @param annotation the InputFileColumn annotation
	 * @return the converted value
	 */
	protected String applyStringConversion(String value, InputFileColumn annotation) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		Class<? extends Function<? super String, ? extends String>> converterClass = annotation.stringConverter();

		if (converterClass == NoOpConverter.class) {
			return value;
		}

		try {
			var converter = converterClass.getDeclaredConstructor().newInstance();
			return converter.apply(value);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to apply string conversion", e);
		}
	}

	/**
	 * Sets the field value with the appropriate type conversion
	 * @param item the target object
	 * @param field the field to set
	 * @param value the string value to set
	 * @param annotation the InputFileColumn annotation
	 */
	protected void setFieldValue(T item, Field field, String value, InputFileColumn annotation) {
		if (value == null || value.isEmpty()) {
			return;
		}

		try {
			ReflectionUtils.makeAccessible(field);
			Class<?> fieldType = field.getType();

			Object convertedValue = convertToFieldType(value, fieldType, annotation);

			if (convertedValue != null) {
				field.set(item, convertedValue);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to set field value: " + field.getName(), e);
		}
	}

	/**
	 * Converts the string value to the field type
	 * @param value the string value
	 * @param fieldType the target field type
	 * @param annotation the InputFileColumn annotation
	 * @return the converted value
	 */
	protected Object convertToFieldType(String value, Class<?> fieldType, InputFileColumn annotation) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		if (String.class.equals(fieldType)) {
			return value;
		}
		else if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
			return parseInteger(value, annotation.columnFormat());
		}
		else if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
			return parseLong(value, annotation.columnFormat());
		}
		else if (Double.class.equals(fieldType) || double.class.equals(fieldType)) {
			return parseDouble(value, annotation.columnFormat());
		}
		else if (Float.class.equals(fieldType) || float.class.equals(fieldType)) {
			return parseFloat(value, annotation.columnFormat());
		}
		else if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
			return parseBoolean(value);
		}
		else if (BigDecimal.class.equals(fieldType)) {
			return parseBigDecimal(value, annotation.columnFormat());
		}
		else if (Date.class.equals(fieldType)) {
			return parseDate(value, annotation.columnFormat());
		}

		return null;
	}

	/**
	 * Parses an integer string according to the specified format
	 * @param value the integer string
	 * @param format the number format
	 * @return the parsed Integer object
	 */
	protected Integer parseInteger(String value, String format) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		if (!StringUtils.hasText(format)) {
			// If no format is specified, use default Integer parsing
			return Integer.valueOf(value);
		}

		try {
			NumberFormat numberFormat = new DecimalFormat(format,
					DecimalFormatSymbols.getInstance(Locale.getDefault()));
			Number parsedNumber = numberFormat.parse(value);
			return parsedNumber.intValue();
		}
		catch (ParseException e) {
			throw new RuntimeException("Failed to parse Integer: " + value + " with format: " + format, e);
		}
	}

	/**
	 * Parses a long string according to the specified format
	 * @param value the long string
	 * @param format the number format
	 * @return the parsed Long object
	 */
	protected Long parseLong(String value, String format) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		if (!StringUtils.hasText(format)) {
			// If no format is specified, use default Long parsing
			return Long.valueOf(value);
		}

		try {
			NumberFormat numberFormat = new DecimalFormat(format,
					DecimalFormatSymbols.getInstance(Locale.getDefault()));
			Number parsedNumber = numberFormat.parse(value);
			return parsedNumber.longValue();
		}
		catch (ParseException e) {
			throw new RuntimeException("Failed to parse Long: " + value + " with format: " + format, e);
		}
	}

	/**
	 * Parses a double string according to the specified format
	 * @param value the double string
	 * @param format the number format
	 * @return the parsed Double object
	 */
	protected Double parseDouble(String value, String format) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		if (!StringUtils.hasText(format)) {
			// If no format is specified, use default Double parsing
			return Double.valueOf(value);
		}

		try {
			NumberFormat numberFormat = new DecimalFormat(format,
					DecimalFormatSymbols.getInstance(Locale.getDefault()));
			Number parsedNumber = numberFormat.parse(value);
			return parsedNumber.doubleValue();
		}
		catch (ParseException e) {
			throw new RuntimeException("Failed to parse Double: " + value + " with format: " + format, e);
		}
	}

	/**
	 * Parses a float string according to the specified format
	 * @param value the float string
	 * @param format the number format
	 * @return the parsed Float object
	 */
	protected Float parseFloat(String value, String format) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		if (!StringUtils.hasText(format)) {
			// If no format is specified, use default Float parsing
			return Float.valueOf(value);
		}

		try {
			NumberFormat numberFormat = new DecimalFormat(format,
					DecimalFormatSymbols.getInstance(Locale.getDefault()));
			Number parsedNumber = numberFormat.parse(value);
			return parsedNumber.floatValue();
		}
		catch (ParseException e) {
			throw new RuntimeException("Failed to parse Float: " + value + " with format: " + format, e);
		}
	}

	/**
	 * Parses a boolean string with multiple format support Recognizes: true/false,
	 * yes/no, y/n, 1/0, T/F
	 * @param value the boolean string
	 * @return the parsed Boolean object
	 */
	protected Boolean parseBoolean(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		value = value.trim().toLowerCase();

		if (value.equals("true") || value.equals("yes") || value.equals("y") || value.equals("1")
				|| value.equals("t")) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}

	/**
	 * Parses a date string according to the specified format
	 * @param value the date string
	 * @param format the date format
	 * @return the parsed Date object
	 */
	protected Date parseDate(String value, String format) {
		if (!StringUtils.hasText(format)) {
			throw new RuntimeException("Date format not specified for date conversion");
		}

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			return dateFormat.parse(value);
		}
		catch (ParseException e) {
			throw new RuntimeException("Failed to parse date: " + value, e);
		}
	}

	/**
	 * Parses a decimal string according to the specified format
	 * @param value the decimal string
	 * @param format the decimal format
	 * @return the parsed BigDecimal object
	 */
	protected BigDecimal parseBigDecimal(String value, String format) {
		if (value == null || value.isEmpty()) {
			return null;
		}

		if (!StringUtils.hasText(format)) {
			// If no format is specified, use default BigDecimal parsing
			return new BigDecimal(value);
		}

		try {
			DecimalFormat decimalFormat = new DecimalFormat(format,
					DecimalFormatSymbols.getInstance(Locale.getDefault()));
			decimalFormat.setParseBigDecimal(true);
			return (BigDecimal) decimalFormat.parse(value);
		}
		catch (ParseException e) {
			throw new RuntimeException("Failed to parse BigDecimal: " + value + " with format: " + format, e);
		}
	}

	/**
	 * Gets the field information for the target class, sorted by column index
	 * @param clazz the target class
	 * @return sorted list of field information
	 */
	protected List<FieldInfo> getFieldInfo(Class<?> clazz) {
		// Return from cache if available
		if (fieldInfoCache.containsKey(clazz)) {
			return fieldInfoCache.get(clazz);
		}

		List<FieldInfo> fieldInfos = new ArrayList<>();

		// Find all fields with InputFileColumn annotation
		ReflectionUtils.doWithFields(clazz, field -> {
			InputFileColumn annotation = field.getAnnotation(InputFileColumn.class);
			if (annotation != null) {
				fieldInfos.add(new FieldInfo(field, annotation));
			}
		});

		// Sort by column index
		fieldInfos.sort(Comparator.comparingInt(fi -> fi.annotation().columnIndex()));

		// Cache the result
		fieldInfoCache.put(clazz, fieldInfos);

		return fieldInfos;
	}

	/**
	 * Helper class to hold field and its InputFileColumn annotation
	 */
	protected record FieldInfo(Field field, InputFileColumn annotation) {

	}

}