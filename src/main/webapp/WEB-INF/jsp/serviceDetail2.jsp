<%@ page import="vn.garaman.model.Service2" %>
<%@ include file="common.jspf" %>

            <%
                Service2 item = (Service2) request.getAttribute("item");
            %>
            <% if (item == null) { %>

                <h1>Dịch vụ không còn khả dụng</h1>
            <% } else { %>
                <h1><%= Web2.html(item.name()) %></h1>
                <p><strong>Mã:</strong> <%= Web2.html(item.code()) %></p>
                <p><strong>Loại:</strong> <%= Web2.html(item.category()) %></p>
                <p><strong>Mô tả:</strong> <%= Web2.html(item.description()) %></p>
                <p><strong>Đơn giá:</strong> <%= Web2.formatNumber(item.unitPrice()) %> đ</p>
                <p><strong>Thời gian dự kiến:</strong> <%= item.estimatedDuration() %> phút</p>
            <% } %>
            <a href="<%= request.getContextPath() %>/services?keyword=<%=
                Web2.url((String) request.getAttribute("keyword")) %>">
                Quay lại danh sách
            </a>
        </main>
    </body>
</html>
