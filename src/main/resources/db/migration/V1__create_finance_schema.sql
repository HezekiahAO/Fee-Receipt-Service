CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    student_number VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
    
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    student_id BIGINT NOT NULL REFERENCES students(id),
    currency CHAR(3) NOT NULL,
    total_amount NUMERIC(19, 4) NOT NULL CHECK (total_amount > 0), 
    status VARCHAR(20) NOT NULL CHECK (status IN ('DRAFT', 'ISSUED', 'PARTIALLY_PAID', 'PAID', 'VOID')),
    
    due_date DATE NOT NULL,
    issued_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE, description VARCHAR(255) NOT NULL,
    quantity NUMERIC(19, 4) NOT NULL CHECK (quantity > 0),
    unit_amount NUMERIC(19, 4) NOT NULL CHECK (unit_amount >= 0),  -- ensures the db nevers allows a negative unit amount
    line_total NUMERIC(19, 4) NOT NULL CHECK (line_total >= 0)
);

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_reference VARCHAR(100) NOT NULL UNIQUE,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id),
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    currency CHAR(3) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    gateway_reference VARCHAR(100),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    checksum VARCHAR(128) NOT NULL,
    paid_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_invoices_student_id ON invoices(student_id);
CREATE INDEX idx_invoice_items_invoice_id ON invoice_items(invoice_id);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_paid_at ON payments(paid_at);


-- BigDecimal is Java's equivalent exact-decimal type