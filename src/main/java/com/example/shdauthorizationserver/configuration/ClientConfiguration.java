package com.example.shdauthorizationserver.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ClientConfiguration {

    @Value("#{'${shd.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;
    private final RegisteredClient clientCms;
    private final RegisteredClient clientIprt;

    public ClientConfiguration(RegisteredClient clientCms, RegisteredClient clientIprt) {
        this.clientCms = clientCms;
        this.clientIprt = clientIprt;
    }

    // Register new Client Applications here until a UI is created
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        List<RegisteredClient> clients = new ArrayList<>();
        clients.add(clientCms);
        clients.add(clientIprt);

        return new InMemoryRegisteredClientRepository(clients);
    }

    // When an SPA is hosted under a different domain, Cross Origin Resource Sharing (CORS) can be used
    // to allow the application to communicate with the backend.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        //config.addAllowedOrigin("http://127.0.0.1:4200");
        //config.addAllowedOrigin("http://127.0.0.1:8080");
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
