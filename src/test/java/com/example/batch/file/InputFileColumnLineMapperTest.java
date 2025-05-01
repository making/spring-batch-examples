package com.example.batch.file;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.batch.item.file.LineMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Test class for {@link InputFileColumnLineMapper}.
 */
public class InputFileColumnLineMapperTest {

	@Nested
	@DisplayName("CSV File Mapping Tests")
	class CsvMappingTests {

		@Test
		@DisplayName("Should map CSV line to object with basic fields")
		void shouldMapCsvLineToObjectWithBasicFields() throws Exception {
			// Given
			LineMapper<TestCsvBean> mapper = new InputFileColumnLineMapper<>(TestCsvBean.class);
			String line = "John,30,true";

			// When
			TestCsvBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("John");
			assertThat(result.getAge()).isEqualTo(30);
			assertThat(result.isActive()).isTrue();
		}

		@Test
		@DisplayName("Should handle custom delimiter")
		void shouldHandleCustomDelimiter() throws Exception {
			// Given
			LineMapper<TestCsvBean> mapper = new InputFileColumnLineMapper<>(TestCsvBean.class, ";");
			String line = "Jane;25;false";

			// When
			TestCsvBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("Jane");
			assertThat(result.getAge()).isEqualTo(25);
			assertThat(result.isActive()).isFalse();
		}

		@Test
		@DisplayName("Should handle enclosed values")
		void shouldHandleEnclosedValues() throws Exception {
			// Given
			LineMapper<TestEnclosedCsvBean> mapper = new InputFileColumnLineMapper<>(TestEnclosedCsvBean.class);
			String line = "\"Smith, John\",35,\"2023/01/15\"";

			// When
			TestEnclosedCsvBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("Smith, John");
			assertThat(result.getAge()).isEqualTo(35);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			assertThat(dateFormat.format(result.getBirthDate())).isEqualTo("2023/01/15");
		}

		@Test
		@DisplayName("Should handle trimming")
		void shouldHandleTrimming() throws Exception {
			// Given
			LineMapper<TestTrimBean> mapper = new InputFileColumnLineMapper<>(TestTrimBean.class);
			String line = "  John  ,###30###,***true***";

			// When
			TestTrimBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("John");
			assertThat(result.getAge()).isEqualTo(30);
			assertThat(result.isActive()).isTrue();
		}

		@Test
		@DisplayName("Should handle string conversion")
		void shouldHandleStringConversion() throws Exception {
			// Given
			LineMapper<TestStringConversionBean> mapper = new InputFileColumnLineMapper<>(
					TestStringConversionBean.class);
			String line = "john doe,USER12345";

			// When
			TestStringConversionBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("John Doe");
			assertThat(result.getUserId()).isEqualTo("user12345");
		}

	}

	@Nested
	@DisplayName("Fixed Length File Mapping Tests")
	class FixedLengthMappingTests {

		@Test
		@DisplayName("Should map fixed-length line to object")
		void shouldMapFixedLengthLineToObject() throws Exception {
			// Given
			LineMapper<TestFixedLengthBean> mapper = new InputFileColumnLineMapper<>(TestFixedLengthBean.class);
			String line = "John      301"; // Name (10 bytes), Age (2 bytes), Active (1
			// byte)

			// When
			TestFixedLengthBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("John");
			assertThat(result.getAge()).isEqualTo(30);
			assertThat(result.isActive()).isTrue();
		}

