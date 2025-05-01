package com.example.batch.file;

import com.example.nyusyukkin.NyusyukkinFileOutput;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for OutputFileColumnLineAggregator Tests various scenarios for field
 * extraction based on OutputFileColumn annotation
 */
class OutputFileColumnLineAggregatorTest {

	private OutputFileColumnLineAggregator<TestPerson> aggregator;

	private TestPerson person;

	@BeforeEach
	void setUp() throws Exception {
		// Initialize the extractor with our test class
		aggregator = new OutputFileColumnLineAggregator<>(TestPerson.class);

		// Initialize date formatter
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		// Create test person with sample data
		person = new TestPerson();
		person.setFirstName("John");
		person.setLastName("Doe, Jr."); // Contains a comma to test enclosing
		person.setBirthDate(dateFormat.parse("1990/01/15"));
		person.setSalary(new BigDecimal("75000.50"));
		person.setEmployeeId("123");
		person.setUpperCaseField("test");
		person.setLowerCaseField("TEST");
		person.setTrimField("  trimmed  ");
	}

	@Test
	@DisplayName("Should aggregate fields in the correct order based on columnIndex")
	void shouldAggregateFieldsInCorrectOrder() {
		// When
		String line = aggregator.aggregate(person);
		// Then
		assertThat(line).isEqualTo("John,\"Doe, Jr.\",1990/01/15,'75,000.50',0000000123,TEST,test,trimmed");
	}

	@Test
	@DisplayName("Should aggregate fields in the correct order based on columnIndex with different delimiter")
	void shouldAggregateFieldsInCorrectOrderWithDifferentDelimiter() {
		// When
		String line = new OutputFileColumnLineAggregator<>(TestPerson.class, "\t").aggregate(person);
		// Then
		assertThat(line).isEqualTo("John\t\"Doe, Jr.\"\t1990/01/15\t'75,000.50'\t0000000123\tTEST\ttest\ttrimmed");
	}

	@Test
	@DisplayName("Should handle null values gracefully")
	void shouldHandleNullValuesGracefully() {
		// Given
		person.setFirstName(null);
		person.setSalary(null);
		// When
		String line = aggregator.aggregate(person);
		// Then
		assertThat(line).isEqualTo(",\"Doe, Jr.\",1990/01/15,'',0000000123,TEST,test,trimmed");
	}

	@Test
	@DisplayName("Should throw exception when duplicate columnIndex is detected")
	void shouldThrowExceptionForDuplicateColumnIndex() {
		// When/Then
		assertThatThrownBy(() -> new OutputFileColumnLineAggregator<>(InvalidPerson.class))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Duplicate columnIndex");
	}

	@Test
	void compatibilityTestWithFileQueryDao() throws Exception {
		NyusyukkinFileOutput dto = new NyusyukkinFileOutput();
		dto.setTorihikibi(new SimpleDateFormat("yyyyMMdd").parse("20111001"));
		dto.setShitenName("東京");
		dto.setNyukinNum(2);
		dto.setSyukkinNum(4);
		dto.setNyukinSum(662349);
		dto.setSyukkinSum(2643052);

		var aggr = new OutputFileColumnLineAggregator<>(NyusyukkinFileOutput.class);
		String line = aggr.aggregate(dto);
		assertThat(line).isEqualTo("20111001,東京,2,4,662349,2643052");
	}

}

/**
 * Test Person class with various OutputFileColumn annotations
 */
class TestPerson {

	@OutputFileColumn(columnIndex = 0)
	private String firstName;

	@OutputFileColumn(columnIndex = 1, columnEncloseChar = '"')
	private String lastName;

	@OutputFileColumn(columnIndex = 2, columnFormat = "yyyy/MM/dd")
	private Date birthDate;

	@OutputFileColumn(columnIndex = 3, columnFormat = "#,###.00", columnEncloseChar = '\'')
	private BigDecimal salary;

	@OutputFileColumn(columnIndex = 4, bytes = 10, paddingType = PaddingType.LEFT, paddingChar = '0')
	private String employeeId;

	@OutputFileColumn(columnIndex = 5, stringConverter = StringConverterToUpperCase.class)
	private String upperCaseField;

	@OutputFileColumn(columnIndex = 6, stringConverter = StringConverterToLowerCase.class)
	private String lowerCaseField;

	@OutputFileColumn(columnIndex = 7, trimType = TrimType.BOTH, trimChar = ' ')
	private String trimField;

	// Getters and setters
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public BigDecimal getSalary() {
		return salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getUpperCaseField() {
		return upperCaseField;
	}

	public void setUpperCaseField(String upperCaseField) {
		this.upperCaseField = upperCaseField;
	}

	public String getLowerCaseField() {
		return lowerCaseField;
	}

	public void setLowerCaseField(String lowerCaseField) {
		this.lowerCaseField = lowerCaseField;
	}

	public String getTrimField() {
		return trimField;
	}

	public void setTrimField(String trimField) {
		this.trimField = trimField;
	}

}

/**
 * Invalid Person class with duplicate columnIndex for testing
 */
class InvalidPerson {

	@OutputFileColumn(columnIndex = 0)
	private String field1;

	@OutputFileColumn(columnIndex = 0) // Duplicate index
	private String field2;

	// Getters and setters
	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2;
	}

}

/**
 * Mock implementation of StringConverter to uppercase for testing
 */
class StringConverterToUpperCase implements Function<String, String> {

	@Override
	public String apply(String input) {
		return input != null ? input.toUpperCase() : null;
	}

}

/**
 * Mock implementation of StringConverter to lowercase for testing
 */
class StringConverterToLowerCase implements Function<String, String> {

	@Override
	public String apply(String input) {
		return input != null ? input.toLowerCase() : null;
	}

}
