package com.example.shdauthorizationserver.model.dao;

import com.example.shdauthorizationserver.model.UserAccount;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.log.LogMessage;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.provisioning.GroupManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

// Based on org.springframework.security.provisioning.JdbcUserDetailsManager
public class UserAccountDaoExtended extends UserAccountDao implements UserDetailsManager, GroupManager {

    // Used to create/persist user to the DB when a new user registers
    // User profile also needs to be created
    public static final String DEF_CREATE_USER_ACCOUNT_SQL = """
    insert into user_accounts (id, created_by, created_at, username, password, enabled,
     account_expired, account_locked, credentials_expired)
      values (?,?,?,?,?,?,?,?,?)
    """;

    // Used by a User (Super Admins) with authorities -  ROLE_ADMIN, DELETE_USER
    // Used to delete a user account from the DB when a user requests to close account or deregister
    // User profile also needs to be deleted
    public static final String DEF_DELETE_USER_ACCOUNT_SQL = """
    delete from user_accounts
     where username = ?
    """;

    // Used by a User (Account Management Team, Super Admins) with authorities -  ROLE_ADMIN, UPDATE_USER or a time-based Service
    // Used to update a user account in the DB when an account needs to be disabled/locked or expires as detected by a time-based service
    public static final String DEF_UPDATE_USER_ACCOUNT_SQL = """
    update user_accounts
     set updated_by = ?, updated_at = ?, enabled = ?, account_expired = ?, account_locked = ?, credentials_expired = ?
      where username = ?
    """;

    // Used to update a user account password in the DB when a request is submitted through client application/ forgot password
    public static final String DEF_CHANGE_PASSWORD_SQL = """
    update users
     set password = ?
      where username = ?
    """;

    // Used to persist user authorities to the DB
    // Used when persisting a user to the DB
    public static final String DEF_INSERT_AUTHORITY_SQL = """
    insert into authorities (username, authority)
     values (?,?)
    """;

    // Used to delete user authorities from the DB
    // Used when deleting a user from the DB
    public static final String DEF_DELETE_USER_AUTHORITIES_SQL = """
    delete from authorities
     where username = ?
    """;

    public static final String DEF_USER_ACCOUNT_EXISTS_SQL = """
    select username
     from user_accounts
      where username = ?
    """;

    public static final String DEF_FIND_GROUPS_SQL = """
    select group_name
     from groups
    """;

    public static final String DEF_FIND_USERS_IN_GROUP_SQL = """
    select username
     from group_members gm, groups g
      where gm.group_id = g.id and g.group_name = ?
    """;

    public static final String DEF_INSERT_GROUP_SQL = """
    insert into groups (group_name)
     values (?)
    """;

    public static final String DEF_FIND_GROUP_ID_SQL = """
    select id from groups
     where group_name = ?
    """;

    public static final String DEF_INSERT_GROUP_AUTHORITY_SQL = """
    insert into group_authorities (group_id, authority)
     values (?,?)
    """;

    public static final String DEF_DELETE_GROUP_SQL = """
    delete from groups
     where id = ?
    """;

    public static final String DEF_DELETE_GROUP_AUTHORITIES_SQL = """
    delete from group_authorities
     where group_id = ?
    """;

    public static final String DEF_DELETE_GROUP_MEMBERS_SQL = """
    delete from group_members
     where group_id = ?
    """;

    public static final String DEF_RENAME_GROUP_SQL = """
    update groups
     set group_name = ?
      where group_name = ?
    """;

    public static final String DEF_INSERT_GROUP_MEMBER_SQL = """
    insert into group_members (group_id, username)
     values (?,?)
    """;

    public static final String DEF_DELETE_GROUP_MEMBER_SQL = """
    delete from group_members
     where group_id = ? and username = ?
    """;

    public static final String DEF_GROUP_AUTHORITIES_QUERY_SQL = """
    select g.id, g.group_name, ga.authority
     from groups g, group_authorities ga
      where g.group_name = ? and g.id = ga.group_id
    """;

