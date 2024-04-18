package com.example.shdauthorizationserver.model.repository;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserRepositoryFactory {

    private final Map<String, UserRepository> userRepositoryMap;

    public UserRepositoryFactory(List<UserRepository> userRepositoryList) {
        // getClass() returns com.example.shdauthorizationserver.model.repository.CmsUserRepository$$SpringCGLIB$$0"
        // dynamically generated (proxy) class by the Spring Framework using CGLIB (Code Generation Library).
        //System.out.println(userRepositoryList.get(0).getClass().getSuperclass().getAnnotation(Repository.class));
        this.userRepositoryMap = userRepositoryList.stream()
                .collect(Collectors
                        .toMap(this::getRepositoryName,
                                userRepository -> userRepository));
    }

    private String getRepositoryName(UserRepository userRepository) {
        Repository repositoryAnnotation = userRepository.getClass().getSuperclass().getAnnotation(Repository.class);
        return (repositoryAnnotation != null) ? repositoryAnnotation.value() : "";
    }

    public UserRepository getUserRepository(String clientId) {
        //System.out.println(userRepositoryMap);
        return userRepositoryMap.get(clientId);
    }
}
