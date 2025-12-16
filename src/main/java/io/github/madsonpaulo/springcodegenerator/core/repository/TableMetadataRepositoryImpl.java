package io.github.madsonpaulo.springcodegenerator.core.repository;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import io.github.madsonpaulo.springcodegenerator.core.dto.ForeignKeyDto;
import io.github.madsonpaulo.springcodegenerator.core.exception.CodeGenerationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Profile("!mock")
public class TableMetadataRepositoryImpl implements TableMetadataRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	public List<Object[]> fetchTableInfo(String databaseName, String tableName) {
		String sql = """
				SELECT
				    t.name AS nome_tabela,
				    ep.value AS descricao_tabela,
				    c.name AS nome_coluna,
				    ty.name AS tipo_coluna,
				    epc.value AS descricao_coluna,
				    CASE WHEN c.is_nullable = 1 THEN 'Y' ELSE 'N' END AS permite_nulo,
				    c.max_length AS comprimento,
				    c.precision as precisao,
				    c.scale AS escala,
				    ic.key_ordinal AS chave_primaria,
				    CASE WHEN c.is_identity = 1 THEN 'Y' ELSE 'N' END AS tipo_identity
				FROM
				    %s.sys.tables t
				JOIN
				    %s.sys.columns c ON t.object_id = c.object_id
				JOIN
				    %s.sys.types ty ON c.user_type_id = ty.user_type_id
				LEFT JOIN
				    %s.sys.extended_properties ep ON ep.major_id = t.object_id AND ep.minor_id = 0 AND ep.name = 'MS_Description'
				LEFT JOIN
				    %s.sys.extended_properties epc ON epc.major_id = c.object_id AND epc.minor_id = c.column_id AND epc.name = 'MS_Description'
				OUTER APPLY (
				    SELECT ic.key_ordinal
				    FROM %s.sys.index_columns ic
				    JOIN %s.sys.indexes i ON ic.object_id = i.object_id AND ic.index_id = i.index_id
				    WHERE ic.object_id = t.object_id
				      AND ic.column_id = c.column_id
				      AND i.is_primary_key = 1
				) ic
				WHERE
				    t.name = '%s'
				    AND ty.name <> 'sysname'
				ORDER BY
				    c.column_id
				"""
				.formatted(databaseName, databaseName, databaseName, databaseName, databaseName, databaseName,
						databaseName, tableName);

		return entityManager.createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> fetchViewInfo(String databaseName, String viewName) {
		String sql = """
				    SELECT
				        v.name AS nome_tabela,
				        ep.value AS descricao_tabela,
				        c.name AS nome_coluna,
				        ty.name AS tipo_coluna,
				        epc.value AS descricao_coluna,
				        CASE WHEN c.is_nullable = 1 THEN 'Y' ELSE 'N' END AS permite_nulo,
				        c.max_length AS comprimento,
				        c.precision as precisao,
				        c.scale AS escala,
				        null AS chave_primaria,
				        CASE WHEN c.is_identity = 1 THEN 'Y' ELSE 'N' END AS tipo_identity
				    FROM
				        %s.sys.views v
				    JOIN
				        %s.sys.columns c ON v.object_id = c.object_id
				    JOIN
				        %s.sys.types ty ON c.user_type_id = ty.user_type_id
				    LEFT JOIN
				        %s.sys.extended_properties ep ON ep.major_id = v.object_id AND ep.minor_id = 0 AND ep.name = 'MS_Description'
				    LEFT JOIN
				        %s.sys.extended_properties epc ON epc.major_id = c.object_id AND epc.minor_id = c.column_id AND epc.name = 'MS_Description'
				    WHERE
				        v.name = '%s'
				        AND ty.name <> 'sysname'
				    ORDER BY
				        c.column_id
				"""
				.formatted(databaseName, databaseName, databaseName, databaseName, databaseName, viewName);

		return entityManager.createNativeQuery(sql).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<ForeignKeyDto> fetchForeignKeys(String databaseName, String tableName) {
		String sql = """
				SELECT
				    c.name AS colunaAtual,
				    fk.name AS nomeChaveEstrangeira,
				    rt.name AS tabelaDestino,
				    rc.name AS colunaDestino
				FROM
				    %s.sys.foreign_keys AS fk
				JOIN
				    %s.sys.tables AS t ON fk.parent_object_id = t.object_id
				JOIN
				    %s.sys.foreign_key_columns AS fkc ON fk.object_id = fkc.constraint_object_id
				JOIN
				    %s.sys.columns AS c ON fkc.parent_column_id = c.column_id AND c.object_id = t.object_id
				JOIN
				    %s.sys.tables AS rt ON fk.referenced_object_id = rt.object_id
				JOIN
				    %s.sys.columns AS rc ON fkc.referenced_column_id = rc.column_id AND rc.object_id = rt.object_id
				WHERE
				    t.name = '%s'
				""".formatted(databaseName, databaseName, databaseName, databaseName, databaseName, databaseName,
				tableName);

		try {
			List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

			return results.stream()
					.map(row -> new ForeignKeyDto((String) row[0], (String) row[1], (String) row[2], (String) row[3]))
					.toList();
		} catch (Exception e) {
			String message = String.format(
					"Unable to retrieve the list of foreign keys of the table '%s'. Please verify SELECT permissions for the configured database user.",
					tableName);

			throw new CodeGenerationException(HttpStatus.INTERNAL_SERVER_ERROR, tableName, message, e);
		}
	}

}
