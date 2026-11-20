<%@ page import="vn.garaman.model.*" %>
<%@ include file="common.jspf" %>

            <%
                Invoice2 invoice = (Invoice2) request.getAttribute("invoice");
            %>

            <h1>Hóa đơn <%= Web2.html(invoice.code()) %></h1>
            <p>Ngày xuất: <%= invoice.issuedAt() %></p>
            <p>Khách: <%= Web2.html(invoice.request().customerName()) %> · Xe: <%= Web2.html(invoice.request().licensePlate()) %></p>

            <table>
                <tr>
                    <th>Mã</th>
                    <th>Tên</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                    <th>Thành tiền</th>
                </tr>
                <% for (InvoiceLine2 line : invoice.lines()) { %>
                    <tr>
                        <td><%= Web2.html(line.code()) %></td>
                        <td><%= Web2.html(line.name()) %></td>
                        <td><%= Web2.formatNumber(line.quantity()) %></td>
                        <td><%= Web2.formatNumber(line.unitPrice()) %></td>
                        <td><%= Web2.formatNumber(line.lineTotal()) %></td>
                    </tr>
                <% } %>
            </table>
            <p class="amount">Tổng: <%= Web2.formatNumber(invoice.calculateTotal()) %> đ</p>
            <p>Thanh toán: <%= "PAID".equals(invoice.paymentStatus()) ? "Đã thanh toán (PAID)" : "Chưa thanh toán (UNPAID)" %></p>

            <div class="actions">
                <button onclick="window.print()">In hóa đơn</button>
                <a href="<%= request.getContextPath() %>/payment?invoiceId=<%= invoice.id() %>">Quay lại hóa đơn</a>
            </div>
        </main>
    </body>
</html>
