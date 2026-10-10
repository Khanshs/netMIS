# Mô tả 2 model chính: Session và FoodService

Tài liệu này mô tả hai model quan trọng trong hệ thống quản lý quán net: `Session` và `FoodService`. Cả hai đều nằm trong package `netcafe.model` và đều triển khai interface `Identifiable` và `Chargeable`.

## 1. Session

### Mục đích
`Session` đại diện cho một phiên sử dụng máy tính của khách hàng trong quán net. Mỗi phiên gắn với một `CustomerAccount`, một `Computer` và thời điểm bắt đầu sử dụng. Khách hàng được lấy qua `session.getAccount().getCustomer()`. Model này quản lý trạng thái phiên, thời gian kết thúc và tính toán số tiền phải trả theo thời gian sử dụng.

### Thuộc tính
- `sessionId`: mã định danh duy nhất của phiên.
- `account`: tài khoản khách hàng đang sử dụng máy.
- `computer`: máy tính được thuê trong phiên.
- `timeStart`: thời điểm bắt đầu phiên.
- `timeEnd`: thời điểm khách nghỉ thực tế (null nếu phiên chưa hoàn tất); dùng để tính tiền và kiểm tra thanh toán.
- `sessionStatus`: trạng thái phiên, ví dụ `ACTIVE` hoặc `COMPLETED`.

### Constructor
```java
public Session(String id, CustomerAccount account, Computer computer, LocalDateTime start)
```

- Kiểm tra `id` không rỗng bằng `Checks.id(id)`.
- Kiểm tra `account`, `computer`, `start` không null bằng `Checks.required(...)`.
- Mặc định trạng thái khởi tạo là `SessionStatus.ACTIVE`.

### Phương thức chính
- `getId()`, `getSessionId()`: trả về mã phiên.
- `getAccount()`: lấy tài khoản; `getAccount().getCustomer()` lấy chủ tài khoản.
- `getComputer()`: lấy máy tính.
- `getTimeStart()`, `getTimeEnd()`: lấy thời điểm bắt đầu/kết thúc.
- `getSessionStatus()`: lấy trạng thái phiên.

#### `complete(LocalDateTime end)`
- Chỉ cho phép kết thúc phiên nếu trạng thái hiện tại là `ACTIVE`.
- Kiểm tra thời điểm kết thúc phải khác null và phải sau thời điểm bắt đầu.
- Nếu hợp lệ thì đặt `timeEnd` và đổi trạng thái thành `COMPLETED`.
- Nếu không hợp lệ sẽ ném ra ngoại lệ tương ứng (`SessionStateException`, `InvalidTimeException`).

#### `amountAt(LocalDateTime at)`
- Tính số tiền phát sinh đến thời điểm `at`.
- Nếu thời điểm `at` trước thời gian bắt đầu hoặc null thì báo lỗi.
- Công thức dựa trên thời lượng sử dụng (`Duration.between`) và giá theo giờ của `computer.getSection().getSectionPrice()`.
- Hệ thống quy đổi theo đơn vị giờ, làm tròn bằng `RoundingMode.HALF_UP`.

#### `calculateAmount()`
- Tính tổng tiền của toàn bộ phiên.
- Chỉ chấp nhận khi phiên đã được kết thúc (`timeEnd != null`).
- Gọi `amountAt(timeEnd)` để lấy số tiền cuối cùng.

### Logic nghiệp vụ chính
- Một phiên chỉ có thể được hoàn tất một lần.
- Không thể tính tiền khi phiên đang hoạt động mà chưa có thời điểm kết thúc.
- Tổng tiền phụ thuộc vào thời gian thực sự sử dụng và giá theo giờ của khu vực máy.
- Khi khách muốn nghỉ, gọi SessionService.endSession(session, giờ nghỉ thực tế), sau đó lập hóa đơn và thanh toán. Ví dụ bắt đầu 3h, nghỉ 4h30: tính tiền cho 90 phút, được thanh toán lúc 4h30. Không đăng ký giờ kết thúc trước.

---

## 2. FoodService

