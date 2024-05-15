package com.example.shdauthorizationserver.oidc;

import com.example.shdauthorizationserver.model.CmsCustomerProfile;
import com.example.shdauthorizationserver.model.dao.cms.CmsCustomerProfileDao;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.stereotype.Service;

import java.util.*;

// Appending with OidcUserInfoService as only client id clashed with other beans with names as client id
// (like CmsUserRepository)
@Service("9DFD919F17AD2C97C24E543C3F954DD3_OidcUserInfoService") // cms
public class CmsOidcUserInfoService implements OidcUserInfoService {

    private final CmsCustomerProfileDao customerProfileDao;

    public CmsOidcUserInfoService(CmsCustomerProfileDao customerProfileDao) {
        this.customerProfileDao = customerProfileDao;
    }

    @Override
    public Map<String, Object> getIdTokenClaims(String principalName, String role) throws UsernameNotFoundException {
        if (role.equals("ROLE_CUSTOMER")) {
            CmsCustomerProfile cmsCustomerProfile = customerProfileDao.loadUserProfileByUsername(principalName);

            // https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
            OidcUserInfo userInfo = OidcUserInfo.builder()
                    .givenName(cmsCustomerProfile.getGivenName())
                    .familyName(cmsCustomerProfile.getFamilyName())
                    .middleName(cmsCustomerProfile.getMiddleName())
                    .preferredUsername(cmsCustomerProfile.getPreferredName())
                    .picture(cmsCustomerProfile.getPicture())
                    .locale(cmsCustomerProfile.getLocale().locale.toString())
                    .zoneinfo(cmsCustomerProfile.getZoneInfo().toString())
                    .email(cmsCustomerProfile.getEmail())
                    .emailVerified((cmsCustomerProfile.isEmailVerified()))
                    .build();

            Map<String, Object> claims = new HashMap<>(userInfo.getClaims());
            claims.put("is_profile_complete", cmsCustomerProfile.isProfileComplete());

            return claims;
        } else {
            // admin
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, Object> getUserInfoClaims(String principalName, String role, Set<String> requestedScopes) {
        if (role.equals("ROLE_CUSTOMER")) {

            return CmsCustomerOidcUserInfoClaims.getClaimsRequestedByScope(
                    getAllCustomerClaims(principalName),
                    requestedScopes);

        } else {
            // admin
            // return new CmsOidcAdminInfoMapper();
            return null;
        }
    }


    private static final class CmsCustomerOidcUserInfoClaims {

        private static final List<String> EMAIL_CLAIMS = Arrays.asList(
                StandardClaimNames.EMAIL,
                StandardClaimNames.EMAIL_VERIFIED
        );
        private static final List<String> PHONE_CLAIMS = Arrays.asList(
                StandardClaimNames.PHONE_NUMBER,
                StandardClaimNames.PHONE_NUMBER_VERIFIED
        );
        private static final List<String> PROFILE_CLAIMS = Arrays.asList(
                StandardClaimNames.NAME,
                StandardClaimNames.FAMILY_NAME,
                StandardClaimNames.GIVEN_NAME,
                StandardClaimNames.MIDDLE_NAME,
                //StandardClaimNames.NICKNAME,
                StandardClaimNames.PREFERRED_USERNAME,
                //StandardClaimNames.PROFILE,
                StandardClaimNames.PICTURE,
                //StandardClaimNames.WEBSITE,
                StandardClaimNames.GENDER,
                StandardClaimNames.BIRTHDATE,
                StandardClaimNames.ZONEINFO,
                StandardClaimNames.LOCALE
                //StandardClaimNames.UPDATED_AT
        );

        private static Map<String, Object> getClaimsRequestedByScope(Map<String, Object> claims,
                                                              Set<String> requestedScopes) {
            Set<String> scopeRequestedClaimNames = new HashSet<>(32);
            scopeRequestedClaimNames.add(StandardClaimNames.SUB); // username

            if (requestedScopes.contains(OidcScopes.ADDRESS)) {
                scopeRequestedClaimNames.add(StandardClaimNames.ADDRESS);
            }
            if (requestedScopes.contains(OidcScopes.EMAIL)) {
                scopeRequestedClaimNames.addAll(EMAIL_CLAIMS);
            }
            if (requestedScopes.contains(OidcScopes.PHONE)) {
                scopeRequestedClaimNames.addAll(PHONE_CLAIMS);
            }
            if (requestedScopes.contains(OidcScopes.PROFILE)) {
                scopeRequestedClaimNames.addAll(CmsCustomerOidcUserInfoClaims.PROFILE_CLAIMS);
            }

            Map<String, Object> requestedClaims = new HashMap<>(claims);
            requestedClaims.keySet().removeIf(claimName -> !scopeRequestedClaimNames.contains(claimName));

            //System.out.println(requestedClaims);

            return requestedClaims;
        }
    }

    private static final class CmsAdminOidcUserInfoClaims {

        private static final List<String> PROFILE_CLAIMS = Arrays.asList(
                StandardClaimNames.NAME,
                StandardClaimNames.FAMILY_NAME,
                StandardClaimNames.GIVEN_NAME,
                StandardClaimNames.MIDDLE_NAME
        );

        private static Map<String, Object> getClaimsRequestedByScope(Map<String, Object> claims,
                                                                     Set<String> requestedScopes) {
            Set<String> scopeRequestedClaimNames = new HashSet<>(32);
            scopeRequestedClaimNames.add(StandardClaimNames.SUB); // username

            if (requestedScopes.contains(OidcScopes.ADDRESS)) {
                scopeRequestedClaimNames.add(StandardClaimNames.ADDRESS);
            }
            if (requestedScopes.contains(OidcScopes.EMAIL)) {
                scopeRequestedClaimNames.add(StandardClaimNames.EMAIL);
            }

            if (requestedScopes.contains(OidcScopes.PROFILE)) {
                scopeRequestedClaimNames.addAll(PROFILE_CLAIMS);
            }

            Map<String, Object> requestedClaims = new HashMap<>(claims);
            requestedClaims.keySet().removeIf(claimName -> !scopeRequestedClaimNames.contains(claimName));

            return requestedClaims;
        }
    }

    private Map<String, Object> getAllCustomerClaims(String principalName) {
        CmsCustomerProfile cmsCustomerProfile = customerProfileDao.loadUserProfileByUsername(principalName);

        OidcUserInfo userInfo = OidcUserInfo.builder()
                .subject(cmsCustomerProfile.getUsername())
                .name(cmsCustomerProfile.getPreferredName())
                .givenName(cmsCustomerProfile.getGivenName())
                .familyName(cmsCustomerProfile.getFamilyName())
                .middleName(cmsCustomerProfile.getMiddleName())
                .preferredUsername(cmsCustomerProfile.getPreferredName())
                .picture(cmsCustomerProfile.getPicture())
                .email(cmsCustomerProfile.getEmail())
                .emailVerified(cmsCustomerProfile.isEmailVerified())
                .gender(cmsCustomerProfile.getGender() == null
                        ? "" : cmsCustomerProfile.getGender().label)
                .birthdate(cmsCustomerProfile.getBirthDate() == null
                        ? "" : cmsCustomerProfile.getBirthDate().toString())
                .zoneinfo(cmsCustomerProfile.getZoneInfo().toString())
                .locale(cmsCustomerProfile.getLocale().locale.toString())
                .phoneNumber(cmsCustomerProfile.getPhoneNumber() == null
                        ? "" : cmsCustomerProfile.getPhoneNumber())
                .phoneNumberVerified(cmsCustomerProfile.isPhoneVerified())
                .address(cmsCustomerProfile.getAddress() == null
                        ? "" : cmsCustomerProfile.getAddress())
                .build();

        //System.out.println(userInfo.getClaims());

        return new HashMap<>(userInfo.getClaims());
    }

    // private Map<String, Object> getAllAdminClaims(String principalName)


}
