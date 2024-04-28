package com.example.shdauthorizationserver.configuration.clients;

import com.example.shdauthorizationserver.model.dao.cms.CmsCustomerProfileDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import javax.sql.DataSource;
import java.util.UUID;

@Configuration
public class CmsConfiguration {

    @Value("${cms.redirect-uri}")
    private String cmsRedirectUri;

    @Value("${cms.post-logout-redirect-uri}")
    private String cmsPostLogoutRedirectUri;

    private final PasswordEncoder passwordEncoder;

    public CmsConfiguration(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CmsCustomerProfileDao cmsCustomerProfileDao(DataSource dataSource) {
        return new CmsCustomerProfileDao(dataSource);
    }

    @Bean
    @Profile("default")
    public RegisteredClient clientCmsDefault() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                // Case - Single Page Application with BFF pattern - Authorization Code with PKCE & Refresh Token
                .clientId("9DFD919F17AD2C97C24E543C3F954DD3")
                .clientName("cms")
                // For testing with Postman (requires a client secret even with PKCE)
                .clientSecret(passwordEncoder.encode("6JyvSMHFqjKL9Pvo47irtLrKTC17yn7yLyqHh6hB3uQ="))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(cmsRedirectUri)
                .postLogoutRedirectUri(cmsPostLogoutRedirectUri)
                .scope(OidcScopes.OPENID)  // required; to indicate that the application intends to use OIDC to verify the user's identity
                .scope(OidcScopes.PROFILE) // for User (Resource Owner) Profile information
                .scope(OidcScopes.EMAIL)   // for User Email address
                .scope(OidcScopes.ADDRESS)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .build();
    }

    @Bean
    @Profile({"dev", "prod"})
    public RegisteredClient clientCms() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                // Case - Single Page Application with BFF pattern - Authorization Code with PKCE & Refresh Token
                .clientId("9DFD919F17AD2C97C24E543C3F954DD3")
                .clientName("cms")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(cmsRedirectUri)
                .postLogoutRedirectUri(cmsPostLogoutRedirectUri)
                .scope(OidcScopes.OPENID)  // required; to indicate that the application intends to use OIDC to verify the user's identity
                .scope(OidcScopes.PROFILE) // for User (Resource Owner) Profile information
                .scope(OidcScopes.EMAIL)   // for User Email address
                .scope(OidcScopes.ADDRESS)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .build();

                /*
                // Case - Public Client Application - Authorization Code with PKCE
                // Not using -
                //      https://github.com/spring-projects/spring-authorization-server/issues/297
                //      https://stackoverflow.com/questions/71500071/how-does-silentrenew-and-and-userefreshtoken-work-in-angular-auth-oidc
                .clientId("9DFD919F17AD2C97C24E543C3F954DD3")
                //.clientSecret(passwordEncoder.encode("6JyvSMHFqjKL9Pvo47irtLrKTC17yn7yLyqHh6hB3uQ="))
                .clientName("cms")
                //.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:4200/login/oauth2/code/cms")
                .postLogoutRedirectUri("http://127.0.0.1:4200")
                .scope(OidcScopes.OPENID)  // required; to indicate that the application intends to use OIDC to verify the user's identity
                .scope(OidcScopes.PROFILE) // for User (Resource Owner) Profile information
                .scope(OidcScopes.EMAIL)   // for User Email address
                .scope(OidcScopes.ADDRESS)
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(true)
                        .build())
                .build();
                */

                /*
                // Case - Server Based Client Application - Authorization Code
                // Client Id - (unique, public, not easily guessable - generally a 32-character hexadecimal string)
                .clientId("9DFD919F17AD2C97C24E543C3F954DD3")

                // Client Secret - (known only to the application and the authorization server)
                .clientSecret(passwordEncoder.encode("6JyvSMHFqjKL9Pvo47irtLrKTC17yn7yLyqHh6hB3uQ="))
                .clientName("cms")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/cms")
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .scope(OidcScopes.OPENID)  // required; to indicate that the application intends to use OIDC to verify the user's identity
                .scope(OidcScopes.PROFILE) // for User (Resource Owner) Profile information
                .scope(OidcScopes.EMAIL)   // for User Email address
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                .build();
                */
    }
}
