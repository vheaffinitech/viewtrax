package viewTrax;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import viewTrax.data.Title;
import viewTrax.data.TitleName;

import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.xml.parsers.DocumentBuilderFactory;


public class QueryHelper {
	private static final Logger	log	= Logger.getLogger( QueryHelper.class.getName() );

	public static Title getTitle( String title ) throws Exception {
		// String title = req.getParameter(TITLE);
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();

		try {
			TitleName bestMatch = TitleName.getFirst( pm, title );
			Title t = Title.get( pm, bestMatch );
			LogInfo( "getTitle", title + " returned match: " + t.getKey() );
			return t;
		} catch( Exception e ) {
			// No match found
			LogInfo( "getTitle", title + " no match found" );
			throw e;
		} finally {
			pm.close();
		}
	}

	public static Title getTitleById( String id ) {
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Title title = pm.getObjectById( Title.class, Long.parseLong( id ) );
		pm.close();
		return title;
	}

	public static List<String> searchTitles( String query ) {
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		List<String> matches = TitleName.searchFor( pm, query );

		LogInfo( "searchTitle", query + " found: " + matches );
		return matches;
	}

	// ***********************************
	// Helper Methods
	// ***********************************

	private static void LogInfo( String queryName, String msg ) {
		log.info( "[Query:" + queryName + "] " + msg );
	}
}
