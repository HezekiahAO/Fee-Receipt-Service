package com.felxisaf.feereceiptservice.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByStudentId(Long studentId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    // Fetches the invoice AND its line items in a single query. Without this, invoice.getItems()
    // stays an uninitialized lazy proxy -- fine while the transaction is open, but accessing it
    // AFTER the transaction commits (e.g. back in the controller) throws LazyInitializationException,
    // since Hibernate can no longer reach the database to fill it in.
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Optional<Invoice> findByIdWithItems(@Param("id") Long id);
}