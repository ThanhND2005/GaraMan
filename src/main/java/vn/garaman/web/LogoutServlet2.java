package vn.garaman.web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public final class LogoutServlet2 extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Web2.checkCsrf(req);
        HttpSession s = req.getSession(false);
        if (s != null)
            s.invalidate();
        res.sendRedirect(req.getContextPath() + "/");
    }
}
