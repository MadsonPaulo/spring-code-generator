package io.github.madsonpaulo.springcodegenerator.core.utils;

import java.util.List;

import io.github.madsonpaulo.springcodegenerator.core.dto.ColumnMetadataDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaAnnotationModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaClassModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaFieldModelDto;

public final class FieldUtil {

	private FieldUtil() {
	}

	public static void populateEntityFieldsFromColumnsMetadata(JavaClassModelDto classModel,
			List<ColumnMetadataDto> columns) {
		for (var column : columns) {
			JavaFieldModelDto field = new JavaFieldModelDto();
			field.setName(column.getJavaName());
			field.setType(column.getJavaType());
			field.setComment(column.getForeignKeyComment());
			field.setPrimaryKey(column.isPrimaryKey());

			addIdAnnotation(column, field);
			addGeneratedValueAnnotation(column, field);
			addNotNullAnnotation(column, field);
			addDigitsAnnotation(column, field);
			addSizeAnnotation(column, field);
			addLobAnnotation(column, field);
			addColumnAnnotation(column, field);

			classModel.addField(field);
		}
	}

	public static void populateFieldsFromColumnsMetadata(JavaClassModelDto classModel,
			List<ColumnMetadataDto> columns) {
		for (var column : columns) {
			JavaFieldModelDto field = new JavaFieldModelDto();
			field.setName(column.getJavaName());
			field.setType(column.getJavaType());
			field.setPrimaryKey(column.isPrimaryKey());

			classModel.addField(field);
		}
	}

	private static void addIdAnnotation(ColumnMetadataDto column, JavaFieldModelDto field) {
		if (column.isPrimaryKey()) {
			field.addAnnotation(new JavaAnnotationModelDto("Id"));
		}
	}

	private static void addNotNullAnnotation(ColumnMetadataDto column, JavaFieldModelDto field) {
		if (!column.isNullable() && !column.isPrimaryKey()) {
			field.addAnnotation(new JavaAnnotationModelDto("NotNull"));
		}
	}

	private static void addColumnAnnotation(ColumnMetadataDto column, JavaFieldModelDto field) {
		JavaAnnotationModelDto columnAnnotation = new JavaAnnotationModelDto("Column");
		columnAnnotation.addAttribute("name", "\"%s\"".formatted(column.getColumnName()));

		field.addAnnotation(columnAnnotation);
	}

	private static void addLobAnnotation(ColumnMetadataDto column, JavaFieldModelDto field) {
		if ("CLOB".equalsIgnoreCase(column.getColumnSqlType())) {
			field.addAnnotation(new JavaAnnotationModelDto("Lob"));
		}
	}

	private static void addGeneratedValueAnnotation(ColumnMetadataDto column, JavaFieldModelDto field) {
		if (column.isIdentity()) {
			JavaAnnotationModelDto generatedValueAnnotation = new JavaAnnotationModelDto("GeneratedValue");
			generatedValueAnnotation.addAttribute("strategy", "GenerationType.IDENTITY");

			field.addAnnotation(generatedValueAnnotation);
		}
	}

	private static void addSizeAnnotation(ColumnMetadataDto column, JavaFieldModelDto field) {
		int length = column.getLength();
		if ("String".equalsIgnoreCase(column.getJavaType()) && length > 0) {
			JavaAnnotationModelDto sizeAnnotation = new JavaAnnotationModelDto("Size");
			sizeAnnotation.addAttribute("max", String.valueOf(length));

			field.addAnnotation(sizeAnnotation);
		}
	}

	private static void addDigitsAnnotation(ColumnMetadataDto column, JavaFieldModelDto field) {
		int scale = column.getScale();
		if ("BigDecimal".equalsIgnoreCase(column.getJavaType()) && scale > 0) {
			int integerPortion = column.getPrecision() - scale;

			JavaAnnotationModelDto digitsAnnotation = new JavaAnnotationModelDto("Digits");
			digitsAnnotation.addAttribute("integer", String.valueOf(integerPortion));
			digitsAnnotation.addAttribute("fraction", String.valueOf(scale));

			field.addAnnotation(digitsAnnotation);
		}
	}

}
