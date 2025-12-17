package io.github.madsonpaulo.springcodegenerator.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringUtilTest {

	@Test
	void testOverrideForTable() {
		String tableName = "T999NULL";
		String resolvedName = StringUtil.resolveJavaClassName(null, tableName);
		assertEquals("CompositeKeyTable", resolvedName, "Override should work correctly");
	}

	@Test
	void testTableDescriptionToClassName() {
		String tableName = "T999TEST";
		String tableDescription = "Test table containing many SQL types. Also, all columns have perfect descriptions.";
		String resolvedName = StringUtil.resolveJavaClassName(tableDescription, tableName);
		assertEquals(
				"TestContainingPerfectDescriptions",
				resolvedName,
				"Description should be converted to Java class name");
	}

	@Test
	void testTableCodeFallback() {
		String tableName = "T999TEST";
		String resolvedName = StringUtil.resolveJavaClassName(null, tableName);
		assertEquals("T999Test", resolvedName, "Fallback to table code should work correctly with camelCase");
	}

	@Test
	void testEmptyDescriptionAndNoOverride() {
		String tableName = "T999DOMA";
		String resolvedName = StringUtil.resolveJavaClassName(null, tableName);
		assertEquals(
				"T999Doma",
				resolvedName,
				"Table name should fall back to camelCase if no description or override exists");
	}

	@Test
	void testSnakeCaseTableName() {
		String tableName = "user_profile";
		String resolvedName = StringUtil.resolveJavaClassName(null, tableName);
		assertEquals(
				"UserProfile",
				resolvedName,
				"Snake_case table names should be converted to PascalCase when no description or override exists");
	}

	@ParameterizedTest
	@MethodSource("arguments_resolveJavaAttributeName")
	void testJavaAttributeName(String columnDescription, String columnCode, String expected) {
		String attributeName = StringUtil.resolveJavaAttributeName(columnDescription, columnCode);

		assertEquals(expected, attributeName);
	}

	static Stream<Arguments> arguments_resolveJavaAttributeName() {
		return Stream.of(
				Arguments.of("Identifier Test", "ID_TST", "identifierTest"),
				Arguments.of("Code Domain", "CD_DOM", "codeDomain"),
				Arguments.of("Value Test", "VL_TST", "valueTest"),
				Arguments.of("Name Test", "NM_TST", "nameTest"),
				Arguments.of("Flag Test", "FL_TST", "flagTest"),
				Arguments.of("Datetime Test", "DT_TST", "datetimeTest"),
				Arguments.of("Date Test", "DA_TST", "dateTest"),
				Arguments.of("Time Test", "TM_TST", "timeTest"),
				Arguments.of("Binary Test", "BL_TST", "binaryTest"),
				Arguments.of("This column will be an Integer", "CD_DOM", "codeDom"),
				Arguments.of("This column is FK for two different tables", "CD_DFK", "codeDoubleForeignKey"),
				Arguments.of("And this one will be a String", "NM_DOM", "nameDom"),
				Arguments.of(null, "CD_FPK", "codeFpk"),
				Arguments.of(null, "CD_SPK", "secondPrimaryKey"),
				Arguments.of(null, "NM_NUL", "nameNul"),
				Arguments.of(null, "SQ_VIE", "sqView"),
				Arguments.of(null, "NM_VIE", "nameView"));
	}

}
