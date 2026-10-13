# NetCafeMIS — Quản lý quán net

Project Maven Java 8 chạy console trên IntelliJ IDEA, dựa theo cách tổ chức SmartDorm và các thực thể trong đặc tả. Mỗi class, interface và enum nằm trong một file riêng. `Main` dùng List<T> trong bộ nhớ; `JdbcDemo` là ví dụ MySQL riêng cho khu máy, máy tính và Billing.

## Clone và mở bằng IntelliJ IDEA

1. Clone repo private bằng tài khoản GitHub có quyền truy cập:

   ```powershell
   git clone https://github.com/Khanshs/netMIS.git
   ```

2. Trong IntelliJ IDEA, chọn **File → Open**, chọn thư mục `netMIS` vừa clone (hoặc file `pom.xml` trong thư mục đó), rồi mở như một Maven project.
3. Chọn Project SDK là JDK 8 hoặc mới hơn và dùng encoding UTF-8. Chờ IntelliJ import Maven xong.
4. Chạy `src/main/java/netcafe/Main.java` để xem dữ liệu mẫu.
5. Chạy `src/test/java/netcafe/Demo/ExceptionDemo.java` để xem xử lý exception.
6. Chạy `src/test/java/netcafe/Checks/NetCafeChecks.java` để kiểm tra nghiệp vụ. Đây là chương trình main độc lập, Maven Surefire không tự chạy nó.
7. Chạy `DiscountDemo.main()` để so sánh Normal/Member/VIP; chạy `DiscountChecks.main()` để kiểm tra chính sách giảm giá.
8. Chạy `AccountChecks.main()` để kiểm tra tài khoản, đăng ký và mở phiên theo tài khoản.
9. Chạy `EarlyEndDemo.main()` để xem khách nghỉ lúc 4h30 sau khi bắt đầu lúc 3h; chạy `EarlyEndChecks.main()` để kiểm tra kết thúc và thanh toán theo giờ thực tế.
10. Chạy `SessionEndChecks.main()` để kiểm tra kết thúc qua service và trực tiếp trên phiên, liên kết đơn món và trạng thái sau thao tác thất bại.

## Cấu trúc

```text
NetCafeMIS/
├── pom.xml
├── README.md
├── HUONG_DAN_NGHIEP_VU.md
├── EXPECTED_OUTPUT.txt
├── Dac_ta_quan_ly_quan_net.md
├── run.ps1
├── examples/billing_demo.sql
├── examples/customer_accounts.sql
├── examples/full_mis_schema.sql
├── TRUY_VAN_30_SQL.md
└── src/
    ├── main/java/netcafe/
    │   ├── Main.java
    │   ├── JdbcDemo.java
    │   ├── Identifiable.java
    │   ├── Chargeable.java
    │   ├── IDiscount.java
    │   ├── dao/        # DAO JDBC cho khu, máy, khách, tài khoản và Billing
    │   ├── database/   # Mở kết nối MySQL từ biến môi trường
    │   ├── model/      # Session, CustomerAccount, FoodOrderDetail, Receipt, Payment, Checks
    │   │   ├── NetSpecs/ # Computer, ComputerSection, Employee, Food
    │   │   └── customer/ # Customer, NormalCustomer, MemberCustomer, VipCustomer
    │   ├── enums/      # 5 enum, gồm AccountStatus
    │   ├── Service/    # Các service, thực thể FoodService và NetCafeData nội bộ
    │   └── exception/  # 14 loại lỗi nghiệp vụ và EXCEPTIONS.md
    └── test/java/netcafe/
        ├── Checks/    # NetCafeChecks, DiscountChecks, AccountChecks, EarlyEndChecks, SessionEndChecks
        └── Demo/      # DiscountDemo, ExceptionDemo, EarlyEndDemo
```

Package `netcafe.Service` gom các service theo chức năng:

| Lớp | Chức năng |
|---|---|
| `CatalogService` | Đăng ký khu, máy, khách, tài khoản, nhân viên và món |
| `SessionService` | Mở và kết thúc phiên, cập nhật trạng thái máy |
| `FoodOrderService` | Ghi nhận và kiểm tra các lần gọi món |
| `ReceiptService` | Chốt tiền và lập hóa đơn |
| `PaymentService` | Thanh toán và tính doanh thu thực thu |
| `NetCafe` | Khởi tạo các service với cùng dữ liệu, cung cấp getter service và danh sách |

`NetCafeData` và `ServiceChecks` là lớp nội bộ trong package `netcafe.Service`. Các service dùng chung danh sách của một `NetCafe`; `Main.sample()` minh họa gọi trực tiếp từng service qua `getCatalogService()`, `getSessionService()` và các getter tương ứng. `Service.FoodService` vẫn là thực thể một lần gọi món trong ERD; nghiệp vụ xử lý thực thể này nằm ở `Service.FoodOrderService`.

