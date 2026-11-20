# GaraMan — Java 23 / PostgreSQL

Ứng dụng WAR hiện thực ba module trong báo cáo thiết kế `28B23DCCN772 (1).docx`: **tìm kiếm dịch vụ**, **tìm kiếm phụ tùng**, và **nhận thanh toán**. Cấu trúc JSP → Servlet → DAO → PostgreSQL theo chương III. File PDF lý thuyết được dùng để đối chiếu cách phân tích, thiết kế và kiểm tra; các quy tắc nghiệp vụ cụ thể lấy từ báo cáo thiết kế.

## Yêu cầu

- JDK 23
- PostgreSQL
- Maven 3.9+
- Apache Tomcat 9 (Servlet 4, JSP)

## Chạy

1. Chạy `docker compose up -d` để dựng PostgreSQL trên `localhost:5432`. Compose tạo database `garaman`, các bảng và dữ liệu mẫu trong lần khởi tạo volume đầu tiên. Mật khẩu database mặc định cho môi trường phát triển là `garaman_dev`; có thể đặt `GARAMAN_DB_PASSWORD` trước khi chạy Compose.
2. Đặt biến môi trường `GARAMAN_DB_PASSWORD` cho tiến trình Tomcat chạy trên máy (trong PowerShell: `$env:GARAMAN_DB_PASSWORD = 'garaman_dev'`). URL JDBC mặc định là `jdbc:postgresql://localhost:5432/garaman`, user là `postgres`. Nếu Tomcat chạy trong container khác cùng Compose network, đặt URL thành `jdbc:postgresql://postgres:5432/garaman`.
3. Trong thư mục dự án, chạy `mvn package` bằng JDK 23.
4. Chép `target/garaman-1.0.0.war` vào thư mục `webapps` của Tomcat 9. Mở `http://localhost:8080/garaman-1.0.0/`.

Với dữ liệu mẫu, tài khoản nhân viên bán hàng là `sales` / `Demo@12345`. Đổi mật khẩu trước khi dùng với dữ liệu thật. Tạo băm mới bằng `java -cp target/classes vn.garaman.web.Password2 <mật-khẩu>`, rồi cập nhật cột `passwordHash` của nhân viên trong `tblMember2`.

## Nghiệp vụ đã triển khai

- Tra cứu công khai theo tên, chỉ lấy mục `active`, xử lý `%`, `_`, dấu nháy như ký tự thường; chi tiết không hiển thị mục ngừng công bố.
- Nhân viên bán hàng đăng nhập, tìm khách theo tên hoặc biển số, chọn phiếu `COMPLETED` chưa thanh toán, mở/lập hóa đơn nháp từ các dòng phiếu.
- Bổ sung dịch vụ hoặc phụ tùng; cập nhật dòng phiếu và dòng hóa đơn, trừ tồn kho phụ tùng trong cùng giao dịch. Lưu đơn giá trên dòng chứng từ.
- Xác nhận hóa đơn, xem trước/in, rồi ghi nhận số tiền thực nhận. Hai trạng thái xác nhận và thanh toán tách biệt; các yêu cầu thanh toán lặp lại trả hóa đơn đã thanh toán.

Schema có thêm `receivedAmount` để lưu số tiền thực nhận, phục vụ đối chiếu sau thanh toán. Phương thức chuyển khoản cần được nhân viên xác minh ngoài ứng dụng trước khi bấm hoàn tất, như mô tả nghiệp vụ.

## Kiểm tra

`mvn package` biên dịch Java và tạo WAR. Để kiểm tra luồng với PostgreSQL, dùng dữ liệu mẫu: tìm “thay dầu”, tìm “lọc dầu”, đăng nhập, chọn Nguyễn Văn A → phiếu hoàn thành → hóa đơn. Tổng mẫu là **620.000 đồng**. Xác nhận hóa đơn, in và ghi nhận đủ tiền; trạng thái cuối là `CONFIRMED / PAID`.
