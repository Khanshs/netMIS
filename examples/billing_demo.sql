-- JDBC demo for MySQL: computer catalog plus billing.
-- This is an isolated teaching example, not the full NetCafeMIS schema.
-- Can be run again in a local MySQL instance: existing tables/seed rows are kept.
-- The sessions/employees tables are reduced prerequisites.

CREATE DATABASE IF NOT EXISTS netcafe_billing_demo
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE netcafe_billing_demo;

-- Confirm the active schema in Workbench.
SELECT DATABASE() AS active_database;

CREATE TABLE IF NOT EXISTS computer_sections (
  section_id VARCHAR(20) PRIMARY KEY,
  section_name VARCHAR(100) NOT NULL,
  section_price DECIMAL(15, 0) NOT NULL
);

CREATE TABLE IF NOT EXISTS computers (
  computer_id VARCHAR(20) PRIMARY KEY,
  section_id VARCHAR(20) NOT NULL,
  computer_status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_computer_section FOREIGN KEY (section_id)
    REFERENCES computer_sections(section_id)
);

CREATE TABLE IF NOT EXISTS employees (
  employee_id VARCHAR(20) PRIMARY KEY,
  employee_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS sessions (
  session_id VARCHAR(20) PRIMARY KEY,
  computer_id VARCHAR(20) NOT NULL,
  session_status VARCHAR(20) NOT NULL,
  time_end DATETIME NULL,
  CONSTRAINT fk_session_computer FOREIGN KEY (computer_id) REFERENCES computers(computer_id)
);

CREATE TABLE IF NOT EXISTS receipts (
  receipt_id VARCHAR(20) PRIMARY KEY,
  session_id VARCHAR(20) NOT NULL UNIQUE,
  employee_id VARCHAR(20) NOT NULL,
  customer_tier VARCHAR(20) NOT NULL,
  gross_computer_amount DECIMAL(15, 0) NOT NULL,
  gross_service_amount DECIMAL(15, 0) NOT NULL,
  computer_discount_rate DECIMAL(5, 4) NOT NULL,
  service_discount_rate DECIMAL(5, 4) NOT NULL,
  computer_discount_amount DECIMAL(15, 0) NOT NULL,
  service_discount_amount DECIMAL(15, 0) NOT NULL,
  computer_amount DECIMAL(15, 0) NOT NULL,
  service_amount DECIMAL(15, 0) NOT NULL,
  total_amount DECIMAL(15, 0) NOT NULL,
  CONSTRAINT fk_receipt_session FOREIGN KEY (session_id) REFERENCES sessions(session_id),
  CONSTRAINT fk_receipt_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE IF NOT EXISTS payments (
  payment_id VARCHAR(20) PRIMARY KEY,
  receipt_id VARCHAR(20) NOT NULL UNIQUE,
  time_paid DATETIME NOT NULL,
  payment_method VARCHAR(20) NOT NULL,
  amount DECIMAL(15, 0) NOT NULL,
  CONSTRAINT fk_payment_receipt FOREIGN KEY (receipt_id) REFERENCES receipts(receipt_id)
);

-- Computer catalog and billing cases from Main.sample().
INSERT IGNORE INTO computer_sections (section_id, section_name, section_price) VALUES
  ('CS01', 'Khu thường', 12000),
  ('CS02', 'Khu VIP', 20000);

INSERT IGNORE INTO computers (computer_id, section_id, computer_status) VALUES
  ('PC01', 'CS01', 'AVAILABLE'),
  ('PC02', 'CS01', 'IN_USE'),
  ('PC03', 'CS02', 'AVAILABLE'),
  ('PC04', 'CS02', 'AVAILABLE'),
  ('PC05', 'CS01', 'MAINTENANCE'),
  ('PC06', 'CS02', 'AVAILABLE');

-- R01 paid, R02 unpaid, S03 without a receipt.
INSERT IGNORE INTO employees (employee_id, employee_name) VALUES
  ('E01', 'Erik TenHag'),
  ('E02', 'Anchelotti');

INSERT IGNORE INTO sessions (session_id, computer_id, session_status, time_end) VALUES
  ('S01', 'PC01', 'COMPLETED', '2026-10-05 10:00:00'),
  ('S02', 'PC03', 'COMPLETED', '2026-10-05 10:30:00'),
  ('S03', 'PC04', 'COMPLETED', '2026-10-05 11:00:00'),
  ('S04', 'PC02', 'ACTIVE', NULL);

INSERT IGNORE INTO receipts (
  receipt_id, session_id, employee_id, customer_tier,
  gross_computer_amount, gross_service_amount,
  computer_discount_rate, service_discount_rate,
  computer_discount_amount, service_discount_amount,
  computer_amount, service_amount, total_amount
) VALUES
  ('R01', 'S01', 'E02', 'NORMAL', 24000, 25000, 0, 0, 0, 0, 24000, 25000, 49000),
  ('R02', 'S02', 'E01', 'NORMAL', 30000, 30000, 0, 0, 0, 0, 30000, 30000, 60000);

INSERT IGNORE INTO payments (payment_id, receipt_id, time_paid, payment_method, amount)
VALUES ('P01', 'R01', '2026-10-05 10:05:00', 'CASH', 49000);

SHOW TABLES;

-- A missing payment means the receipt is unpaid; do not duplicate this state in receipts.
SELECT r.receipt_id, r.session_id, r.total_amount,
       CASE WHEN p.payment_id IS NULL THEN 'UNPAID' ELSE 'PAID' END AS payment_status
FROM receipts AS r
LEFT JOIN payments AS p ON p.receipt_id = r.receipt_id
ORDER BY r.receipt_id;

-- Revenue is money actually collected, not the sum of all receipts.
SELECT COALESCE(SUM(amount), 0) AS collected_amount FROM payments;

-- A completed session can still be waiting for its receipt.
SELECT s.session_id
FROM sessions AS s
LEFT JOIN receipts AS r ON r.session_id = s.session_id
WHERE s.session_status = 'COMPLETED' AND r.receipt_id IS NULL;

-- The application must additionally verify before INSERT: completed session,
-- active employee, correct discount/amount, and payment time >= session end.
-- INSERT receipt/payment within a service-controlled transaction when a workflow spans tables.
