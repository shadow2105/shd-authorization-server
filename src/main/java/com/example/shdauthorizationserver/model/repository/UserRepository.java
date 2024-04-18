package com.example.shdauthorizationserver.model.repository;

import com.example.shdauthorizationserver.model.User;

import java.util.Optional;

public interface UserRepository {

    // create a UserAccount and UserProfile when a user registers
    Optional<User> save(User user, String role);

    boolean existsByUsernameOrEmail(String username, String email, String role);

    /*Optional<User> findById(UUID id);                 // used by admin to delete
    Optional<User> findByUserName(String username);     // used to check if a username exists while registering

    Set<User> findAll();                                // used by admin to display a list of UserAccounts and UserProfiles

    void remove(User user);*/
}
