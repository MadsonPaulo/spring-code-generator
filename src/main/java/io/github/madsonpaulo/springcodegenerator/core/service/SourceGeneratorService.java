package io.github.madsonpaulo.springcodegenerator.core.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.madsonpaulo.springcodegenerator.core.dto.CodeGenerationRequestDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.ColumnMetadataDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.GeneratedJavaSourceDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaAnnotationModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.JavaClassModelDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.TableMetadataDto;
import io.github.madsonpaulo.springcodegenerator.core.enums.JavaClassType;
import io.github.madsonpaulo.springcodegenerator.core.utils.FieldUtil;
import io.github.madsonpaulo.springcodegenerator.core.utils.ImportUtil;
import io.github.madsonpaulo.springcodegenerator.core.utils.StringUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourceGeneratorService {
	private final TableMetadataService tableMetadataService;
	private final SourceRendererService sourceRendererService;

	private static final String DATA = "Data";
	private static final String NO_ARGS_CONSTRUCTOR = "NoArgsConstructor";
	private static final String ALL_ARGS_CONSTRUCTOR = "AllArgsConstructor";

	public List<GeneratedJavaSourceDto> generateSources(CodeGenerationRequestDto request) {
		List<JavaClassModelDto> classModels = buildClassModels(request);

		return sourceRendererService.render(classModels);
	}

	private List<JavaClassModelDto> buildClassModels(CodeGenerationRequestDto request) {
		List<String> tableNames = StringUtil.splitCommaSeparatedValues(request.getTableNames());
		String rootPackage = request.getRootPackage();

		List<JavaClassModelDto> classModels = new ArrayList<>();
		for (String tableName : tableNames) {
			TableMetadataDto tableMetadata = tableMetadataService.findTableMetadata(tableName);

			JavaClassModelDto entityModel = generateEntityModel(tableMetadata, rootPackage);
			classModels.add(entityModel);

			if (tableMetadata.isCompositePk()) {
				JavaClassModelDto entityPkModel = generateEntityPkModel(tableMetadata, rootPackage);
				classModels.add(entityPkModel);
			}

			if (request.isGenerateDto()) {
				JavaClassModelDto dtoModel = generateDtoModel(tableMetadata, rootPackage);
				classModels.add(dtoModel);
			}

			if (request.isGenerateServiceRepository()) {
				JavaClassModelDto serviceModel = generateServiceModel(tableMetadata, rootPackage);
				classModels.add(serviceModel);

				JavaClassModelDto repositoryModel = generateRepositoryModel(tableMetadata, rootPackage);
				classModels.add(repositoryModel);
			}
		}
		return classModels;
	}

	private JavaClassModelDto generateEntityModel(TableMetadataDto tableMetadata, String rootPackage) {
		JavaClassModelDto classModel = new JavaClassModelDto();
		classModel.setClassName(tableMetadata.getJavaName());
		classModel.setClassType(JavaClassType.ENTITY);
		populatePackageName(rootPackage, classModel);

		classModel.addClassComment(tableMetadata.getTableName());
		classModel.addClassComment("Type: Entity");
		classModel.addClassComment(tableMetadata.getTableDescription());

		FieldUtil.populateEntityFieldsFromColumnsMetadata(classModel, tableMetadata.getColumns());

		if (classModel.isCompositePrimaryKey()) {
			String pkClassName = StringUtil.getPkClassName(classModel.getClassName());

			JavaAnnotationModelDto idClassAnnotation = new JavaAnnotationModelDto("IdClass");
			idClassAnnotation.setSingleValue("%s.class".formatted(pkClassName));
			classModel.addAnnotation(idClassAnnotation);
		}
		classModel.addAnnotation(new JavaAnnotationModelDto(DATA));
		classModel.addAnnotation(new JavaAnnotationModelDto(ALL_ARGS_CONSTRUCTOR));
		classModel.addAnnotation(new JavaAnnotationModelDto(NO_ARGS_CONSTRUCTOR));
		classModel.addAnnotation(new JavaAnnotationModelDto("Entity"));
		JavaAnnotationModelDto tableAnnotation = new JavaAnnotationModelDto("Table");
		tableAnnotation.addAttribute("name", "\"%s\"".formatted(tableMetadata.getTableName()));
		classModel.addAnnotation(tableAnnotation);

		ImportUtil.populateImportsFromModel(classModel);

		return classModel;
	}

	private JavaClassModelDto generateEntityPkModel(TableMetadataDto tableMetadata, String rootPackage) {
		JavaClassModelDto classModel = new JavaClassModelDto();
		classModel.setClassName(tableMetadata.getJavaName());
		classModel.setClassType(JavaClassType.ENTITY_PK);
		populatePackageName(rootPackage, classModel);

		classModel.addClassComment(tableMetadata.getTableName());
		classModel.addClassComment("Type: Composite Primary Key");
		classModel.addClassComment(tableMetadata.getTableDescription());

		List<ColumnMetadataDto> primaryKeyColumns = tableMetadata.getColumns().stream().filter(c -> c.isPrimaryKey())
				.toList();
		FieldUtil.populateEntityFieldsFromColumnsMetadata(classModel, primaryKeyColumns);

		classModel.addAnnotation(new JavaAnnotationModelDto(DATA));
		classModel.addAnnotation(new JavaAnnotationModelDto(ALL_ARGS_CONSTRUCTOR));
		classModel.addAnnotation(new JavaAnnotationModelDto(NO_ARGS_CONSTRUCTOR));

		ImportUtil.populateImportsFromModel(classModel);

		return classModel;
	}

	private JavaClassModelDto generateDtoModel(TableMetadataDto tableMetadata, String rootPackage) {
		JavaClassModelDto classModel = new JavaClassModelDto();
		classModel.setClassName(tableMetadata.getJavaName());
		classModel.setClassType(JavaClassType.DTO);
		populatePackageName(rootPackage, classModel);

		classModel.addClassComment(tableMetadata.getTableName());
		classModel.addClassComment("Type: DTO");
		classModel.addClassComment(tableMetadata.getTableDescription());

		FieldUtil.populateFieldsFromColumnsMetadata(classModel, tableMetadata.getColumns());

		classModel.addAnnotation(new JavaAnnotationModelDto(DATA));
		classModel.addAnnotation(new JavaAnnotationModelDto(ALL_ARGS_CONSTRUCTOR));
		classModel.addAnnotation(new JavaAnnotationModelDto(NO_ARGS_CONSTRUCTOR));

		ImportUtil.populateImportsFromModel(classModel);

		return classModel;
	}

	private JavaClassModelDto generateServiceModel(TableMetadataDto tableMetadata, String rootPackage) {
		JavaClassModelDto classModel = new JavaClassModelDto();
		classModel.setClassName(tableMetadata.getJavaName());
		classModel.setClassType(JavaClassType.SERVICE);
		populatePackageName(rootPackage, classModel);

		classModel.addClassComment(tableMetadata.getTableName());
		classModel.addClassComment("Type: Service");
		classModel.addClassComment(tableMetadata.getTableDescription());

		FieldUtil.populateFieldsFromColumnsMetadata(classModel, tableMetadata.getColumns());

		classModel.addAnnotation(new JavaAnnotationModelDto("Service"));
		classModel.addAnnotation(new JavaAnnotationModelDto("RequiredArgsConstructor"));

		ImportUtil.populateImportsFromModel(classModel);
		classModel.addImport(getImportString(rootPackage, JavaClassType.REPOSITORY,
				StringUtil.getRepositoryClassName(tableMetadata.getJavaName())));

		return classModel;
	}

	private JavaClassModelDto generateRepositoryModel(TableMetadataDto tableMetadata, String rootPackage) {
		JavaClassModelDto classModel = new JavaClassModelDto();
		classModel.setClassName(tableMetadata.getJavaName());
		classModel.setClassType(JavaClassType.REPOSITORY);
		populatePackageName(rootPackage, classModel);

		classModel.addClassComment(tableMetadata.getTableName());
		classModel.addClassComment("Type: Repository");
		classModel.addClassComment(tableMetadata.getTableDescription());

		FieldUtil.populateFieldsFromColumnsMetadata(classModel, tableMetadata.getColumns());

		if (tableMetadata.isCompositePk()) {
			classModel.addImport(getImportString(rootPackage, JavaClassType.ENTITY,
					StringUtil.getPkClassName(tableMetadata.getJavaName())));
		}
		classModel.addImport("import org.springframework.data.jpa.repository.JpaRepository;");
		classModel.addImport(getImportString(rootPackage, JavaClassType.ENTITY, tableMetadata.getJavaName()));

		return classModel;
	}

	private void populatePackageName(String rootPackage, JavaClassModelDto classModel) {
		classModel.setPackageName("%s.%s".formatted(rootPackage, classModel.getClassType().getPackageName()));
	}

	private String getImportString(String rootPackage, JavaClassType javaClassType, String javaClassName) {
		return "import %s.%s.%s;".formatted(rootPackage, javaClassType.getPackageName(), javaClassName);
	}

}
