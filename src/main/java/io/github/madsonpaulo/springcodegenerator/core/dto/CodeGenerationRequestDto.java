package io.github.madsonpaulo.springcodegenerator.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenerationRequestDto {
	private String tableNames;
	private String rootPackage;

	private boolean generateDto;
	private boolean generateServiceRepository;

}
