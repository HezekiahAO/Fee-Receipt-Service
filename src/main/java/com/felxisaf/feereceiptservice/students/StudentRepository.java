package com.felxisaf.feereceiptservice.students;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByStudentNumber(String studentNumber);
}

// Helps automatically generate queries based on method names, allowing for easy retrieval of student data from the database.