package io.github.madsonpaulo.springcodegenerator.api.config.handler.exception;

import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.madsonpaulo.springcodegenerator.api.config.handler.exception.dto.Falha;
import io.github.madsonpaulo.springcodegenerator.core.exception.CodeGenerationException;
import jakarta.annotation.Nullable;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception Handler
 * 
 * SONAR
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(CodeGenerationException.class)
	public ResponseEntity<Falha> handleUtilitario(CodeGenerationException ex) {
		String detalhe = extrairDetalhe(ex);
		Falha payload = new Falha(ex.getTableName(), detalhe);

		logPorStatus(ex.getStatus(), detalhe, ex.getCause() != null ? ex.getCause() : ex);
		return ResponseEntity.status(ex.getStatus()).contentType(MediaType.APPLICATION_JSON).body(payload);
	}

	@ExceptionHandler(PersistenceException.class)
	public ResponseEntity<Falha> handlePersistence(PersistenceException ex) {
		return handle(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(InvalidDataAccessResourceUsageException.class)
	public ResponseEntity<Falha> handlePersistence(InvalidDataAccessResourceUsageException ex) {
		return handle(ex.getMostSpecificCause(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Falha> handleGeneric(Exception ex) {
		return handle(ex, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<Falha> handle(@Nullable Throwable ex, HttpStatus status) {
		String detalhe = extrairDetalhe(ex);
		Falha payload = new Falha(detalhe);

		logPorStatus(status, detalhe, ex);
		return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(payload);
	}

	private String extrairDetalhe(@Nullable Throwable ex) {
		if (ex == null) {
			return "Erro não especificado";
		}
		if (ex instanceof CodeGenerationException ux && ux.getCause() != null && ux.getCause().getMessage() != null) {
			return ux.getCause().getMessage();
		}

		String msg = ex.getMessage();

		return (msg != null && !msg.isBlank()) ? msg : ex.toString();
	}

	private void logPorStatus(HttpStatusCode status, String detalhe, @Nullable Throwable ex) {
		if (status.is5xxServerError()) {
			log.error(detalhe, ex);
		} else {
			log.warn(detalhe, ex);
		}
	}

}
