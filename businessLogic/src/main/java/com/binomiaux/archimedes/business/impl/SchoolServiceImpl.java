package com.binomiaux.archimedes.business.impl;

import com.binomiaux.archimedes.business.SchoolService;
import com.binomiaux.archimedes.model.School;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SchoolServiceImpl implements SchoolService {
    @Override
    public School getSchool(String id) {
        School school = new School();
        school.setId("e868dec2-a788-43b7-96ff-43dbea97a64b");
        school.setName("Lamar");
        return null;
    }

    @Override
    public List<School> getSchools() {
        School school = new School();
        school.setId("e868dec2-a788-43b7-96ff-43dbea97a64b");
        school.setName("Lamar");
        return Arrays.asList(school);
    }
}
