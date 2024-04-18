package com.example.shdauthorizationserver.oidc;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OidcUserInfoServiceFactory {

    private final Map<String, OidcUserInfoService> oidcUserInfoServiceMap;

    public OidcUserInfoServiceFactory(List<OidcUserInfoService> oidcUserInfoServiceList) {
        // getClass() returns com.example.shdauthorizationserver.oidc.CmsOidcUserInfoService and not a
        // dynamically generated (proxy) class by the Spring Framework using CGLIB (Code Generation Library)
        // like in case of a Repository
        //System.out.println(oidcUserInfoServiceList.get(0).getClass().getAnnotation(Service.class));
        this.oidcUserInfoServiceMap = oidcUserInfoServiceList.stream()
                .collect(Collectors
                        .toMap(this::getServiceName,
                                oidcUserInfoService -> oidcUserInfoService));
    }

    private String getServiceName(OidcUserInfoService oidcUserInfoService) {
        Service serviceAnnotation = oidcUserInfoService.getClass().getAnnotation(Service.class);
        return (serviceAnnotation != null) ? serviceAnnotation.value() : "";
    }

    public OidcUserInfoService getOidcUserInfoService(String clientId) {
        //System.out.println(oidcUserInfoServiceMap);
        return oidcUserInfoServiceMap.get(clientId);
    }
}
