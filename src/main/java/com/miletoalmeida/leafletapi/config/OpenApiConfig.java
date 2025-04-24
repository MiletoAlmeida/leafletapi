package com.miletoalmeida.leafletapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("API de Medicamentos e Bulas")
                        .description("API para buscar informações sobre medicamentos e bulas da ANVISA")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mileto Almeida")
                                .email("contato@exemplo.com")
                                .url("https://github.com/miletoalmeida"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}