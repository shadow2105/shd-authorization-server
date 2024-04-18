package com.example.shdauthorizationserver.configuration.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.util.UUID;

@Configuration
public class IprtConfiguration {

    @Value("${iprt.redirect-uri}")
    private String iprtRedirectUri;

    @Value("${iprt.post-logout-redirect-uri}")
    private String iprtPostLogoutRedirectUri;

    @Bean
    public RegisteredClient clientIprt() {
        return RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("75B1BDB9672DA1E259D95DB08AEECDF7")
                .clientSecret("{noop}secret")
                .clientName("iprt")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(iprtRedirectUri)
                .postLogoutRedirectUri(iprtPostLogoutRedirectUri)
                .scope(OidcScopes.OPENID)  // for OIDC ID Token
                .scope(OidcScopes.PROFILE) // for User (Resource Owner) Profile information
                .scope(OidcScopes.EMAIL)   // for User Email address
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();
    }
}