    public static final String DEF_DELETE_GROUP_AUTHORITY_SQL = """
    delete from group_authorities
     where group_id = ? and authority = ?
    """;

    protected final Log logger = LogFactory.getLog(getClass());

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    private String createUserAccountSql = DEF_CREATE_USER_ACCOUNT_SQL;

    private String deleteUserAccountSql = DEF_DELETE_USER_ACCOUNT_SQL;

    private String updateUserAccountSql = DEF_UPDATE_USER_ACCOUNT_SQL;

    private String createAuthoritySql = DEF_INSERT_AUTHORITY_SQL;

    private String deleteUserAuthoritiesSql = DEF_DELETE_USER_AUTHORITIES_SQL;

    private String userAccountExistsSql = DEF_USER_ACCOUNT_EXISTS_SQL;

    private String changePasswordSql = DEF_CHANGE_PASSWORD_SQL;

    private String findAllGroupsSql = DEF_FIND_GROUPS_SQL;

    private String findUsersInGroupSql = DEF_FIND_USERS_IN_GROUP_SQL;

    private String insertGroupSql = DEF_INSERT_GROUP_SQL;

    private String findGroupIdSql = DEF_FIND_GROUP_ID_SQL;

    private String insertGroupAuthoritySql = DEF_INSERT_GROUP_AUTHORITY_SQL;

    private String deleteGroupSql = DEF_DELETE_GROUP_SQL;

    private String deleteGroupAuthoritiesSql = DEF_DELETE_GROUP_AUTHORITIES_SQL;

    private String deleteGroupMembersSql = DEF_DELETE_GROUP_MEMBERS_SQL;

    private String renameGroupSql = DEF_RENAME_GROUP_SQL;

    private String insertGroupMemberSql = DEF_INSERT_GROUP_MEMBER_SQL;

    private String deleteGroupMemberSql = DEF_DELETE_GROUP_MEMBER_SQL;

    private String groupAuthoritiesSql = DEF_GROUP_AUTHORITIES_QUERY_SQL;

    private String deleteGroupAuthoritySql = DEF_DELETE_GROUP_AUTHORITY_SQL;

    private AuthenticationManager authenticationManager;

    private UserCache userCache = new NullUserCache();

    public UserAccountDaoExtended() {
    }

