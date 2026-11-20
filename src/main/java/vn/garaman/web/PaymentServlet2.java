package vn.garaman.web;

import vn.garaman.dao.*;
import vn.garaman.model.Invoice2;
import java.math.BigDecimal;
import java.sql.Connection;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;

@WebServlet("/payment")
public final class PaymentServlet2 extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Web2.staff(req);
        } catch (Exception e) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try (Connection c = DAO2.open()) {
            String invoiceId = req.getParameter("invoiceId");
            if (invoiceId != null) {
                Invoice2 invoice = new InvoiceDAO2(c).findById(Web2.id(invoiceId));
                if (invoice == null) {
                    res.sendError(404);
                    return;
                }
                req.setAttribute("invoice", invoice);
                String view = req.getParameter("view");
                if ("print".equals(view)) {
                    if (!"CONFIRMED".equals(invoice.invoiceStatus()))
                        throw new IllegalArgumentException("Cần xác nhận hóa đơn trước khi in");
                    req.getRequestDispatcher("/WEB-INF/jsp/invoicePrint2.jsp").forward(req, res);
                } else if ("add".equals(view)) {
                    if (!"DRAFT".equals(invoice.invoiceStatus()))
                        throw new IllegalArgumentException("Hóa đơn đã xác nhận");
                    String itemKeyword = req.getParameter("itemKeyword");
                    String type = req.getParameter("type");
                    req.setAttribute("itemKeyword", itemKeyword == null ? "" : itemKeyword.strip());
                    req.setAttribute("type", "PART".equals(type) ? "PART" : "SERVICE");
                    if (itemKeyword != null && !itemKeyword.isBlank()) {
                        if ("PART".equals(type))
                            req.setAttribute("parts", new SparePartDAO2(c).findByKeyword(itemKeyword));
                        else
                            req.setAttribute("services", new ServiceDAO2(c).findByKeyword(itemKeyword));
                    }
                    req.getRequestDispatcher("/WEB-INF/jsp/addInvoiceItem2.jsp").forward(req, res);
                } else
                    req.getRequestDispatcher("/WEB-INF/jsp/invoiceDetail2.jsp").forward(req, res);
            } else {
                String keyword = req.getParameter("keyword");
                req.setAttribute("keyword", keyword == null ? "" : keyword.strip());
                if (keyword != null && !keyword.isBlank())
                    req.setAttribute("customers", new CustomerDAO2(c).findByKeyword(keyword));
                if (req.getParameter("customerId") != null)
                    req.setAttribute("requests", new ServiceRequestDAO2(c)
                            .findCompletedUnpaidByCustomer(Web2.id(req.getParameter("customerId"))));
                req.getRequestDispatcher("/WEB-INF/jsp/payment2.jsp").forward(req, res);
            }
        } catch (IllegalArgumentException e) {
            res.sendError(400, e.getMessage());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            long staffId = Web2.staff(req);
            Web2.checkCsrf(req);
            try (Connection c = DAO2.open()) {
                InvoiceDAO2 dao = new InvoiceDAO2(c);
                String action = req.getParameter("action");
                Invoice2 invoice;
                switch (action == null ? "" : action) {
                    case "open" -> invoice = dao.getOrCreateInvoice(Web2.id(req.getParameter("requestId")), staffId);
                    case "add" ->
                        invoice = dao.addItemToInvoice(Web2.id(req.getParameter("invoiceId")), req.getParameter("type"),
                                Web2.id(req.getParameter("itemId")), new BigDecimal(req.getParameter("quantity")));
                    case "confirm" -> invoice = dao.confirmInvoice(Web2.id(req.getParameter("invoiceId")));
                    case "pay" -> invoice = dao.receivePayment(Web2.id(req.getParameter("invoiceId")),
                            req.getParameter("method"), new BigDecimal(req.getParameter("amount")), staffId);
                    default -> throw new IllegalArgumentException("Thao tác không hợp lệ");
                }
                res.sendRedirect(req.getContextPath() + "/payment?invoiceId=" + invoice.id()
                        + ("confirm".equals(action) ? "&view=print" : ""));
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            String invoiceId = req.getParameter("invoiceId");
            if (invoiceId != null) {
                req.setAttribute("invoiceId", invoiceId);
                String action = req.getParameter("action");
                if ("add".equals(action))
                    req.setAttribute("errorView", "add");
                try (Connection c = DAO2.open()) {
                    req.setAttribute("invoice", new InvoiceDAO2(c).findById(Web2.id(invoiceId)));
                } catch (Exception ignored) {
                }
                req.getRequestDispatcher(
                        "add".equals(action) ? "/WEB-INF/jsp/addInvoiceItem2.jsp" : "/WEB-INF/jsp/invoiceDetail2.jsp")
                        .forward(req, res);
            } else
                req.getRequestDispatcher("/WEB-INF/jsp/payment2.jsp").forward(req, res);
        }
    }
}
