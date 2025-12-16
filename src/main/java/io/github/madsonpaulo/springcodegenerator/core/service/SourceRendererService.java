package io.github.madsonpaulo.springcodegenerator.core.service;

import java.util.List;
import java.util.StringJoiner;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.github.madsonpaulo.springcodegenerator.core.dto.GeneratedJavaSourceDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaAnnotationModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaClassModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaFieldModelDto;
import io.github.madsonpaulo.springcodegenerator.core.utils.StringUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourceRendererService {

	public List<GeneratedJavaSourceDto> render(List<JavaClassModelDto> classModels) {
		return classModels.stream().map(this::render).toList();
	}

	public GeneratedJavaSourceDto render(JavaClassModelDto classModel) {
		StringBuilder sb = new StringBuilder();

		renderPackage(sb, classModel);
		renderImports(sb, classModel);
		renderClassComments(sb, classModel);
		renderClassAnnotations(sb, classModel);
		renderClassDeclaration(sb, classModel);
		renderFields(sb, classModel);

		sb.append("}\n");

		GeneratedJavaSourceDto source = new GeneratedJavaSourceDto();
		source.setJavaName(fetchJavaName(classModel));
		source.setPackageName(classModel.getClassType().getPackageName());
		source.setJavaSourceCode(sb.toString());

		return source;
	}

	private String fetchJavaName(JavaClassModelDto classModel) {
		String className = classModel.getClassName();

		return switch (classModel.getClassType()) {
		case ENTITY -> className;
		case ENTITY_PK -> StringUtil.getPkClassName(className);
		case DTO -> StringUtil.getDtoClassName(className);
		case SERVICE -> StringUtil.getServiceClassName(className);
		case REPOSITORY -> StringUtil.getRepositoryClassName(className);
		default -> className;
		};
	}

	private void renderPackage(StringBuilder sb, JavaClassModelDto classModel) {
		if (StringUtils.hasText(classModel.getPackageName())) {
			sb.append("package ").append(classModel.getPackageName()).append(";\n\n");
		}
	}

	private void renderImports(StringBuilder sb, JavaClassModelDto classModel) {
		if (!classModel.hasImports()) {
			return;
		}

		classModel.getImportsSnapshot().stream().sorted()
				.forEach(importStatement -> sb.append(importStatement).append("\n"));

		sb.append("\n");
	}

	private void renderClassComments(StringBuilder sb, JavaClassModelDto classModel) {
		if (!classModel.hasClassComments()) {
			return;
		}

		sb.append("/*-\n");
		for (String comment : classModel.getClassCommentsSnapshot()) {
			sb.append(" * ").append(comment).append("\n");
		}
		sb.append(" */\n");
	}

	private void renderClassAnnotations(StringBuilder sb, JavaClassModelDto classModel) {
		if (!classModel.hasAnnotations()) {
			return;
		}

		for (JavaAnnotationModelDto annotation : classModel.getAnnotationsSnapshot()) {
			renderAnnotation(sb, annotation);
		}
	}

	private void renderClassDeclaration(StringBuilder sb, JavaClassModelDto classModel) {
		switch (classModel.getClassType()) {
		case ENTITY -> renderEntityClassDeclaration(sb, classModel);
		case ENTITY_PK -> renderEntityPkClassDeclaration(sb, classModel);
		case DTO -> renderDtoClassDeclaration(sb, classModel);
		case SERVICE -> renderServiceClassDeclaration(sb, classModel);
		case REPOSITORY -> renderRepositoryClassDeclaration(sb, classModel);
		}
	}

	private void renderEntityClassDeclaration(StringBuilder sb, JavaClassModelDto classModel) {
		sb.append("public class ").append(classModel.getClassName()).append(" {\n\n");
	}

	private void renderEntityPkClassDeclaration(StringBuilder sb, JavaClassModelDto classModel) {
		sb.append("public class ").append(StringUtil.getPkClassName(classModel.getClassName()))
				.append(" implements Serializable {\n");
		sb.append("\tprivate static final long serialVersionUID = 1L;\n\n");
	}

	private void renderDtoClassDeclaration(StringBuilder sb, JavaClassModelDto classModel) {
		sb.append("public class ").append(StringUtil.getDtoClassName(classModel.getClassName())).append(" {\n");
	}

	private void renderServiceClassDeclaration(StringBuilder sb, JavaClassModelDto classModel) {
		sb.append("public class ").append(StringUtil.getServiceClassName(classModel.getClassName())).append(" {\n");
	}

	private void renderRepositoryClassDeclaration(StringBuilder sb, JavaClassModelDto classModel) {
		sb.append("public interface ").append(StringUtil.getRepositoryClassName(classModel.getClassName()))
				.append(" extends JpaRepository<").append(classModel.getClassName()).append(", ")
				.append(classModel.getPrimaryKeyJavaType()).append("> {\n\n");
	}

	private void renderFields(StringBuilder sb, JavaClassModelDto classModel) {
		switch (classModel.getClassType()) {
		case ENTITY -> renderEntityFields(sb, classModel);
		case ENTITY_PK -> renderEntityPkFields(sb, classModel);
		case DTO -> renderDtoFields(sb, classModel);
		case SERVICE -> renderServiceFields(sb, classModel);
		case REPOSITORY -> {
			// nothing to render
		}
		}
	}

	private void renderEntityFields(StringBuilder sb, JavaClassModelDto classModel) {
		for (JavaFieldModelDto field : classModel.getFieldsSnapshot()) {

			if (field.getComment() != null && !field.getComment().isBlank()) {
				sb.append("\t// ").append(field.getComment()).append("\n");
			}

			for (JavaAnnotationModelDto annotation : field.getAnnotationsSnapshot()) {
				sb.append("\t");
				renderAnnotation(sb, annotation);
			}

			sb.append("\tprivate ").append(field.getType()).append(" ").append(field.getName()).append(";\n\n");
		}
	}

	private void renderEntityPkFields(StringBuilder sb, JavaClassModelDto classModel) {
		for (JavaFieldModelDto field : classModel.getFieldsSnapshot()) {

			if (field.getComment() != null && !field.getComment().isBlank()) {
				sb.append("\t// ").append(field.getComment()).append("\n");
			}

			sb.append("\tprivate ").append(field.getType()).append(" ").append(field.getName()).append(";\n\n");
		}
	}

	private void renderDtoFields(StringBuilder sb, JavaClassModelDto classModel) {
		for (JavaFieldModelDto field : classModel.getFieldsSnapshot()) {
			sb.append("\tprivate ").append(field.getType()).append(" ").append(field.getName()).append(";\n");
		}

		sb.append("\n");
	}

	private void renderServiceFields(StringBuilder sb, JavaClassModelDto classModel) {
		sb.append("\tprivate final ").append(StringUtil.getRepositoryClassName(classModel.getClassName()))
				.append(" repository;\n");

		sb.append("\n");
	}

	private void renderAnnotation(StringBuilder sb, JavaAnnotationModelDto annotation) {
		sb.append("@").append(annotation.getName());

		if (annotation.hasSingleValue()) {
			sb.append("(").append(annotation.getSingleValue()).append(")");
		} else if (annotation.hasAttributes()) {
			StringJoiner joiner = new StringJoiner(", ", "(", ")");
			annotation.getAttributesSnapshot().forEach((k, v) -> joiner.add(k + " = " + v));
			sb.append(joiner);
		}

		sb.append("\n");
	}

}
