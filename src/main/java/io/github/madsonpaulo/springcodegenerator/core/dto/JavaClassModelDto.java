package io.github.madsonpaulo.springcodegenerator.core.dto;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import io.github.madsonpaulo.springcodegenerator.core.enums.JavaClassType;
import io.github.madsonpaulo.springcodegenerator.core.utils.StringUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JavaClassModelDto {
	private String packageName;
	private String className;
	private JavaClassType classType;

	@Getter(AccessLevel.NONE)
	private final List<String> classComments = new ArrayList<>();

	@Getter(AccessLevel.NONE)
	private final Set<String> imports = new LinkedHashSet<>();

	@Getter(AccessLevel.NONE)
	private final List<JavaAnnotationModelDto> annotations = new ArrayList<>();

	@Getter(AccessLevel.NONE)
	private final List<JavaFieldModelDto> fields = new ArrayList<>();

	public void addClassComment(String comment) {
		if (StringUtils.hasText(comment)) {
			this.classComments.add(comment.trim());
		}
	}

	public List<String> getClassCommentsSnapshot() {
		return List.copyOf(classComments);
	}

	public boolean hasClassComments() {
		return !classComments.isEmpty();
	}

	public void addImport(String importStatement) {
		if (StringUtils.hasText(importStatement)) {
			this.imports.add(importStatement.trim());
		}
	}

	public Set<String> getImportsSnapshot() {
		return Set.copyOf(imports);
	}

	public boolean hasImports() {
		return !imports.isEmpty();
	}

	public void addAnnotation(JavaAnnotationModelDto annotation) {
		if (annotation != null) {
			this.annotations.add(annotation);
		}
	}

	public List<JavaAnnotationModelDto> getAnnotationsSnapshot() {
		return List.copyOf(annotations);
	}

	public boolean hasAnnotations() {
		return !annotations.isEmpty();
	}

	public void addField(JavaFieldModelDto field) {
		if (field != null) {
			this.fields.add(field);
		}
	}

	public List<JavaFieldModelDto> getFieldsSnapshot() {
		return List.copyOf(fields);
	}

	public boolean hasFields() {
		return !fields.isEmpty();
	}

	public boolean isCompositePrimaryKey() {
		return fields.stream().filter(JavaFieldModelDto::isPrimaryKey).count() > 1;
	}

	public String getPrimaryKeyJavaType() {
		if (isCompositePrimaryKey()) {
			return StringUtil.getPkClassName(className);
		}

		return fields.stream().filter(JavaFieldModelDto::isPrimaryKey).map(JavaFieldModelDto::getType).findFirst()
				.orElse(null);
	}

}
