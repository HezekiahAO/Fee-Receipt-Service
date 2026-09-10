package com.felxisaf.feereceiptservice.students.dto;

import com.felxisaf.feereceiptservice.students.Student;

import java.time.OffsetDateTime;

public class StudentResponse {

    private final Long id;
    private final String studentNumber;
    private final String fullName;
    private final String email;
    private final OffsetDateTime createdAt;

    public StudentResponse(Long id, String studentNumber, String fullName,
                            String email, OffsetDateTime createdAt) {
        this.id = id;
        this.studentNumber = studentNumber;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public static StudentResponse fromEntity(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getFullName(),
                student.getEmail(),
                student.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}