<%@ page import="java.util.*,vn.garaman.model.SparePart2" %>
<%@ include file="common.jspf" %>

            <h1>Tìm kiếm phụ tùng</h1>

            <form method="get" action="<%= request.getContextPath() %>/parts" class="row">
                <label>
                    Tên phụ tùng<br>
                    <input name="keyword" value="<%= Web2.html(request.getAttribute("keyword")) %>" required>
                </label>
                <button>Tìm kiếm</button>
            </form>

            <%
                SparePart2[] items = (SparePart2[]) request.getAttribute("items");
            %>

            <% if (items != null) { %>
                <% if (items.length == 0) { %>
                    <p>Không tìm thấy phụ tùng.</p>
                <% } else { %>
                    <table>
                        <thead>
                            <tr>
                                <th>Mã</th>
                                <th>Tên phụ tùng</th>
                                <th>Đơn vị</th>
                                <th>Đơn giá</th>
                                <th>Tình trạng</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (SparePart2 item : items) { %>
                                <tr>
                                    <td><%= Web2.html(item.code()) %></td>
                                    <td><%= Web2.html(item.name()) %></td>
                                    <td><%= Web2.html(item.unit()) %></td>
                                    <td><%= Web2.formatNumber(item.unitPrice()) %> đ</td>
                                    <td><%= item.stockQuantity().signum() > 0 ? "Còn hàng" : "Hết hàng" %></td>
                                    <td>
                                        <a href="<%= request.getContextPath() %>/parts?partId=<%= item.id() %>&keyword=<%=
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
