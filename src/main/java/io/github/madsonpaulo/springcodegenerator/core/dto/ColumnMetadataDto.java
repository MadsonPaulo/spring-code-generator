package io.github.madsonpaulo.springcodegenerator.core.dto;

import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnMetadataDto {
	private String columnName;
	private String columnSqlType;
	private String columnDescription;

	private String javaName;
	private String javaType;
	private String foreignKeyComment;

	@Getter(AccessLevel.NONE)
	private String allowsNull;

	@Getter(AccessLevel.NONE)
	private String identityFlag;

	private int length;
	private int precision;
	private int scale;
	private boolean primaryKey;

	public boolean isIdentity() {
		return identityFlag != null && Set.of("A", "D", "Y").contains(identityFlag.toUpperCase());
	}

	public boolean isNullable() {
		return "Y".equalsIgnoreCase(allowsNull);
	}

}
