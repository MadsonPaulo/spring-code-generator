package io.github.madsonpaulo.springcodegenerator.core.dto;

import java.util.ArrayList;
import java.util.List;

import io.github.madsonpaulo.springcodegenerator.core.utils.StringUtil;
import lombok.Data;

@Data
public class TableMetadataDto {
	private String tableName;
	private String tableDescription;
	private String databaseName;

	private String javaName;

	private List<ColumnMetadataDto> columns = new ArrayList<>();

	public boolean isCompositePk() {
		return columns.stream().filter(ColumnMetadataDto::isPrimaryKey).limit(2).count() > 1;
	}

	public boolean isView() {
		return this.tableName != null && !this.tableName.isBlank() && this.tableName.charAt(0) == 'V';
	}

	public String getPkJavaType() {
		if (isCompositePk()) {
			return StringUtil.getPkClassName(javaName);
		}

		return columns.stream().filter(ColumnMetadataDto::isPrimaryKey).map(ColumnMetadataDto::getJavaType).findFirst()
				.orElse(null);
	}

}
