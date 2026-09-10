package com.felxisaf.feereceiptservice.students;

import com.felxisaf.feereceiptservice.common.exception.DuplicateResourceException;
import com.felxisaf.feereceiptservice.common.exception.ResourceNotFoundException;
import com.felxisaf.feereceiptservice.students.dto.StudentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Student createStudent(StudentRequest request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "A student with email '" + request.getEmail() + "' already exists");
        }
        if (studentRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new DuplicateResourceException(
                    "A student with studentNumber '" + request.getStudentNumber() + "' already exists");
        }

        Student student = new Student(
                request.getStudentNumber(),
                request.getFullName(),
                request.getEmail()
        );
        return studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }
}