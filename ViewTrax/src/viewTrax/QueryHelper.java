package viewTrax;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query.FilterOperator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import viewTrax.data.Title;
import viewTrax.data.TitleName;

import java.util.ArrayList;
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
	
	@SuppressWarnings( "unchecked" )
	public static List<String> searchTitles( String query ) {
		// Pre-condition check (search shouldn't be a getAll() )

		// Capitalize first letter
		char firstChar = query.charAt( 0 );
		if( Character.isLetter( firstChar )
			&& Character.isLowerCase( firstChar ) ) {
			query = query.replaceFirst( String.valueOf( firstChar ),
					String.valueOf( Character.toUpperCase( firstChar ) ) );
		}

		// Build search query
		String entityName = TitleName.class.getSimpleName();
		com.google.appengine.api.datastore.Query gq = new com.google.appengine.api.datastore.Query(
				entityName );
		gq.addFilter( "name", FilterOperator.GREATER_THAN_OR_EQUAL, query );
		gq.addFilter( "name", FilterOperator.LESS_THAN, query + 'z' );

		// Get a handle on the datastore itself
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Iterable<Entity> iterable = datastore.prepare( gq ).asIterable();
		List foundNames = new ArrayList<String>();
		for( Entity e : iterable ) {
			foundNames.add( e.getProperty( "name" ) );
		}
		
		LogInfo( "searchTitle", query + " found: " + foundNames );
		return foundNames;
	}

	// ***********************************
	// Helper Methods
	// ***********************************

	private static void LogInfo( String queryName, String msg ) {
		msg = "[Query:" + queryName + "] " + msg;
		log.info( msg );
		System.out.println( msg );
	}
}
