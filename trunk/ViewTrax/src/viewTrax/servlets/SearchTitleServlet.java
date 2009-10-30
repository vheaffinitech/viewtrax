package viewTrax.servlets;

import viewTrax.QueryHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class SearchTitleServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/xml");

		String name = req.getParameter("name");
		List<String> matches = QueryHelper.searchTitles(name);
		PrintWriter writer = resp.getWriter();
		
		for (String title : matches) {
			writer.println(title);
		}
		
		return;
	}

}
