package viewTrax.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import viewTrax.GeneralHelper;
import viewTrax.SingletonWrapper;
import viewTrax.data.Title;
import viewTrax.data.TitleName;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class AddTitleServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(AddTitleServlet.class
			.getName());

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		// Set return URL
		resp.sendRedirect("/");

		// Check Access rights
		if (user == null || !userService.isUserAdmin()) {
			// TODO Deny access for now
			return;
		}

		// We have full access rights
		// Let's do it!
		String name = req.getParameter(Title.NAMES);
		String description = req.getParameter("description");

		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		// First check if the title already exists
		try {
			TitleName titleName = TitleName.getFirst(pm, name);
			getOrCreateTitle(pm, name, description, titleName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			pm.close();
		}
	}

	private void getOrCreateTitle(PersistenceManager pm, String title, String description, TitleName titleName) throws Exception {
		Title titleObj;
		if (titleName == null) {
			titleObj = Title.createTitle(title, description);
			log.info("Added title: " + title);
		} else {
			log.info("Title found: " + title);
			titleObj = Title.get(pm, titleName);
			titleObj.setDescription(description);

			if (titleName.getTitle() == null) {
				if (titleObj.getNames().contains(titleName)) {
					titleName.setTitle(titleObj);
				}
			}
		}

		GeneralHelper.transactionalPersist(titleObj);
	}
}