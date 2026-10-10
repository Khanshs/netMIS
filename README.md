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
5. Chạy `src/main/java/netcafe/ExceptionDemo.java` để xem xử lý exception.
6. Chạy `src/test/java/netcafe/NetCafeChecks.java` để kiểm tra nghiệp vụ. Đây là chương trình main độc lập, Maven Surefire không tự chạy nó.
7. Chạy `DiscountDemo.main()` để so sánh Normal/Member/VIP; chạy `DiscountChecks.main()` để kiểm tra chính sách giảm giá.
8. Chạy `AccountChecks.main()` để kiểm tra tài khoản, đăng ký và mở phiên theo tài khoản.

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
    │   ├── ExceptionDemo.java
    │   ├── DiscountDemo.java
    │   ├── Identifiable.java
    │   ├── Chargeable.java
    │   ├── IDiscount.java
    │   ├── model/      # 11 lớp thực thể, gồm CustomerAccount, Customer trừu tượng, Checks nội bộ
    │   │   └── customer/ # NormalCustomer, MemberCustomer, VipCustomer
    │   ├── enums/      # 5 enum, gồm AccountStatus
    │   ├── Service/    # Các lớp điều phối nghiệp vụ
    │   └── exception/  # 14 loại lỗi nghiệp vụ và EXCEPTIONS.md
    └── test/java/netcafe/  # NetCafeChecks, DiscountChecks, AccountChecks
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
```

Trên Windows, dùng dấu `;` thay cho `:` trong classpath. Với JDK 9 trở lên, có thể dùng `mvn -Dmaven.compiler.release=8 package` để kiểm tra tương thích API Java 8. Maven Surefire không tự chạy ba chương trình kiểm tra độc lập này.

## Chạy trực tiếp bằng PowerShell

```powershell
.\run.ps1
.\run.ps1 -Checks
.\run.ps1 -ExceptionDemo
.\run.ps1 -DiscountDemo
.\run.ps1 -DiscountChecks
.\run.ps1 -AccountChecks
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

Đã chạy Maven 3.9.9 `clean package` với JDK 21 và `-Dmaven.compiler.release=8`: build thành công. Chạy trực tiếp 43 kiểm tra nghiệp vụ, 42 kiểm tra giảm giá/kế thừa và 54 kiểm tra tài khoản, tất cả đều qua. JAR cho kết quả khớp `EXPECTED_OUTPUT.txt`; `DiscountDemo` và `ExceptionDemo` chạy thành công.

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
