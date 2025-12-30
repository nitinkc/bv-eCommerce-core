package com.bitvelocity.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI productServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort + "/api");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("BitVelocity Team");
        contact.setEmail("team@bitvelocity.com");
        contact.setUrl("https://github.com/nitinkc/BitVelocity");

        License license = new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0");

        Info info = new Info()
                .title("Product Service API")
                .version("1.0.0")
                .description("REST API for managing products in BitVelocity eCommerce platform. " +
                           "Provides CRUD operations, search, filtering, and pagination capabilities.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
