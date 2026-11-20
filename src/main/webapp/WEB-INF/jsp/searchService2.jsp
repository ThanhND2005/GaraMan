<%@ page import="java.util.*,vn.garaman.model.Service2" %>
<%@ include file="common.jspf" %>

            <h1>Tìm kiếm dịch vụ</h1>

            <form method="get" action="<%= request.getContextPath() %>/services" class="row">
                <label>
                    Tên dịch vụ<br>
                    <input name="keyword" value="<%= Web2.html(request.getAttribute("keyword")) %>" required>
                </label>
                <button>Tìm kiếm</button>
            </form>

            <%
                Service2[] items = (Service2[]) request.getAttribute("items");
            %>

            <% if (items != null) { %>
                <% if (items.length == 0) { %>
                    <p>Không tìm thấy dịch vụ.</p>
                <% } else { %>
                    <table>
                        <thead>
                            <tr>
                                <th>Mã</th>
                                <th>Tên dịch vụ</th>
                                <th>Thời gian</th>
                                <th>Đơn giá</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Service2 item : items) { %>
                                <tr>
                                    <td><%= Web2.html(item.code()) %></td>
                                    <td><%= Web2.html(item.name()) %></td>
                                    <td><%= item.estimatedDuration() %> phút</td>
                                    <td><%= Web2.formatNumber(item.unitPrice()) %> đ</td>
                                    <td>
                                        <a href="<%= request.getContextPath() %>/services?serviceId=<%= item.id() %>&keyword=<%=
                                            Web2.url((String) request.getAttribute("keyword")) %>">
                                            Xem chi tiết
                                        </a>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } %>
            <% } %>
        </main>
    </body>
</html>
