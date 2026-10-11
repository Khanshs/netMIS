# Hướng dẫn nghiệp vụ

## Trình tự sử dụng

1. Đăng ký khu, khách, nhân viên và món bằng addSection/addCustomer/addEmployee/addFood.
2. Thêm máy tham chiếu đúng khu đã đăng ký, trạng thái AVAILABLE hoặc MAINTENANCE.
3. Đăng ký CustomerAccount bằng addAccount: chủ tài khoản đã tồn tại, mã và username không trùng (username không phân biệt hoa/thường). openSession nhận tài khoản đã đăng ký, kiểm tra ACTIVE, máy trống, tài khoản chưa có phiên ACTIVE và lịch sử thời gian theo tài khoản/máy. Máy chuyển IN_USE.
4. order: chỉ phiên ACTIVE và nhân viên ACTIVE; các món lấy giá hiện tại hoặc giá lịch sử truyền vào. Cùng món trong một lần gọi gộp số lượng nếu cùng giá. Khi đã tạo đủ dòng món hợp lệ, gọi session.recordFoodService để khóa và liên kết đơn với phiên, rồi lưu cùng đối tượng vào danh sách toàn quán.
5. endSession: SessionService kiểm tra phiên thuộc quán, rồi gọi session.endSession. Phiên tự kiểm tra ACTIVE, thời gian kết thúc sau bắt đầu và không trước bất kỳ lần gọi món đã ghi nhận nào; hợp lệ thì chuyển COMPLETED và máy AVAILABLE. Gọi trực tiếp session.endSession cũng chạy các kiểm tra này. Thất bại không đổi giờ kết thúc hoặc trạng thái phiên/máy; complete() đã được bỏ.
6. issueReceipt: phiên COMPLETED, tối đa một hóa đơn. Tính tiền gốc, lấy hai tỷ lệ từ IDiscount của khách qua session.getAccount().getCustomer() và chốt tiền giảm cùng tiền phải trả vào hóa đơn.
7. pay: tổng tiền phải bằng hóa đơn; thời gian không trước kết thúc; tối đa một giao dịch thành công cho hóa đơn.

Khách muốn nghỉ lúc nào thì gọi endSession(session, giờ nghỉ thực tế) lúc đó, rồi issueReceipt và pay. timeEnd ban đầu là null, chỉ được ghi khi kết thúc phiên. Ví dụ bắt đầu 3h, nghỉ 4h30: tiền máy tính 90 phút; được thanh toán lúc 4h30 hoặc sau đó. isBefore trong Payment ngăn thanh toán trước giờ nghỉ đã ghi. Không cần lưu giờ kết thúc dự kiến hoặc gọi endSession trước khi khách nghỉ.

Thời gian có thể do người dùng nhập để mô phỏng, không cần chờ thời gian thực. SessionService xác nhận phiên thuộc quán rồi gọi Session.endSession(end). Session giữ danh sách các lần gọi món đã ghi nhận để tự kiểm tra giờ kết thúc, kể cả khi được gọi trực tiếp. Chỉ khi mọi điều kiện hợp lệ mới giải phóng máy và cập nhật phiên; thao tác sai giữ nguyên trạng thái. Khi một phiên đã giữ máy, không thể đổi trạng thái máy thủ công hoặc tạo phiên khác trên máy đó.

Các bước trên được tách vào package `netcafe.Service`: `CatalogService` đăng ký dữ liệu, `SessionService` quản lý phiên, `FoodOrderService` xử lý gọi món, `ReceiptService` lập hóa đơn và `PaymentService` ghi nhận thanh toán. `NetCafe` khởi tạo các service với cùng dữ liệu và cung cấp getter tương ứng. Không xóa các danh sách qua getter; không cung cấp nghiệp vụ xóa làm mất tham chiếu lịch sử. Các thao tác chạy tuần tự.

## Tính tiền

- Tiền máy = số giây × đơn giá khu / 3600, HALF_UP đến đồng.
- Dòng món = số lượng × PriceAtOrder; dịch vụ = tổng dòng món.
- Normal: giảm 0%; Member: giảm 10% tiền máy và 5% tiền món; VIP: giảm 15% tiền máy và 8% tiền món.
- Mỗi khoản giảm = tiền gốc × tỷ lệ, làm tròn HALF_UP đến đồng, tối đa bằng tiền gốc.
- Tổng hóa đơn = tiền máy gốc + dịch vụ gốc − hai khoản giảm. Receipt giữ cả tiền gốc và tiền sau giảm.
- Lớp con của Customer quyết định hạng khách. Hóa đơn lấy tỷ lệ qua IDiscount lúc lập và giữ riêng các giá trị đã chốt.
- Thực thu = tổng Payment.Amount. Hóa đơn chưa có Payment chưa góp doanh thu.
- Đơn giá khu và khu của máy cố định; Food.Price có thể đổi nhưng PriceAtOrder giữ nguyên.

## Dữ liệu mẫu

Ngày 05/10/2026: 2 khu, 6 máy, 4 khách, 4 tài khoản, 3 nhân viên, 3 món; 4 phiên, 3 dịch vụ, 4 dòng chi tiết, 2 hóa đơn và 1 thanh toán. Chỉ PC02 đang dùng, PC05 bảo trì. Tổng hóa đơn 109000 = 49000 đã thu + 60000 chưa thu. S04 chưa đưa vào hóa đơn hoặc doanh thu.

## OOP

- Đóng gói: các thuộc tính private, getter danh sách chỉ đọc, hóa đơn và giá lúc gọi cố định.
- Quan hệ đối tượng: Session tham chiếu CustomerAccount/Computer, CustomerAccount tham chiếu Customer; FoodService tham chiếu Session/Employee; FoodOrderDetail tham chiếu FoodService/Food; Receipt và Payment theo đúng ERD.
- Interface: Identifiable dùng kiểm tra mã trong danh sách; Chargeable dùng tính tiền ở Session, FoodOrderDetail, FoodService, Receipt; IDiscount được ReceiptService sử dụng để lấy hai tỷ lệ giảm giá.
- Kế thừa: NormalCustomer, MemberCustomer, VipCustomer kế thừa Customer trừu tượng; exception nghiệp vụ kế thừa RuntimeException.
- Enum: kiểm tra trạng thái máy/phiên/nhân viên/tài khoản và phương thức thanh toán.

CustomerAccount giữ thông tin đăng nhập, số dư không âm và trạng thái ACTIVE/INACTIVE. Hiện tại chưa xác thực mật khẩu, nạp tiền hay tự trừ số dư khi lập hóa đơn/thanh toán. passwordHash do bên gọi cung cấp; hash trong demo là placeholder. Kiểm tra phiên hoạt động áp dụng theo tài khoản, chưa giới hạn số tài khoản của một khách.

Customer là lớp cha trừu tượng triển khai Identifiable và IDiscount, chứa dữ liệu chung mã/tên. Ba lớp con NormalCustomer, MemberCustomer, VipCustomer nằm trong netcafe.model.customer, override tỷ lệ tiền máy và món. ReceiptService gọi qua IDiscount trên Customer nên tự lấy đúng tỷ lệ theo đối tượng thực tế. Receipt chỉ giữ giá trị đã chốt. Dữ liệu mẫu gốc dùng NormalCustomer; DiscountDemo khởi tạo đủ ba lớp con trong một mảng Customer[] để minh họa đa hình. Luồng hiện tại xác định hạng khi tạo đối tượng; nâng hạng cần nghiệp vụ riêng để quản lý các tham chiếu lịch sử.
