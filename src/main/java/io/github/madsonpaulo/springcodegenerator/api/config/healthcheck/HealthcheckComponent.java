package io.github.madsonpaulo.springcodegenerator.api.config.healthcheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/**
 * Classe de configuraçao do componente do HealthCheck
 * 
 * SONAR
 */
@Component
public class HealthcheckComponent implements ReactiveHealthIndicator {

	@Override
	public Mono<Health> health() {
		return checkDownstreamServiceHealth().onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()));
	}

	private Mono<Health> checkDownstreamServiceHealth() {
		return Mono.just(new Health.Builder().up().build());
	}

}
