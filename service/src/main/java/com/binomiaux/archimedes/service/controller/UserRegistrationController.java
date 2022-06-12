package com.binomiaux.archimedes.service.controller;

import com.binomiaux.archimedes.business.UserService;
import com.binomiaux.archimedes.dao.IUserDAO;
import com.binomiaux.archimedes.service.config.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("user")
public class UserRegistrationController {

    //@Autowired
    AuthenticationManager authenticationManager;

    //@Autowired
    JwtTokenProvider jwtTokenProvider;

    //@Autowired
    //private UserService mUserRegistrationService;

    //@Autowired
    //private IUserDAO mUserDao;

    @PostMapping("/login")
    public ResponseEntity getPotro2(@RequestBody AuthBody data) {
        try {
            log.info("accedio!!!");

            String username = data.getEmail();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            Set<String> roles =  new HashSet<>();
            roles.add("STUDENT");
            String token = jwtTokenProvider.createToken(username, roles); //mUserDao.findByUserName(username).getRoles());
            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            return ok(model);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid email/password supplied");
        }
    }

    @GetMapping("/potro")
    public List<String> getPotro() {
        log.info("UserRegistrationController.getPotro()");
        //mUserRegistrationService.foo();
        log.info("Potro2");

        return Arrays.asList("Oscar", "Pony", "Fernando");
    }

    @PostMapping("/registerStudent")
    public void register() {
        log.debug("Potro2");
    }
}

class AuthBody {

    private String email;
    private String password;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}
