package io.github.madsonpaulo.springcodegenerator.core.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import io.github.madsonpaulo.springcodegenerator.core.dto.ColumnMetadataDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.ForeignKeyDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.TableMetadataDto;
import io.github.madsonpaulo.springcodegenerator.core.exception.CodeGenerationException;
import io.github.madsonpaulo.springcodegenerator.core.repository.TableMetadataRepository;
import io.github.madsonpaulo.springcodegenerator.core.utils.StringUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableMetadataService {
	private final TableMetadataRepository repository;

	@Value("${database.name}")
	private String defaultDatabaseName;

	public static final String SEPARATOR_MULTI_FK = "#";

	public TableMetadataDto findTableMetadata(String qualifiedTableName) {
		TableMetadataDto tableMetadata = new TableMetadataDto();

		String tableName = StringUtil.extractTableName(qualifiedTableName);
		tableMetadata.setTableName(tableName);

		String databaseName = StringUtil.extractDatabaseName(qualifiedTableName);
		tableMetadata.setDatabaseName(databaseName);

		populateColumnsMetadata(tableMetadata);
		ensureAtLeastOnePk(tableMetadata);

		String javaName = StringUtil.resolveJavaClassName(tableMetadata.getTableDescription(),
				tableMetadata.getTableName());
		tableMetadata.setJavaName(javaName);

		Map<String, ForeignKeyDto> foreignKeyMap = fetchForeignKeyMap(tableMetadata);
		tableMetadata.getColumns().forEach(column -> {
			if (foreignKeyMap.containsKey(column.getColumnName())) {
				column.setForeignKeyComment(extractForeignKeyComment(foreignKeyMap.get(column.getColumnName())));
			}
		});

		return tableMetadata;
	}

	public Map<String, ForeignKeyDto> fetchForeignKeyMap(TableMetadataDto tableMetadata) {
		try {
			String databaseName = tableMetadata.getDatabaseName() != null ? tableMetadata.getDatabaseName()
					: defaultDatabaseName;
			List<ForeignKeyDto> foreignKeys = repository.fetchForeignKeys(databaseName, tableMetadata.getTableName());

			return foreignKeys.stream()
					.collect(Collectors.toMap(ForeignKeyDto::getSourceColumnName, Function.identity(), (fk1, fk2) -> {
						fk1.setForeignKeyName(concatSep(fk1.getForeignKeyName(), fk2.getForeignKeyName()));
						fk1.setReferencedTableName(
								concatSep(fk1.getReferencedTableName(), fk2.getReferencedTableName()));
						fk1.setReferencedColumnName(
								concatSep(fk1.getReferencedColumnName(), fk2.getReferencedColumnName()));
						return fk1;
					}));
		} catch (Exception e) {
			throw new CodeGenerationException(HttpStatus.INTERNAL_SERVER_ERROR, tableMetadata.getTableName(),
					e.getMessage(), e);
		}
	}

	private void populateColumnsMetadata(TableMetadataDto tableMetadata) {
		List<Object[]> tableInfo = findTableInfo(tableMetadata);

		if (tableInfo.isEmpty()) {
			String message = "Table '%s' was not found in the system catalog views."
					.formatted(tableMetadata.getTableName());

			throw new CodeGenerationException(HttpStatus.NOT_FOUND, tableMetadata.getTableName(), message);
		}

		tableMetadata.setTableDescription((String) tableInfo.get(0)[1]);

		List<ColumnMetadataDto> columnsMetadata = tableInfo.stream().map(registro -> {
			String columnName = (String) registro[2];
			String columnSqlType = (String) registro[3];
			String columnDescription = (String) registro[4];

			String allowsNull = registro[5] != null ? registro[5].toString() : null;

			Integer length = registro[6] != null ? ((Number) registro[6]).intValue() : null;

			Integer precision = registro[7] != null ? ((Number) registro[7]).intValue() : null;

			Integer scale = registro[8] != null ? ((Number) registro[8]).intValue() : null;

			boolean primaryKey = (registro[9] instanceof Number number && number.intValue() > 0)
					|| (registro[10] != null && "Y".equalsIgnoreCase(registro[10].toString()));

			String identityFlag = registro[10] != null ? registro[10].toString() : null;

			String javaName = StringUtil.resolveJavaAttributeName(columnDescription, columnName);

			return new ColumnMetadataDto(columnName, columnSqlType, columnDescription, javaName, null, null, allowsNull,
					identityFlag, length, precision, scale, primaryKey);
		}).toList();

		columnsMetadata.forEach(column -> {
			String javaType = resolveJavaAttributeType(tableMetadata.getTableName(), column);
			column.setJavaType(javaType);
		});

		tableMetadata.getColumns().addAll(columnsMetadata);
	}

	private void ensureAtLeastOnePk(TableMetadataDto tableMetadata) {
		List<ColumnMetadataDto> columns = tableMetadata.getColumns();

		boolean hasPk = columns.stream().anyMatch(ColumnMetadataDto::isPrimaryKey);

		if (!hasPk) {
			columns.get(0).setPrimaryKey(true);
		}
	}

	private List<Object[]> findTableInfo(TableMetadataDto tableMetadata) {
		try {
			return tableMetadata.isView()
					? repository.fetchViewInfo(tableMetadata.getDatabaseName(), tableMetadata.getTableName())
					: repository.fetchTableInfo(tableMetadata.getDatabaseName(), tableMetadata.getTableName());
		} catch (InvalidDataAccessResourceUsageException e) {
			throw new CodeGenerationException(HttpStatus.INTERNAL_SERVER_ERROR, tableMetadata.getTableName(),
					e.getMessage(), e);
		}
	}

	private String resolveJavaAttributeType(String tableName, ColumnMetadataDto columnMetadata) {
		String columnName = columnMetadata.getColumnName();
		String columnSqlType = columnMetadata.getColumnSqlType().toUpperCase();
		int precision = columnMetadata.getPrecision();
		int scale = columnMetadata.getScale();

		if (columnSqlType.startsWith("DECIMAL") || columnSqlType.startsWith("NUMERIC")) {
			if (scale == 0) {
				boolean isInIntegerPrecision = precision < 7;

				return isInIntegerPrecision ? "Integer" : "Long";
			}

			return "BigDecimal";
		}

		return switch (columnSqlType) {
		case "BIT" -> "Boolean";
		case "TINYINT", "SMALLINT", "INT" -> "Integer";
		case "BIGINT" -> "Long";
		case "NCHAR", "NVARCHAR", "CHAR", "VARCHAR", "TEXT", "UNIQUEIDENTIFIER" -> "String";
		case "BINARY", "VARBINARY", "IMAGE" -> "byte[]";
		case "DATE" -> "LocalDate";
		case "TIME" -> "LocalTime";
		case "DATETIME", "DATETIME2", "SMALLDATETIME", "DATETIMEOFFSET" -> "LocalDateTime";
		case "FLOAT", "REAL", "MONEY", "SMALLMONEY" -> "BigDecimal";

		default -> throw new CodeGenerationException(tableName,
				"No Java type is known for the column '%s' with SQL type '%s'.".formatted(columnName, columnSqlType));
		};
	}

	private String extractForeignKeyComment(ForeignKeyDto fk) {
		final String SEP = SEPARATOR_MULTI_FK;

		Function<String, String[]> splitOrOne = s -> {
			if (s == null || s.isBlank())
				return new String[] { "" };
			return s.indexOf(SEP) >= 0 ? s.split(java.util.regex.Pattern.quote(SEP)) : new String[] { s };
		};

		String[] nomes = splitOrOne.apply(fk.getForeignKeyName());
		String[] tabelas = splitOrOne.apply(fk.getReferencedTableName());
		String[] colunas = splitOrOne.apply(fk.getReferencedColumnName());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nomes.length; i++) {
			if (i > 0)
				sb.append(" | ");
			sb.append(nomes[i]);
			if (tabelas.length > i && tabelas[i] != null && !tabelas[i].isBlank()) {
				sb.append(" -> ").append(tabelas[i]);
				if (colunas.length > i && colunas[i] != null && !colunas[i].isBlank()) {
					sb.append(".").append(colunas[i]);
				}
			}
		}
		return sb.toString().toUpperCase();
	}

	private static String concatSep(String a, String b) {
		if (a == null || a.isBlank()) {
			return b;
		}
		if (b == null || b.isBlank()) {
			return a;
		}
		return "%s%s%s".formatted(a, SEPARATOR_MULTI_FK, b);
	}

}
