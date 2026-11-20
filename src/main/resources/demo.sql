-- Chạy một lần trên cơ sở dữ liệu mới sau schema.sql.
-- Tài khoản mẫu: sales / Demo@12345. Đổi mật khẩu trước khi dùng với dữ liệu thật.
INSERT INTO tblMember2(username,passwordHash,fullName,phone,role,position) VALUES
('sales','pbkdf2$210000$c0dONBs/T8u9ulCQOjVMqA==$BA/yJ8uZThccXDVueCwXyPQzj7k4B7B4lJMMEXV7Z8w=','Nhân viên bán hàng','0900000001','SALES_STAFF','Thu ngân');
INSERT INTO tblCustomer2(fullName,phone) VALUES ('Nguyễn Văn A','0900000002');
INSERT INTO tblVehicle2(customerId,licensePlate,brand,manufactureYear) SELECT id,'51A-12345','Toyota',2020 FROM tblCustomer2 WHERE phone='0900000002';
INSERT INTO tblService2(code,name,description,category,unitPrice,estimatedDuration) VALUES
('DV001','Thay dầu động cơ','Thay dầu động cơ định kỳ và kiểm tra mức dầu','Bảo dưỡng định kỳ',150000,30),
('DV002','Kiểm tra phanh','Kiểm tra hệ thống phanh','Kiểm tra',100000,45);
INSERT INTO tblSparePart2(code,name,description,category,unit,unitPrice,stockQuantity) VALUES
('PT001','Lọc dầu động cơ','Lọc cặn trong dầu bôi trơn động cơ','Lọc và bảo dưỡng','cái',50000,10),
('PT002','Bộ má phanh','Bộ má phanh thay thế','Phanh','bộ',320000,5);
INSERT INTO tblServiceRequest2(vehicleId,receivedById,status) SELECT v.id,s.id,'COMPLETED' FROM tblVehicle2 v JOIN tblMember2 s ON s.username='sales' AND s.role='SALES_STAFF' WHERE v.licensePlate='51A-12345';
INSERT INTO tblServiceRequestDetail2(requestId,serviceId,quantity,unitPrice,status)
SELECT r.id,s.id,1,s.unitPrice,'COMPLETED' FROM tblServiceRequest2 r CROSS JOIN tblService2 s WHERE s.code IN ('DV001','DV002');
INSERT INTO tblRequestPartDetail2(requestId,sparePartId,quantity,unitPrice)
SELECT r.id,p.id,1,p.unitPrice FROM tblServiceRequest2 r CROSS JOIN tblSparePart2 p WHERE p.code IN ('PT001','PT002');
