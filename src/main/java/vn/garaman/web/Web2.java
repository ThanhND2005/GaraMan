package vn.garaman.web;

import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class Web2 {
    private Web2() {
    }

    public static String html(Object value) {
        if (value == null)
            return "";
        return value.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String url(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    public static long id(String value) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0)
                throw new NumberFormatException();
            return id;
        } catch (Exception e) {
            throw new IllegalArgumentException("Mã không hợp lệ");
        }
    }

    public static String csrf(HttpSession session) {
        String token = (String) session.getAttribute("csrf");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("csrf", token);
        }
        return token;
    }

    public static void checkCsrf(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null || !csrf(s).equals(req.getParameter("csrf")))
            throw new IllegalArgumentException("Phiên thao tác không hợp lệ");
    }

    public static long staff(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("staffId") == null)
            throw new IllegalArgumentException("Vui lòng đăng nhập");
        return (Long) s.getAttribute("staffId");
    }

    public static void error(HttpServletRequest req, HttpServletResponse res, Exception e, String page)
            throws IOException, javax.servlet.ServletException {
        req.setAttribute("error", e.getMessage());
        req.getRequestDispatcher(page).forward(req, res);
    }
}
