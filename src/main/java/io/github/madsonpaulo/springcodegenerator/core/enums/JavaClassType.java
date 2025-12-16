package io.github.madsonpaulo.springcodegenerator.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JavaClassType {
	ENTITY("entity"),
	ENTITY_PK("entity"),
	DTO("dto"),
	SERVICE("service"),
	REPOSITORY("repository"),

	;

	private String packageName;

}