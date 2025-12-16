package io.github.madsonpaulo.springcodegenerator.api.config.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

/**
 * Configuration
 * 
 * SONAR
 */
@Configuration
public class JacksonConfiguration {

	/**
	 * Formato de data.
	 */
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * Formato de data e hora.
	 */
	private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	@Bean
	public ObjectMapper objectMapper() {
		return jackson2ObjectMapperBuilder().modules(new JavaTimeModule())
				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
	}

	/**
	 * Adiciona as customizações do mapeamento para JSON.
	 *
	 * @return Builder do mapeamento objeto/JSON.
	 */
	@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		return Jackson2ObjectMapperBuilder.json().serializationInclusion(JsonInclude.Include.NON_EMPTY)
				.simpleDateFormat(DATETIME_FORMAT)
				.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
				.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)))
				.deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
				.deserializers(new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
	}
}