package com.felxisaf.feereceiptservice.invoice;

import com.felxisaf.feereceiptservice.common.exception.BusinessRuleViolationException;
import com.felxisaf.feereceiptservice.common.exception.DuplicateResourceException;
import com.felxisaf.feereceiptservice.common.exception.ResourceNotFoundException;
import com.felxisaf.feereceiptservice.invoice.dto.InvoiceItemRequest;
import com.felxisaf.feereceiptservice.invoice.dto.InvoiceRequest;
import com.felxisaf.feereceiptservice.students.Student;
import com.felxisaf.feereceiptservice.students.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final StudentRepository studentRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, StudentRepository studentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Invoice createInvoice(InvoiceRequest request) {
        if (invoiceRepository.existsByInvoiceNumber(request.getInvoiceNumber())) {
            throw new DuplicateResourceException(
                    "An invoice with invoiceNumber '" + request.getInvoiceNumber() + "' already exists");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getStudentId()));

        BigDecimal computedTotal = BigDecimal.ZERO;
        for (InvoiceItemRequest itemRequest : request.getItems()) {
            BigDecimal lineTotal = itemRequest.getQuantity().multiply(itemRequest.getUnitAmount());
            computedTotal = computedTotal.add(lineTotal);
        }

        if (computedTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleViolationException(
                    "Invoice total must be greater than zero based on the provided line items");
        }

        Invoice invoice = new Invoice(
                request.getInvoiceNumber(),
                student,
                request.getCurrency().toUpperCase(),
                computedTotal,
                request.getDueDate()
        );

        for (InvoiceItemRequest itemRequest : request.getItems()) {
            InvoiceItem item = new InvoiceItem(
                    itemRequest.getDescription(),
                    itemRequest.getQuantity(),
                    itemRequest.getUnitAmount()
            );
            invoice.addItem(item);
        }

        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    @Transactional
    public Invoice issueInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessRuleViolationException(
                    "Only invoices in DRAFT status can be issued. Current status: " + invoice.getStatus());
        }

        invoice.issue();
        return invoiceRepository.save(invoice);
    }
}