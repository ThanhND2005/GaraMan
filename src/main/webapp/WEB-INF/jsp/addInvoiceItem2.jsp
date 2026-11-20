<%@ page import="java.util.*,vn.garaman.model.*" %>
<%@ include file="common.jspf" %>

            <%
                Invoice2 invoice = (Invoice2) request.getAttribute("invoice");
            %>

            <h1>Bổ sung khoản mục</h1>
            <% if (invoice != null) { %>
                <p>Hóa đơn <%= Web2.html(invoice.code()) %></p>

                <form method="get" action="<%= request.getContextPath() %>/payment" class="row">
                    <input type="hidden" name="invoiceId" value="<%= invoice.id() %>">
                    <input type="hidden" name="view" value="add">
                    <label>
                        Loại mục<br>
                        <select name="type">
                            <option value="SERVICE"
                                    <%= "SERVICE".equals(request.getAttribute("type")) ? "selected" : "" %>>Dịch vụ</option>
                            <option value="PART" <%= "PART".equals(request.getAttribute("type")) ? "selected" : "" %>>Phụ tùng</option>
                        </select>
                    </label>
                    <label>
                        Tên / mã mục<br>
                        <input name="itemKeyword"
                               value="<%= Web2.html(request.getAttribute("itemKeyword")) %>"
                               required>
                    </label>
                    <button>Tìm mục</button>
                </form>

                <%
                    List<Service2> services = (List<Service2>) request.getAttribute("services");
                    List<SparePart2> parts = (List<SparePart2>) request.getAttribute("parts");
                %>

                <% if (services != null || parts != null) { %>
                    <% if ((services != null && services.isEmpty()) || (parts != null && parts.isEmpty())) { %>
                        <p>Không tìm thấy mục phù hợp.</p>
                    <% } else { %>
                        <form method="post" action="<%= request.getContextPath() %>/payment">
                            <input type="hidden" name="csrf" value="<%= Web2.csrf(session) %>">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden"
                                   name="itemKeyword"
                                   value="<%= Web2.html(request.getAttribute("itemKeyword")) %>">
                            <input type="hidden" name="invoiceId" value="<%= invoice.id() %>">
                            <input type="hidden" name="type" value="<%= Web2.html(request.getAttribute("type")) %>">
                            <p>
                                <label>
                                    Mục cần bổ sung<br>
                                    <select name="itemId">
                                        <%
                                            if (services != null)
                                                for (Service2 item : services) {
                                        %>
                                            <option value="<%= item.id() %>"
                                                    <%= String.valueOf(item.id()).equals(request.getParameter("itemId")) ? "selected" : "" %>>
                                                <%= Web2.html(item.code() + " – " + item.name()
                                                        + " – " + Web2.formatNumber(item.unitPrice()) + " đ") %>
                                            </option>
                                        <% } %>

                                        <%
                                            if (parts != null)
                                                for (SparePart2 item : parts) {
                                        %>
                                            <option value="<%= item.id() %>"
                                                    <%= String.valueOf(item.id()).equals(request.getParameter("itemId")) ? "selected" : "" %>>
                                                <%= Web2.html(item.code() + " – " + item.name()
                                                        + " – " + Web2.formatNumber(item.unitPrice())
                                                        + " đ (tồn " + Web2.formatNumber(item.stockQuantity()) + ")") %>
                                            </option>
                                        <% } %>
                                    </select>
                                </label>
                            </p>
                            <p>
                                <label>
                                    Số lượng<br>
                                    <input name="quantity"
                                           type="number"
                                           value="<%= Web2.html(request.getParameter("quantity")) %>"
                                           min="<%= services != null ? "1" : "0.001" %>"
                                           step="<%= services != null ? "1" : "0.001" %>"
                                           required>
                                </label>
                            </p>
                            <button>Cập nhật hóa đơn</button>
                        </form>
                    <% } %>
                <% } %>
                <a href="<%= request.getContextPath() %>/payment?invoiceId=<%= invoice.id() %>">Hủy</a>
            <% } %>
        </main>
    </body>
</html>
