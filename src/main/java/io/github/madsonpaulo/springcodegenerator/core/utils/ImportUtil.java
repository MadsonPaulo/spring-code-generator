package io.github.madsonpaulo.springcodegenerator.core.utils;

import java.util.Map;

import io.github.madsonpaulo.springcodegenerator.core.dto.JavaAnnotationModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaClassModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaFieldModelDto;
import io.github.madsonpaulo.springcodegenerator.core.enums.JavaClassType;

public final class ImportUtil {

	private static final String IMPORT_JAVA_IO_SERIALIZABLE = "import java.io.Serializable;";
	private static final String IMPORT_JAKARTA_PERSISTENCE = "import jakarta.persistence.*;";

	private ImportUtil() {
	}

	private static final Map<String, String> ANNOTATION_IMPORTS = Map.ofEntries(
			Map.entry("Service", "import org.springframework.stereotype.Service;"),
			Map.entry("JpaRepository", "import org.springframework.data.jpa.repository.JpaRepository;"),
			Map.entry("RequiredArgsConstructor", "import lombok.RequiredArgsConstructor;"),
			Map.entry("NoArgsConstructor", "import lombok.NoArgsConstructor;"),
			Map.entry("Getter", "import lombok.Getter;"), Map.entry("Data", "import lombok.Data;"),
			Map.entry("AllArgsConstructor", "import lombok.AllArgsConstructor;"),
			Map.entry("Size", "import jakarta.validation.constraints.Size;"),
			Map.entry("NotNull", "import jakarta.validation.constraints.NotNull;"),
			Map.entry("Digits", "import jakarta.validation.constraints.Digits;"),
			Map.entry("Serializable", IMPORT_JAVA_IO_SERIALIZABLE), Map.entry("Entity", IMPORT_JAKARTA_PERSISTENCE),
			Map.entry("Table", IMPORT_JAKARTA_PERSISTENCE), Map.entry("Id", IMPORT_JAKARTA_PERSISTENCE),
			Map.entry("GeneratedValue", IMPORT_JAKARTA_PERSISTENCE), Map.entry("Column", IMPORT_JAKARTA_PERSISTENCE),
			Map.entry("IdClass", IMPORT_JAKARTA_PERSISTENCE), Map.entry("Lob", IMPORT_JAKARTA_PERSISTENCE),
			Map.entry("BigDecimal", "import java.math.BigDecimal;"),
			Map.entry("LocalDateTime", "import java.time.LocalDateTime;"),
			Map.entry("LocalDate", "import java.time.LocalDate;"),
			Map.entry("LocalTime", "import java.time.LocalTime;"));

	public static void populateImportsFromModel(JavaClassModelDto classModel) {
		if (JavaClassType.ENTITY_PK.equals(classModel.getClassType())) {
			classModel.addImport(IMPORT_JAVA_IO_SERIALIZABLE);
		}

		for (JavaAnnotationModelDto annotation : classModel.getAnnotationsSnapshot()) {
			resolveImportForAnnotation(annotation, classModel);
		}

		for (JavaFieldModelDto field : classModel.getFieldsSnapshot()) {
			if (requiresFieldAnnotationImports(classModel.getClassType())) {
				for (JavaAnnotationModelDto annotation : field.getAnnotationsSnapshot()) {
					resolveImportForAnnotation(annotation, classModel);
				}
			}

			if (requiresFieldJavaTypeImports(classModel.getClassType())) {
				resolveImportForJavaType(field.getType(), classModel);
			}
		}

	}

	private static boolean requiresFieldAnnotationImports(JavaClassType type) {
		return switch (type) {
		case ENTITY -> true;
		default -> false;
		};
	}

	private static boolean requiresFieldJavaTypeImports(JavaClassType type) {
		return switch (type) {
		case ENTITY, ENTITY_PK, DTO -> true;
		default -> false;
		};
	}

	private static void resolveImportForAnnotation(JavaAnnotationModelDto annotation, JavaClassModelDto classModel) {
		String importStatement = ANNOTATION_IMPORTS.get(annotation.getName());

		if (importStatement != null) {
			classModel.addImport(importStatement);
		}
	}

	private static void resolveImportForJavaType(String type, JavaClassModelDto classModel) {
		String importStatement = ANNOTATION_IMPORTS.get(type);

		if (importStatement != null) {
			classModel.addImport(importStatement);
		}
	}

}
