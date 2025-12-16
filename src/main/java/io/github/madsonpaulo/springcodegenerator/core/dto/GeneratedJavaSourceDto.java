package io.github.madsonpaulo.springcodegenerator.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "javaSourceCode")
public class GeneratedJavaSourceDto {
	private String javaName;
	private String packageName;

	private String javaSourceCode;

}
