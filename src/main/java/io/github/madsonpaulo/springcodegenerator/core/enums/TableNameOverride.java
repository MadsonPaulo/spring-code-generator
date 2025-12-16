package io.github.madsonpaulo.springcodegenerator.core.enums;

import java.util.Arrays;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TableNameOverride {
	T999NULL("T999NULL", "CompositeKeyTable"),

	;

	private final String tableName;
	private final String javaClassName;

	public static Optional<String> fromTableName(String tableName) {
		if (tableName == null || tableName.isBlank()) {
			return Optional.empty();
		}

		return Arrays.stream(values()).filter(e -> e.tableName.equalsIgnoreCase(tableName))
				.map(TableNameOverride::getJavaClassName).findFirst();
	}

}
