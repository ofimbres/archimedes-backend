package com.binomiaux.archimedes.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "enrollment_id", unique = true, nullable = false)
    private String enrollmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private Period period;
    
    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;
    
    @Column(name = "status")
    private String status = "ACTIVE";
    
    @Column(name = "grade")
    private String grade;
    
    @Column(name = "created_date")
    private LocalDate createdDate;
    
    // Constructors
    public Enrollment() {
        this.createdDate = LocalDate.now();
        this.enrollmentDate = LocalDate.now();
    }
    
    public Enrollment(String enrollmentId, Student student, Period period) {
        this();
        this.enrollmentId = enrollmentId;
        this.student = student;
        this.period = period;
    }
    
    // Helper methods
    public String getStudentFullName() {
        return student != null ? student.getFullName() : "";
    }
    
    public String getPeriodDisplayName() {
        return period != null ? period.getDisplayName() : "";
    }
    
    public String getTeacherLastName() {
        return (period != null && period.getTeacher() != null) ? period.getTeacher().getLastName() : "";
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }
    
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    
    public Period getPeriod() { return period; }
    public void setPeriod(Period period) { this.period = period; }
    
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
}