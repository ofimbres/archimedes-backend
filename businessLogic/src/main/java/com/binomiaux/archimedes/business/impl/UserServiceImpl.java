package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.business.UserService;
import com.binomiaux.archimedes.dao.IUserDAO;
import com.binomiaux.archimedes.db.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
//@Service
public class UserServiceImpl implements UserService {

    //@Autowired
    private IUserDAO mUserDAO;

    //@Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void foo() {
        log.info("UserRegistrationLogic.foo()");
        User usr = mUserDAO.findById(1).get();
        log.info(usr.getUserId() + "" + usr.getFirstName());
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String pwd = "potro";
        log.info("Password " + pwd + " encoded is "+ encoder.encode(pwd));

        User usr2 = mUserDAO.findByFirstName("Oscar");
        log.info(usr2.getUserId() + "-" + usr2.getFirstName());
    }

    @Override
    public void register(User user) {

    }
}
