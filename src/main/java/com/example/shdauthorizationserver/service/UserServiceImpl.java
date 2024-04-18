package com.example.shdauthorizationserver.service;

import com.example.shdauthorizationserver.model.RegisteredClientStore;
import com.example.shdauthorizationserver.model.User;
import com.example.shdauthorizationserver.model.UserAccount;
import com.example.shdauthorizationserver.model.UserProfile;
import com.example.shdauthorizationserver.model.repository.UserRepository;
import com.example.shdauthorizationserver.model.repository.UserRepositoryFactory;
import com.example.shdauthorizationserver.service.exception.UserAlreadyExistsException;
import com.example.shdauthorizationserver.service.exception.UserRegistrationException;
import com.example.shdauthorizationserver.dto.CustomerRegistrationRequestDto;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepositoryFactory userRepositoryFactory;

    public UserServiceImpl(PasswordEncoder passwordEncoder, UserRepositoryFactory userRepositoryFactory) {
        this.passwordEncoder = passwordEncoder;
        this.userRepositoryFactory = userRepositoryFactory;
    }

    // Create User [ROLE_CUSTOMER] object from CustomerRegistrationRequestDto
    // Encode entered Password
    // Check if the user with entered Username exists else throw exception
    @Override
    @Transactional
    public void addCustomer(CustomerRegistrationRequestDto customerRegistrationRequestDto, String clientId) {

        String username = customerRegistrationRequestDto.getUsername();
        String email = customerRegistrationRequestDto.getEmail();

        UserAccount userAccount = UserAccount.builder().username(username)
                .password(customerRegistrationRequestDto.getPassword())
                .passwordEncoder(passwordEncoder::encode)
                .authorities("ROLE_CUSTOMER",
                        "APP_" + RegisteredClientStore.valueOf("CLIENT_" + clientId).aname)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();

        userAccount.setId(UUID.randomUUID());
        userAccount.setCreatedBy(username);
        userAccount.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        UserProfile userProfile = new UserProfile(username,
                customerRegistrationRequestDto.getGivenName(),
                customerRegistrationRequestDto.getFamilyName(),
                customerRegistrationRequestDto.getMiddleName(),
                customerRegistrationRequestDto.getEmail(),
                false);

        User newCustomer = new User(userAccount, userProfile);

        UserRepository userRepository = userRepositoryFactory.getUserRepository(clientId + "_UserRepository");
        try {
            // read from db
            if (userRepository.existsByUsernameOrEmail(username, email, "ROLE_CUSTOMER")) {
                throw new UserAlreadyExistsException("User with username '" + username + "' or email '" + email
                        + "'\nalready exists. Login or Contact Support to register for the application.");
            }
        }
        catch (IncorrectResultSizeDataAccessException ex) {
            // bad-coding , log error
            throw new UserAlreadyExistsException("User with username " + username + " already exists.");
        }

        // write to db
        Optional<User> savedUser = userRepository.save(newCustomer, "ROLE_CUSTOMER");
        if (savedUser.isEmpty()) {
            throw new UserRegistrationException("""
                    An unexpected error occurred while registering the user.
                    Please try again later.
                    """);
        }
    }
}
