package io.github.madsonpaulo.springcodegenerator.core.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.springframework.util.StringUtils;

import io.github.madsonpaulo.springcodegenerator.core.enums.TableNameOverride;

public final class StringUtil {
	public static final Locale LOCALE = Locale.ENGLISH;
	private static final int MAX_WORDS_IN_CLASS_NAME = 4;

	private static final Set<String> UNDESIRED_WORDS = Set.of("table of ", "table from ", "table ", " of ", " and ",
			" the ", " to ", " for ", " by ", " with ", " that ", " between ", " in ", " on ", " domain ");

	private StringUtil() {
	}

	public static String resolveJavaClassName(String tableDescription, String tableName) {
		Optional<String> overridden = TableNameOverride.fromTableName(tableName);
		if (overridden.isPresent()) {
			return overridden.get();
		}

		if (StringUtils.hasText(tableDescription)) {
			String fromDescription = toCamelCase(tableDescription);

			if (StringUtils.hasText(fromDescription)) {
				return capitalizeFirstLetter(fromDescription);
			}
		}

		return toJavaClassNameFromTableCode(tableName);
	}

	public static String resolveJavaAttributeName(String columnDescription, String columnCode) {
		return StringUtils.hasText(columnDescription) && isDescriptionSufficient(columnDescription, columnCode)
				? toCamelCase(columnDescription)
				: ColumnNameUtil.toJavaFieldName(columnCode);
	}

	public static String extractTableName(String qualifiedTableName) {
		if (!StringUtils.hasText(qualifiedTableName)) {
			throw new IllegalArgumentException("Table name must be provided.");
		}

		String normalized = qualifiedTableName.trim().toUpperCase(LOCALE);

		if (normalized.contains(".")) {
			String[] parts = normalized.split("\\.", 2);
			return parts[1];
		}

		return normalized;
	}

	public static String extractDatabaseName(String qualifiedTableName) {
		if (!StringUtils.hasText(qualifiedTableName)) {
			return null;
		}

		String normalized = qualifiedTableName.trim().toUpperCase(LOCALE);

		if (normalized.contains(".")) {
			String[] parts = normalized.split("\\.", 2);
			return parts[0];
		}

		return null;
	}

	public static List<String> splitCommaSeparatedValues(String input) {
		if (!StringUtils.hasText(input)) {
			throw new IllegalArgumentException("The provided comma-separated value list is invalid.");
		}

		List<String> values = Arrays.stream(input.split(",")).map(String::trim).filter(s -> !s.isEmpty()).distinct()
				.toList();

		if (values.isEmpty()) {
			throw new IllegalArgumentException("The provided comma-separated value list is invalid.");
		}

		return values;
	}

	public static String capitalizeFirstLetter(String text) {
		return changeFirstLetterCase(text, true);
	}

	public static String decapitalizeFirstLetter(String text) {
		return changeFirstLetterCase(text, false);
	}

	public static String getPkClassName(String className) {
		return className + "PK";
	}

	public static String getDtoClassName(String className) {
		return className + "Dto";
	}

	public static String getServiceClassName(String className) {
		return className + "Service";
	}

	public static String getRepositoryClassName(String className) {
		return className + "Repository";
	}

	private static String toCamelCase(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		text = Normalizer.normalize(text, Normalizer.Form.NFD);
		text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		text = text.replace("_", " ");
		text = text.replace(".", " ");
		text = text.replace("/", " ");
		text = handleBracketedText(text);
		text = handleHyphenatedText(text);
		text = removeInvalidCharacters(text);
		text = removeUndesiredWords(text);
		text = truncateByWordLimit(text);

		StringBuilder camelCaseString = new StringBuilder();
		boolean nextCharUpperCase = false;

		for (char c : text.toCharArray()) {
			if (Character.isWhitespace(c) || c == '_' || c == '-') {
				nextCharUpperCase = true;
			} else {
				if (nextCharUpperCase) {
					camelCaseString.append(Character.toUpperCase(c));
					nextCharUpperCase = false;
				} else {
					camelCaseString.append(Character.toLowerCase(c));
				}
			}
		}

		camelCaseString.setCharAt(0, Character.toLowerCase(camelCaseString.charAt(0)));

		return camelCaseString.toString();
	}

