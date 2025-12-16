package io.github.madsonpaulo.springcodegenerator.api.config.rest;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Classe de Configuração do Swagger
 * 
 * SONAR
 */
@Configuration
public class SwaggerConfiguration {
	private static final String DESCRICAO_SWAGGER = "Permite gerar classes Java automaticamente.";
	private static final String LICENCA_NO_SWAGGER = "Apache License Version 2.0";
	private static final String LICENCA_URL_NO_SWAGGER = "https://www.apache.org/licenses/LICENSE-2.0";

	@Value("${info.app.name}")
	private String nomeAplicacao;

	@Value("${info.app.version}")
	private String versaoAplicacao;

	@Bean
	public GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder().group(nomeAplicacao).pathsToMatch("/v1/**").build();
	}

	@Bean
	public OpenAPI customizeOpenAPI() {
		final String securitySchemeName = "bearerAuth";
		return new OpenAPI()
				.info(new Info().title(nomeAplicacao).description(DESCRICAO_SWAGGER).version(versaoAplicacao)
						.license(new License().name(LICENCA_NO_SWAGGER).url(LICENCA_URL_NO_SWAGGER)))
				.externalDocs(new ExternalDocumentation())
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
						.name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
	}
}