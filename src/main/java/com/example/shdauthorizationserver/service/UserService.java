package com.example.shdauthorizationserver.service;

import com.example.shdauthorizationserver.dto.CustomerRegistrationRequestDto;

public interface UserService {

    void addCustomer(CustomerRegistrationRequestDto customerRegistrationRequestDto, String clientId);

    //void addAdmin(AdminRegistrationRequestDto customerRegistrationRequestDto, String clientId);;

}
