package io.github.madsonpaulo.springcodegenerator.core.utils;

import java.util.Locale;
import java.util.Map;

import org.springframework.util.StringUtils;

public final class ColumnNameUtil {

	private ColumnNameUtil() {
	}

	private static final Map<String, String> ABBREVIATION_MAP = Map.of("CD", "code", "DFK", "doubleForeignKey", "NM",
			"name", "VIE", "view");

	private static final Map<String, String> FINAL_REPLACEMENTS = Map.of("codeSpk", "secondPrimaryKey");

	public static String toJavaFieldName(String columnName) {
		if (!StringUtils.hasText(columnName)) {
			return columnName;
		}

		Locale locale = StringUtil.LOCALE;
		String[] parts = columnName.trim().toLowerCase(locale).split("_");
		StringBuilder fieldName = new StringBuilder();

		for (int i = 0; i < parts.length; i++) {
			String resolvedPart = ABBREVIATION_MAP.getOrDefault(parts[i].toUpperCase(locale), parts[i]);

			fieldName.append(i == 0 ? StringUtil.decapitalizeFirstLetter(resolvedPart)
					: StringUtil.capitalizeFirstLetter(resolvedPart));
		}

		String result = fieldName.toString();
		return FINAL_REPLACEMENTS.getOrDefault(result, result);
	}

}
