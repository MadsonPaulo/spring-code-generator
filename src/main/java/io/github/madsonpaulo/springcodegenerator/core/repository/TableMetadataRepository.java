package io.github.madsonpaulo.springcodegenerator.core.repository;

import java.util.List;

import io.github.madsonpaulo.springcodegenerator.core.dto.ForeignKeyDto;

public interface TableMetadataRepository {
	List<Object[]> fetchTableInfo(String databaseName, String tableName);

	List<Object[]> fetchViewInfo(String databaseName, String viewName);

	List<ForeignKeyDto> fetchForeignKeys(String databaseName, String tableName);

}
