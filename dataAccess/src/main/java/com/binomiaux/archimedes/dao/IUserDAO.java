package com.binomiaux.archimedes.dao;

import com.binomiaux.archimedes.db.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserDAO extends CrudRepository<User, Integer> {
    User findByUserName(String userName);
    User findByFirstName(String firstName);
}