`model.Checks` và các hàm kiểm tra là public để các package con sử dụng. `FoodOrderDetail` có constructor và `addQuantity()` public để `Service.FoodService` gọi được; khi đơn đã khóa, không thể đổi số lượng kể cả gọi trực tiếp trên chi tiết món. `NetCafeData` được giữ trong `Service` để danh sách dùng chung không phải công khai ra ngoài package.

## Chạy bằng Maven

```text
mvn package
java -jar target/netcafe-mis-1.0.0.jar
java -cp target/classes:target/test-classes netcafe.Checks.NetCafeChecks
java -cp target/classes:target/test-classes netcafe.Checks.DiscountChecks
java -cp target/classes:target/test-classes netcafe.Checks.AccountChecks
java -cp target/classes:target/test-classes netcafe.Checks.EarlyEndChecks
java -cp target/classes:target/test-classes netcafe.Checks.SessionEndChecks
java -cp target/classes:target/test-classes netcafe.Demo.EarlyEndDemo
```

Trên Windows, dùng dấu `;` thay cho `:` trong classpath. Với JDK 9 trở lên, có thể dùng `mvn -Dmaven.compiler.release=8 package` để kiểm tra tương thích API Java 8. Maven Surefire không tự chạy năm chương trình kiểm tra độc lập này.

## Chạy trực tiếp bằng PowerShell

```powershell
.\run.ps1
.\run.ps1 -Checks
.\run.ps1 -ExceptionDemo
.\run.ps1 -DiscountDemo
.\run.ps1 -DiscountChecks
.\run.ps1 -AccountChecks
.\run.ps1 -EarlyEndDemo
.\run.ps1 -EarlyEndChecks
.\run.ps1 -SessionEndChecks
```

Script tìm javac/java từ JAVA_HOME, PATH hoặc JDK đi kèm IntelliJ. Có thể truyền thư mục JDK bằng `-JavaHome 'C:\path\to\jdk'`. Các chương trình dùng dữ liệu trong bộ nhớ không cần MySQL.

## Thử DAO và kết nối MySQL

Đây là ví dụ JDBC dựa trên `student-crud-jdbc`, **chưa thay thế các Service dùng List**. File `examples/billing_demo.sql` tạo database demo, bảng khu/máy, bảng điều kiện rút gọn cho phiên/nhân viên, hóa đơn, thanh toán và dữ liệu mẫu. Chạy file này trong MySQL trước khi chạy Java; có thể chạy lại mà không xóa bảng hoặc dữ liệu đã có.

Trong IntelliJ, import lại Maven để tải MySQL Connector/J, tạo Run Configuration cho `netcafe.JdbcDemo` và đặt ba biến môi trường:

```text
NETCAFE_DB_URL=jdbc:mysql://localhost:3306/netcafe_billing_demo
NETCAFE_DB_USER=<tài khoản MySQL của bạn>
NETCAFE_DB_PASSWORD=<mật khẩu MySQL của bạn>
```

Không đưa giá trị thật vào code hoặc Git. Chạy `JdbcDemo` không có argument để đọc khu, máy, hóa đơn chưa trả và doanh thu đã thu từ MySQL. Thêm program argument `--add-computer` để lưu thử khu `CS03` và máy `PC07` trong cùng một transaction. Dùng `--write-billing` để lấy dữ liệu từ `Main.sample()`, lưu hóa đơn `R03` cho phiên `S03` và thanh toán `P02` cho hóa đơn `R02` trong một transaction. Hai thao tác mẫu đều kiểm tra bản ghi đã có trước khi thêm, nên có thể chạy lại.

Sau khi chạy SQL và trước các thao tác ghi, kết quả JDBC cần có `Sections=2`, `R01 total=49000 paid=true`, `Unpaid: R02 total=60000` và `Collected=49000`. Sau `--write-billing`, `R02` được trả, `R03` chưa trả và `Collected=109000`. Nếu kết nối bị từ chối, kiểm tra MySQL đã chạy ở cổng 3306 và ba biến môi trường của Run Configuration.

`ComputerSectionDAO` và `ComputerDAO` minh họa CRUD/JOIN. `ReceiptDAO` và `PaymentDAO` minh họa lưu hóa đơn/thanh toán và đọc kết quả Billing. Các DAO nhận cùng `Connection` từ lớp điều phối khi một thao tác cần transaction. Các bảng `sessions` và `employees` trong ví dụ được rút gọn; cần mở rộng schema và DAO cho phiên, nhân viên, món và gọi món trước khi chuyển toàn bộ luồng `Main` sang SQL.

