package com.example.shdauthorizationserver.oidc;

import java.util.Map;
import java.util.Set;

public interface OidcUserInfoService {

    Map<String, Object> getIdTokenClaims(String principalName, String role);

    Map<String, Object> getUserInfoClaims(String principalName, String role, Set<String> requestedScopes);
}
