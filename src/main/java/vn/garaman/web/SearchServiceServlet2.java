package vn.garaman.web;

import vn.garaman.dao.*;
import vn.garaman.model.Service2;
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
            String detailId = req.getParameter("serviceId");
            if (detailId == null) detailId = req.getParameter("id");
            if (detailId != null) {
                req.setAttribute("item", dao.findById(Web2.id(detailId)));
                req.getRequestDispatcher("/WEB-INF/jsp/serviceDetail2.jsp").forward(req, res);
            } else {
                if (req.getParameter("keyword") != null && keyword.isEmpty())
                    req.setAttribute("error", "Vui lòng nhập từ khóa tìm kiếm.");
                if (!keyword.isEmpty())
                    req.setAttribute("items", dao.findByKeyword(keyword).toArray(Service2[]::new));
                req.getRequestDispatcher("/WEB-INF/jsp/searchService2.jsp").forward(req, res);
            }
        } catch (IllegalArgumentException e) {
            res.sendError(400, e.getMessage());
        } catch (java.sql.SQLException e) {
            throw new ServletException(e);
        }
    }
}