Để thử `CustomerDAO` và `CustomerAccountDAO`, chạy thêm `examples/customer_accounts.sql` trong Workbench trên cùng server. Sau đó chạy `JdbcDemo` với program argument `--customers`. Kết quả cho thấy hạng khách, tỷ lệ giảm tiền máy và tài khoản liên kết; chương trình không in `password_hash`. Hai DAO hỗ trợ thêm, tìm theo ID, liệt kê, cập nhật và xóa. `CustomerAccountDAO` còn tìm theo username. Các dòng hash trong SQL chỉ là dữ liệu giả để minh họa, không dùng cho đăng nhập.

Để chạy đủ 30 câu SQL trong [`TRUY_VAN_30_SQL.md`](TRUY_VAN_30_SQL.md), tiếp tục chạy `examples/full_mis_schema.sql` sau hai file trên. File này bổ sung tài khoản và thời điểm bắt đầu cho phiên, trạng thái nhân viên, cùng các bảng món, lần gọi món, chi tiết món. Nó chỉ mở rộng database demo, có thể chạy lại và không chuyển các Service của `Main` sang JDBC. Các câu SQL là tài liệu thực hành MySQL; yêu cầu 10 câu mỗi thành viên bằng Java Lambda và C# LINQ vẫn cần triển khai riêng.

## Kết quả mẫu

R01 = 24000 tiền máy + 25000 dịch vụ = **49000 VND**, đã trả. R02 = 30000 + 30000 = **60000 VND**, chưa trả. S03 hoàn tất chưa có hóa đơn; S04 đang hoạt động trên PC02. Tổng hóa đơn **109000 VND**, thực thu **49000 VND**. Xem `EXPECTED_OUTPUT.txt`.

`src/main/java/netcafe/exception/EXCEPTIONS.md` mô tả các lỗi, nơi phát sinh và cách xử lý. `HUONG_DAN_NGHIEP_VU.md` giải thích luồng và OOP. Mục 4 của đặc tả đã có 30 ví dụ SQL trong tài liệu riêng, nhưng chưa có bản Java Lambda/C# LINQ tương ứng. Chạy lại `Main` sẽ khởi tạo lại dữ liệu trong bộ nhớ; `JdbcDemo` đọc và có thể ghi vào database demo riêng.

## Giảm giá theo hạng khách

| Hạng | Giảm tiền giờ chơi | Giảm tiền món |
|---|---:|---:|
| Normal | 0% | 0% |
| Member | 10% | 5% |
| VIP | 15% | 8% |

`Customer` là lớp trừu tượng chứa mã và tên, triển khai `Identifiable` và `IDiscount`. Ba lớp `NormalCustomer`, `MemberCustomer`, `VipCustomer` trong `netcafe.model.customer` kế thừa `Customer`, override các phương thức lấy tên hạng và tỷ lệ giảm. `IDiscount` có `getComputerDiscountRate()` và `getFoodDiscountRate()` trả `BigDecimal`; 0.10 là 10%. `ReceiptService` lấy tỷ lệ qua interface từ đối tượng khách, gọi đa hình mà không cần kiểm tra kiểu lớp hay viết if/switch cho từng hạng.

```text
Customer (abstract, implements Identifiable, IDiscount)
├── NormalCustomer
├── MemberCustomer
└── VipCustomer
```

```java
Customer normal = new NormalCustomer("C04", "Chi");
Customer member = new MemberCustomer("C05", "An");
Customer vip = new VipCustomer("C06", "Binh");
```

Hạng khách được xác định bởi lớp con khi khởi tạo đối tượng. Hóa đơn chốt tên hạng, tỷ lệ và số tiền giảm lúc lập, giữ các giá trị này riêng và không tính lại theo đối tượng khách. Mỗi khoản giảm được làm tròn HALF_UP đến đồng và không vượt tiền gốc. Tiền dịch vụ gốc vẫn lấy từ các FoodService; giá gọi món không đổi.

Luồng hiện tại tạo khách ở một hạng cố định. Đổi đối tượng MemberCustomer thành VipCustomer cần một nghiệp vụ nâng hạng riêng để cập nhật tham chiếu hiện hành và giữ đúng lịch sử; không có setter đổi lớp đối tượng Java.

`Receipt.getGrossComputerAmount()` / `getGrossServiceAmount()` trả tiền gốc; `getComputerAmount()` / `getServiceAmount()` trả tiền sau giảm. `getTotalAmount()` là số phải thanh toán. Với 100000 tiền máy + 50000 tiền món, `DiscountDemo` cho Normal **150000**, Member **137500**, VIP **131000 VND**. Thanh toán và thực thu dùng số tiền sau giảm.

## Kiểm chứng

