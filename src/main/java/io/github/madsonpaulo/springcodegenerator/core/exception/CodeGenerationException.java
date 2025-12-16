package io.github.madsonpaulo.springcodegenerator.core.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CodeGenerationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final HttpStatus status;
	private final String tableName;

	public CodeGenerationException(String tableName, String message) {
		super(message);
		this.tableName = tableName;
		this.status = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public CodeGenerationException(HttpStatus status, String tableName, String message) {
		super(message);
		this.status = status;
		this.tableName = tableName;
	}

	public CodeGenerationException(HttpStatus status, String tableName, String message, Throwable cause) {
		super(message, cause);
		this.status = status;
		this.tableName = tableName;
	}

}
