package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.business.ActivityService;
import com.binomiaux.archimedes.dao.IActivityDAO;
import com.binomiaux.archimedes.db.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
//@Service
public class ActivityServiceImpl implements ActivityService {

    //@Autowired
    private IActivityDAO mActivityDao;

    // Request
    @Override
    public void create(Activity activity) {
        mActivityDao.save(activity);
    }

    public List<Activity> getAll() {
        List<Activity> activities = new ArrayList<>();
        mActivityDao.findAll().forEach(activities::add);
        return activities;
    }
}
