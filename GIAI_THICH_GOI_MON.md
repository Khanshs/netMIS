# Giải thích FoodService và FoodOrderDetail

## 1. Ví dụ thực tế

Khách đang sử dụng máy PC1 trong phiên S01 và gọi một lần gồm:

- 2 mì trộn.
- 2 cơm trộn.
- 2 Coca.

Hệ thống tạo **1 FoodService và 3 FoodOrderDetail**. Mỗi dòng chi tiết giữ số lượng là 2; không cần tạo 6 dòng riêng cho 6 phần món.

```text
Session S01 — khách đang sử dụng PC1
└── FoodService SV01 — một lần gọi món
    ├── FoodOrderDetail: Mì trộn × 2
    ├── FoodOrderDetail: Cơm trộn × 2
    └── FoodOrderDetail: Coca × 2
```

Đơn gọi món gắn với Session đang hoạt động trên PC1. Liên kết này giúp phân biệt khách và phiên sử dụng khi PC1 được dùng lại vào thời điểm khác.

## 2. Vai trò của từng lớp

| Lớp | Biểu diễn | Dữ liệu chính |
|---|---|---|
| Food | Một món trong danh mục | Mã món, tên món, giá hiện tại |
| FoodService | Một lần gọi món của một phiên | Mã lần gọi, phiên, nhân viên xử lý, thời gian gọi, danh sách chi tiết |
| FoodOrderDetail | Một loại món trong lần gọi | Lần gọi, món, số lượng, giá lúc gọi |

Food không chứa số lượng khách gọi vì cùng một món có thể được nhiều khách gọi với các số lượng khác nhau.

FoodOrderDetail giữ PriceAtOrder để bảo toàn giá lịch sử: khi Food.Price thay đổi, tiền của lần gọi đã ghi nhận vẫn giữ nguyên.

FoodService gom các dòng của cùng một lần gọi, giúp biết các món nào được gọi cùng thời điểm và do nhân viên nào xử lý. Tổng tiền được tính từ các dòng chi tiết.

## 3. Cách tính tiền

Giả sử giá tại thời điểm gọi là:

| Món | Số lượng | Giá lúc gọi | Thành tiền |
|---|---:|---:|---:|
| Mì trộn | 2 | 20.000đ | 40.000đ |
| Cơm trộn | 2 | 30.000đ | 60.000đ |
| Coca | 2 | 10.000đ | 20.000đ |
| **Tổng SV01** | | | **120.000đ** |

Các mức giá trong bảng là ví dụ minh họa.

```text
FoodOrderDetail.calculateAmount() = orderQuantity × priceAtOrder
FoodService.getTotal() = tổng thành tiền các FoodOrderDetail
```

120.000đ là tiền món gốc. Giảm giá theo hạng khách được áp dụng khi lập Receipt:

| Hạng khách | Giảm tiền món | Tiền món sau giảm |
|---|---:|---:|
| NormalCustomer | 0% | 120.000đ |
| MemberCustomer | 5% | 114.000đ |
| VipCustomer | 8% | 110.400đ |

Tiền giờ chơi được tính và giảm riêng; chưa nằm trong bảng tiền món trên.

## 4. Nếu khách gọi thêm món

Sau 15 phút, khách gọi thêm 1 Coca thì ghi nhận một FoodService mới:

```text
Session S01
├── FoodService SV01: 2 mì trộn + 2 cơm trộn + 2 Coca
└── FoodService SV02: 1 Coca
```

SV02 có thời gian gọi và nhân viên xử lý riêng. Tổng tiền món gốc của phiên bằng tổng SV01 cộng tổng SV02. Khi lập hóa đơn, mức giảm tiền món được tính trên tổng tiền món của phiên.

Trong cùng một FoodService, nếu một món xuất hiện nhiều lần với cùng giá, chương trình gộp số lượng vào một dòng. Nếu cùng món nhưng khác giá trong một lần gọi, chương trình báo InvalidOrderException. FoodService đã ghi nhận được khóa; món gọi thêm sau đó thuộc lần gọi mới.

## 5. Code minh họa theo project hiện tại

Đoạn code dưới giả định sessionPc1 là phiên ACTIVE, employee là nhân viên ACTIVE, và ba đối tượng món đã được đăng ký trong danh mục của cafe.

```java
FoodOrderService orders = cafe.getFoodOrderService();

FoodService firstOrder = orders.order(
    "SV01",
    sessionPc1,
    employee,
    time,
    new Food[]{miTron, comTron, coca},
    new int[]{2, 2, 2}
);

// Một lần gọi mới sau 15 phút.
FoodService secondOrder = orders.order(
    "SV02",
    sessionPc1,
    employee,
    time.plusMinutes(15),
    new Food[]{coca},
    new int[]{1}
);
```

Đây là ví dụ rút gọn, các biến cafe, sessionPc1, employee, time và các món cần được khởi tạo trước. FoodOrderService lấy giá hiện tại của mỗi món, tạo các dòng chi tiết và khóa đơn trước khi ghi nhận vào danh sách.

## 6. Có cần giữ cả hai lớp không?

**FoodOrderDetail cần thiết** để lưu số lượng và giá lúc gọi của từng món.

**FoodService phù hợp với đặc tả hiện tại** vì mỗi lần gọi món có mã, thời gian và nhân viên xử lý riêng. Nếu ứng dụng chỉ cần tổng món của cả phiên, có thể thiết kế một danh sách chi tiết trực tiếp trong Session; khi đó cần xem lại yêu cầu quản lý từng lần gọi.

Với project này, nên giữ cả hai lớp để bám theo ERD và truy vết các lần gọi. Tên FoodService trong đặc tả biểu diễn một lần gọi món; tên FoodOrder có thể dễ hiểu hơn nếu sau này thống nhất đổi tên trong toàn bộ thiết kế.

## 7. Phân biệt FoodService và FoodOrderService

| Lớp | Vai trò trong code |
|---|---|
| netcafe.Service.FoodService | Đối tượng dữ liệu của một lần gọi món |
| netcafe.Service.FoodOrderService | Điều phối thao tác gọi món: kiểm tra phiên, nhân viên, món, dữ liệu và ghi nhận đơn |

FoodOrderService tạo và quản lý các FoodService. Một Session có thể có nhiều FoodService, và mỗi FoodService chứa ít nhất một FoodOrderDetail khi được ghi nhận.
