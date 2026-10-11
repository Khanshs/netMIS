# NetCafeMIS — Quản lý quán net

Project Maven Java 8 chạy console trên IntelliJ IDEA, dựa theo cách tổ chức SmartDorm và các thực thể trong đặc tả. Mỗi class, interface và enum nằm trong một file riêng. Dữ liệu dùng List<T> trong bộ nhớ.

## Clone và mở bằng IntelliJ IDEA

1. Clone repo private bằng tài khoản GitHub có quyền truy cập:

   ```powershell
   git clone https://github.com/Khanshs/netMIS.git
   ```

2. Trong IntelliJ IDEA, chọn **File → Open**, chọn thư mục `netMIS` vừa clone (hoặc file `pom.xml` trong thư mục đó), rồi mở như một Maven project.
3. Chọn Project SDK là JDK 8 hoặc mới hơn và dùng encoding UTF-8. Chờ IntelliJ import Maven xong.
4. Chạy `src/main/java/netcafe/Main.java` để xem dữ liệu mẫu.
5. Chạy `src/main/java/netcafe/Demo/ExceptionDemo.java` để xem xử lý exception.
6. Chạy `src/test/java/netcafe/NetCafeChecks.java` để kiểm tra nghiệp vụ. Đây là chương trình main độc lập, Maven Surefire không tự chạy nó.
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
└── src/
    ├── main/java/netcafe/
    │   ├── Main.java
    │   ├── Demo/       # ExceptionDemo, DiscountDemo, EarlyEndDemo
    │   ├── Identifiable.java
    │   ├── Chargeable.java
    │   ├── IDiscount.java
    │   ├── model/      # Customer, CustomerAccount, FoodService, hóa đơn và Checks dùng chung
    │   │   ├── NET/    # Computer, ComputerSection, Employee, Food, Session
    │   │   └── customer/ # NormalCustomer, MemberCustomer, VipCustomer
    │   ├── enums/      # 5 enum, gồm AccountStatus
    │   ├── Service/    # Các lớp điều phối nghiệp vụ
    │   └── exception/  # 14 loại lỗi nghiệp vụ và EXCEPTIONS.md
    └── test/java/netcafe/  # NetCafeChecks, DiscountChecks, AccountChecks, EarlyEndChecks, SessionEndChecks
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

`NetCafeData` và `ServiceChecks` là lớp nội bộ trong package. Các service dùng chung danh sách của một `NetCafe`; `Main.sample()` minh họa gọi trực tiếp từng service qua `getCatalogService()`, `getSessionService()` và các getter tương ứng. `model.FoodService` vẫn là thực thể một lần gọi món trong ERD; nghiệp vụ xử lý thực thể này nằm ở `Service.FoodOrderService`.

## Chạy bằng Maven

```text
mvn package
java -jar target/netcafe-mis-1.0.0.jar
java -cp target/classes:target/test-classes netcafe.NetCafeChecks
java -cp target/classes:target/test-classes netcafe.DiscountChecks
java -cp target/classes:target/test-classes netcafe.AccountChecks
java -cp target/classes:target/test-classes netcafe.EarlyEndChecks
java -cp target/classes:target/test-classes netcafe.SessionEndChecks
java -cp target/classes netcafe.Demo.EarlyEndDemo
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

Script tìm javac/java từ JAVA_HOME, PATH hoặc JDK đi kèm IntelliJ. Có thể truyền thư mục JDK bằng `-JavaHome 'C:\path\to\jdk'`. Không cần thư viện ngoài.

## Kết quả mẫu

R01 = 24000 tiền máy + 25000 dịch vụ = **49000 VND**, đã trả. R02 = 30000 + 30000 = **60000 VND**, chưa trả. S03 hoàn tất chưa có hóa đơn; S04 đang hoạt động trên PC02. Tổng hóa đơn **109000 VND**, thực thu **49000 VND**. Xem `EXPECTED_OUTPUT.txt`.

`src/main/java/netcafe/exception/EXCEPTIONS.md` mô tả các lỗi, nơi phát sinh và cách xử lý. `HUONG_DAN_NGHIEP_VU.md` giải thích luồng và OOP. Mục 4 chỉ yêu cầu đặc tả 30 truy vấn, chưa triển khai code truy vấn. Chạy lại sẽ khởi tạo lại dữ liệu; project chưa lưu file hay database.

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

Đã biên dịch toàn bộ mã nguồn bằng JDK của IntelliJ với `javac --release 8`. Chạy trực tiếp NetCafeChecks (43), DiscountChecks (42), AccountChecks (54), EarlyEndChecks (78) và SessionEndChecks (135), tổng cộng 352 kiểm tra đều qua. Kết quả Main khớp `EXPECTED_OUTPUT.txt`; `DiscountDemo`, `ExceptionDemo` và `EarlyEndDemo` chạy thành công. Các kiểm tra bao gồm kết thúc trực tiếp trên model, gọi món thất bại, bảo vệ trạng thái máy và tính đủ món ghi trực tiếp vào phiên.

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

Giờ bắt đầu và kết thúc có thể nhập để mô phỏng; không cần chờ thời gian trôi qua. `SessionService.endSession` kiểm tra phiên thuộc quán rồi gọi `Session.endSession`. Model tự kiểm tra phiên ACTIVE, giờ kết thúc sau giờ bắt đầu và không trước bất kỳ lần gọi món nào. Gọi trực tiếp trên model cũng thực hiện các kiểm tra này và cập nhật máy. Máy giữ phiên đang sử dụng nó; không thể đổi trạng thái thủ công khi đang có phiên hoặc gán `IN_USE` bằng setter.

`Session.recordFoodService()` kiểm tra, khóa và liên kết đơn món với phiên. Khi lập hóa đơn, ReceiptService lấy các đơn đã ghi nhận từ `session.getFoodServices()` để tính tiền đầy đủ. Trong ứng dụng, dùng `cafe.order()` để có thêm kiểm tra món/nhân viên đã đăng ký và cập nhật danh sách gọi món toàn quán.

```java
LocalDateTime start = LocalDateTime.of(2026, 10, 5, 3, 0);
Session session = cafe.openSession("S01", account, computer, start);
// Mô phỏng khách nghỉ lúc 4h30, không cần chờ 90 phút.
LocalDateTime actualEnd = start.plusMinutes(90);
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