    public UserAccountDaoExtended(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    protected void initDao() throws ApplicationContextException {
        if (this.authenticationManager == null) {
            this.logger.info(
                    "No authentication manager set. Reauthentication of users when changing passwords will not be performed.");
        }
        super.initDao();
    }

    @Override
    protected List<UserDetails> loadUserAccountsByUsername(String username) {
        return getJdbcTemplate().query(getUserAccountsByUsernameQuery(), this::mapToUserAccount, username);
    }

    private UserDetails mapToUserAccount(ResultSet rs, int rowNum) throws SQLException {
        String userName = rs.getString(1);
        String password = rs.getString(2);
        boolean enabled = rs.getBoolean(3);
        boolean accLocked = false;
        boolean accExpired = false;
        boolean credsExpired = false;
        if (rs.getMetaData().getColumnCount() > 3) {
            // NOTE: acc_locked, acc_expired and creds_expired are also to be loaded
            accExpired = rs.getBoolean(4);
            accLocked = rs.getBoolean(5);
            credsExpired = rs.getBoolean(6);
        }
        return new UserAccount(userName, password, enabled, !accExpired, !credsExpired, !accLocked,
                AuthorityUtils.NO_AUTHORITIES);
    }

    // Used to create/persist user account to the DB when a new user registers
    // User profile also needs to be created
    @Override
    public void createUser(final UserDetails userDetails) {
        validateUserDetails(userDetails);                          // Checks whether the username is not empty or null
        if (userDetails instanceof UserAccount userAccount) {
            getJdbcTemplate().update(this.createUserAccountSql, (ps) -> {
                ps.setString(1, userAccount.getId().toString());
                ps.setString(2, userAccount.getCreatedBy());
                ps.setTimestamp(3, userAccount.getCreatedAt());
                ps.setString(4, userAccount.getUsername());
                ps.setString(5, userAccount.getPassword());          // encrypt this first
                ps.setBoolean(6, userAccount.isEnabled());
                int paramCount = ps.getParameterMetaData().getParameterCount();
                if (paramCount > 6) {
                    // NOTE: acc_locked, acc_expired and creds_expired are also to be inserted
                    ps.setBoolean(7, !userAccount.isAccountNonExpired());
                    ps.setBoolean(8, !userAccount.isAccountNonLocked());
                    ps.setBoolean(9, !userAccount.isCredentialsNonExpired());
                }
            });
            if (getEnableAuthorities()) {
                insertUserAuthorities(userAccount);
            }
        }
    }

    public int createUserAccount(final UserAccount userAccount) {
        try {
            createUser(userAccount);
            return 1;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    // Used by a User (Account Management Team, Super Admins) with authorities -  ROLE_ADMIN, UPDATE_USER or a time-based Service
    // Used to update a user in the DB when a user account needs to be disabled/locked or expires as detected by a time-based service
    @Override
    public void updateUser(final UserDetails userDetails) {
        validateUserDetails(userDetails);
        if (userDetails instanceof UserAccount userAccount) {
            getJdbcTemplate().update(this.updateUserAccountSql, (ps) -> {
                ps.setString(1, userAccount.getUpdatedBy());
                ps.setTimestamp(2, userAccount.getUpdatedAt());
                ps.setBoolean(3, userAccount.isEnabled());
                int paramCount = ps.getParameterMetaData().getParameterCount();
                if (paramCount == 4) {
                    ps.setString(4, userAccount.getUsername());
                }
                else {
                    // NOTE: acc_locked, acc_expired and creds_expired are also updated
                    ps.setBoolean(4, !userAccount.isAccountNonExpired());
                    ps.setBoolean(5, !userAccount.isAccountNonLocked());
                    ps.setBoolean(6, !userAccount.isCredentialsNonExpired());
                    ps.setString(7, userAccount.getUsername());
                }
            });
            if (getEnableAuthorities()) {
                deleteUserAuthorities(userAccount.getUsername());
                insertUserAuthorities(userAccount);
            }
            this.userCache.removeUserFromCache(userAccount.getUsername());
        }
    }

    public int updateUserAccount(final UserAccount userAccount) {
        try {
            updateUser(userAccount);
            return 1;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    // Used by a User (Super Admins) with authorities -  ROLE_ADMIN, DELETE_USER
    // Used to delete a user from the DB when a user requests to close account or deregister
    // User profile also needs to be deleted
    @Override
    public void deleteUser(String username) {
        if (getEnableAuthorities()) {
            deleteUserAuthorities(username);
        }
        //deleteCustomerProfile(username);
        getJdbcTemplate().update(this.deleteUserAccountSql, username);
        this.userCache.removeUserFromCache(username);
    }

    public int deleteUserAccount(String username) {
        try {
            deleteUser(username);
            return 1;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    private void insertUserAuthorities(UserDetails user) {
        for (GrantedAuthority auth : user.getAuthorities()) {
            getJdbcTemplate().update(this.createAuthoritySql, user.getUsername(), auth.getAuthority());
        }
    }

    // Used to delete user authorities from the DB
    // Used when deleting a user from the DB
    private void deleteUserAuthorities(String username) {
        getJdbcTemplate().update(this.deleteUserAuthoritiesSql, username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        Authentication currentUser = this.securityContextHolderStrategy.getContext().getAuthentication();
        if (currentUser == null) {
            // This would indicate bad coding somewhere
            throw new AccessDeniedException(
                    "Can't change password as no Authentication object found in context " + "for current user.");
        }
        String username = currentUser.getName();
        // If an authentication manager has been set, re-authenticate the user with the
        // supplied password.
        if (this.authenticationManager != null) {
            this.logger.debug(LogMessage.format("Reauthenticating user '%s' for password change request.", username));
            this.authenticationManager
                    .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, oldPassword));
        }
        else {
            this.logger.debug("No authentication manager set. Password won't be re-checked.");
        }
        this.logger.debug("Changing password for user '" + username + "'");
        getJdbcTemplate().update(this.changePasswordSql, newPassword, username); // just update the password in the DB
        Authentication authentication = createNewAuthentication(currentUser, newPassword);
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        this.securityContextHolderStrategy.setContext(context);
        this.userCache.removeUserFromCache(username);
    }

    protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
        UserDetails user = loadUserByUsername(currentAuth.getName());
        UsernamePasswordAuthenticationToken newAuthentication = UsernamePasswordAuthenticationToken.authenticated(user,
                null, user.getAuthorities());
        newAuthentication.setDetails(currentAuth.getDetails());
        return newAuthentication;
    }

    @Override
    public boolean userExists(String username) {
        List<String> users = getJdbcTemplate().queryForList(this.userAccountExistsSql, String.class, username);
        if (users.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("More than one user found with name '" + username + "'",
                    1);
        }
        return users.size() == 1;
    }

    public boolean userAccountExists(String username) {
        return userExists(username);
    }

    @Override
    public List<String> findAllGroups() {
        return getJdbcTemplate().queryForList(this.findAllGroupsSql, String.class);
    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        Assert.hasText(groupName, "groupName should have text");
        return getJdbcTemplate().queryForList(this.findUsersInGroupSql, String.class, groupName);
    }

    @Override
    public void createGroup(final String groupName, final List<GrantedAuthority> authorities) {
        Assert.hasText(groupName, "groupName should have text");
        Assert.notNull(authorities, "authorities cannot be null");
        this.logger.debug("Creating new group '" + groupName + "' with authorities "
                + AuthorityUtils.authorityListToSet(authorities));
        getJdbcTemplate().update(this.insertGroupSql, groupName);
        int groupId = findGroupId(groupName);
        for (GrantedAuthority a : authorities) {
            String authority = a.getAuthority();
            getJdbcTemplate().update(this.insertGroupAuthoritySql, (ps) -> {
                ps.setInt(1, groupId);
                ps.setString(2, authority);
            });
        }
    }

    @Override
    public void deleteGroup(String groupName) {
        this.logger.debug("Deleting group '" + groupName + "'");
        Assert.hasText(groupName, "groupName should have text");
        int id = findGroupId(groupName);
        PreparedStatementSetter groupIdPSS = (ps) -> ps.setInt(1, id);
        getJdbcTemplate().update(this.deleteGroupMembersSql, groupIdPSS);
        getJdbcTemplate().update(this.deleteGroupAuthoritiesSql, groupIdPSS);
        getJdbcTemplate().update(this.deleteGroupSql, groupIdPSS);
    }

    @Override
    public void renameGroup(String oldName, String newName) {
        this.logger.debug("Changing group name from '" + oldName + "' to '" + newName + "'");
        Assert.hasText(oldName, "oldName should have text");
        Assert.hasText(newName, "newName should have text");
        getJdbcTemplate().update(this.renameGroupSql, newName, oldName);
    }

    @Override
    public void addUserToGroup(final String username, final String groupName) {
        this.logger.debug("Adding user '" + username + "' to group '" + groupName + "'");
        Assert.hasText(username, "username should have text");
        Assert.hasText(groupName, "groupName should have text");
        int id = findGroupId(groupName);
        getJdbcTemplate().update(this.insertGroupMemberSql, (ps) -> {
            ps.setInt(1, id);
            ps.setString(2, username);
        });
        this.userCache.removeUserFromCache(username);
    }

    @Override
    public void removeUserFromGroup(final String username, final String groupName) {
        this.logger.debug("Removing user '" + username + "' to group '" + groupName + "'");
        Assert.hasText(username, "username should have text");
        Assert.hasText(groupName, "groupName should have text");
        int id = findGroupId(groupName);
        getJdbcTemplate().update(this.deleteGroupMemberSql, (ps) -> {
            ps.setInt(1, id);
            ps.setString(2, username);
        });
        this.userCache.removeUserFromCache(username);
    }

    @Override
    public List<GrantedAuthority> findGroupAuthorities(String groupName) {
        this.logger.debug("Loading authorities for group '" + groupName + "'");
        Assert.hasText(groupName, "groupName should have text");
        return getJdbcTemplate().query(this.groupAuthoritiesSql, new String[] { groupName },
                this::mapToGrantedAuthority);
    }

    private GrantedAuthority mapToGrantedAuthority(ResultSet rs, int rowNum) throws SQLException {
        String roleName = getRolePrefix() + rs.getString(3);
        return new SimpleGrantedAuthority(roleName);
    }

    @Override
    public void removeGroupAuthority(String groupName, final GrantedAuthority authority) {
        this.logger.debug("Removing authority '" + authority + "' from group '" + groupName + "'");
        Assert.hasText(groupName, "groupName should have text");
        Assert.notNull(authority, "authority cannot be null");
        int id = findGroupId(groupName);
        getJdbcTemplate().update(this.deleteGroupAuthoritySql, (ps) -> {
            ps.setInt(1, id);
            ps.setString(2, authority.getAuthority());
        });
    }

    @Override
    public void addGroupAuthority(final String groupName, final GrantedAuthority authority) {
        this.logger.debug("Adding authority '" + authority + "' to group '" + groupName + "'");
        Assert.hasText(groupName, "groupName should have text");
        Assert.notNull(authority, "authority cannot be null");
        int id = findGroupId(groupName);
        getJdbcTemplate().update(this.insertGroupAuthoritySql, (ps) -> {
            ps.setInt(1, id);
            ps.setString(2, authority.getAuthority());
        });
    }

    private int findGroupId(String group) {
        return getJdbcTemplate().queryForObject(this.findGroupIdSql, Integer.class, group);
    }

    public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
        Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy cannot be null");
        this.securityContextHolderStrategy = securityContextHolderStrategy;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setCreateUserAccountSql(String createUserAccountSql) {
        Assert.hasText(createUserAccountSql, "createUserAccountSql should have text");
        this.createUserAccountSql = createUserAccountSql;
    }

    public void setDeleteUserAccountSql(String deleteUserAccountSql) {
        Assert.hasText(deleteUserAccountSql, "deleteUserAccountSql should have text");
        this.deleteUserAccountSql = deleteUserAccountSql;
    }
    public void setUpdateUserAccountSql(String updateUserAccountSql) {
        Assert.hasText(updateUserAccountSql, "updateUserAccountSql should have text");
        this.updateUserAccountSql = updateUserAccountSql;
    }

    public void setCreateAuthoritySql(String createAuthoritySql) {
        Assert.hasText(createAuthoritySql, "createAuthoritySql should have text");
        this.createAuthoritySql = createAuthoritySql;
    }

    public void setDeleteUserAuthoritiesSql(String deleteUserAuthoritiesSql) {
        Assert.hasText(deleteUserAuthoritiesSql, "deleteUserAuthoritiesSql should have text");
        this.deleteUserAuthoritiesSql = deleteUserAuthoritiesSql;
    }

    public void setUserAccountExistsSql(String userAccountExistsSql) {
        Assert.hasText(userAccountExistsSql, "userExistsSql should have text");
        this.userAccountExistsSql = userAccountExistsSql;
    }

    public void setChangePasswordSql(String changePasswordSql) {
        Assert.hasText(changePasswordSql, "changePasswordSql should have text");
        this.changePasswordSql = changePasswordSql;
    }

    public void setFindAllGroupsSql(String findAllGroupsSql) {
        Assert.hasText(findAllGroupsSql, "findAllGroupsSql should have text");
        this.findAllGroupsSql = findAllGroupsSql;
    }

    public void setFindUsersInGroupSql(String findUsersInGroupSql) {
        Assert.hasText(findUsersInGroupSql, "findUsersInGroupSql should have text");
        this.findUsersInGroupSql = findUsersInGroupSql;
    }

    public void setInsertGroupSql(String insertGroupSql) {
        Assert.hasText(insertGroupSql, "insertGroupSql should have text");
        this.insertGroupSql = insertGroupSql;
    }

    public void setFindGroupIdSql(String findGroupIdSql) {
        Assert.hasText(findGroupIdSql, "findGroupIdSql should have text");
        this.findGroupIdSql = findGroupIdSql;
    }

    public void setInsertGroupAuthoritySql(String insertGroupAuthoritySql) {
        Assert.hasText(insertGroupAuthoritySql, "insertGroupAuthoritySql should have text");
        this.insertGroupAuthoritySql = insertGroupAuthoritySql;
    }

    public void setDeleteGroupSql(String deleteGroupSql) {
        Assert.hasText(deleteGroupSql, "deleteGroupSql should have text");
        this.deleteGroupSql = deleteGroupSql;
    }

    public void setDeleteGroupAuthoritiesSql(String deleteGroupAuthoritiesSql) {
        Assert.hasText(deleteGroupAuthoritiesSql, "deleteGroupAuthoritiesSql should have text");
        this.deleteGroupAuthoritiesSql = deleteGroupAuthoritiesSql;
    }

    public void setDeleteGroupMembersSql(String deleteGroupMembersSql) {
        Assert.hasText(deleteGroupMembersSql, "deleteGroupMembersSql should have text");
        this.deleteGroupMembersSql = deleteGroupMembersSql;
    }

    public void setRenameGroupSql(String renameGroupSql) {
        Assert.hasText(renameGroupSql, "renameGroupSql should have text");
        this.renameGroupSql = renameGroupSql;
    }

    public void setInsertGroupMemberSql(String insertGroupMemberSql) {
        Assert.hasText(insertGroupMemberSql, "insertGroupMemberSql should have text");
        this.insertGroupMemberSql = insertGroupMemberSql;
    }

    public void setDeleteGroupMemberSql(String deleteGroupMemberSql) {
        Assert.hasText(deleteGroupMemberSql, "deleteGroupMemberSql should have text");
        this.deleteGroupMemberSql = deleteGroupMemberSql;
    }

    public void setGroupAuthoritiesSql(String groupAuthoritiesSql) {
        Assert.hasText(groupAuthoritiesSql, "groupAuthoritiesSql should have text");
        this.groupAuthoritiesSql = groupAuthoritiesSql;
    }

    public void setDeleteGroupAuthoritySql(String deleteGroupAuthoritySql) {
        Assert.hasText(deleteGroupAuthoritySql, "deleteGroupAuthoritySql should have text");
        this.deleteGroupAuthoritySql = deleteGroupAuthoritySql;
    }

    /**
     * Optionally sets the UserCache if one is in use in the application. This allows the
     * user to be removed from the cache after updates have taken place to avoid stale
     * data.
     * @param userCache the cache used by the AuthenticationManager.
     */
    public void setUserCache(UserCache userCache) {
        Assert.notNull(userCache, "userCache cannot be null");
        this.userCache = userCache;
    }

    private void validateUserDetails(UserDetails user) {
        Assert.hasText(user.getUsername(), "Username may not be empty or null");
        validateAuthorities(user.getAuthorities());
    }

    private void validateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Authorities list must not be null");
        for (GrantedAuthority authority : authorities) {
            Assert.notNull(authority, "Authorities list contains a null entry");
            Assert.hasText(authority.getAuthority(), "getAuthority() method must return a non-empty string");
        }
    }
}
