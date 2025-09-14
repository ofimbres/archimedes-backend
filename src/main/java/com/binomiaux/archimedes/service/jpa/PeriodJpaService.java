package com.binomiaux.archimedes.service.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.binomiaux.archimedes.entity.Period;
import com.binomiaux.archimedes.entity.School;
import com.binomiaux.archimedes.entity.Teacher;
import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.repository.jpa.PeriodRepository;
import com.binomiaux.archimedes.repository.jpa.SchoolRepository;
import com.binomiaux.archimedes.repository.jpa.TeacherRepository;

/**
 * Pure JPA-based PeriodService that uses only JPA entities and repositories.
 * No dependencies on DynamoDB services or models.
 */
@Service
@Transactional
public class PeriodJpaService {

    private final PeriodRepository periodRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherRepository teacherRepository;

    public PeriodJpaService(PeriodRepository periodRepository, SchoolRepository schoolRepository, TeacherRepository teacherRepository) {
        this.periodRepository = periodRepository;
        this.schoolRepository = schoolRepository;
        this.teacherRepository = teacherRepository;
    }

    /**
     * Get a period by its period ID (business identifier)
     */
    public Period getPeriodByPeriodId(String periodId) {
        return periodRepository.findByPeriodId(periodId)
            .orElseThrow(() -> new EntityNotFoundException("Period not found: " + periodId, null));
    }

    /**
     * Get a period by its database ID
     */
    public Period getPeriodById(Long id) {
        return periodRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Period not found with ID: " + id, null));
    }

    /**
     * Get all periods
     */
    public List<Period> getAllPeriods() {
        return periodRepository.findAll();
    }

    /**
     * Get periods by school code
     */
    public List<Period> getPeriodsBySchoolCode(String schoolCode) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findBySchool(school);
    }

    /**
     * Get active periods by school code ordered by period number
     */
    public List<Period> getActivePeriodsOrderedByNumber(String schoolCode) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findActivePeriodsOrderedByNumber(school);
    }

    /**
     * Get periods by teacher ID
     */
    public List<Period> getPeriodsByTeacherId(String teacherId) {
        Teacher teacher = getTeacherByTeacherId(teacherId);
        return periodRepository.findByTeacher(teacher);
    }

    /**
     * Get active periods by teacher ID
     */
    public List<Period> getActivePeriodsByTeacherId(String teacherId) {
        Teacher teacher = getTeacherByTeacherId(teacherId);
        return periodRepository.findByTeacherAndStatus(teacher, "ACTIVE");
    }

    /**
     * Get periods by school and subject
     */
    public List<Period> getPeriodsBySchoolAndSubject(String schoolCode, String subject) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findBySchoolAndSubject(school, subject);
    }

    /**
     * Get periods by school and period number
     */
    public List<Period> getPeriodsBySchoolAndNumber(String schoolCode, Integer periodNumber) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findBySchoolAndPeriodNumber(school, periodNumber);
    }

    /**
     * Get periods by academic year
     */
    public List<Period> getPeriodsByAcademicYear(String schoolCode, String academicYear) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findBySchoolAndAcademicYear(school, academicYear);
    }

    /**
     * Get periods by semester
     */
    public List<Period> getPeriodsBySemester(String schoolCode, String semester) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findBySchoolAndSemester(school, semester);
    }

    /**
     * Get periods by student ID
     */
    public List<Period> getPeriodsByStudentId(Long studentId) {
        return periodRepository.findPeriodsByStudent(studentId);
    }

    /**
     * Get subjects by school
     */
    public List<String> getSubjectsBySchool(String schoolCode) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findSubjectsBySchool(school);
    }

    /**
     * Create a new period
     */
    public Period createPeriod(String schoolCode, String teacherId, String name, String subject, 
                              Integer periodNumber, LocalTime startTime, LocalTime endTime, 
                              String roomNumber, Integer maxStudents, String academicYear, String semester) {
        School school = getSchoolByCode(schoolCode);
        Teacher teacher = getTeacherByTeacherId(teacherId);
        
        // Check if period ID already exists (generate unique one)
        String periodId;
        do {
            periodId = generatePeriodId();
        } while (periodRepository.findByPeriodId(periodId).isPresent());

        Period period = new Period();
        period.setPeriodId(periodId);
        period.setSchool(school);
        period.setTeacher(teacher);
        period.setName(name);
        period.setSubject(subject);
        period.setPeriodNumber(periodNumber);
        period.setStartTime(startTime);
        period.setEndTime(endTime);
        period.setRoomNumber(roomNumber);
        period.setMaxStudents(maxStudents);
        period.setAcademicYear(academicYear);
        period.setSemester(semester);
        period.setCreatedDate(LocalDate.now());
        period.setStatus("ACTIVE");

        return periodRepository.save(period);
    }

    /**
     * Update a period
     */
    public Period updatePeriod(String periodId, String name, String subject, Integer periodNumber,
                              LocalTime startTime, LocalTime endTime, String roomNumber, Integer maxStudents) {
        Period period = getPeriodByPeriodId(periodId);
        
        period.setName(name);
        period.setSubject(subject);
        period.setPeriodNumber(periodNumber);
        period.setStartTime(startTime);
        period.setEndTime(endTime);
        period.setRoomNumber(roomNumber);
        period.setMaxStudents(maxStudents);
        
        return periodRepository.save(period);
    }

    /**
     * Update period teacher
     */
    public Period updatePeriodTeacher(String periodId, String newTeacherId) {
        Period period = getPeriodByPeriodId(periodId);
        Teacher newTeacher = getTeacherByTeacherId(newTeacherId);
        
        period.setTeacher(newTeacher);
        
        return periodRepository.save(period);
    }

    /**
     * Update period capacity
     */
    public Period updatePeriodCapacity(String periodId, Integer maxStudents) {
        Period period = getPeriodByPeriodId(periodId);
        period.setMaxStudents(maxStudents);
        return periodRepository.save(period);
    }

    /**
     * Deactivate a period (soft delete)
     */
    public void deactivatePeriod(String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        period.setStatus("INACTIVE");
        periodRepository.save(period);
    }

    /**
     * Activate a period
     */
    public Period activatePeriod(String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        period.setStatus("ACTIVE");
        return periodRepository.save(period);
    }

    /**
     * Delete a period permanently
     */
    public void deletePeriod(String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        periodRepository.delete(period);
    }

    /**
     * Check if period exists by period ID
     */
    public boolean existsByPeriodId(String periodId) {
        return periodRepository.findByPeriodId(periodId).isPresent();
    }

    /**
     * Get active enrollment count for a period
     */
    public long getActiveEnrollmentCount(String periodId) {
        Period period = getPeriodByPeriodId(periodId);
        return periodRepository.countActiveEnrollmentsByPeriod(period.getId());
    }

    /**
     * Get period count by school
     */
    public long getPeriodCountBySchool(String schoolCode) {
        School school = getSchoolByCode(schoolCode);
        return periodRepository.findBySchoolAndStatus(school, "ACTIVE").size();
    }

    // Private helper methods
    private School getSchoolByCode(String schoolCode) {
        return schoolRepository.findBySchoolCode(schoolCode)
            .orElseThrow(() -> new EntityNotFoundException("School not found: " + schoolCode, null));
    }

    private Teacher getTeacherByTeacherId(String teacherId) {
        return teacherRepository.findByTeacherId(teacherId)
            .orElseThrow(() -> new EntityNotFoundException("Teacher not found: " + teacherId, null));
    }

    private String generatePeriodId() {
        return "PER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}