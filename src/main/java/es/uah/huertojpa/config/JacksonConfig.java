package es.uah.huertojpa.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * P7 – Antes se creaba un {@code new ObjectMapper()} a mano (sin {@code JavaTimeModule}); solo funcionaba porque
 * {@code @EnableWebMvc} lo ignoraba. Ahora se ajusta el ObjectMapper de Spring Boot, que ya trae el módulo de
 * {@code java.time} y escribe las fechas en ISO-8601 ({@code "2026-11-08T10:00:00"}, {@code "2026-10-01"}).
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer personalizarJackson() {
        return builder -> builder
                .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
