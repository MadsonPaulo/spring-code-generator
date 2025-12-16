package io.github.madsonpaulo.springcodegenerator.api.config.handler.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Falha {

	private String nomeTabela;
	private String detalhesErro;

	public Falha(String detalhesErro) {
		this.detalhesErro = detalhesErro;
	}
}