Bản sửa sau khi chuyển package đã được biên dịch riêng mã nguồn chính và mã kiểm tra/demo bằng JDK của IntelliJ với `javac --release 8`. Chạy trực tiếp 43 kiểm tra nghiệp vụ, 42 kiểm tra giảm giá/kế thừa, 54 kiểm tra tài khoản, 40 kiểm tra kết thúc/thanh toán theo giờ thực tế và 132 kiểm tra kết thúc phiên qua cả hai API, tổng cộng 311 kiểm tra đều qua. Main cho kết quả khớp `EXPECTED_OUTPUT.txt`; `DiscountDemo`, `ExceptionDemo` và `EarlyEndDemo` chạy thành công. Có kiểm tra ngăn thay đổi số lượng trực tiếp qua FoodOrderDetail khi đơn đã khóa.

## Tài khoản khách hàng

`CustomerAccount` giữ `accountId`, `username`, `passwordHash`, `balance`, `status` và `customer`. `Customer` tiếp tục giữ mã/tên và chính sách giảm giá theo Normal/Member/VIP. Quan hệ là **Session → CustomerAccount → Customer → IDiscount**.

```java
Customer customer = new MemberCustomer("C01", "An");
cafe.addCustomer(customer);
// passwordHash là chuỗi đã được băm do bên gọi cung cấp.
CustomerAccount account = new CustomerAccount("A01", "an", passwordHash,
        BigDecimal.ZERO, AccountStatus.ACTIVE, customer);
cafe.addAccount(account);
Session session = cafe.openSession("S01", account, computer, start);
Customer owner = session.getAccount().getCustomer();
```

`addAccount` yêu cầu chủ tài khoản đã đăng ký, mã không trùng và username không trùng kể cả khác chữ hoa/thường; khoảng trắng quanh mã/username được loại bỏ. Mã, username và chủ tài khoản là thuộc tính cố định. Balance không âm; status là `ACTIVE` hoặc `INACTIVE`.

`openSession` nhận tài khoản đã đăng ký, chỉ cho phép `ACTIVE`, kiểm tra phiên đang hoạt động và lịch sử thời gian theo tài khoản/máy. Mỗi tài khoản có tối đa một phiên ACTIVE; chưa giới hạn số tài khoản của cùng khách. `ReceiptService` lấy ưu đãi qua `session.getAccount().getCustomer()` và vẫn chốt giá trị vào hóa đơn.

Phạm vi hiện tại là lưu thông tin tài khoản và kiểm tra khi mở phiên. Chưa có đăng nhập/xác thực, nạp tiền, tự trừ số dư hay yêu cầu số dư dương khi mở phiên. Chuỗi hash trong demo chỉ là placeholder được ghi rõ, không dùng để đăng nhập.


## Kết thúc phiên khi khách muốn nghỉ

Khi khách muốn nghỉ, gọi endSession với giờ nghỉ thực tế, rồi lập hóa đơn và thanh toán. timeEnd ban đầu là null; khi kết thúc, nó ghi giờ nghỉ thực tế và máy được giải phóng. Tiền máy được tính theo thời lượng thực tế; không đăng ký giờ kết thúc trước.

`Session` giữ danh sách các lần gọi món đã khóa; `getFoodServices()` chỉ cho đọc. `cafe.endSession(session, end)` kiểm tra phiên thuộc quán rồi gọi `session.endSession(end)`. Cả hai cách đều kiểm tra phiên ACTIVE, end sau start và không có đơn món sau end trước khi cập nhật phiên và máy. Ví dụ S04 bắt đầu 11:00, gọi món 11:15: kết thúc 11:01 bị từ chối và giữ nguyên trạng thái; kết thúc 12:00 thành công, máy AVAILABLE. Phương thức `complete()` đã được bỏ.

```java
Session session = cafe.openSession("S01", account, computer, start);
// Trong ứng dụng, thời điểm khách yêu cầu kết thúc:
LocalDateTime actualEnd = LocalDateTime.now();
cafe.endSession(session, actualEnd);
Receipt receipt = cafe.issueReceipt("R01", session, employee);
cafe.pay("P01", receipt, actualEnd, PaymentMethod.CASH, receipt.getTotalAmount());
```

Payment giữ điều kiện isBefore(timeEnd): từ chối thanh toán trước giờ nghỉ thực tế, chấp nhận đúng giờ nghỉ hoặc sau đó. Phiên cần kết thúc trước khi lập hóa đơn và thanh toán. Các ràng buộc thời gian gọi món vẫn áp dụng.

Chạy EarlyEndDemo: khách bắt đầu 3h, tự kết thúc và thanh toán lúc 4h30. Giá 20.000đ/giờ, khách Normal không gọi món:

```text
Start=03:00 ActualEnd=04:30
Minutes=90
Total=30000
Collected=30000
```
