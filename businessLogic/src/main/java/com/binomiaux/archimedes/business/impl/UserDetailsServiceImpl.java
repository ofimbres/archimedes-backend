package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.dao.IUserDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
//@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    //@Autowired
    private IUserDAO mUserDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.binomiaux.archimedes.db.model.User user = mUserDao.findByUserName(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found.");
        }

        log.info("+++User found: {}", user);

        return User.withUsername(user.getUserName())
                .password(user.getPassword())
                .roles("STUDENT")
                .build();
    }
}
