# 30 câu truy vấn MySQL cho NetCafeMIS

Tài liệu này triển khai đúng 30 mục ở [đặc tả](Dac_ta_quan_ly_quan_net.md#4-mỗi-thành-viên-viết-10-câu-truy-vấn--chưa-code). Đây là truy vấn **MySQL 8** để xem dữ liệu trong Workbench. Khi nộp bài, nhóm vẫn cần trình bày 10 câu của mỗi thành viên bằng **Java Lambda và C# LINQ** theo yêu cầu của thầy; SQL không thay cho hai phần đó.

Chạy lần lượt `examples/billing_demo.sql`, `examples/customer_accounts.sql`, `examples/full_mis_schema.sql` trong Workbench. Chọn schema `netcafe_billing_demo`. Mỗi khối dưới đây chạy độc lập; có thể sửa các giá trị mẫu như `PC01`, `C01`, `S01`, ngày hoặc ngưỡng giá. Dữ liệu thay đổi khi chạy `JdbcDemo`, nên kết quả không cố định. Truy vấn không ra dòng nào có thể là kết quả đúng, ví dụ câu tìm sai lệch thanh toán.

## Thành viên 1: khu máy, máy tính, phiên sử dụng

### 1. Máy đang trống

Nếu muốn xem **máy AVAILABLE, khu và giá giờ, rẻ trước**:

```sql
SELECT c.computer_id, cs.section_name, cs.section_price
FROM computers AS c
JOIN computer_sections AS cs ON cs.section_id = c.section_id
WHERE c.computer_status = 'AVAILABLE'
ORDER BY cs.section_price, c.computer_id;
```

### 2. Máy đang bảo trì

Nếu muốn xem **máy MAINTENANCE và khu tương ứng**:

```sql
SELECT c.computer_id, cs.section_name
FROM computers AS c
JOIN computer_sections AS cs ON cs.section_id = c.section_id
WHERE c.computer_status = 'MAINTENANCE'
ORDER BY c.computer_id;
```

### 3. Đếm từng trạng thái trong mỗi khu

Nếu muốn xem **cả khu chưa có máy**, mỗi khu có ba dòng trạng thái:

```sql
SELECT cs.section_id, cs.section_name, st.computer_status,
       COUNT(c.computer_id) AS computer_count
FROM computer_sections AS cs
CROSS JOIN (
  SELECT 'AVAILABLE' AS computer_status
  UNION ALL SELECT 'IN_USE'
  UNION ALL SELECT 'MAINTENANCE'
) AS st
LEFT JOIN computers AS c ON c.section_id = cs.section_id
  AND c.computer_status = st.computer_status
GROUP BY cs.section_id, cs.section_name, st.computer_status
ORDER BY cs.section_id, st.computer_status;
```

### 4. Khu có giá cao hơn ngưỡng

Nếu muốn xem **khu có giá giờ trên 15.000đ** (đổi `15000` theo ý):

```sql
SELECT section_id, section_name, section_price
FROM computer_sections
WHERE section_price > 15000
ORDER BY section_price DESC, section_id;
```

### 5. Lịch sử của một máy

Nếu muốn xem **các phiên của PC01, khách sử dụng và thời gian**:

```sql
SELECT s.session_id, c.customer_name, s.time_start, s.time_end,
       s.session_status
FROM sessions AS s
LEFT JOIN customer_accounts AS a ON a.account_id = s.account_id
LEFT JOIN customers AS c ON c.customer_id = a.customer_id
WHERE s.computer_id = 'PC01'
ORDER BY s.time_start DESC, s.session_id DESC;
```

### 6. Phiên đang hoạt động

Nếu muốn xem **khách, máy, khu và lúc bắt đầu của phiên ACTIVE**:

```sql
SELECT s.session_id, cu.customer_name, s.computer_id,
       cs.section_name, s.time_start
FROM sessions AS s
LEFT JOIN customer_accounts AS a ON a.account_id = s.account_id
LEFT JOIN customers AS cu ON cu.customer_id = a.customer_id
JOIN computers AS c ON c.computer_id = s.computer_id
JOIN computer_sections AS cs ON cs.section_id = c.section_id
WHERE s.session_status = 'ACTIVE'
ORDER BY s.time_start, s.session_id;
```

### 7. Phiên hoàn tất từ 120 phút

Nếu muốn xem **phiên dài ít nhất hai giờ**:

```sql
SELECT s.session_id, s.computer_id, s.time_start, s.time_end,
       TIMESTAMPDIFF(MINUTE, s.time_start, s.time_end) AS duration_minutes
FROM sessions AS s
WHERE s.session_status = 'COMPLETED'
  AND TIMESTAMPDIFF(MINUTE, s.time_start, s.time_end) >= 120
ORDER BY duration_minutes DESC, s.session_id;
```

### 8. Tổng phút chơi theo máy trong ngày

Nếu muốn xem **tổng phút của phiên hoàn tất bắt đầu ngày 05/10/2026**, kể cả máy có 0 phút:

```sql
SELECT c.computer_id,
       COALESCE(SUM(TIMESTAMPDIFF(MINUTE, s.time_start, s.time_end)), 0)
         AS completed_minutes
FROM computers AS c
LEFT JOIN sessions AS s ON s.computer_id = c.computer_id
  AND s.session_status = 'COMPLETED'
  AND s.time_start >= '2026-10-05 00:00:00'
  AND s.time_start < '2026-10-06 00:00:00'
GROUP BY c.computer_id
ORDER BY c.computer_id;
```

### 9. Máy chưa từng được dùng

Nếu muốn xem **máy chưa có bất kỳ phiên nào**:

```sql
SELECT c.computer_id, cs.section_name, c.computer_status
FROM computers AS c
JOIN computer_sections AS cs ON cs.section_id = c.section_id
WHERE NOT EXISTS (
  SELECT 1 FROM sessions AS s WHERE s.computer_id = c.computer_id
)
ORDER BY c.computer_id;
```

### 10. Tạm tính tiền máy đang chơi

Nếu muốn xem **tiền máy của phiên ACTIVE tại 12:00 ngày 05/10/2026**, làm tròn đến đồng:

```sql
SELECT s.session_id, s.computer_id, s.time_start,
       cs.section_price,
       ROUND(TIMESTAMPDIFF(SECOND, s.time_start, '2026-10-05 12:00:00')
             * cs.section_price / 3600, 0) AS estimated_computer_amount
FROM sessions AS s
JOIN computers AS c ON c.computer_id = s.computer_id
JOIN computer_sections AS cs ON cs.section_id = c.section_id
WHERE s.session_status = 'ACTIVE'
  AND s.time_start <= '2026-10-05 12:00:00'
ORDER BY s.session_id;
```

## Thành viên 2: khách hàng và dịch vụ ăn uống

### 1. Lịch sử của một khách

Nếu muốn xem **các phiên của khách C01, mới nhất trước**:

```sql
SELECT s.session_id, s.computer_id, s.time_start, s.time_end,
       s.session_status
FROM customers AS c
JOIN customer_accounts AS a ON a.customer_id = c.customer_id
JOIN sessions AS s ON s.account_id = a.account_id
WHERE c.customer_id = 'C01'
ORDER BY s.time_start DESC, s.session_id DESC;
```

### 2. Khách chưa từng chơi

Nếu muốn xem **khách không có phiên qua bất kỳ tài khoản nào**:

```sql
SELECT c.customer_id, c.customer_name
FROM customers AS c
WHERE NOT EXISTS (
  SELECT 1
  FROM customer_accounts AS a
  JOIN sessions AS s ON s.account_id = a.account_id
  WHERE a.customer_id = c.customer_id
)
ORDER BY c.customer_id;
```

### 3. Đếm phiên hoàn tất theo khách

Nếu muốn xem **số phiên COMPLETED, kể cả khách chưa chơi**:

```sql
SELECT c.customer_id, c.customer_name,
       COUNT(DISTINCT s.session_id) AS completed_sessions
FROM customers AS c
LEFT JOIN customer_accounts AS a ON a.customer_id = c.customer_id
LEFT JOIN sessions AS s ON s.account_id = a.account_id
  AND s.session_status = 'COMPLETED'
GROUP BY c.customer_id, c.customer_name
ORDER BY completed_sessions DESC, c.customer_id;
```

### 4. Món có giá dưới ngưỡng

Nếu muốn xem **món hiện có giá không quá 15.000đ**:

```sql
SELECT food_id, food_name, price
FROM foods
WHERE price <= 15000
ORDER BY price, food_id;
```

### 5. Chi tiết gọi món của một phiên

Nếu muốn xem **S01 gọi món gì, ai phục vụ, giá lúc gọi và thành tiền**:

```sql
SELECT fs.service_id, fs.time_start_service, e.employee_name,
       f.food_name, d.order_quantity, d.price_at_order,
       d.order_quantity * d.price_at_order AS line_total
FROM food_services AS fs
JOIN employees AS e ON e.employee_id = fs.employee_id
JOIN food_order_details AS d ON d.service_id = fs.service_id
JOIN foods AS f ON f.food_id = d.food_id
WHERE fs.session_id = 'S01'
ORDER BY fs.time_start_service, fs.service_id, f.food_id;
```

### 6. Tổng tiền món theo phiên

Nếu muốn xem **tiền ăn uống của mọi phiên**, phiên không gọi món là 0:

```sql
SELECT s.session_id,
       COALESCE(SUM(d.order_quantity * d.price_at_order), 0)
         AS food_total
FROM sessions AS s
LEFT JOIN food_services AS fs ON fs.session_id = s.session_id
LEFT JOIN food_order_details AS d ON d.service_id = fs.service_id
GROUP BY s.session_id
ORDER BY food_total DESC, s.session_id;
```

### 7. Phiên hoàn tất không gọi món

Nếu muốn xem **COMPLETED nhưng không phát sinh FoodService**:

```sql
SELECT s.session_id, s.computer_id, s.time_start, s.time_end
FROM sessions AS s
WHERE s.session_status = 'COMPLETED'
  AND NOT EXISTS (
    SELECT 1 FROM food_services AS fs WHERE fs.session_id = s.session_id
  )
ORDER BY s.session_id;
```

### 8. Món được gọi nhiều nhất

Nếu muốn xem **món có tổng số lượng lớn nhất**, giữ tất cả món đồng hạng:

```sql
WITH quantity_by_food AS (
  SELECT f.food_id, f.food_name,
         SUM(d.order_quantity) AS total_quantity
  FROM foods AS f
  JOIN food_order_details AS d ON d.food_id = f.food_id
  GROUP BY f.food_id, f.food_name
)
SELECT food_id, food_name, total_quantity
FROM quantity_by_food
WHERE total_quantity = (SELECT MAX(total_quantity) FROM quantity_by_food)
ORDER BY food_id;
```

### 9. Lần gọi món vượt ngưỡng

Nếu muốn xem **lần gọi món trên 20.000đ, kèm tên khách**:

```sql
SELECT fs.service_id, fs.session_id, c.customer_name,
       SUM(d.order_quantity * d.price_at_order) AS service_total
FROM food_services AS fs
JOIN sessions AS s ON s.session_id = fs.session_id
LEFT JOIN customer_accounts AS a ON a.account_id = s.account_id
LEFT JOIN customers AS c ON c.customer_id = a.customer_id
JOIN food_order_details AS d ON d.service_id = fs.service_id
GROUP BY fs.service_id, fs.session_id, c.customer_name
HAVING service_total > 20000
ORDER BY service_total DESC, fs.service_id;
```

### 10. Gọi món theo nhân viên trong ngày

Nếu muốn xem **số lần gọi và tổng tiền do từng nhân viên xử lý ngày 05/10/2026**, kể cả người có 0:

```sql
SELECT e.employee_id, e.employee_name,
       COUNT(DISTINCT fs.service_id) AS service_count,
       COALESCE(SUM(d.order_quantity * d.price_at_order), 0)
         AS service_total
FROM employees AS e
LEFT JOIN food_services AS fs ON fs.employee_id = e.employee_id
  AND fs.time_start_service >= '2026-10-05 00:00:00'
  AND fs.time_start_service < '2026-10-06 00:00:00'
LEFT JOIN food_order_details AS d ON d.service_id = fs.service_id
GROUP BY e.employee_id, e.employee_name
ORDER BY e.employee_id;
```

## Thành viên 3: hóa đơn, thanh toán, thống kê

### 1. Hóa đơn chưa thanh toán

Nếu muốn xem **hóa đơn chưa có Payment, khách và nhân viên lập**:

```sql
SELECT r.receipt_id, c.customer_name, e.employee_name,
       r.total_amount
FROM receipts AS r
JOIN sessions AS s ON s.session_id = r.session_id
LEFT JOIN customer_accounts AS a ON a.account_id = s.account_id
LEFT JOIN customers AS c ON c.customer_id = a.customer_id
JOIN employees AS e ON e.employee_id = r.employee_id
WHERE NOT EXISTS (
  SELECT 1 FROM payments AS p WHERE p.receipt_id = r.receipt_id
)
ORDER BY r.receipt_id;
```

### 2. Doanh thu theo ngày thu tiền

Nếu muốn xem **tiền thực thu từng ngày thanh toán**, không lấy ngày lập hóa đơn:

```sql
SELECT DATE(p.time_paid) AS payment_day,
       SUM(p.amount) AS collected_amount
FROM payments AS p
GROUP BY DATE(p.time_paid)
ORDER BY payment_day;
```

### 3. Doanh thu theo phương thức thanh toán

Nếu muốn xem **CASH, BANK_TRANSFER và CARD**, kể cả phương thức chưa có giao dịch:

```sql
SELECT m.payment_method, COUNT(p.payment_id) AS payment_count,
       COALESCE(SUM(p.amount), 0) AS collected_amount
FROM (
  SELECT 'CASH' AS payment_method
  UNION ALL SELECT 'BANK_TRANSFER'
  UNION ALL SELECT 'CARD'
) AS m
LEFT JOIN payments AS p ON p.payment_method = m.payment_method
GROUP BY m.payment_method
ORDER BY m.payment_method;
```

### 4. Hóa đơn lớn nhất

Nếu muốn xem **hóa đơn tổng tiền cao nhất**, giữ tất cả hóa đơn đồng hạng:

```sql
SELECT r.receipt_id, r.session_id, c.customer_name, r.total_amount
FROM receipts AS r
JOIN sessions AS s ON s.session_id = r.session_id
LEFT JOIN customer_accounts AS a ON a.account_id = s.account_id
LEFT JOIN customers AS c ON c.customer_id = a.customer_id
WHERE r.total_amount = (SELECT MAX(total_amount) FROM receipts)
ORDER BY r.receipt_id;
```

### 5. Ba khách trả nhiều tiền nhất

Nếu muốn xem **tối đa ba khách có tổng tiền thực trả cao nhất**:

```sql
SELECT c.customer_id, c.customer_name,
       SUM(p.amount) AS paid_total
FROM customers AS c
JOIN customer_accounts AS a ON a.customer_id = c.customer_id
JOIN sessions AS s ON s.account_id = a.account_id
JOIN receipts AS r ON r.session_id = s.session_id
JOIN payments AS p ON p.receipt_id = r.receipt_id
GROUP BY c.customer_id, c.customer_name
ORDER BY paid_total DESC, c.customer_id
LIMIT 3;
```

### 6. Hóa đơn theo nhân viên lập

Nếu muốn xem **số hóa đơn, giá trị, tiền đã thu và chưa thu của từng nhân viên**, kể cả người chưa lập:

```sql
SELECT e.employee_id, e.employee_name,
       COUNT(r.receipt_id) AS receipt_count,
       COALESCE(SUM(r.total_amount), 0) AS receipt_total,
       COALESCE(SUM(CASE WHEN p.payment_id IS NOT NULL
                         THEN p.amount ELSE 0 END), 0) AS collected_amount,
       COALESCE(SUM(CASE WHEN r.receipt_id IS NOT NULL
                              AND p.payment_id IS NULL
                         THEN r.total_amount ELSE 0 END), 0) AS outstanding_amount
FROM employees AS e
LEFT JOIN receipts AS r ON r.employee_id = e.employee_id
LEFT JOIN payments AS p ON p.receipt_id = r.receipt_id
GROUP BY e.employee_id, e.employee_name
ORDER BY e.employee_id;
```

### 7. Tiền còn phải thu theo khách

Nếu muốn xem **khách có hóa đơn chưa trả và số tiền còn phải thu**:

```sql
SELECT c.customer_id, c.customer_name,
       SUM(r.total_amount) AS outstanding_amount
FROM customers AS c
JOIN customer_accounts AS a ON a.customer_id = c.customer_id
JOIN sessions AS s ON s.account_id = a.account_id
JOIN receipts AS r ON r.session_id = s.session_id
LEFT JOIN payments AS p ON p.receipt_id = r.receipt_id
WHERE p.payment_id IS NULL
GROUP BY c.customer_id, c.customer_name
ORDER BY outstanding_amount DESC, c.customer_id;
```

### 8. Doanh thu thực thu theo khu

Nếu muốn xem **tiền máy và tiền dịch vụ trên hóa đơn đã thanh toán theo khu**, kể cả khu 0đ:

```sql
SELECT cs.section_id, cs.section_name,
       COALESCE(SUM(r.computer_amount), 0) AS computer_revenue,
       COALESCE(SUM(r.service_amount), 0) AS service_revenue,
       COALESCE(SUM(p.amount), 0) AS collected_amount
FROM computer_sections AS cs
LEFT JOIN computers AS c ON c.section_id = cs.section_id
LEFT JOIN sessions AS s ON s.computer_id = c.computer_id
LEFT JOIN receipts AS r ON r.session_id = s.session_id
  AND EXISTS (SELECT 1 FROM payments AS px WHERE px.receipt_id = r.receipt_id)
LEFT JOIN payments AS p ON p.receipt_id = r.receipt_id
GROUP BY cs.section_id, cs.section_name
ORDER BY cs.section_id;
```

`computer_revenue + service_revenue = collected_amount` khi các thanh toán hợp lệ theo quy tắc `Payment.Amount = Receipt.TotalAmount`; câu 9 bên dưới dò sai lệch.

### 9. Dò thanh toán sai lệch

Nếu muốn xem **hóa đơn có nhiều Payment hoặc số tiền trả khác tổng hóa đơn**:

```sql
SELECT r.receipt_id, COUNT(p.payment_id) AS payment_count,
       r.total_amount,
       COALESCE(SUM(p.amount), 0) AS paid_amount,
       CASE WHEN COUNT(p.payment_id) > 1 THEN 'MULTIPLE_PAYMENTS'
            ELSE 'AMOUNT_MISMATCH' END AS problem
FROM receipts AS r
LEFT JOIN payments AS p ON p.receipt_id = r.receipt_id
GROUP BY r.receipt_id, r.total_amount
HAVING COUNT(p.payment_id) > 1
   OR SUM(CASE WHEN p.payment_id IS NOT NULL
                   AND p.amount <> r.total_amount THEN 1 ELSE 0 END) > 0
ORDER BY r.receipt_id;
```

Khóa `UNIQUE` của `payments.receipt_id` đang ngăn nhiều Payment trong schema mẫu. Vì vậy vế `MULTIPLE_PAYMENTS` chỉ phát hiện được nếu ràng buộc đó bị bỏ hoặc dữ liệu nhập từ nguồn khác.

### 10. Phiên hoàn tất chưa có hóa đơn

Nếu muốn xem **phiên COMPLETED còn chờ lập hóa đơn**:

```sql
SELECT s.session_id, s.computer_id, s.time_end
FROM sessions AS s
WHERE s.session_status = 'COMPLETED'
  AND NOT EXISTS (
    SELECT 1 FROM receipts AS r WHERE r.session_id = s.session_id
  )
ORDER BY s.session_id;
```
