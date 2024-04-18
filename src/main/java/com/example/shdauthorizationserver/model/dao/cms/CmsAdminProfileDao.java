package com.example.shdauthorizationserver.model.dao.cms;

import com.example.shdauthorizationserver.model.UserProfile;
import com.example.shdauthorizationserver.model.dao.UserProfileDao;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CmsAdminProfileDao extends JdbcDaoSupport implements UserProfileDao, MessageSourceAware {
    @Override
    public UserProfile loadUserProfileByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {

    }
}
