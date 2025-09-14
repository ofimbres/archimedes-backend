package com.binomiaux.archimedes.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO for Period entity.
 * Contains only the necessary information for API responses.
 */
public class PeriodResponse {

    private String id;
    private String periodId;
    private String schoolId;
    private String teacherId;
    private String teacherName;
    private String name;
    private String subject;
    private Integer periodNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomNumber;
    private Integer maxStudents;
    private Integer currentEnrollment;
    private String academicYear;
    private String semester;
    private String status;
    private LocalDate createdDate;

    // Constructors
    public PeriodResponse() {}

    public PeriodResponse(String id, String periodId, String name) {
        this.id = id;
        this.periodId = periodId;
        this.name = name;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriodId() {
        return periodId;
    }

    public void setPeriodId(String periodId) {
        this.periodId = periodId;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(Integer periodNumber) {
        this.periodNumber = periodNumber;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public Integer getCurrentEnrollment() {
        return currentEnrollment;
    }

    public void setCurrentEnrollment(Integer currentEnrollment) {
        this.currentEnrollment = currentEnrollment;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "PeriodResponse{" +
                "id='" + id + '\'' +
                ", periodId='" + periodId + '\'' +
                ", schoolId='" + schoolId + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", name='" + name + '\'' +
                ", subject='" + subject + '\'' +
                ", periodNumber=" + periodNumber +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", roomNumber='" + roomNumber + '\'' +
                ", maxStudents=" + maxStudents +
                ", currentEnrollment=" + currentEnrollment +
                ", academicYear='" + academicYear + '\'' +
                ", semester='" + semester + '\'' +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}