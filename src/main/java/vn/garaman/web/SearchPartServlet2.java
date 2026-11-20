package vn.garaman.web;

import vn.garaman.dao.*;
import vn.garaman.model.SparePart2;
import java.sql.Connection;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.IOException;

@WebServlet("/parts")
public final class SearchPartServlet2 extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        keyword = keyword == null ? "" : keyword.strip();
        req.setAttribute("keyword", keyword);
        try (Connection c = DAO2.open()) {
            SparePartDAO2 dao = new SparePartDAO2(c);
            String detailId = req.getParameter("partId");
            if (detailId == null) detailId = req.getParameter("id");
            if (detailId != null) {
                req.setAttribute("item", dao.findById(Web2.id(detailId)));
                req.getRequestDispatcher("/WEB-INF/jsp/partDetail2.jsp").forward(req, res);
            } else {
                if (req.getParameter("keyword") != null && keyword.isEmpty())
                    req.setAttribute("error", "Vui lòng nhập từ khóa tìm kiếm.");
                if (!keyword.isEmpty())
                    req.setAttribute("items", dao.findByKeyword(keyword).toArray(SparePart2[]::new));
                req.getRequestDispatcher("/WEB-INF/jsp/searchPart2.jsp").forward(req, res);
            }
        } catch (IllegalArgumentException e) {
            res.sendError(400, e.getMessage());
        } catch (java.sql.SQLException e) {
            throw new ServletException(e);
        }
    }
}
