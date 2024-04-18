package com.example.shdauthorizationserver.model.repository;

import com.example.shdauthorizationserver.model.CmsCustomerProfile;
import com.example.shdauthorizationserver.model.User;
import com.example.shdauthorizationserver.model.UserAccount;
import com.example.shdauthorizationserver.model.dao.UserAccountDaoExtended;
import com.example.shdauthorizationserver.model.dao.cms.CmsCustomerProfileDao;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository("9DFD919F17AD2C97C24E543C3F954DD3_UserRepository") // cms
public class CmsUserRepository implements UserRepository {

    private final UserAccountDaoExtended userAccountDao;
    private final CmsCustomerProfileDao customerProfileDao;

    //private final CmsAdminProfileDao adminProfileDao;

    public CmsUserRepository(UserAccountDaoExtended userAccountDao, CmsCustomerProfileDao customerProfileDao) {
        this.userAccountDao = userAccountDao;
        this.customerProfileDao = customerProfileDao;
    }

    @Override
    public Optional<User> save(User user, String role) {
        if (role.equals("ROLE_CUSTOMER")) {
            UserAccount userAccount = user.getUserAccount();

            int accountStatus = userAccountDao.createUserAccount(userAccount);

            CmsCustomerProfile cmsCustomerProfile = new CmsCustomerProfile(
                    user.getUserProfile().getUsername(),
                    user.getUserProfile().getGivenName(),
                    user.getUserProfile().getFamilyName(),
                    user.getUserProfile().getMiddleName(),
                    user.getUserProfile().getEmail()
            );

            cmsCustomerProfile.setCreatedBy(user.getUserProfile().getUsername());
            cmsCustomerProfile.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            int profileStatus = customerProfileDao.createUserProfile(cmsCustomerProfile);

            if (accountStatus == 1 && profileStatus == 1) {
                return Optional.of(user);
            }

            return Optional.empty();
        }
        else {
            //admin
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByUsernameOrEmail(String username, String email, String role) throws IncorrectResultSizeDataAccessException {
        if (role.equals("ROLE_CUSTOMER")) {
            boolean userAccountExists = userAccountDao.userAccountExists(username);
            boolean customerProfileExists = customerProfileDao.customerProfileExists(username, email);


            /*
            Scenario 1: first time user registers for this application ("cms") - username (UserAccount) , email (cmsCustomerProfile)

            Case 1- username exists, email exists - won't register (userAccountExists, customerProfileExists): true
            Case 2- username exists, email doesn't exist - won't register (userAccountExists, customerProfileExists): true
            Case 3- username doesn't exist, email exists - won't register (!userAccountExists, customerProfileExists): true
            Case 4- username doesn't exist, email doesn't exist - will register (!userAccountExists, !customerProfileExists): false


            Scenario 2: user registers for this application ("cms") and is also registered with another application
            (say "iprt") - username (UserAccount) , email (iprtCustomerProfile, cmsCustomerProfile)

            Case 1- username exists, email exists for another application ("iprt") - won't register: true

            Case 2- username exists, email doesn't exist for another application - won't register: true

            Case 3- username doesn't exist, email exists for another application ("iprt")
                  * email exists for this application ("cms") - won't register: true
                  * email doesn't exist for this application ("cms"): will register: false >> needs to be handled
                        ** can't be handled since authorities are tied to a userAccount (to look for authority APP_NAME)
                        ** which would be new because of the newly entered username.
                        ** verify email to give access to an application so that one user cannot use another user's email
                        ** to register for an account

            Case 4- username doesn't exist, email doesn't exist for another application ("iprt")
                  * email exists for this application ("cms") - won't register: true
                  * email doesn't exist for this application ("cms"): will register: false
            */
            if (userAccountExists || customerProfileExists) {
                return true;
            }

            /*if (userAccountExists || customerProfileExists) {
                // bad coding, log error
                System.out.println("bad-coding");
            }*/
            return false;
        }
        else {
            //admin
            return true;
        }
    }
}
