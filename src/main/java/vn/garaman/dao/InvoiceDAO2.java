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
    private void requireSalesStaff(long staffId) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("SELECT 1 FROM tblSalesStaff2 s JOIN tblMember2 m ON m.id=s.id WHERE s.id=? AND m.active")) {
            p.setLong(1,staffId); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Nhân viên không có quyền thanh toán"); }
        }
    }
    private long lockedInvoice(long id) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("SELECT serviceRequestId FROM tblInvoice2 WHERE id=? FOR UPDATE")) {
            p.setLong(1,id); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new SQLException("Không tìm thấy hóa đơn"); return r.getLong(1); }
        }
    }
    private String status(long id) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("SELECT invoiceStatus FROM tblInvoice2 WHERE id=?")) { p.setLong(1,id); try(ResultSet r=p.executeQuery()) { r.next(); return r.getString(1); } }
    }
    public Invoice2 getOrCreateInvoice(long requestId,long staffId) throws SQLException {
        return transaction(() -> {
            requireSalesStaff(staffId);
            try(PreparedStatement p=connection.prepareStatement("SELECT status FROM tblServiceRequest2 WHERE id=? FOR UPDATE")) {
                p.setLong(1,requestId); try(ResultSet r=p.executeQuery()) { if(!r.next() || !"COMPLETED".equals(r.getString(1))) throw new SQLException("Phiếu chưa hoàn thành"); }
            }
            long id;
            try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoice2(serviceRequestId,createdById) VALUES (?,?) ON CONFLICT(serviceRequestId) DO UPDATE SET serviceRequestId=EXCLUDED.serviceRequestId RETURNING id")) {
                p.setLong(1,requestId); p.setLong(2,staffId); try(ResultSet r=p.executeQuery()) { r.next(); id=r.getLong(1); }
            }
            try(PreparedStatement p=connection.prepareStatement("UPDATE tblInvoice2 SET code='HD'||LPAD(id::text,6,'0') WHERE id=? AND code IS NULL")) { p.setLong(1,id); p.executeUpdate(); }
            if("DRAFT".equals(status(id))) {
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoiceServiceDetail2(invoiceId,serviceId,quantity,unitPrice) SELECT ?,serviceId,quantity,unitPrice FROM tblServiceRequestDetail2 WHERE requestId=? ON CONFLICT(invoiceId,serviceId) DO NOTHING")) { p.setLong(1,id); p.setLong(2,requestId); p.executeUpdate(); }
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoicePartDetail2(invoiceId,sparePartId,quantity,unitPrice) SELECT ?,sparePartId,quantity,unitPrice FROM tblRequestPartDetail2 WHERE requestId=? ON CONFLICT(invoiceId,sparePartId) DO NOTHING")) { p.setLong(1,id); p.setLong(2,requestId); p.executeUpdate(); }
            }
            return findById(id);
        });
    }
    public Invoice2 findById(long id) throws SQLException {
        String sql="SELECT code,serviceRequestId,invoiceStatus,paymentStatus,paymentMethod,issuedAt,paidAt FROM tblInvoice2 WHERE id=?";
        try(PreparedStatement p=connection.prepareStatement(sql)) {
            p.setLong(1,id); try(ResultSet r=p.executeQuery()) {
                if(!r.next()) return null;
                ServiceRequest2 request=new ServiceRequestDAO2(connection).findById(r.getLong(2));
                Timestamp paid=r.getTimestamp(7);
                return new Invoice2(id,r.getString(1),request,r.getString(3),r.getString(4),r.getString(5),r.getTimestamp(6).toLocalDateTime(),paid==null?null:paid.toLocalDateTime(),lines(id));
            }
        }
    }
    private List<InvoiceLine2> lines(long id) throws SQLException {
        String sql="SELECT 'SERVICE',s.id,s.code,s.name,d.quantity,d.unitPrice FROM tblInvoiceServiceDetail2 d JOIN tblService2 s ON s.id=d.serviceId WHERE d.invoiceId=? UNION ALL SELECT 'PART',p.id,p.code,p.name,d.quantity,d.unitPrice FROM tblInvoicePartDetail2 d JOIN tblSparePart2 p ON p.id=d.sparePartId WHERE d.invoiceId=? ORDER BY 1,3";
        try(PreparedStatement p=connection.prepareStatement(sql)) { p.setLong(1,id);p.setLong(2,id);try(ResultSet r=p.executeQuery()) { List<InvoiceLine2> out=new ArrayList<>();while(r.next()) out.add(new InvoiceLine2(r.getString(1),r.getLong(2),r.getString(3),r.getString(4),r.getBigDecimal(5),r.getBigDecimal(6)));return out; } }
    }
    public Invoice2 addItemToInvoice(long invoiceId,String type,long itemId,BigDecimal quantity) throws SQLException {
        if(quantity==null || quantity.signum()<=0 || quantity.scale()>3) throw new SQLException("Số lượng không hợp lệ");
        if(!"SERVICE".equals(type) && !"PART".equals(type)) throw new SQLException("Loại mục không hợp lệ");
        if("SERVICE".equals(type) && (quantity.scale()>0 && quantity.stripTrailingZeros().scale()>0 || quantity.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE))>0)) throw new SQLException("Số lượng dịch vụ phải là số nguyên");
        return transaction(() -> {
            long requestId=lockedInvoice(invoiceId);
            if(!"DRAFT".equals(status(invoiceId))) throw new SQLException("Hóa đơn đã xác nhận");
            try(PreparedStatement p=connection.prepareStatement("SELECT 1 FROM tblServiceRequest2 WHERE id=? AND status='COMPLETED'")) { p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){if(!r.next()) throw new SQLException("Phiếu chưa hoàn thành");} }
            if("SERVICE".equals(type)) {
                Service2 item=new ServiceDAO2(connection).findById(itemId);
                if(item==null) throw new SQLException("Dịch vụ không khả dụng");
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblServiceRequestDetail2(requestId,serviceId,quantity,unitPrice,status) VALUES (?,?,?,?,'COMPLETED') ON CONFLICT(requestId,serviceId) DO UPDATE SET quantity=tblServiceRequestDetail2.quantity+EXCLUDED.quantity")) { p.setLong(1,requestId);p.setLong(2,itemId);p.setInt(3,quantity.intValueExact());p.setBigDecimal(4,item.unitPrice());p.executeUpdate(); }
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoiceServiceDetail2(invoiceId,serviceId,quantity,unitPrice) VALUES (?,?,?,?) ON CONFLICT(invoiceId,serviceId) DO UPDATE SET quantity=tblInvoiceServiceDetail2.quantity+EXCLUDED.quantity")) { p.setLong(1,invoiceId);p.setLong(2,itemId);p.setInt(3,quantity.intValueExact());p.setBigDecimal(4,item.unitPrice());p.executeUpdate(); }
            } else {
                SparePart2 item=new SparePartDAO2(connection).findById(itemId);
                if(item==null) throw new SQLException("Phụ tùng không khả dụng");
                if(new SparePartDAO2(connection).decreaseStockIfAvailable(itemId,quantity)!=1) throw new SQLException("Không đủ tồn kho");
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblRequestPartDetail2(requestId,sparePartId,quantity,unitPrice) VALUES (?,?,?,?) ON CONFLICT(requestId,sparePartId) DO UPDATE SET quantity=tblRequestPartDetail2.quantity+EXCLUDED.quantity")) { p.setLong(1,requestId);p.setLong(2,itemId);p.setBigDecimal(3,quantity);p.setBigDecimal(4,item.unitPrice());p.executeUpdate(); }
                try(PreparedStatement p=connection.prepareStatement("INSERT INTO tblInvoicePartDetail2(invoiceId,sparePartId,quantity,unitPrice) VALUES (?,?,?,?) ON CONFLICT(invoiceId,sparePartId) DO UPDATE SET quantity=tblInvoicePartDetail2.quantity+EXCLUDED.quantity")) { p.setLong(1,invoiceId);p.setLong(2,itemId);p.setBigDecimal(3,quantity);p.setBigDecimal(4,item.unitPrice());p.executeUpdate(); }
            }
            return findById(invoiceId);
        });
    }
    public Invoice2 confirmInvoice(long invoiceId) throws SQLException {
        return transaction(() -> {
            long requestId=lockedInvoice(invoiceId);
            if("CONFIRMED".equals(status(invoiceId))) return findById(invoiceId);
            try(PreparedStatement p=connection.prepareStatement("SELECT 1 FROM tblServiceRequest2 WHERE id=? AND status='COMPLETED'")) {p.setLong(1,requestId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Phiếu chưa hoàn thành");}}
            Invoice2 invoice=findById(invoiceId);
            if(invoice.lines().isEmpty()) throw new SQLException("Hóa đơn chưa có khoản mục");
            try(PreparedStatement p=connection.prepareStatement("UPDATE tblInvoice2 SET invoiceStatus='CONFIRMED' WHERE id=?")) {p.setLong(1,invoiceId);p.executeUpdate();}
            return findById(invoiceId);
        });
    }
    public Invoice2 receivePayment(long invoiceId,String method,BigDecimal amount,long staffId) throws SQLException {
        return transaction(() -> {
            requireSalesStaff(staffId);
            lockedInvoice(invoiceId);
            Invoice2 invoice=findById(invoiceId);
            if("PAID".equals(invoice.paymentStatus())) return invoice;
            if(!"CONFIRMED".equals(invoice.invoiceStatus())) throw new SQLException("Hóa đơn chưa xác nhận");
            if(!Set.of("CASH","TRANSFER","CARD").contains(method)) throw new SQLException("Phương thức không hợp lệ");
            if(amount==null || amount.compareTo(invoice.calculateTotal())<0) throw new SQLException("Số tiền thực nhận chưa đủ");
            try(PreparedStatement p=connection.prepareStatement("UPDATE tblInvoice2 SET paymentStatus='PAID',paymentMethod=?,receivedAmount=?,paidAt=CURRENT_TIMESTAMP,receivedById=? WHERE id=? AND paymentStatus='UNPAID'")) {
                p.setString(1,method);p.setBigDecimal(2,amount);p.setLong(3,staffId);p.setLong(4,invoiceId);p.executeUpdate();
            }
            return findById(invoiceId);
        });
    }
}
