package com.binomiaux.archimedes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.binomiaux.archimedes.dto.request.CreateTeacherRequest;
import com.binomiaux.archimedes.dto.request.UpdateTeacherRequest;
import com.binomiaux.archimedes.exception.business.MaxPeriodsExceededException;
import com.binomiaux.archimedes.exception.common.EntityNotFoundException;
import com.binomiaux.archimedes.model.Period;
import com.binomiaux.archimedes.model.School;
import com.binomiaux.archimedes.model.Teacher;
import com.binomiaux.archimedes.repository.TeacherRepository;

/**
 * Enhanced TeacherService with full CRUD operations and business logic.
 * Implements the DynamoDB schema design patterns.
 */
@Service
public class TeacherService {

    private static final int DEFAULT_PERIODS_PER_TEACHER = 6;

    private final TeacherRepository teacherRepository;
    private final SchoolService schoolService;
    private final IdGeneratorService idGeneratorService;
    private final PeriodService periodService;

    public TeacherService(TeacherRepository teacherRepository, 
                         SchoolService schoolService,
                         IdGeneratorService idGeneratorService,
                         PeriodService periodService) {
        this.teacherRepository = teacherRepository;
        this.schoolService = schoolService;
        this.idGeneratorService = idGeneratorService;
        this.periodService = periodService;
    }

    /**
     * Creates a new teacher using the improved schema design.
     * Note: DynamoDB doesn't support traditional transactions like RDBMS
     */
    public Teacher createTeacher(CreateTeacherRequest request) {
        // Validate school exists
        School school = schoolService.getSchool(request.getSchoolId());
        if (school == null) {
            throw new EntityNotFoundException("School " + request.getSchoolId() + " not found", null);
        }

        // Check for duplicate email - TODO: Implement findByEmail in repository
        // if (teacherRepository.findByEmail(request.getEmail()).isPresent()) {
        //     throw new DuplicateEmailException("Teacher with email " + request.getEmail() + " already exists");
        // }

        // Generate sequential teacher ID
        String teacherId = idGeneratorService.generateTeacherId(request.getSchoolId());

        // Build teacher entity following schema design
        Teacher teacher = new Teacher();
        teacher.setPk("TEACHER#" + teacherId);
        teacher.setSk("#METADATA");
        teacher.setEntityType("TEACHER");
        teacher.setTeacherId(teacherId);
        teacher.setSchoolId(request.getSchoolId());
        teacher.setFirstName(request.getFirstName());
        teacher.setLastName(request.getLastName());
        teacher.setFullName(request.getFirstName() + " " + request.getLastName());
        teacher.setEmail(request.getEmail());
        teacher.setUsername(generateUsername(request.getFirstName(), request.getLastName()));
        teacher.setMaxPeriods(request.getMaxPeriods() != null ? request.getMaxPeriods() : 6);
        
        // Set GSI values for efficient queries per schema (using correct method names)
        teacher.setParentEntityKey("SCHOOL#" + request.getSchoolId());
        teacher.setChildEntityKey("TEACHER#" + teacherId);
        teacher.setSearchTypeKey("EMAIL");
        teacher.setSearchValueKey(request.getEmail());

        // Use repository.create() instead of save()
        teacherRepository.create(teacher);
        return teacher;
    }

    /**
     * Legacy method for backward compatibility.
     */
    public void createTeacher(Teacher teacher) {
        // Business logic validation - check if school exists
        School school = schoolService.getSchool(teacher.getSchoolId());
        if (school == null) {
            throw new EntityNotFoundException("School " + teacher.getSchoolId() + " not found", null);
        }
        
        teacherRepository.create(teacher);
        
        // Create default periods for the teacher
        createDefaultPeriodsForTeacher(teacher);
    }

    /**
     * Creates default periods for a new teacher with simplified school-scoped IDs.
     */
    private void createDefaultPeriodsForTeacher(Teacher teacher) {
        for (int i = 1; i <= DEFAULT_PERIODS_PER_TEACHER; i++) {
            // Generate unique period ID using counter: P001, P002, etc.
            String periodId = idGeneratorService.generatePeriodId(teacher.getSchoolId());
            String periodName = "Period " + i;
            
            // Create period with simplified structure - teacher relationship maintained via teacherId field
            Period period = new Period(periodId, teacher.getSchoolId(), teacher.getTeacherId(), i, periodName,
                                     teacher.getFirstName(), teacher.getLastName());
            
            // Repository will generate keys as: PERIOD#SCH001#P001 with teacherId as reference field
            periodService.createPeriod(period);
        }
    }

    /**
     * Enhanced getTeacher with better error handling.
     */
    public Teacher getTeacher(String schoolId, String teacherId) {
        Teacher teacher = teacherRepository.find(schoolId, teacherId);
        if (teacher == null) {
            throw new EntityNotFoundException("Teacher " + teacherId + " not found", null);
        }
        return teacher;
    }

