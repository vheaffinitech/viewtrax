package viewTrax.servlets;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import viewTrax.GeneralHelper;
import viewTrax.SingletonWrapper;
import viewTrax.data.Title;

import java.io.IOException;
import java.util.Dictionary;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings( "serial" )
public class UpdateMyTraxServlet extends HttpServlet {
	private static final Logger	log	= Logger.getLogger( UpdateMyTraxServlet.class.getName() );

	public void doPost( HttpServletRequest req, HttpServletResponse resp )
			throws IOException {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();

		// Check Access rights
		if( user == null || !userService.isUserAdmin() ) {
			// TODO Deny access for now
			resp.sendRedirect( "/" );
			return;
		}

		// We have full access rights
		// Let's do it!
		String key = req.getParameter( Title.KEY );
		Dictionary<String, List<String>> params = GeneralHelper.getContextParameters( req.getReader() );
		
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Title title = pm.getObjectById( Title.class, Long.parseLong( key ) );
		Transaction tx = null;
		
		final String header = String.format( "[Title:{0}] ", title.getKey() );
//		try {
//			
//		} catch( IOException e ) {
//			log.info( header + "IO Error" );
//			throw e;
//		} finally {
//			if( tx != null && tx.isActive() ) {
//				tx.rollback();
//				log.info( header + "Transaction rolled back" );
//			}
//			pm.close();
//		}
	}

}
