package com.example.shdauthorizationserver.model.dao;

import com.example.shdauthorizationserver.model.UserAccount;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Based on org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl
public class UserAccountDao extends JdbcDaoSupport implements UserDetailsService, MessageSourceAware {

    public static final String CUSTOM_USER_ACCOUNT_SCHEMA_DDL_LOCATION = "com/example/shdauthorizationserver/jdbc/user_account.ddl";

    // Used to read/retrieve user accounts from the DB when user tries to log in
    // Used to create a UserDetails object
    // User profile and Audit fields are not required to be fetched at this point - lazily fetching ?
    public static final String DEF_USER_ACCOUNTS_BY_USERNAME_QUERY = """
            select username, password, enabled, account_expired,
             account_locked, credentials_expired
             from user_accounts
              where username = ?
            """;

    // Used to read/retrieve user authorities from the DB
    // Used in the creation of a UserDetails object
    public static final String DEF_AUTHORITIES_BY_USERNAME_QUERY = """
            select username, authority
             from authorities
              where username = ?
            """;

    // Used to read/retrieve user groups and associated authorities from the DB
    // Used when group-based authorities are enabled; in the creation of a UserDetails object
    public static final String DEF_GROUP_AUTHORITIES_BY_USERNAME_QUERY = """
            select g.id, g.group_name, ga.authority
             from groups g, group_members gm, group_authorities ga
              where gm.username = ? and g.id = ga.group_id and g.id = gm.group_id
            """;

    /*
    SELECT g.id, g.group_name, ga.authority
    FROM groups g
    JOIN group_members gm ON g.id = gm.group_id
    JOIN group_authorities ga ON g.id = ga.group_id
    WHERE gm.username = ?;
    */

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private String authoritiesByUsernameQuery;

    private String groupAuthoritiesByUsernameQuery;

    private String userAccountsByUsernameQuery;

    private String rolePrefix = "";

    private boolean usernameBasedPrimaryKey = true;

    private boolean enableAuthorities = true;

    private boolean enableGroups;

    public UserAccountDao() {
        this.userAccountsByUsernameQuery = DEF_USER_ACCOUNTS_BY_USERNAME_QUERY;
        this.authoritiesByUsernameQuery = DEF_AUTHORITIES_BY_USERNAME_QUERY;
        this.groupAuthoritiesByUsernameQuery = DEF_GROUP_AUTHORITIES_BY_USERNAME_QUERY;
    }

    protected MessageSourceAccessor getMessages() {
        return this.messages;
    }

    protected void addCustomAuthorities(String username, List<GrantedAuthority> authorities) {
    }

    @Override
    protected void initDao() throws ApplicationContextException {
        Assert.isTrue(this.enableAuthorities || this.enableGroups,
                "Use of either authorities or groups must be enabled");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserDetails> userAccounts = loadUserAccountsByUsername(username);
        if (userAccounts.size() == 0) {
            this.logger.debug("Query returned no results for user '" + username + "'");
            throw new UsernameNotFoundException(this.messages.getMessage("UserAccountDao.notFound",
                    new Object[] { username }, "Username {0} not found"));
        }
        UserDetails userAccount = userAccounts.get(0); // contains no GrantedAuthority[]
        Set<GrantedAuthority> dbAuthsSet = new HashSet<>();
        if (this.enableAuthorities) {
            dbAuthsSet.addAll(loadUserAuthorities(userAccount.getUsername()));
        }
        if (this.enableGroups) {
            dbAuthsSet.addAll(loadGroupAuthorities(userAccount.getUsername()));
        }
        List<GrantedAuthority> dbAuths = new ArrayList<>(dbAuthsSet);
        addCustomAuthorities(userAccount.getUsername(), dbAuths);
        if (dbAuths.size() == 0) {
            this.logger.debug("User '" + username + "' has no authorities and will be treated as 'not found'");
            throw new UsernameNotFoundException(this.messages.getMessage("JdbcDaoImpl.noAuthority",
                    new Object[] { username }, "User {0} has no GrantedAuthority"));
        }
        return createUserDetails(username, userAccount, dbAuths);
    }

