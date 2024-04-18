package com.example.shdauthorizationserver.model.dao;

import com.example.shdauthorizationserver.model.UserProfile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserProfileDao {

    UserProfile loadUserProfileByUsername(String username) throws UsernameNotFoundException;
}