### Mục đích
`FoodService` đại diện cho một đơn hàng đồ ăn hoặc dịch vụ thức ăn liên quan đến một `Session` cụ thể. Một `FoodService` thuộc về một `Session` và do một `Employee` phụ trách. Nó lưu trữ danh sách các món ăn đã đặt, kiểm soát tính hợp lệ của đơn hàng và tính tổng tiền thanh toán.

### Thuộc tính
- `serviceId`: mã định danh duy nhất của dịch vụ món ăn.
- `session`: phiên sử dụng máy tính mà đơn này gắn với.
- `employee`: nhân viên xử lý đơn hàng.
- `timeStartService`: thời điểm bắt đầu phục vụ đơn.
- `details`: danh sách các dòng đơn hàng (`FoodOrderDetail`).
- `sealed`: cờ báo hiệu đơn hàng đã được ghi nhận và không thể chỉnh sửa nữa.

### Constructor
```java
public FoodService(String id, Session session, Employee employee, LocalDateTime time)
```

- Kiểm tra `id` hợp lệ bằng `Checks.id(id)`.
- Kiểm tra `session`, `employee`, `time` không null.
- Khởi tạo `details` dưới dạng `ArrayList<>` và trạng thái ban đầu là chưa khóa (`sealed = false`).

### Phương thức chính
- `getId()`, `getServiceId()`: trả về mã dịch vụ.
- `getSession()`: lấy phiên tương ứng.
- `getEmployee()`: lấy nhân viên phụ trách.
- `getTimeStartService()`: lấy thời điểm bắt đầu phục vụ.
- `getDetails()`: trả về danh sách chi tiết món ăn dạng đọc-only (`Collections.unmodifiableList`).

#### `addItem(Food food, int quantity, BigDecimal priceAtOrder)`
- Không cho phép thêm món nếu đơn hàng đã bị khóa (`sealed == true`).
- Kiểm tra `food` và `priceAtOrder` hợp lệ.
- Kiểm tra `quantity > 0`.
- Nếu cùng món ăn đã tồn tại trong đơn thì cộng thêm số lượng nếu giá ở lần đó giống nhau.
- Nếu cùng món nhưng giá khác nhau thì ném `InvalidOrderException`.
- Nếu món mới thì thêm `new FoodOrderDetail(this, food, quantity, priceAtOrder)` vào `details`.

#### `getTotal()`
- Gọi `calculateAmount()` để lấy tổng tiền đơn hàng.

#### `seal()`
- Khóa đơn hàng để không thể chỉnh sửa.
- Nếu đơn hàng chưa có dòng nào thì ném ngoại lệ `InvalidOrderException`.

#### `calculateAmount()`
- Duyệt qua tất cả `FoodOrderDetail` trong `details`.
- Cộng tổng tiền từng dòng thành `total`.
- Trả về tổng tiền cuối cùng của đơn.

### Logic nghiệp vụ chính
- Một đơn hàng có thể chứa nhiều món, nhưng cùng một món trong cùng đơn không được có giá khác nhau.
- Đơn hàng phải có ít nhất một món trước khi được khóa.
- Sau khi khóa, không thể thay đổi món hoặc số lượng nữa.
- Tổng tiền được tính dựa trên từng dòng `FoodOrderDetail` và là dữ liệu chính để thanh toán.

---

## Mối quan hệ giữa hai model
- `Session` đại diện cho thời gian khách hàng sử dụng máy.
- `FoodService` đại diện cho dịch vụ đồ ăn trong thời gian đó.
- Một `Session` có thể có nhiều `FoodService` liên quan (nếu khách hàng đặt đồ ăn trong quá trình sử dụng máy).
- `FoodService` giữ tham chiếu tới `Session`, cho thấy đơn hàng này được tạo trong phiên nào.

## Kết luận
`Session` tập trung vào việc quản lý thời gian và phí sử dụng máy tính. `FoodService` tập trung vào việc quản lý danh sách món ăn, tính toán tổng tiền mua hàng và bảo vệ dữ liệu đơn hàng khỏi việc sửa đổi sau khi xác nhận. Cả hai model cùng đóng vai trò then chốt trong xử lý nghiệp vụ của hệ thống quán net.
