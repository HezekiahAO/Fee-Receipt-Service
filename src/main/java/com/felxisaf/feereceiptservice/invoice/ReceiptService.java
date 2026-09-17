package com.felxisaf.feereceiptservice.invoice;

import com.felxisaf.feereceiptservice.common.exception.ResourceNotFoundException;
import com.felxisaf.feereceiptservice.students.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReceiptService {

    private final PaymentRepository paymentRepository;
    private final TemplateEngine templateEngine;

    public ReceiptService(PaymentRepository paymentRepository, TemplateEngine templateEngine) {
        this.paymentRepository = paymentRepository;
        this.templateEngine = templateEngine;
    }

    @Transactional(readOnly = true)
    public byte[] generateReceiptPdf(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        Invoice invoice = payment.getInvoice();
        Student student = invoice.getStudent();

        BigDecimal totalPaidToDate = sumPaymentsForInvoice(invoice.getId());
        BigDecimal outstandingBalance = invoice.getTotalAmount().subtract(totalPaidToDate);

        Context context = new Context();
        context.setVariable("payment", payment);
        context.setVariable("invoice", invoice);
        context.setVariable("student", student);
        context.setVariable("totalPaidToDate", totalPaidToDate);
        context.setVariable("outstandingBalance", outstandingBalance);
        context.setVariable("generatedAt", OffsetDateTime.now());

        String renderedHtml = templateEngine.process("receipt", context);

        return convertHtmlToPdf(renderedHtml);
    }

    private BigDecimal sumPaymentsForInvoice(Long invoiceId) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoiceId);
        BigDecimal total = BigDecimal.ZERO;
        for (Payment p : payments) {
            total = total.add(p.getAmount());
        }
        return total;
    }

    private byte[] convertHtmlToPdf(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF receipt", e);
        }
    }
}