package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.SchoolService;
import com.binomiaux.archimedes.model.pojo.School;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SchoolServiceImpl implements SchoolService {
    @Override
    public School getSchool(String id) {
        School school = new School("e868dec2-a788-43b7-96ff-43dbea97a64b", "Lamar");
        return school;
    }

    @Override
    public List<School> getSchools() {
        School school = new School("e868dec2-a788-43b7-96ff-43dbea97a64b", "Lamar");
        return Arrays.asList(school);
    }
}
