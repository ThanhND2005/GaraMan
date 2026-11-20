package vn.garaman.web;

import vn.garaman.dao.DAO2;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.*;

@WebServlet("/login")
public final class LoginServlet2 extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/login2.jsp").forward(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try (Connection c = DAO2.open();
                PreparedStatement p = c.prepareStatement(
                        "SELECT m.id,m.passwordHash FROM tblMember2 m JOIN tblSalesStaff2 s ON s.id=m.id WHERE m.username=? AND m.active")) {
            p.setString(1, req.getParameter("username"));
            try (ResultSet r = p.executeQuery()) {
                if (!r.next() || !Password2.verify(req.getParameter("password"), r.getString(2)))
                    throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng");
                HttpSession old = req.getSession(false);
                if (old != null)
                    old.invalidate();
                HttpSession session = req.getSession(true);
                session.setAttribute("staffId", r.getLong(1));
                Web2.csrf(session);
                res.sendRedirect(req.getContextPath() + "/payment");
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, res);
        }
    }
}