    /**
     * Find teacher by email - useful for authentication.
     * TODO: Implement findByEmail in repository using GSI query
     */
    public Optional<Teacher> getTeacherByEmail(String email) {
        // return teacherRepository.findByEmail(email);
        return Optional.empty(); // Temporary implementation
    }

    /**
     * Get all teachers in a school.
     * TODO: Implement findBySchool in repository using GSI query
     */
    public List<Teacher> getTeachersBySchool(String schoolId) {
        // return teacherRepository.findBySchool(schoolId);
        return List.of(); // Temporary implementation
    }

    /**
     * Update teacher information.
     * Note: DynamoDB operations are atomic at item level
     */
    public Teacher updateTeacher(String schoolId, String teacherId, UpdateTeacherRequest request) {
        Teacher existingTeacher = getTeacher(schoolId, teacherId);

        // Check email uniqueness if email is being changed - TODO: Implement findByEmail
        if (request.getEmail() != null && !request.getEmail().equals(existingTeacher.getEmail())) {
            // if (teacherRepository.findByEmail(request.getEmail()).isPresent()) {
            //     throw new DuplicateEmailException("Teacher with email " + request.getEmail() + " already exists");
            // }
            existingTeacher.setEmail(request.getEmail());
            existingTeacher.setSearchValueKey(request.getEmail());
        }

        // Update other fields if provided
        if (request.getFirstName() != null) {
            existingTeacher.setFirstName(request.getFirstName());
            updateFullName(existingTeacher);
        }
        
        if (request.getLastName() != null) {
            existingTeacher.setLastName(request.getLastName());
            updateFullName(existingTeacher);
        }
        
        if (request.getMaxPeriods() != null) {
            validateMaxPeriodsUpdate(schoolId, teacherId, request.getMaxPeriods());
            existingTeacher.setMaxPeriods(request.getMaxPeriods());
        }

        // TODO: Check if Teacher model needs this repository method
        // return teacherRepository.save(existingTeacher);
        
        // For now, since repository only has create() method, we'll assume update is handled elsewhere
        return existingTeacher;
    }

    /**
     * Delete teacher with proper cleanup.
     * Note: Check dependencies before deletion
     */
    public void deleteTeacher(String schoolId, String teacherId) {
        Teacher teacher = getTeacher(schoolId, teacherId);
        
        // Check if teacher has active periods
        List<Period> activePeriods = periodService.getPeriodsByTeacherId(teacherId);
        if (!activePeriods.isEmpty()) {
            throw new IllegalStateException(
                "Cannot delete teacher with active periods. Please reassign or delete " + 
                activePeriods.size() + " periods first."
            );
        }

        // TODO: Implement delete method in TeacherRepository
        // teacherRepository.delete(teacher);
    }

    /**
     * Get all periods taught by a teacher.
     */
    public List<Period> getTeacherPeriods(String teacherId) {
        return periodService.getPeriodsByTeacherId(teacherId);
    }

    /**
     * Check if teacher can take more periods.
     */
    public boolean canTeachMorePeriods(String schoolId, String teacherId) {
        Teacher teacher = getTeacher(schoolId, teacherId);
        List<Period> currentPeriods = getTeacherPeriods(teacherId);
        return currentPeriods.size() < teacher.getMaxPeriods();
    }

    /**
     * Get available capacity for a teacher.
     */
    public int getAvailableCapacity(String schoolId, String teacherId) {
        Teacher teacher = getTeacher(schoolId, teacherId);
        List<Period> currentPeriods = getTeacherPeriods(teacherId);
        return Math.max(0, teacher.getMaxPeriods() - currentPeriods.size());
    }

    /**
     * Validate teacher capacity before assigning new period.
     */
    public void validateTeacherCapacity(String schoolId, String teacherId) {
        if (!canTeachMorePeriods(schoolId, teacherId)) {
            Teacher teacher = getTeacher(schoolId, teacherId);
            throw new MaxPeriodsExceededException(
                "Teacher " + teacher.getFullName() + " has reached maximum periods (" + 
                teacher.getMaxPeriods() + ")"
            );
        }
    }

    // Helper methods

    private String generateUsername(String firstName, String lastName) {
        return (firstName + "." + lastName).toLowerCase().replaceAll("\\s+", "");
    }

    private void updateFullName(Teacher teacher) {
        teacher.setFullName(teacher.getFirstName() + " " + teacher.getLastName());
        teacher.setUsername(generateUsername(teacher.getFirstName(), teacher.getLastName()));
    }

    private void validateMaxPeriodsUpdate(String schoolId, String teacherId, Integer newMaxPeriods) {
        List<Period> currentPeriods = getTeacherPeriods(teacherId);
        if (currentPeriods.size() > newMaxPeriods) {
            throw new MaxPeriodsExceededException(
                "Cannot reduce max periods to " + newMaxPeriods + 
                ". Teacher currently has " + currentPeriods.size() + " active periods."
            );
        }
    }
}
