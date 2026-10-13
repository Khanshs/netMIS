-- Extend the existing MySQL demo so all 30 report queries can run.
-- Run billing_demo.sql and customer_accounts.sql first. Safe to rerun on the demo database.
USE netcafe_billing_demo;

-- MySQL 8.0 does not support ADD COLUMN IF NOT EXISTS. Prepare conditional DDL.
SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employees'
     AND COLUMN_NAME = 'employee_status') = 0,
  'ALTER TABLE employees ADD COLUMN employee_status VARCHAR(20) NOT NULL DEFAULT ''ACTIVE''',
  'SELECT 1');
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sessions'
     AND COLUMN_NAME = 'account_id') = 0,
  'ALTER TABLE sessions ADD COLUMN account_id VARCHAR(20) NULL',
  'SELECT 1');
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sessions'
     AND COLUMN_NAME = 'time_start') = 0,
  'ALTER TABLE sessions ADD COLUMN time_start DATETIME NULL',
  'SELECT 1');
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

-- Fill the four historical sessions from Main.sample(); keep any user-entered values.
UPDATE sessions SET account_id = CASE session_id
    WHEN 'S01' THEN 'A01' WHEN 'S02' THEN 'A02'
    WHEN 'S03' THEN 'A03' WHEN 'S04' THEN 'A01' END
WHERE account_id IS NULL AND session_id IN ('S01', 'S02', 'S03', 'S04');

UPDATE sessions SET time_start = CASE session_id
    WHEN 'S01' THEN '2026-10-05 08:00:00'
    WHEN 'S02' THEN '2026-10-05 09:00:00'
    WHEN 'S03' THEN '2026-10-05 10:00:00'
    WHEN 'S04' THEN '2026-10-05 11:00:00' END
WHERE time_start IS NULL AND session_id IN ('S01', 'S02', 'S03', 'S04');

SET @ddl = IF(
  (SELECT COUNT(*) FROM information_schema.REFERENTIAL_CONSTRAINTS
   WHERE CONSTRAINT_SCHEMA = DATABASE()
     AND CONSTRAINT_NAME = 'fk_session_account') = 0,
  'ALTER TABLE sessions ADD CONSTRAINT fk_session_account FOREIGN KEY (account_id) REFERENCES customer_accounts(account_id)',
  'SELECT 1');
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

CREATE TABLE IF NOT EXISTS foods (
  food_id VARCHAR(20) PRIMARY KEY,
  food_name VARCHAR(100) NOT NULL,
  price DECIMAL(15, 0) NOT NULL,
  CONSTRAINT ck_food_price CHECK (price >= 0)
);

CREATE TABLE IF NOT EXISTS food_services (
  service_id VARCHAR(20) PRIMARY KEY,
  session_id VARCHAR(20) NOT NULL,
  employee_id VARCHAR(20) NOT NULL,
  time_start_service DATETIME NOT NULL,
  CONSTRAINT fk_food_service_session FOREIGN KEY (session_id) REFERENCES sessions(session_id),
  CONSTRAINT fk_food_service_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE IF NOT EXISTS food_order_details (
  service_id VARCHAR(20) NOT NULL,
  food_id VARCHAR(20) NOT NULL,
  order_quantity INT NOT NULL,
  price_at_order DECIMAL(15, 0) NOT NULL,
  PRIMARY KEY (service_id, food_id),
  CONSTRAINT fk_order_detail_service FOREIGN KEY (service_id) REFERENCES food_services(service_id),
  CONSTRAINT fk_order_detail_food FOREIGN KEY (food_id) REFERENCES foods(food_id),
  CONSTRAINT ck_order_quantity CHECK (order_quantity > 0),
  CONSTRAINT ck_order_price CHECK (price_at_order >= 0)
);

INSERT IGNORE INTO employees (employee_id, employee_name, employee_status)
VALUES ('E03', 'Luis Enrique', 'INACTIVE');

INSERT IGNORE INTO foods (food_id, food_name, price) VALUES
  ('F01', 'Mì ly', 13000),
  ('F02', 'Nước ngọt', 15000),
  ('F03', 'Snack', 7000);

INSERT IGNORE INTO food_services (service_id, session_id, employee_id, time_start_service) VALUES
  ('SV01', 'S01', 'E01', '2026-10-05 08:20:00'),
  ('SV02', 'S02', 'E02', '2026-10-05 09:30:00'),
  ('SV03', 'S04', 'E01', '2026-10-05 11:15:00');

-- Historical order prices stay fixed even when foods.price changes.
INSERT IGNORE INTO food_order_details (service_id, food_id, order_quantity, price_at_order) VALUES
  ('SV01', 'F01', 1, 15000),
  ('SV01', 'F02', 1, 10000),
  ('SV02', 'F01', 2, 15000),
  ('SV03', 'F02', 2, 10000);

SELECT s.session_id, s.account_id, s.computer_id, s.time_start, s.time_end,
       s.session_status
FROM sessions s ORDER BY s.session_id;

SELECT fs.service_id, COALESCE(SUM(d.order_quantity * d.price_at_order), 0) AS total
FROM food_services fs JOIN food_order_details d ON d.service_id = fs.service_id
GROUP BY fs.service_id ORDER BY fs.service_id;
