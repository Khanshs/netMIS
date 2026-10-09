# Exception trong NetCafeMIS

Tất cả exception do project định nghĩa nằm trong `netcafe.exception`, kế thừa trực tiếp `RuntimeException`. Đây là unchecked exception: bên gọi có thể bắt loại lỗi cụ thể để hiển thị thông báo hoặc xử lý lại thao tác. Thông báo chứa nguyên nhân và mã đối tượng khi có thể.

| Exception | Khi phát sinh | Cách xử lý |
|---|---|---|
| `ValidationException` | Mã/tên rỗng, tham số bắt buộc null, giá/tiền âm, chính sách giảm null hoặc tỷ lệ ngoài 0..1 | Kiểm tra và nhập lại dữ liệu |
| `DuplicateIdException` | Mã trùng trong cùng loại, kể cả có khoảng trắng quanh mã | Chọn mã mới |
| `EntityNotFoundException` | Tham chiếu tới đối tượng chưa được đăng ký vào danh sách | Đăng ký hoặc lấy đúng đối tượng từ danh sách |
| `ComputerUnavailableException` | Mở phiên trên máy đang dùng hoặc bảo trì; thêm máy IN_USE chưa có phiên | Chọn máy AVAILABLE |
| `ActiveSessionExistsException` | Khách đang có phiên ACTIVE nhưng mở thêm phiên trên máy khác | Kết thúc phiên hiện tại |
| `SessionStateException` | Kết thúc phiên đã hoàn tất, gọi món trên phiên đã kết thúc, lập hóa đơn/tiền chốt cho phiên ACTIVE | Kiểm tra trạng thái và thực hiện đúng trình tự |
| `InvalidTimeException` | Thiếu thời gian phiên; kết thúc không sau bắt đầu; phiên chồng lấn lịch sử; gọi món trước phiên; kết thúc trước lần gọi món; thanh toán trước kết thúc | Sửa thời gian; các phiên mới được nhập theo lịch sử không lùi |
| `InactiveEmployeeException` | Nhân viên INACTIVE xử lý gọi món hoặc lập hóa đơn | Chọn nhân viên ACTIVE |
| `InvalidOrderException` | Danh sách món rỗng, số dòng không khớp, số lượng không dương/tràn int, một món có hai giá trong cùng lần gọi, sửa đơn đã ghi nhận | Sửa dữ liệu món hoặc tạo lần gọi món mới |
| `ReceiptAlreadyExistsException` | Lập hóa đơn thứ hai cho cùng phiên | Dùng hóa đơn đã có |
| `PaymentAlreadyExistsException` | Thanh toán lại hóa đơn đã có Payment | Tra cứu giao dịch đã thu |
| `PaymentAmountMismatchException` | Số tiền thanh toán khác tổng hóa đơn, cả thiếu và thừa | Thu đúng toàn bộ tổng tiền một lần |

Khi nhiều điều kiện cùng sai, chương trình trả lỗi đầu tiên theo thứ tự kiểm tra trong thao tác. Ví dụ, mã Payment trùng được kiểm tra trước điều kiện hóa đơn đã trả. Trường hợp tiền âm/null là lỗi dữ liệu; tiền hợp lệ nhưng không bằng tổng hóa đơn là lỗi số tiền thanh toán.

Mọi thao tác nghiệp vụ đi qua các lớp trong `netcafe.Service`: `CatalogService`, `SessionService`, `FoodOrderService`, `ReceiptService`, `PaymentService`, được khởi tạo bởi `NetCafe` với cùng dữ liệu. Kiểm tra xong mới thêm đối tượng hoặc đổi trạng thái; thao tác thất bại không ghi thêm phiên, đơn món, hóa đơn hoặc giao dịch. Đơn gọi món đã ghi nhận được khóa, giá món lúc gọi được giữ riêng. Giá khu và khu của máy được cố định bằng thuộc tính final để giữ tiền máy lịch sử. Các getter danh sách trả danh sách chỉ đọc.

Chạy `ExceptionDemo.main()` để xem ví dụ bắt từng loại lỗi cụ thể. Chạy `NetCafeChecks.main()` để kiểm tra 12 loại lỗi, dữ liệu mẫu, trạng thái sau thất bại, giá lịch sử, gộp món và luồng thanh toán thành công. Không dùng `catch (Exception)` để bỏ qua lỗi lập trình.
