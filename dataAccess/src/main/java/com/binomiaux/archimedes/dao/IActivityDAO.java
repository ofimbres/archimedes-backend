package com.binomiaux.archimedes.dao;

import com.binomiaux.archimedes.db.model.Activity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IActivityDAO extends CrudRepository<Activity, Integer> {
    Activity findByUserName(String userName);
    Activity findByFirstName(String firstName);
}
