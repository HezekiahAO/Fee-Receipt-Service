package com.felxisaf.feereceiptservice.students;

import com.felxisaf.feereceiptservice.common.exception.DuplicateResourceException;
import com.felxisaf.feereceiptservice.common.exception.ResourceNotFoundException;
import com.felxisaf.feereceiptservice.invoice.Invoice;
import com.felxisaf.feereceiptservice.invoice.InvoiceRepository;
import com.felxisaf.feereceiptservice.invoice.InvoiceService;
import com.felxisaf.feereceiptservice.invoice.dto.InvoiceBalanceResponse;
import com.felxisaf.feereceiptservice.students.dto.StudentBalanceResponse;
import com.felxisaf.feereceiptservice.students.dto.StudentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    public StudentService(StudentRepository studentRepository, InvoiceRepository invoiceRepository,
                           InvoiceService invoiceService) {
        this.studentRepository = studentRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceService = invoiceService;
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

    @Transactional(readOnly = true)
    public StudentBalanceResponse getStudentBalance(Long studentId) {
        // Confirm the student exists before doing anything else.
        getStudentById(studentId);

        List<Invoice> invoices = invoiceRepository.findByStudentId(studentId);

        BigDecimal totalInvoiced = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        List<InvoiceBalanceResponse> invoiceBalances = invoices.stream()
                .map(invoice -> invoiceService.getInvoiceBalance(invoice.getId()))
                .toList();

        for (InvoiceBalanceResponse balance : invoiceBalances) {
            totalInvoiced = totalInvoiced.add(balance.getTotalAmount());
            totalPaid = totalPaid.add(balance.getAmountPaid());
            totalOutstanding = totalOutstanding.add(balance.getOutstandingBalance());
        }

        return new StudentBalanceResponse(studentId, totalInvoiced, totalPaid, totalOutstanding, invoiceBalances);
    }
}