package com.felxisaf.feereceiptservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests exercising the real Spring context and your actual local Postgres database
 * (via the "local" profile, same as application-local.yml). Every test class is @Transactional,
 * which makes Spring wrap each test method in a transaction and automatically roll it back the
 * moment the test finishes -- so nothing written here ever permanently lands in your dev database,
 * regardless of how many times you run this suite.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class PaymentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------------
    // Test 1: the full "First Working Milestone" flow from the README
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Full flow: create student, create invoice, issue it, pay it, check balances")
    void fullMilestoneFlow() throws Exception {
        String suffix = uniqueSuffix();

        Long studentId = createStudent(suffix);
        Long invoiceId = createInvoice(suffix, studentId, "USD", "100.00");
        issueInvoice(invoiceId);

        JsonNode payment = createPayment(
                "idem-" + suffix, "PAY-" + suffix, invoiceId, "60.00", "USD", "CARD");
        assertMoneyEquals(payment.get("amount"), "60.00");

        JsonNode invoiceBalance = getInvoiceBalance(invoiceId);
        assertMoneyEquals(invoiceBalance.get("amountPaid"), "60.00");
        assertMoneyEquals(invoiceBalance.get("outstandingBalance"), "40.00");
        assertThat(invoiceBalance.get("status").asText()).isEqualTo("PARTIALLY_PAID");

        JsonNode studentBalance = getStudentBalance(studentId);
        assertMoneyEquals(studentBalance.get("totalOutstanding"), "40.00");

        // Paying the remaining balance should flip the invoice to fully PAID.
        createPayment("idem-" + suffix + "-b", "PAY-" + suffix + "-b", invoiceId, "40.00", "USD", "CARD");
        JsonNode finalBalance = getInvoiceBalance(invoiceId);
        assertMoneyEquals(finalBalance.get("outstandingBalance"), "0.00");
        assertThat(finalBalance.get("status").asText()).isEqualTo("PAID");
    }

    // ---------------------------------------------------------------------
    // Test 2 & 3: idempotency, both directions
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Same idempotency key + same body returns the original payment, not a duplicate")
    void idempotentRetryReturnsOriginalPayment() throws Exception {
        String suffix = uniqueSuffix();
        Long studentId = createStudent(suffix);
        Long invoiceId = createInvoice(suffix, studentId, "USD", "100.00");
        issueInvoice(invoiceId);

        String idempotencyKey = "idem-" + suffix;
        String requestBody = paymentJson("PAY-" + suffix, invoiceId, "50.00", "USD", "CARD");

        MvcResult firstAttempt = mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult secondAttempt = mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn();

        JsonNode firstPayment = objectMapper.readTree(firstAttempt.getResponse().getContentAsString());
        JsonNode secondPayment = objectMapper.readTree(secondAttempt.getResponse().getContentAsString());

        // Same payment ID returned both times -- proof no duplicate was created.
        assertThat(secondPayment.get("id").asLong()).isEqualTo(firstPayment.get("id").asLong());
    }

    @Test
    @DisplayName("Same idempotency key + different body returns 409 Conflict")
    void idempotencyKeyReusedWithDifferentBody_returns409() throws Exception {
        String suffix = uniqueSuffix();
        Long studentId = createStudent(suffix);
        Long invoiceId = createInvoice(suffix, studentId, "USD", "100.00");
        issueInvoice(invoiceId);

        String idempotencyKey = "idem-" + suffix;

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("PAY-" + suffix + "-a", invoiceId, "50.00", "USD", "CARD")))
                .andExpect(status().isCreated());

        // Same key, but the amount is different this time -> should be rejected.
        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("PAY-" + suffix + "-b", invoiceId, "75.00", "USD", "CARD")))
                .andExpect(status().isConflict());
    }

    // ---------------------------------------------------------------------
    // Test 4: duplicate payment reference
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Reusing a payment reference is rejected with 409, even under a new idempotency key")
    void duplicatePaymentReference_returns409() throws Exception {
        String suffix = uniqueSuffix();
        Long studentId = createStudent(suffix);
        Long invoiceId = createInvoice(suffix, studentId, "USD", "100.00");
        issueInvoice(invoiceId);

        String sharedReference = "PAY-" + suffix;

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "idem-" + suffix + "-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(sharedReference, invoiceId, "30.00", "USD", "CARD")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "idem-" + suffix + "-b")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(sharedReference, invoiceId, "30.00", "USD", "CARD")))
                .andExpect(status().isConflict());
    }

    // ---------------------------------------------------------------------
    // Test 5: overpayment rejected
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("A payment larger than the outstanding balance is rejected with 422")
    void overpayment_returns422() throws Exception {
        String suffix = uniqueSuffix();
        Long studentId = createStudent(suffix);
        Long invoiceId = createInvoice(suffix, studentId, "USD", "100.00");
        issueInvoice(invoiceId);

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "idem-" + suffix)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("PAY-" + suffix, invoiceId, "150.00", "USD", "CARD")))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------------------------------------------------------------------
    // Test 6: currency mismatch -- regression test for the real bug we caught
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("REGRESSION: a payment in a different currency than its invoice is rejected with 422")
    void paymentCurrencyMustMatchInvoiceCurrency_returns422() throws Exception {
        String suffix = uniqueSuffix();
        Long studentId = createStudent(suffix);
        Long invoiceId = createInvoice(suffix, studentId, "USD", "100.00");
        issueInvoice(invoiceId);

        // Invoice is USD, this payment is NGN -- this exact mismatch is the bug that was
        // caught while testing PDF receipts. This test guarantees it can never silently
        // regress if this code is touched again in the future.
        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "idem-" + suffix)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("PAY-" + suffix, invoiceId, "50.00", "NGN", "CARD")))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------------------------------------------------------------------
    // Test 7: an already-issued invoice cannot be issued again
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Issuing an already-ISSUED invoice a second time is rejected with 422")
    void issuingAnAlreadyIssuedInvoice_returns422() throws Exception {
        String suffix = uniqueSuffix();
        Long studentId = createStudent(suffix);
        Long invoiceId = createInvoice(suffix, studentId, "USD", "100.00");
        issueInvoice(invoiceId);

        mockMvc.perform(post("/invoices/{id}/issue", invoiceId))
                .andExpect(status().isUnprocessableEntity());
    }

    // ---------------------------------------------------------------------
    // Test 8: bean validation on a plain bad request
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("Creating a student with a missing required field returns 422 with details")
    void createStudent_missingRequiredField_returns422() throws Exception {
        String badRequestBody = """
                {
                    "studentNumber": "STU-MISSING-EMAIL",
                    "fullName": "No Email Here"
                }
                """;

        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRequestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details").isArray());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Compares a JSON decimal field against an expected value NUMERICALLY, not as exact text.
     * JSON's own number format doesn't preserve trailing zeros through every parsing path
     * (ObjectMapper.readTree builds numbers as double by default, which drops them), so
     * "60.00" and "60.0" can appear different as strings while being the same number.
     * BigDecimal's isEqualByComparingTo checks mathematical value, ignoring scale.
     */
    private void assertMoneyEquals(JsonNode actualNode, String expected) {
        assertThat(new BigDecimal(actualNode.asText())).isEqualByComparingTo(new BigDecimal(expected));
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private Long createStudent(String suffix) throws Exception {
        String body = """
                {
                    "studentNumber": "STU-%s",
                    "fullName": "Test Student %s",
                    "email": "test-%s@example.com"
                }
                """.formatted(suffix, suffix, suffix);

        MvcResult result = mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private Long createInvoice(String suffix, Long studentId, String currency, String totalAmount) throws Exception {
        String body = """
                {
                    "invoiceNumber": "INV-%s",
                    "studentId": %d,
                    "currency": "%s",
                    "dueDate": "%s",
                    "items": [
                        { "description": "Tuition Fee", "quantity": 1, "unitAmount": %s }
                    ]
                }
                """.formatted(suffix, studentId, currency, LocalDate.now().plusDays(30), totalAmount);

        MvcResult result = mockMvc.perform(post("/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertMoneyEquals(json.get("totalAmount"), totalAmount);
        return json.get("id").asLong();
    }

    private void issueInvoice(Long invoiceId) throws Exception {
        mockMvc.perform(post("/invoices/{id}/issue", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ISSUED"));
    }

    private String paymentJson(String reference, Long invoiceId, String amount, String currency, String method) {
        return """
                {
                    "paymentReference": "%s",
                    "invoiceId": %d,
                    "amount": %s,
                    "currency": "%s",
                    "paymentMethod": "%s"
                }
                """.formatted(reference, invoiceId, amount, currency, method);
    }

    private JsonNode createPayment(String idempotencyKey, String reference, Long invoiceId,
                                    String amount, String currency, String method) throws Exception {
        MvcResult result = mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(reference, invoiceId, amount, currency, method)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getInvoiceBalance(Long invoiceId) throws Exception {
        MvcResult result = mockMvc.perform(get("/invoices/{id}/balance", invoiceId))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getStudentBalance(Long studentId) throws Exception {
        MvcResult result = mockMvc.perform(get("/students/{id}/balance", studentId))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}