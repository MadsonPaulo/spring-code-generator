package io.github.madsonpaulo.springcodegenerator.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForeignKeyDto {
	private String sourceColumnName;
	private String foreignKeyName;
	private String referencedTableName;
	private String referencedColumnName;

}
