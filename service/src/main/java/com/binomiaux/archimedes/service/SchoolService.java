package com.binomiaux.archimedes.service;

import com.binomiaux.archimedes.model.pojo.School;

import java.util.List;

public interface SchoolService {
    School getSchool(String id);
    List<School> getSchools();
}