    protected List<UserDetails> loadUserAccountsByUsername(String username) {

        RowMapper<UserDetails> mapper = (rs, rowNum) -> {
            String username1 = rs.getString(1);
            String password = rs.getString(2);
            boolean enabled = rs.getBoolean(3);
            boolean accountNonExpired = !rs.getBoolean(4);
            boolean accountNonLocked = !rs.getBoolean(5);
            boolean credentialsNonExpired = !rs.getBoolean(6);
            return new UserAccount(username1, password, enabled, accountNonExpired, credentialsNonExpired,
                    accountNonLocked, AuthorityUtils.NO_AUTHORITIES);
        };

        return getJdbcTemplate().query(this.userAccountsByUsernameQuery, mapper, username);
    }

    protected List<GrantedAuthority> loadUserAuthorities(String username) {
        return getJdbcTemplate().query(this.authoritiesByUsernameQuery, (rs, rowNum) -> {
            String roleName = UserAccountDao.this.rolePrefix + rs.getString(2);
            return new SimpleGrantedAuthority(roleName);
        }, username);
    }

    protected List<GrantedAuthority> loadGroupAuthorities(String username) {
        return getJdbcTemplate().query(this.groupAuthoritiesByUsernameQuery, (rs, rowNum) -> {
            String roleName = getRolePrefix() + rs.getString(3);
            return new SimpleGrantedAuthority(roleName);
        }, username);
    }

    protected UserDetails createUserDetails(String username, UserDetails userFromUserQuery,
                                            List<GrantedAuthority> combinedAuthorities) {
        String returnUsername = userFromUserQuery.getUsername();
        if (!this.usernameBasedPrimaryKey) {
            returnUsername = username;
        }
        return new UserAccount(returnUsername, userFromUserQuery.getPassword(), userFromUserQuery.isEnabled(),
                userFromUserQuery.isAccountNonExpired(), userFromUserQuery.isCredentialsNonExpired(),
                userFromUserQuery.isAccountNonLocked(), combinedAuthorities);
    }

    public String getAuthoritiesByUsernameQuery() {
        return authoritiesByUsernameQuery;
    }

    public void setAuthoritiesByUsernameQuery(String authoritiesByUsernameQuery) {
        this.authoritiesByUsernameQuery = authoritiesByUsernameQuery;
    }

    public String getGroupAuthoritiesByUsernameQuery() {
        return groupAuthoritiesByUsernameQuery;
    }

    public void setGroupAuthoritiesByUsernameQuery(String groupAuthoritiesByUsernameQuery) {
        this.groupAuthoritiesByUsernameQuery = groupAuthoritiesByUsernameQuery;
    }

    public String getUserAccountsByUsernameQuery() {
        return userAccountsByUsernameQuery;
    }

    public void setUserAccountsByUsernameQuery(String userAccountsByUsernameQuery) {
        this.userAccountsByUsernameQuery = userAccountsByUsernameQuery;
    }

    public String getRolePrefix() {
        return rolePrefix;
    }

    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }

    protected boolean isUsernameBasedPrimaryKey() {
        return this.usernameBasedPrimaryKey;
    }

    public void setUsernameBasedPrimaryKey(boolean usernameBasedPrimaryKey) {
        this.usernameBasedPrimaryKey = usernameBasedPrimaryKey;
    }

    protected boolean getEnableAuthorities() {
        return this.enableAuthorities;
    }

    public void setEnableAuthorities(boolean enableAuthorities) {
        this.enableAuthorities = enableAuthorities;
    }

    protected boolean getEnableGroups() {
        return this.enableGroups;
    }

    public void setEnableGroups(boolean enableGroups) {
        this.enableGroups = enableGroups;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        Assert.notNull(messageSource, "messageSource cannot be null");
        this.messages = new MessageSourceAccessor(messageSource);
    }
}
