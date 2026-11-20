package vn.garaman.web;

import vn.garaman.dao.*;
import java.sql.Connection;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;

@WebServlet("/services")
public final class SearchServiceServlet2 extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        keyword = keyword == null ? "" : keyword.strip();
        req.setAttribute("keyword", keyword);
        try (Connection c = DAO2.open()) {
            ServiceDAO2 dao = new ServiceDAO2(c);
            if (req.getParameter("id") != null) {
                req.setAttribute("item", dao.findById(Web2.id(req.getParameter("id"))));
                req.getRequestDispatcher("/WEB-INF/jsp/serviceDetail2.jsp").forward(req, res);
            } else {
                if (!keyword.isEmpty())
                    req.setAttribute("items", dao.findByKeyword(keyword));
                req.getRequestDispatcher("/WEB-INF/jsp/searchService2.jsp").forward(req, res);
            }
        } catch (Exception e) {
            res.sendError(500, e.getMessage());
        }
    }
}
