package io.github.madsonpaulo.springcodegenerator.api.config.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Classe de configuração do Cors
 * 
 * SONAR
 */
@Configuration
public class CorsConfig {

	public static final String ALL = "*";
	private static final List<String> DEFAULT_PERMIT_ALL = Collections.unmodifiableList(Collections.singletonList(ALL));

	@Value("${url.cors}")
	private String urlsCors;

	@Value("${metodos.http.cors}")
	private String metodosHttpCors;

	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(recuperaLista(urlsCors));
		config.setAllowedHeaders(DEFAULT_PERMIT_ALL);
		config.setAllowedMethods(recuperaLista(metodosHttpCors));
		source.registerCorsConfiguration("/**", config);
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
		bean.setOrder(Integer.MIN_VALUE);
		return bean;
	}

	private List<String> recuperaLista(String url) {
		String[] split = url.split(",");
		List<String> urls = new ArrayList<>();
		for (String string : split) {
			urls.add(string.trim());
		}
		return urls;
	}
}