	private static String truncateByWordLimit(String text) {
		String[] words = text.trim().split("\\s+");

		if (words.length <= MAX_WORDS_IN_CLASS_NAME) {
			return text;
		}

		return "%s %s %s %s".formatted(words[0], words[1], words[words.length - 2], words[words.length - 1]);
	}

	private static String toJavaClassNameFromTableCode(String tableName) {
		if (!StringUtils.hasText(tableName)) {
			return tableName;
		}

		int lastDigitIndex = -1;

		for (int i = 0; i < tableName.length(); i++) {
			if (Character.isDigit(tableName.charAt(i))) {
				lastDigitIndex = i;
			}
		}

		if (lastDigitIndex < 0 || lastDigitIndex == tableName.length() - 1) {
			return capitalizeFirstLetter(tableName.toLowerCase(LOCALE));
		}

		String prefix = tableName.substring(0, lastDigitIndex + 1);
		String suffix = tableName.substring(lastDigitIndex + 1);

		return prefix + capitalizeFirstLetter(suffix.toLowerCase(LOCALE));
	}

	private static String handleBracketedText(String text) {
		if (!StringUtils.hasText(text)) {
			return text;
		}

		text = handleDelimiter(text, '(', ')');
		text = handleDelimiter(text, '[', ']');
		text = handleDelimiter(text, '{', '}');

		return text;
	}

	private static String handleDelimiter(String text, char open, char close) {
		if (!text.contains(String.valueOf(open))) {
			return text;
		}

		if (text.startsWith(String.valueOf(open)) && text.contains(String.valueOf(close))) {
			return text.substring(text.indexOf(close) + 1);
		}

		return text.substring(0, text.indexOf(open));
	}

	private static String handleHyphenatedText(String text) {
		if (!StringUtils.hasText(text) || !text.contains("-")) {
			return text;
		}

		String[] parts = text.split("-");
		String firstPart = parts[0].trim();
		String secondPart = parts[1].trim();

		int firstWordCount = firstPart.split(" ").length;
		int secondWordCount = secondPart.split(" ").length;

		if (firstWordCount == secondWordCount) {
			return firstPart.length() > secondPart.length() ? firstPart : secondPart;
		}

		return firstWordCount > secondWordCount ? firstPart : secondPart;
	}

	private static String removeUndesiredWords(String text) {
		if (!StringUtils.hasText(text)) {
			return text;
		}

		String normalized = text.toLowerCase(LOCALE);

		for (String word : UNDESIRED_WORDS) {
			normalized = normalized.replace(word, " ").trim();
		}

		return normalized;
	}

	private static String removeInvalidCharacters(String text) {
		String sanitized = text.replaceAll("[^a-zA-Z0-9 ]", "");
		return sanitized.replaceFirst("^\\d+", "").trim();
	}

	private static String changeFirstLetterCase(String text, boolean upperCase) {
		if (!StringUtils.hasText(text)) {
			return text;
		}

		String firstChar = text.substring(0, 1);
		String rest = text.substring(1);

		return (upperCase ? firstChar.toUpperCase(Locale.ROOT) : firstChar.toLowerCase(Locale.ROOT)) + rest;
	}

	private static boolean isDescriptionSufficient(String description, String columnCode) {
		if (columnCode == null) {
			return true;
		}

		int columnPartsCount = columnCode.trim().split("_").length;
		int descriptionPartsCount = removeUndesiredWords(description).trim().split(" ").length;

		return columnPartsCount == descriptionPartsCount;
	}

}