		@Test
		@DisplayName("Should handle padding")
		void shouldHandlePadding() throws Exception {
			// Given
			LineMapper<TestPaddingBean> mapper = new InputFileColumnLineMapper<>(TestPaddingBean.class);
			String line = "Smith     000125.50";

			// When
			TestPaddingBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("Smith");
			assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("125.50"));
		}

	}

	@Nested
	@DisplayName("Error Handling Tests")
	class ErrorHandlingTests {

		@Test
		@DisplayName("Should handle missing columns")
		void shouldHandleMissingColumns() throws Exception {
			// Given
			LineMapper<TestCsvBean> mapper = new InputFileColumnLineMapper<>(TestCsvBean.class);
			String line = "John,30";

			// When
			TestCsvBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("John");
			assertThat(result.getAge()).isEqualTo(30);
			assertThat(result.isActive()).isFalse(); // Default value for boolean
			// primitive
		}

		@Test
		@DisplayName("Should handle type conversion errors")
		void shouldHandleTypeConversionErrors() {
			// Given
			LineMapper<TestCsvBean> mapper = new InputFileColumnLineMapper<>(TestCsvBean.class);
			String line = "John,notANumber,true";

			// Then
			assertThatExceptionOfType(Exception.class).isThrownBy(() -> mapper.mapLine(line, 1));
		}

	}

	// Test Bean classes

	public static class TestCsvBean {

		@InputFileColumn(columnIndex = 0)
		private String name;

		@InputFileColumn(columnIndex = 1)
		private int age;

		@InputFileColumn(columnIndex = 2)
		private boolean active;

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public boolean isActive() {
			return active;
		}

	}

	public static class TestEnclosedCsvBean {

		@InputFileColumn(columnIndex = 0, columnEncloseChar = '"')
		private String name;

		@InputFileColumn(columnIndex = 1)
		private int age;

		@InputFileColumn(columnIndex = 2, columnFormat = "yyyy/MM/dd", columnEncloseChar = '"')
		private Date birthDate;

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public Date getBirthDate() {
			return birthDate;
		}

	}

	public static class TestTrimBean {

		@InputFileColumn(columnIndex = 0, trimType = TrimType.BOTH, trimChar = ' ')
		private String name;

		@InputFileColumn(columnIndex = 1, trimType = TrimType.BOTH, trimChar = '#')
		private int age;

		@InputFileColumn(columnIndex = 2, trimType = TrimType.BOTH, trimChar = '*')
		private boolean active;

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public boolean isActive() {
			return active;
		}

	}

	public static class TestFixedLengthBean {

		@InputFileColumn(columnIndex = 0, bytes = 10, trimType = TrimType.RIGHT, trimChar = ' ')
		private String name;

		@InputFileColumn(columnIndex = 1, bytes = 2)
		private int age;

		@InputFileColumn(columnIndex = 2, bytes = 1)
		private boolean active;

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public boolean isActive() {
			return active;
		}

	}

	public static class TestPaddingBean {

		@InputFileColumn(columnIndex = 0, bytes = 10, trimType = TrimType.RIGHT, trimChar = ' ')
		private String name;

		@InputFileColumn(columnIndex = 1, bytes = 8, paddingType = PaddingType.LEFT, paddingChar = '0')
		private BigDecimal amount;

		public String getName() {
			return name;
		}

		public BigDecimal getAmount() {
			return amount;
		}

	}

	public static class TestStringConversionBean {

		@InputFileColumn(columnIndex = 0, stringConverter = CapitalizeConverter.class)
		private String name;

		@InputFileColumn(columnIndex = 1, stringConverter = LowercaseConverter.class)
		private String userId;

		public String getName() {
			return name;
		}

		public String getUserId() {
			return userId;
		}

	}

	public static class CapitalizeConverter implements Function<String, String> {

		@Override
		public String apply(String s) {
			if (s == null || s.isEmpty())
				return s;
			String[] words = s.split("\\s+");
			StringBuilder result = new StringBuilder();

			for (int i = 0; i < words.length; i++) {
				if (i > 0)
					result.append(" ");
				String word = words[i];
				if (!word.isEmpty()) {
					result.append(Character.toUpperCase(word.charAt(0)));
					if (word.length() > 1) {
						result.append(word.substring(1).toLowerCase());
					}
				}
			}

			return result.toString();
		}

	}

	public static class LowercaseConverter implements Function<String, String> {

		@Override
		public String apply(String s) {
			return s != null ? s.toLowerCase() : null;
		}

	}

	public static class NoOpConverter implements Function<String, String> {

		@Override
		public String apply(String s) {
			return s;
		}

	}

	@Nested
	@DisplayName("Enclosed Values Tests")
	class EnclosedValuesTests {

		@Test
		@DisplayName("Should handle mixed enclosing characters")
		void shouldHandleMixedEnclosingCharacters() throws Exception {
			// Given
			LineMapper<TestMixedEnclosedCsvBean> mapper = new InputFileColumnLineMapper<>(
					TestMixedEnclosedCsvBean.class);
			String line = "\"Smith, John\",35,'2023/01/15',|Product, Description|";

			// When
			TestMixedEnclosedCsvBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("Smith, John");
			assertThat(result.getAge()).isEqualTo(35);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			assertThat(dateFormat.format(result.getBirthDate())).isEqualTo("2023/01/15");

			assertThat(result.getDescription()).isEqualTo("Product, Description");
		}

		@Test
		@DisplayName("Should handle enclosing character within enclosed text")
		void shouldHandleEnclosingCharacterWithinEnclosedText() throws Exception {
			// Given
			LineMapper<TestComplexEnclosedBean> mapper = new InputFileColumnLineMapper<>(TestComplexEnclosedBean.class);
			String line = "\"Smith \"\"John\"\"\",35";

			// When
			TestComplexEnclosedBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("Smith \"John\"");
			assertThat(result.getAge()).isEqualTo(35);
		}

		@Disabled("NOT SUPPORTED")
		@Test
		@DisplayName("Should handle unclosed enclosing character")
		void shouldHandleUnclosedEnclosingCharacter() throws Exception {
			// Given
			LineMapper<TestEnclosedCsvBean> mapper = new InputFileColumnLineMapper<>(TestEnclosedCsvBean.class);
			String line = "\"Smith, John,35,\"2023/01/15\"";

			// When
			TestEnclosedCsvBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			// The first value will contain the unclosed quote and subsequent content
			// until the next enclosing character
			assertThat(result.getName()).isEqualTo("Smith, John,35");
			// Date should still be parsed correctly
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			assertThat(dateFormat.format(result.getBirthDate())).isEqualTo("2023/01/15");
		}

	}

	@Nested
	@DisplayName("Advanced Processing Tests")
	class AdvancedProcessingTests {

		@Test
		@DisplayName("Should handle combination of trimming and padding")
		void shouldHandleCombinationOfTrimmingAndPadding() throws Exception {
			// Given
			LineMapper<TestCombinedProcessingBean> mapper = new InputFileColumnLineMapper<>(
					TestCombinedProcessingBean.class);
			String line = "  John  ,0000100.50";

			// When
			TestCombinedProcessingBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("John");
			assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.50"));
		}

		@Test
		@DisplayName("Should apply custom string converter")
		void shouldApplyCustomStringConverter() throws Exception {
			// Given
			LineMapper<TestAdvancedStringConversionBean> mapper = new InputFileColumnLineMapper<>(
					TestAdvancedStringConversionBean.class);
			String line = "john.doe@example.com,JOHN DOE,123-45-6789";

			// When
			TestAdvancedStringConversionBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
			assertThat(result.getName()).isEqualTo("John Doe"); // Properly capitalized
			assertThat(result.getSsn()).isEqualTo("XXX-XX-6789"); // Masked SSN
		}

		@Test
		@DisplayName("Should handle null and empty values")
		void shouldHandleNullAndEmptyValues() throws Exception {
			// Given
			LineMapper<TestOptionalFieldsBean> mapper = new InputFileColumnLineMapper<>(TestOptionalFieldsBean.class);
			String line = "John,,";

			// When
			TestOptionalFieldsBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getName()).isEqualTo("John");
			assertThat(result.getEmail()).isNull();
			assertThat(result.getPhone()).isNull();
		}

	}

	@Nested
	@DisplayName("Date and Number Formatting Tests")
	class DateAndNumberFormattingTests {

		@Test
		@DisplayName("Should parse dates with specified format")
		void shouldParseDatesWithSpecifiedFormat() throws Exception {
			// Given
			LineMapper<TestDateFormatsBean> mapper = new InputFileColumnLineMapper<>(TestDateFormatsBean.class, "\t");
			String line = "2023/01/15\t01-15-2023\tJan 15, 2023";

			// When
			TestDateFormatsBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();

			Calendar cal = Calendar.getInstance();
			cal.setTime(result.getDate1());
			assertThat(cal.get(Calendar.YEAR)).isEqualTo(2023);
			assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
			assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);

			cal.setTime(result.getDate2());
			assertThat(cal.get(Calendar.YEAR)).isEqualTo(2023);
			assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
			assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);

			cal.setTime(result.getDate3());
			assertThat(cal.get(Calendar.YEAR)).isEqualTo(2023);
			assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
			assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
		}

		@Test
		@DisplayName("Should parse numbers with specified format")
		void shouldParseNumbersWithSpecifiedFormat() throws Exception {
			// Given
			LineMapper<TestNumberFormatsBean> mapper = new InputFileColumnLineMapper<>(TestNumberFormatsBean.class,
					"\t");
			String line = "1,234.56\t1234.56\t1,234";

			// When
			TestNumberFormatsBean result = mapper.mapLine(line, 1);

			// Then
			assertThat(result).isNotNull();
			assertThat(result.getAmount1()).isEqualByComparingTo(new BigDecimal("1234.56"));
			assertThat(result.getAmount2()).isEqualByComparingTo(new BigDecimal("1234.56"));
			assertThat(result.getAmount3()).isEqualTo(1234);
		}

	}

	// Additional test bean classes

	public static class TestMixedEnclosedCsvBean {

		@InputFileColumn(columnIndex = 0, columnEncloseChar = '"')
		private String name;

		@InputFileColumn(columnIndex = 1)
		private int age;

		@InputFileColumn(columnIndex = 2, columnFormat = "yyyy/MM/dd", columnEncloseChar = '\'')
		private Date birthDate;

		@InputFileColumn(columnIndex = 3, columnEncloseChar = '|')
		private String description;

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public Date getBirthDate() {
			return birthDate;
		}

		public String getDescription() {
			return description;
		}

	}

	public static class TestComplexEnclosedBean {

		@InputFileColumn(columnIndex = 0, columnEncloseChar = '"')
		private String name;

		@InputFileColumn(columnIndex = 1)
		private int age;

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

	}

	public static class TestCombinedProcessingBean {

		@InputFileColumn(columnIndex = 0, trimType = TrimType.BOTH, trimChar = ' ')
		private String name;

		@InputFileColumn(columnIndex = 1, paddingType = PaddingType.LEFT, paddingChar = '0')
		private BigDecimal amount;

		public String getName() {
			return name;
		}

		public BigDecimal getAmount() {
			return amount;
		}

	}

	public static class TestAdvancedStringConversionBean {

		@InputFileColumn(columnIndex = 0)
		private String email;

		@InputFileColumn(columnIndex = 1, stringConverter = ProperCaseConverter.class)
		private String name;

		@InputFileColumn(columnIndex = 2, stringConverter = SsnMaskConverter.class)
		private String ssn;

		public String getEmail() {
			return email;
		}

		public String getName() {
			return name;
		}

		public String getSsn() {
			return ssn;
		}

	}

	public static class TestOptionalFieldsBean {

		@InputFileColumn(columnIndex = 0)
		private String name;

		@InputFileColumn(columnIndex = 1)
		private String email;

		@InputFileColumn(columnIndex = 2)
		private String phone;

		public String getName() {
			return name;
		}

		public String getEmail() {
			return email;
		}

		public String getPhone() {
			return phone;
		}

	}

	public static class TestDateFormatsBean {

		@InputFileColumn(columnIndex = 0, columnFormat = "yyyy/MM/dd")
		private Date date1;

		@InputFileColumn(columnIndex = 1, columnFormat = "MM-dd-yyyy")
		private Date date2;

		@InputFileColumn(columnIndex = 2, columnFormat = "MMM dd, yyyy", columnEncloseChar = '"')
		private Date date3;

		public Date getDate1() {
			return date1;
		}

		public Date getDate2() {
			return date2;
		}

		public Date getDate3() {
			return date3;
		}

	}

	public static class TestNumberFormatsBean {

		@InputFileColumn(columnIndex = 0, columnFormat = "#,##0.00")
		private BigDecimal amount1;

		@InputFileColumn(columnIndex = 1)
		private BigDecimal amount2;

		@InputFileColumn(columnIndex = 2, columnFormat = "#,###")
		private int amount3;

		public BigDecimal getAmount1() {
			return amount1;
		}

		public BigDecimal getAmount2() {
			return amount2;
		}

		public int getAmount3() {
			return amount3;
		}

	}

	// Additional string converters

	public static class ProperCaseConverter implements Function<String, String> {

		@Override
		public String apply(String s) {
			if (s == null || s.isEmpty())
				return s;
			String[] words = s.split("\\s+");
			StringBuilder result = new StringBuilder();

			for (int i = 0; i < words.length; i++) {
				if (i > 0)
					result.append(" ");
				String word = words[i].toLowerCase();
				if (!word.isEmpty()) {
					result.append(Character.toUpperCase(word.charAt(0)));
					if (word.length() > 1) {
						result.append(word.substring(1));
					}
				}
			}

			return result.toString();
		}

	}

	public static class SsnMaskConverter implements Function<String, String> {

		@Override
		public String apply(String s) {
			if (s == null || s.isEmpty())
				return s;

			if (s.matches("\\d{3}-\\d{2}-\\d{4}")) {
				return "XXX-XX-" + s.substring(7);
			}

			return s;
		}

	}

}