-- Dữ liệu thử nghiệm. Chạy sau demo.sql, có thể chạy lại trên database phát triển.
DO $$
DECLARE
    sample record;
    staff_id bigint;
    customer_id bigint;
    vehicle_id bigint;
    request_id bigint;
    invoice_id bigint;
    service_id bigint;
    part_id bigint;
    service_price numeric;
    part_price numeric;
BEGIN
    PERFORM pg_advisory_xact_lock(620021);

    SELECT m.id INTO STRICT staff_id
    FROM tblMember2 m
    WHERE m.username = 'sales' AND m.role='SALES_STAFF' AND m.active;

    FOR sample IN
        SELECT * FROM (VALUES
            ('HD-DEMO-NHAP', 'Khách mẫu Nháp', '0900000101', '51A-90001', 'DV001', 'PT001', 1, 'DRAFT', 'UNPAID'),
            ('HD-DEMO-CHO', 'Khách mẫu Chờ thu', '0900000102', '51A-90002', 'DV002', 'PT002', 1, 'CONFIRMED', 'UNPAID'),
            ('HD-DEMO-DA-THU', 'Khách mẫu Đã thu', '0900000103', '51A-90003', 'DV001', 'PT001', 2, 'CONFIRMED', 'PAID')
        ) AS samples(code, customer_name, phone, plate, service_code, part_code, part_quantity, invoice_status, payment_status)
    LOOP
        IF EXISTS (SELECT 1 FROM tblInvoice2 WHERE code = sample.code) THEN
            CONTINUE;
        END IF;

        SELECT id, unitPrice INTO STRICT service_id, service_price FROM tblService2 WHERE code = sample.service_code;
        SELECT id, unitPrice INTO STRICT part_id, part_price FROM tblSparePart2 WHERE code = sample.part_code;

        INSERT INTO tblCustomer2(fullName, phone) VALUES (sample.customer_name, sample.phone)
        RETURNING id INTO customer_id;
        INSERT INTO tblVehicle2(customerId, licensePlate) VALUES (customer_id, sample.plate)
        RETURNING id INTO vehicle_id;
        INSERT INTO tblServiceRequest2(vehicleId, receivedById, status) VALUES (vehicle_id, staff_id, 'COMPLETED')
        RETURNING id INTO request_id;

        INSERT INTO tblServiceRequestDetail2(requestId, serviceId, quantity, unitPrice, status)
        VALUES (request_id, service_id, 1, service_price, 'COMPLETED');
        INSERT INTO tblRequestPartDetail2(requestId, sparePartId, quantity, unitPrice)
        VALUES (request_id, part_id, sample.part_quantity, part_price);

        INSERT INTO tblInvoice2(code, serviceRequestId, createdById, invoiceStatus, paymentStatus,
                                paymentMethod, receivedAmount, paidAt, receivedById)
        VALUES (sample.code, request_id, staff_id, sample.invoice_status, sample.payment_status,
                CASE WHEN sample.payment_status = 'PAID' THEN 'CASH' END,
                CASE WHEN sample.payment_status = 'PAID' THEN service_price + sample.part_quantity * part_price END,
                CASE WHEN sample.payment_status = 'PAID' THEN LOCALTIMESTAMP END,
                CASE WHEN sample.payment_status = 'PAID' THEN staff_id END)
        RETURNING id INTO invoice_id;

        INSERT INTO tblInvoiceServiceDetail2(invoiceId, serviceId, quantity, unitPrice)
        VALUES (invoice_id, service_id, 1, service_price);
        INSERT INTO tblInvoicePartDetail2(invoiceId, sparePartId, quantity, unitPrice)
        VALUES (invoice_id, part_id, sample.part_quantity, part_price);
    END LOOP;
END;
$$;
