package io.github.madsonpaulo.springcodegenerator.core.repository;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.github.madsonpaulo.springcodegenerator.core.dto.ForeignKeyDto;

@Repository
@Profile("mock")
public class TableMetadataRepositoryMock implements TableMetadataRepository {

	@Override
	public List<Object[]> fetchTableInfo(String databaseName, String tableName) {
		String tableName1 = "T999TEST";
		if (tableName.equalsIgnoreCase(tableName1)) {
			String tableDesc = "Test table containing many SQL types. Also, all columns have perfect descriptions.";
			return List.of(
					new Object[] { tableName1, tableDesc, "ID_TST", "decimal", "Identifier Test", 'N', 5, 18, 0, 1,
							'Y' },
					new Object[] { tableName1, tableDesc, "CD_DOM", "decimal", "Code Domain", 'N', 5, 3, 0, 0, 'N' },
					new Object[] { tableName1, tableDesc, "VL_TST", "decimal", "Value Test", 'Y', 9, 18, 2, null, 'N' },
					new Object[] { tableName1, tableDesc, "NM_TST", "char", "Name Test", 'N', 30, 0, 0, null, 'N' },
					new Object[] { tableName1, tableDesc, "FL_TST", "bit", "Flag Test", 'Y', 1, 1, 0, null, 'N' },
					new Object[] { tableName1, tableDesc, "DT_TST", "datetime", "Datetime Test", 'Y', 8, 23, 3, null,
							'N' },
					new Object[] { tableName1, tableDesc, "DA_TST", "date", "Date Test", 'Y', 3, 10, 0, null, 'N' },
					new Object[] { tableName1, tableDesc, "TM_TST", "time", "Time Test", 'Y', 5, 16, 7, null, 'N' },
					new Object[] { tableName1, tableDesc, "BL_TST", "binary", "Binary Test", 'Y', 8, 0, 0, null, 'N' });
		}

		String tableName2 = "T999DOMA";
		if (tableName.equalsIgnoreCase(tableName2)) {
			String tableDesc = "In this table, no column descriptions fit the column's codes.";
			return List.of(
					new Object[] { tableName2, tableDesc, "CD_DOM", "decimal", "This column will be an Integer", 'N', 5,
							3, 0, 1, 'Y' },
					new Object[] { tableName2, tableDesc, "CD_DFK", "decimal",
							"This column is FK for two different tables", 'N', 5, 3, 0, null, 'N' },
					new Object[] { tableName2, tableDesc, "NM_DOM", "char", "And this one will be a String", 'N', 200,
							0, 0, null, 'N' });
		}

		String tableName3 = "T999NULL";
		if (tableName.equalsIgnoreCase(tableName3)) {
			String tableDesc = null;
			return List.of(new Object[] { tableName3, tableDesc, "CD_FPK", "decimal", null, 'N', 5, 3, 0, 1, 'N' },
					new Object[] { tableName3, tableDesc, "CD_SPK", "decimal", null, 'N', 5, 3, 0, 2, 'N' },
					new Object[] { tableName3, tableDesc, "NM_NUL", "varchar", null, 'Y', 50, 0, 0, null, 'N' });
		}

		return List.of();
	}

	@Override
	public List<Object[]> fetchViewInfo(String databaseName, String viewName) {
		String viewName1 = "V999VIEW";
		if (viewName.equalsIgnoreCase(viewName1)) {
			String viewDesc = null;
			return List.of(new Object[] { viewName1, viewDesc, "SQ_VIE", "decimal", null, 'N', 5, 18, 0, 1, 'N' },
					new Object[] { viewName1, viewDesc, "NM_VIE", "varchar", null, 'Y', 50, 0, 0, null, 'N' });
		}

		return List.of();
	}

	@Override
	public List<ForeignKeyDto> fetchForeignKeys(String databaseName, String tableName) {
		String tableName1 = "T999TEST";
		if (tableName.equalsIgnoreCase(tableName1)) {
			return List.of(new ForeignKeyDto("CD_DOM", "FK_TEST_DOMA_01", "T999DOMA", "CD_DOM"));
		}

		String tableName2 = "T999DOMA";
		if (tableName.equalsIgnoreCase(tableName2)) {
			return List.of(new ForeignKeyDto("CD_DFK", "FK_DOMA_FFKY_01", "T999FFKY", "CD_DFK"),
					new ForeignKeyDto("CD_DFK", "FK_DOMA_SFKY_02", "T999SFKY", "CD_DFK"));
		}

		String tableName3 = "T999NULL";
		if (tableName.equalsIgnoreCase(tableName3)) {
			return List.of(new ForeignKeyDto("CD_FPK", "FK_NULL_FNUL_01", "T999FNUL", "CD_FPK"),
					new ForeignKeyDto("CD_SPK", "FK_NULL_SNUL_02", "T999SNUL", "CD_SPK"));
		}

		return List.of();
	}

}
