package com.example.shdauthorizationserver.model.dao.cms;

import com.example.shdauthorizationserver.model.*;
import com.example.shdauthorizationserver.model.dao.UserProfileDao;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Date;
import java.util.List;

public class CmsCustomerProfileDao extends JdbcDaoSupport implements UserProfileDao, MessageSourceAware {

    public static final String CMS_CUSTOMER_PROFILE_SCHEMA_DDL_LOCATION =
            "com/example/shdauthorizationserver/jdbc/cms/customer_profile.ddl";

    // Used to read/retrieve user profile from the DB from username after user has successfully authenticated
    // Used when the ID Token is sent - fields like username, given_name, family_name, gender, birthdate, email, pfp
    // Used when the client application queries the /userinfo endpoint - send as a JSON object ?
    // Used to display user profile section in the client application
    public static final String DEF_CUSTOMER_PROFILES_BY_USERNAME_QUERY = """
            select username, given_name, family_name, middle_name, preferred_name, picture, email, email_verified,
             gender, dob, zone_info, locale, phone, phone_verified, address, sin, occupation, profile_completed
            from cms_customer_profiles
            where username = ?
            """;

    // Used to create/persist customer profile to the DB when a new customer registers
    public static final String DEF_CREATE_CUSTOMER_PROFILE_SQL = """
            insert into cms_customer_profiles (created_by, created_at, username, given_name, family_name, middle_name,
             preferred_name, picture, email, email_verified, gender, dob, zone_info, locale, phone, phone_verified,
              address, sin, occupation)
               values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;

    // Used to delete a user profile from the DB when a user requests to close account or deregister
    public static final String DEF_DELETE_CUSTOMER_PROFILE_SQL = """
    delete from cms_customer_profiles
     where username = ?
    """;

    // Used by a User with authorities -  ROLE_CUSTOMER
    // Used to update a user profile in the DB when a request is submitted to edit a field through client application
    // Certain fixed fields like given_name, family_name, middle_name, dob, sin can only -
    // - be updated by submitting an offline request with a government ID; Account Management Team can update that
    public static final String DEF_UPDATE_CUSTOMER_PROFILE_SQL = """
    update cms_customer_profiles
     set updated_by = ?, updated_at = ?, preferred_name = ?, picture = ?, email = ?, gender = ?, zone_info = ?, locale = ?, phone = ?, address = ?,
      occupation = ?
       where username = ?
    """;

    // Used by a User (Account Management Team, Super Admins) with authorities -  ROLE_ADMIN, UPDATE_USER
    // Used to update a user in the DB when a request is submitted to edit a fixed field offline
    public static final String DEF_UPDATE_CUSTOMER_PROFILE_FIXED_SQL = """
    update cms_customer_profiles
     set updated_by = ?, updated_at = ?, given_name = ?, family_name = ?, middle_name = ?, dob = ?, sin = ?
      where username = ?
    """;

    public static final String DEF_CUSTOMER_PROFILE_EXISTS_SQL = """
    select username
     from cms_customer_profiles
      where username = ?
    """;

    public static final String DEF_CUSTOMER_PROFILE_EXISTS_USERNAME_EMAIL_SQL = """
    select username
     from cms_customer_profiles
      where username = ? or email = ?
    """;

    public CmsCustomerProfileDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private String customerProfilesByUsernameQuery = DEF_CUSTOMER_PROFILES_BY_USERNAME_QUERY;

    private String createCustomerProfileSql = DEF_CREATE_CUSTOMER_PROFILE_SQL;

    private String deleteCustomerProfileSql = DEF_DELETE_CUSTOMER_PROFILE_SQL;

    private String updateCustomerProfileSql = DEF_UPDATE_CUSTOMER_PROFILE_SQL;

    private String updateCustomerProfileFixedSql = DEF_UPDATE_CUSTOMER_PROFILE_FIXED_SQL;

    private String customerProfileExistsSql = DEF_CUSTOMER_PROFILE_EXISTS_SQL;

    private String customerProfileExistsByUsernameOrEmailSql = DEF_CUSTOMER_PROFILE_EXISTS_USERNAME_EMAIL_SQL;

    protected MessageSourceAccessor getMessages() {
        return this.messages;
    }

    @Override
    public CmsCustomerProfile loadUserProfileByUsername(String username) throws UsernameNotFoundException {
        List<CmsCustomerProfile> cmsCustomerProfiles = loadUserProfilesByUsername(username);
        if (cmsCustomerProfiles.isEmpty()) {
            this.logger.debug("Query returned no results for user '" + username + "'");
            throw new UsernameNotFoundException(this.messages.getMessage("UserProfileDao.notFound",
                    new Object[] { username }, "Username {0} not found"));
        }

        CmsCustomerProfile userProfile = cmsCustomerProfiles.get(0);
        return CmsCustomerProfile.builder()
                .username(userProfile.getUsername())
                .givenName(userProfile.getGivenName())
                .familyName(userProfile.getFamilyName())
                .middleName(userProfile.getMiddleName())
                .preferredName(userProfile.getPreferredName())
                .picture(userProfile.getPicture())
                .email(userProfile.getEmail())
                .isEmailVerified(userProfile.isEmailVerified())
                .gender(userProfile.getGender())
                .birthDate(userProfile.getBirthDate())
                .zoneInfo(userProfile.getZoneInfo())
                .locale(userProfile.getLocale())
                .phoneNumber(userProfile.getPhoneNumber())
                .isPhoneVerified(userProfile.isPhoneVerified())
                .address(userProfile.getAddress())
                .sin(userProfile.getSIN())
                .occupation(userProfile.getOccupation())
                .isProfileComplete(userProfile.isProfileComplete())
                .build();
    }
    protected List<CmsCustomerProfile> loadUserProfilesByUsername(String username) {

        RowMapper<CmsCustomerProfile> mapper = (rs, rowNum) -> {
            String username1 = rs.getString(1);
            String givenName = rs.getString(2);
            String familyName = rs.getString(3);
            String middleName = rs.getString(4);
            String preferredName = rs.getString(5);
            String picture = rs.getString(6);
            String email = rs.getString(7);
            boolean isEmailVerified = rs.getBoolean(8);
            String gender = rs.getString(9);
            Date birthDate = rs.getDate(10);
            String zoneInfo = rs.getString(11);
            String locale = rs.getString(12);
            String phoneNumber = rs.getString(13);
            boolean isPhoneVerified = rs.getBoolean(14);
            String address = rs.getString(15);
            String SIN = rs.getString(16);
            String occupation = rs.getString(17);
            boolean isProfileComplete = rs.getBoolean(18);
            return new CmsCustomerProfile(username1, givenName, familyName, middleName, preferredName,
                    picture, email, isEmailVerified,
                    gender == null ? null : Gender.valueOf(gender),
                    birthDate,
                    zoneInfo == null ? null : ZoneInfo.valueOf(zoneInfo),
                    locale == null ? null: CustomLocale.valueOf(locale),
                    phoneNumber, isPhoneVerified, address, SIN, occupation, isProfileComplete);
        };

        return getJdbcTemplate().query(this.customerProfilesByUsernameQuery, mapper, username);
    }

    public int createUserProfile(final UserProfile userProfile) {
        int status = 0;
        if (userProfile instanceof CmsCustomerProfile customerProfile) {
            Assert.hasText(customerProfile.getUsername(), "Username may not be empty or null");                          // Checks whether the username is not empty or null
            status  = getJdbcTemplate().update(this.createCustomerProfileSql, (ps) -> {
                ps.setString(1, customerProfile.getCreatedBy());
                ps.setTimestamp(2, customerProfile.getCreatedAt());
                ps.setString(3, customerProfile.getUsername());
                ps.setString(4, customerProfile.getGivenName());
                ps.setString(5, customerProfile.getFamilyName());
                ps.setString(6, customerProfile.getMiddleName());
                ps.setString(7, customerProfile.getPreferredName());
                ps.setString(8, customerProfile.getPicture());
                ps.setString(9, customerProfile.getEmail());
                ps.setBoolean(10, customerProfile.isEmailVerified());
                ps.setString(11,
                        customerProfile.getGender() != null ? customerProfile.getGender().toString() : null);
                ps.setDate(12, customerProfile.getBirthDate());
                ps.setString(13,
                        customerProfile.getZoneInfo() != null ? customerProfile.getZoneInfo().toString() : null);
                ps.setString(14,
                        customerProfile.getLocale() != null ? customerProfile.getLocale().toString() : null);
                ps.setString(15, customerProfile.getPhoneNumber());
                ps.setBoolean(16, customerProfile.isPhoneVerified());
                ps.setString(17, customerProfile.getAddress());
                ps.setString(18, customerProfile.getSIN());                //encrypt this first
                ps.setString(19, customerProfile.getOccupation());
            });
        }

        return status;
    }

    public int updateUserProfile(final UserProfile userProfile) {
        int status = 0;
        if (userProfile instanceof CmsCustomerProfile customerProfile) {
            Assert.hasText(customerProfile.getUsername(), "Username may not be empty or null");                          // Checks whether the username is not empty or null
            status = getJdbcTemplate().update(this.updateCustomerProfileSql, (ps) -> {
                ps.setString(1, customerProfile.getUpdatedBy());
                ps.setTimestamp(2, customerProfile.getUpdatedAt());
                ps.setString(3, customerProfile.getPreferredName());
                ps.setString(4, customerProfile.getPicture());
                ps.setString(5, customerProfile.getEmail());
                ps.setString(6,
                        customerProfile.getGender() != null ? customerProfile.getGender().label : null);
                ps.setString(7,
                        customerProfile.getZoneInfo() != null ? customerProfile.getZoneInfo().label : null);
                ps.setString(8,
                        customerProfile.getLocale() != null ? customerProfile.getLocale().toString() : null);
                ps.setString(9, customerProfile.getPhoneNumber());
                ps.setString(10, customerProfile.getAddress());
                ps.setString(11, customerProfile.getOccupation());
                ps.setString(12, customerProfile.getUsername());
            });
        }

        return status;
    }

    public int updateUserProfileFixed(final UserProfile userProfile) {
        int status = 0;
        if (userProfile instanceof CmsCustomerProfile customerProfile) {
            Assert.hasText(customerProfile.getUsername(), "Username may not be empty or null");                          // Checks whether the username is not empty or null
            status = getJdbcTemplate().update(this.updateCustomerProfileFixedSql, (ps) -> {
                ps.setString(1, customerProfile.getUpdatedBy());
                ps.setTimestamp(2, customerProfile.getUpdatedAt());
                ps.setString(3, customerProfile.getGivenName());
                ps.setString(4, customerProfile.getFamilyName());
                ps.setString(5, customerProfile.getMiddleName());
                ps.setDate(6, customerProfile.getBirthDate());
                ps.setString(7, customerProfile.getSIN());                //encrypt this first
                ps.setString(8, customerProfile.getUsername());
            });
        }

        return status;
    }

    // Used to delete a user profile from the DB when a user requests to close account or deregister
    public int deleteCustomerProfile(String username) {
        return getJdbcTemplate().update(this.deleteCustomerProfileSql, username);
    }

    public boolean customerProfileExists(String username) {
        List<String> customerProfiles = getJdbcTemplate()
                .queryForList(this.customerProfileExistsSql, String.class, username);
        if (customerProfiles.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(
                    "More than one user profile found with name '" + username + "'",
                    1);
        }
        return customerProfiles.size() == 1;
    }

    public boolean customerProfileExists(String username, String email) {
        List<String> customerProfiles = getJdbcTemplate().queryForList(this.customerProfileExistsByUsernameOrEmailSql,
                String.class, username, email);
        if (customerProfiles.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("More than one user profile found with username '"
                    + username + "' or email '" + email + "'.", 1);
        }
        return customerProfiles.size() == 1;
    }

    public String getCustomerProfilesByUsernameQuery() {
        return customerProfilesByUsernameQuery;
    }

    public void setCustomerProfilesByUsernameQuery(String customerProfilesByUsernameQuery) {
        this.customerProfilesByUsernameQuery = customerProfilesByUsernameQuery;
    }

    public String getCreateCustomerProfileSql() {
        return createCustomerProfileSql;
    }

    public void setCreateCustomerProfileSql(String createCustomerProfileSql) {
        this.createCustomerProfileSql = createCustomerProfileSql;
    }

    public String getDeleteCustomerProfileSql() {
        return deleteCustomerProfileSql;
    }

    public void setDeleteCustomerProfileSql(String deleteCustomerProfileSql) {
        this.deleteCustomerProfileSql = deleteCustomerProfileSql;
    }

    public String getUpdateCustomerProfileSql() {
        return updateCustomerProfileSql;
    }

    public void setUpdateCustomerProfileSql(String updateCustomerProfileSql) {
        this.updateCustomerProfileSql = updateCustomerProfileSql;
    }

    public String getUpdateCustomerProfileFixedSql() {
        return updateCustomerProfileFixedSql;
    }

    public void setUpdateCustomerProfileFixedSql(String updateCustomerProfileFixedSql) {
        this.updateCustomerProfileFixedSql = updateCustomerProfileFixedSql;
    }

    public String getCustomerProfileExistsSql() {
        return customerProfileExistsSql;
    }

    public void setCustomerProfileExistsSql(String customerProfileExistsSql) {
        this.customerProfileExistsSql = customerProfileExistsSql;
    }

    public String getCustomerProfileExistsByUsernameOrEmailSql() {
        return customerProfileExistsByUsernameOrEmailSql;
    }

    public void setCustomerProfileExistsByUsernameOrEmailSql(String customerProfileExistsByUsernameOrEmailSql) {
        this.customerProfileExistsByUsernameOrEmailSql = customerProfileExistsByUsernameOrEmailSql;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        Assert.notNull(messageSource, "messageSource cannot be null");
        this.messages = new MessageSourceAccessor(messageSource);
    }
}
