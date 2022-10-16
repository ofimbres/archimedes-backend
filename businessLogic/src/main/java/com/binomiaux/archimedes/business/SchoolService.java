package com.binomiaux.archimedes.business;

import com.binomiaux.archimedes.model.School;

import java.util.List;

public interface SchoolService {
    School getSchool(String id);
    List<School> getSchools();
}
