<%@ page import="vn.garaman.model.SparePart2" %>
<%@ include file="common.jspf" %>

            <%
                SparePart2 item = (SparePart2) request.getAttribute("item");
            %>
            <% if (item == null) { %>

                <h1>Phụ tùng không còn khả dụng</h1>
            <% } else { %>
                <h1><%= Web2.html(item.name()) %></h1>
                <p><strong>Mã:</strong> <%= Web2.html(item.code()) %></p>
                <p><strong>Loại:</strong> <%= Web2.html(item.category()) %></p>
                <p><strong>Mô tả:</strong> <%= Web2.html(item.description()) %></p>
                <p><strong>Đơn vị:</strong> <%= Web2.html(item.unit()) %></p>
                <p><strong>Đơn giá:</strong> <%= Web2.formatNumber(item.unitPrice()) %> đ</p>
                <p><strong>Tình trạng:</strong> <%= item.stockQuantity().signum() > 0 ? "Còn hàng" : "Hết hàng" %></p>
            <% } %>
            <a href="<%= request.getContextPath() %>/parts?keyword=<%=
                Web2.url((String) request.getAttribute("keyword")) %>">
                Quay lại danh sách
            </a>
        </main>
    </body>
</html>
