-- Add customer and account persistence to the existing MySQL demo database.
-- Safe to rerun: existing rows are preserved. Demo password hashes cannot be used for login.

CREATE DATABASE IF NOT EXISTS netcafe_billing_demo
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE netcafe_billing_demo;

CREATE TABLE IF NOT EXISTS customers (
  customer_id VARCHAR(20) PRIMARY KEY,
  customer_name VARCHAR(100) NOT NULL,
  tier VARCHAR(20) NOT NULL,
  CONSTRAINT ck_customer_tier CHECK (tier IN ('NORMAL', 'MEMBER', 'VIP'))
);

CREATE TABLE IF NOT EXISTS customer_accounts (
  account_id VARCHAR(20) PRIMARY KEY,
  customer_id VARCHAR(20) NOT NULL,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  balance DECIMAL(15, 0) NOT NULL DEFAULT 0,
  account_status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  CONSTRAINT ck_account_balance CHECK (balance >= 0),
  CONSTRAINT ck_account_status CHECK (account_status IN ('ACTIVE', 'INACTIVE'))
);

INSERT IGNORE INTO customers (customer_id, customer_name, tier) VALUES
  ('C01', 'Lamine Yamal', 'NORMAL'),
  ('C02', 'Bruno Fernades', 'NORMAL'),
  ('C03', 'Cristiano Ronaldo', 'NORMAL'),
  ('C04', 'Lionel Messi', 'NORMAL');

INSERT IGNORE INTO customer_accounts
  (account_id, customer_id, username, password_hash, balance, account_status) VALUES
  ('A01', 'C01', 'yamal', 'demo-hash-not-for-login', 0, 'ACTIVE'),
  ('A02', 'C02', 'bruno', 'demo-hash-not-for-login', 0, 'ACTIVE'),
  ('A03', 'C03', 'ronaldo', 'demo-hash-not-for-login', 0, 'ACTIVE'),
  ('A04', 'C04', 'messi', 'demo-hash-not-for-login', 0, 'ACTIVE');

SELECT c.customer_id, c.customer_name, c.tier,
       a.account_id, a.username, a.balance, a.account_status
FROM customers c LEFT JOIN customer_accounts a ON a.customer_id = c.customer_id
ORDER BY c.customer_id;
