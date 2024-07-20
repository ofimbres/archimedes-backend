package com.binomiaux.archimedes.service.impl;

import com.binomiaux.archimedes.service.SchoolService;
import com.binomiaux.archimedes.model.School;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SchoolServiceImpl implements SchoolService {
    @Override
    public School getSchool(String id) {
        School school = new School();
        school.setName("Lamar");
        return null;
    }

    @Override
    public List<School> getSchools() {
        School school = new School();
        school.setName("Lamar");
        return Arrays.asList(school);
    }
}
