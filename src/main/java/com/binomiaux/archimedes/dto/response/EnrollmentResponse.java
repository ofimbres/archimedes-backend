package com.binomiaux.archimedes.dto.response;

import java.time.LocalDate;

/**
 * Response DTO for Enrollment entity.
 * Contains only the necessary information for API responses.
 */
public class EnrollmentResponse {

    private String id;
    private String enrollmentId;
    private String studentId;
    private String studentName;
    private String periodId;
    private String periodName;
    private String subject;
    private String teacherId;
    private String teacherName;
    private String schoolId;
    private LocalDate enrollmentDate;
    private String status;
    private String grade;
    private LocalDate createdDate;

    // Constructors
    public EnrollmentResponse() {}

    public EnrollmentResponse(String id, String enrollmentId, String studentName, String periodName) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.studentName = studentName;
        this.periodName = periodName;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "EnrollmentResponse{" +
                "id='" + id + '\'' +
                ", enrollmentId='" + enrollmentId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", periodId='" + periodId + '\'' +
                ", periodName='" + periodName + '\'' +
                ", subject='" + subject + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", schoolId='" + schoolId + '\'' +
                ", enrollmentDate=" + enrollmentDate +
                ", status='" + status + '\'' +
                ", grade='" + grade + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}