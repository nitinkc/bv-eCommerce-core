package com.bitvelocity.product;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@Slf4j
public class ProductServiceApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext context = SpringApplication.run(ProductServiceApplication.class, args);
        Environment env = context.getEnvironment();
        
        String protocol = "http";
        String serverPort = env.getProperty("server.port", "8081");
        String contextPath = env.getProperty("server.servlet.context-path", "/api");
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        
        log.info("\n----------------------------------------------------------\n\t" +
                "Application '{}' is running! Access URLs:\n\t" +
                "Local: \t\t{}://localhost:{}{}\n\t" +
                "External: \t{}://{}:{}{}\n\t" +
                "Swagger UI: \t{}://localhost:{}{}/swagger-ui.html\n\t" +
                "API Docs: \t{}://localhost:{}{}/v3/api-docs\n" +
                "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol, serverPort, contextPath,
                protocol, serverPort, contextPath);
    }
}
