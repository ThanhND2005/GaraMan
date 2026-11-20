package vn.garaman.dao;
import vn.garaman.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public final class InvoiceDAO2 extends DAO2 {
    public InvoiceDAO2(Connection c) { super(c); }
    private interface Work<T> { T run() throws SQLException; }
    private <T> T transaction(Work<T> work) throws SQLException {
        boolean old=connection.getAutoCommit();
        if(!old) throw new SQLException("Connection đang ở trong giao dịch khác");
        connection.setAutoCommit(false);
        try { T value=work.run(); connection.commit(); return value; }
        catch(SQLException | RuntimeException e) { connection.rollback(); throw e; }
        finally { connection.setAutoCommit(true); }
    }
    public boolean isActiveSalesStaff(long staffId) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT id FROM tblMember2 WHERE id=? AND role='SALES_STAFF' AND active FOR SHARE")) {
            p.setLong(1, staffId);
            try (ResultSet rows = p.executeQuery()) {
                return rows.next();
            }
        }
    }

    private void requireSalesStaff(long staffId) throws SQLException {
        if (!isActiveSalesStaff(staffId)) throw new SQLException("Nhân viên không có quyền thanh toán");
    }
    private long lockedInvoice(long id) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("SELECT serviceRequestId FROM tblInvoice2 WHERE id=? FOR UPDATE")) {
            p.setLong(1,id); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Không tìm thấy hóa đơn"); return r.getLong(1); }
        }
    }
    private String status(long id) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("SELECT invoiceStatus FROM tblInvoice2 WHERE id=?")) { p.setLong(1,id); try(ResultSet r=p.executeQuery()) { r.next(); return r.getString(1); } }
    }
    public Invoice2 getOrCreateInvoice(Long requestId,Long staffId) throws SQLException {
        return transaction(() -> {
            requireSalesStaff(staffId);
            try(PreparedStatement p=connection.prepareStatement("SELECT status FROM tblServiceRequest2 WHERE id=? FOR UPDATE")) {
                p.setLong(1,requestId); try(ResultSet r=p.executeQuery()) { if(!r.next() || !"COMPLETED".equals(r.getString(1))) throw new SQLException("Phiếu chưa hoàn thành"); }
            }
            // The request lock serializes opening the same invoice at two counters.
            try (PreparedStatement p = connection.prepareStatement(
                    "SELECT id,paymentStatus FROM tblInvoice2 WHERE serviceRequestId=? FOR UPDATE")) {
                p.setLong(1, requestId);
                try (ResultSet rows = p.executeQuery()) {
                    if (rows.next()) {
                        if ("PAID".equals(rows.getString("paymentStatus")))
                            throw new SQLException("Phiếu đã thanh toán");
                        return findById(rows.getLong("id"));
                    }
                }
            }
            long id;
            try (PreparedStatement p = connection.prepareStatement(
                    "INSERT INTO tblInvoice2(serviceRequestId,createdById) VALUES (?,?) RETURNING id")) {
                p.setLong(1, requestId);
                p.setLong(2, staffId);
                try (ResultSet rows = p.executeQuery()) {
                    rows.next();
                    id = rows.getLong(1);
                }
            }
            try(PreparedStatement p=connection.prepareStatement("UPDATE tblInvoice2 SET code='HD'||LPAD(id::text,GREATEST(6,length(id::text)),'0') WHERE id=? AND code IS NULL")) { p.setLong(1,id); p.executeUpdate(); }
            if("DRAFT".equals(status(id))) {
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoiceServiceDetail2(invoiceId,serviceId,quantity,unitPrice) SELECT ?,serviceId,quantity,unitPrice FROM tblServiceRequestDetail2 WHERE requestId=? ON CONFLICT(invoiceId,serviceId) DO NOTHING")) { p.setLong(1,id); p.setLong(2,requestId); p.executeUpdate(); }
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoicePartDetail2(invoiceId,sparePartId,quantity,unitPrice) SELECT ?,sparePartId,quantity,unitPrice FROM tblRequestPartDetail2 WHERE requestId=? ON CONFLICT(invoiceId,sparePartId) DO NOTHING")) { p.setLong(1,id); p.setLong(2,requestId); p.executeUpdate(); }
            }
            return findById(id);
        });
    }
    public Invoice2 findById(Long id) throws SQLException {
        String sql="SELECT code,serviceRequestId,invoiceStatus,paymentStatus,paymentMethod,issuedAt,paidAt,createdById,receivedById,receivedAmount FROM tblInvoice2 WHERE id=?";
        try(PreparedStatement p=connection.prepareStatement(sql)) {
            p.setLong(1,id); try(ResultSet r=p.executeQuery()) {
                if(!r.next()) return null;
                ServiceRequest2 request=new ServiceRequestDAO2(connection).findById(r.getLong(2));
                Timestamp paid=r.getTimestamp(7);
                ServiceRequestDAO2 requests = new ServiceRequestDAO2(connection);
                Invoice2 invoice = new Invoice2(id, r.getString(1), request, requests.salesStaff(r.getLong("createdById")),
                        r.getTimestamp(6).toLocalDateTime(), r.getString(3), r.getString(4), r.getString(5),
                        paid == null ? null : paid.toLocalDateTime(),
                        r.getObject("receivedById") == null ? null : requests.salesStaff(r.getLong("receivedById")), r.getBigDecimal("receivedAmount"));
                loadDetails(invoice);
                return invoice;
            }
        }
    }
    private void loadDetails(Invoice2 invoice) throws SQLException {
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT i.*, d.id detailId,d.quantity,d.unitPrice appliedPrice FROM tblInvoiceServiceDetail2 d JOIN tblService2 i ON i.id=d.serviceId WHERE d.invoiceId=? ORDER BY i.code")) {
            p.setLong(1, invoice.id());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    invoice.addInvoiceServiceDetail(new InvoiceServiceDetail2(r.getLong("detailId"), invoice, ServiceDAO2.map(r),
                            r.getInt("quantity"), r.getBigDecimal("appliedPrice")));
                }
            }
        }
        try (PreparedStatement p = connection.prepareStatement(
                "SELECT i.*, d.id detailId,d.quantity,d.unitPrice appliedPrice FROM tblInvoicePartDetail2 d JOIN tblSparePart2 i ON i.id=d.sparePartId WHERE d.invoiceId=? ORDER BY i.code")) {
            p.setLong(1, invoice.id());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    invoice.addInvoicePartDetail(new InvoicePartDetail2(r.getLong("detailId"), invoice, SparePartDAO2.map(r),
                            r.getBigDecimal("quantity"), r.getBigDecimal("appliedPrice")));
                }
            }
        }
    }
    public Invoice2 addItemToInvoice(Long invoiceId,String type,Long itemId,BigDecimal quantity) throws SQLException {
        if(quantity==null || quantity.signum()<=0 || quantity.stripTrailingZeros().scale()>3) throw new SQLException("Số lượng không hợp lệ");
        if(!"SERVICE".equals(type) && !"PART".equals(type)) throw new SQLException("Loại mục không hợp lệ");
        if("SERVICE".equals(type) && (quantity.scale()>0 && quantity.stripTrailingZeros().scale()>0 || quantity.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE))>0)) throw new SQLException("Số lượng dịch vụ phải là số nguyên");
        return transaction(() -> {
            long requestId=lockedInvoice(invoiceId);
            if(!"DRAFT".equals(status(invoiceId))) throw new SQLException("Hóa đơn đã xác nhận");
            try(PreparedStatement p=connection.prepareStatement("SELECT 1 FROM tblServiceRequest2 WHERE id=? AND status='COMPLETED'")) { p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){if(!r.next()) throw new SQLException("Phiếu chưa hoàn thành");} }
            ServiceRequestDAO2 requests = new ServiceRequestDAO2(connection);
            ServiceRequest2 request = requests.findById(requestId);
            if("SERVICE".equals(type)) {
                Service2 item=new ServiceDAO2(connection).findById(itemId);
                if(item==null) throw new SQLException("Dịch vụ không khả dụng");
                request.supplement(item, quantity.intValueExact());
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoiceServiceDetail2(invoiceId,serviceId,quantity,unitPrice) VALUES (?,?,?,?) ON CONFLICT(invoiceId,serviceId) DO UPDATE SET quantity=tblInvoiceServiceDetail2.quantity+EXCLUDED.quantity")) { p.setLong(1,invoiceId);p.setLong(2,itemId);p.setInt(3,quantity.intValueExact());p.setBigDecimal(4,item.unitPrice());p.executeUpdate(); }
            } else {
                SparePart2 item=new SparePartDAO2(connection).findById(itemId);
                if(item==null) throw new SQLException("Phụ tùng không khả dụng");
                if(new SparePartDAO2(connection).decreaseStockIfAvailable(itemId,quantity)!=1) throw new SQLException("Không đủ tồn kho");
                request.supplement(item, quantity);
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoicePartDetail2(invoiceId,sparePartId,quantity,unitPrice) VALUES (?,?,?,?) ON CONFLICT(invoiceId,sparePartId) DO UPDATE SET quantity=tblInvoicePartDetail2.quantity+EXCLUDED.quantity")) { p.setLong(1,invoiceId);p.setLong(2,itemId);p.setBigDecimal(3,quantity);p.setBigDecimal(4,item.unitPrice());p.executeUpdate(); }
            }
            requests.saveDetails(request);
            return findById(invoiceId);
        });
    }
    public Invoice2 confirmInvoice(Long invoiceId) throws SQLException {
        return transaction(() -> {
            long requestId=lockedInvoice(invoiceId);
            if("CONFIRMED".equals(status(invoiceId))) return findById(invoiceId);
            try(PreparedStatement p=connection.prepareStatement("SELECT 1 FROM tblServiceRequest2 WHERE id=? AND status='COMPLETED'")) {p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Phiếu chưa hoàn thành");}}
            Invoice2 invoice=findById(invoiceId);
            if(invoice.lines().isEmpty()) throw new SQLException("Hóa đơn chưa có khoản mục");
            invoice.confirm();
            try(PreparedStatement p=connection.prepareStatement("UPDATE tblInvoice2 SET invoiceStatus='CONFIRMED' WHERE id=?")) {p.setLong(1,invoiceId);p.executeUpdate();}
            return findById(invoiceId);
        });
    }
    public Invoice2 receivePayment(Long invoiceId,String method,BigDecimal amount,Long staffId) throws SQLException {
        return transaction(() -> {
            requireSalesStaff(staffId);
            lockedInvoice(invoiceId);
            Invoice2 invoice=findById(invoiceId);
            if("PAID".equals(invoice.paymentStatus())) return invoice;
            if(!"CONFIRMED".equals(invoice.invoiceStatus())) throw new SQLException("Hóa đơn chưa xác nhận");
            if(method == null || !Set.of("CASH","TRANSFER","CARD").contains(method)) throw new SQLException("Phương thức không hợp lệ");
            if(amount==null || amount.compareTo(invoice.calculateTotal())<0) throw new SQLException("Số tiền thực nhận chưa đủ");
            if (amount.stripTrailingZeros().scale() > 2) throw new SQLException("Số tiền thực nhận chỉ được có tối đa hai chữ số thập phân");
            invoice.markPaid(method, java.time.LocalDateTime.now(), new ServiceRequestDAO2(connection).salesStaff(staffId));
            try(PreparedStatement p=connection.prepareStatement("UPDATE tblInvoice2 SET paymentStatus='PAID',paymentMethod=?,receivedAmount=?,paidAt=?,receivedById=? WHERE id=? AND paymentStatus='UNPAID'")) {
                p.setString(1,method);p.setBigDecimal(2,amount);p.setTimestamp(3,Timestamp.valueOf(invoice.paidAt()));p.setLong(4,staffId);p.setLong(5,invoiceId);p.executeUpdate();
            }
            return findById(invoiceId);
        });
    }
}
