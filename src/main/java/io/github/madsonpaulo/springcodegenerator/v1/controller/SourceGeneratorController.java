package io.github.madsonpaulo.springcodegenerator.v1.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.madsonpaulo.springcodegenerator.core.dto.CodeGenerationRequestDto;
import io.github.madsonpaulo.springcodegenerator.core.dto.GeneratedJavaSourceDto;
import io.github.madsonpaulo.springcodegenerator.core.service.SourceGeneratorService;
import io.github.madsonpaulo.springcodegenerator.core.utils.SourcePackagingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/generator")
@Tag(name = "Java Source Generator")
public class SourceGeneratorController {
	private final SourceGeneratorService sourceGeneratorService;

	@Operation(summary = "Generate Java classes from SQL Server tables or views", description = "Generates Java entities (and optionally DTOs, services and repositories) "
			+ "based on the provided SQL Server tables or views. "
			+ "If multiple classes are generated, the result is returned as a ZIP file.")
	@GetMapping(value = "/sqlserver/classes", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<ByteArrayResource> generateClassesFromSqlServer(

			@Parameter(description = "Comma-separated list of table or view names. "
					+ "A database prefix may be provided (e.g. DB_NAME.TABLE_NAME).", example = "T999TEST, T999DOMA, T999NULL, V999VIEW") @RequestParam @NotBlank String tableNames,

			@Parameter(description = "Root Java package for generated sources.", example = "io.github.madsonpaulo.springcodegenerator.core") @RequestParam @NotBlank String rootPackage,

			@Parameter(description = "Whether DTO classes should be generated.", example = "true") @RequestParam(defaultValue = "true") boolean generateDto,

			@Parameter(description = "Whether Service and Repository classes should be generated.", example = "true") @RequestParam(defaultValue = "true") boolean generateServiceRepository

	) throws IOException {
		List<GeneratedJavaSourceDto> generatedSources = sourceGeneratorService.generateSources(
				new CodeGenerationRequestDto(tableNames, rootPackage, generateDto, generateServiceRepository));

		String fileName = SourcePackagingUtil.resolveOutputFileName(generatedSources, "generated-sources");
		ByteArrayResource payload = SourcePackagingUtil.generatePayload(generatedSources);

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s".formatted(fileName));

		return ResponseEntity.status(HttpStatus.OK).headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(payload);
	}

}
