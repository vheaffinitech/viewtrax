package viewTrax;

import com.google.appengine.api.datastore.Key;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;


/**
 * Generic set of helper methods
 * 
 * @author ShihSam@gmail.com (Sam Shih)
 */
public class GeneralHelper {
	private static final String	PARAM_END	= "\t-ParamEnd";

	public static interface RemoveRelationship<T> {
		/**
		 * Allows the child to remove the relationship to its owner before it is
		 * persisted.
		 * 
		 * @param child
		 */
		public void run( T child );
	}

	private static final Logger	log	= Logger.getLogger( GeneralHelper.class.getName() );

	private static void LogInfo( String queryName, String msg ) {
		log.info( "[Query:" + queryName + "] " + msg );
	}

	/**
	 * Attempts to create a transaction write/commit. Upon failure, it will
	 * rollback the transaction.
	 * 
	 * @param <T>
	 *            Type of the object to be persisted
	 * @param data
	 *            Object containing the data to be sent to the DB
	 * @throws Exception
	 */
	public static <T> void transactionalPersist( T data ) throws Exception {
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			pm.makePersistent( data );
			tx.commit();
		} catch( Exception e ) {
			tx.rollback();
			throw e;
		} finally {
			pm.close();
		}
	}

	/**
	 * Attempts to delete a data object. Upon failure, it will rollback the
	 * transaction.
	 * 
	 * @param <T>
	 *            Type of the object to be deleted
	 * @param aClass
	 *            Class of the object to be removed
	 * @param key
	 *            key identifier for the object to be removed
	 * @throws Exception
	 */
	public static <T> void transactionalDelete( Class<T> aClass, Key key )
			throws Exception {
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			T data = pm.getObjectById( aClass, key );
			pm.deletePersistent( data );
			tx.commit();
		} catch( Exception e ) {
			tx.rollback();
			throw e;
		} finally {
			pm.close();
		}
	}

	public static <T> void transactionalDelete( Class<T> aClass, Key key,
			RemoveRelationship<T> removeRelationship ) throws Exception {
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			T data = pm.getObjectById( aClass, key );
			removeRelationship.run( data );
			pm.makePersistent( data );
			pm.deletePersistent( data );
			tx.commit();
		} catch( Exception e ) {
			tx.rollback();
			throw e;
		} finally {
			pm.close();
		}
	}

	/**
	 * Attempts to delete a data object. Upon failure, it will rollback the
	 * transaction. This calls deletePersistentAll.
	 * 
	 * @param <T>
	 *            Type of the object to be deleted
	 * @param data
	 *            Object or Collection of objects containing the data to be
	 *            deleted from the DB
	 * @throws Exception
	 */
	public static <T> void transactionalDeleteAll( T data ) throws Exception {
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			pm.deletePersistentAll( data );
			tx.commit();
		} catch( Exception e ) {
			tx.rollback();
			throw e;
		} finally {
			pm.close();
		}
	}

	/**
	 * @see #transactionalDeleteAll(Object)
	 * @param <T>
	 * @param collection
	 * @throws Exception
	 */
	public static <T> void transactionalDeleteCollection(
			Collection<?> collection ) throws Exception {
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();
			pm.deletePersistentAll( collection );
			tx.commit();
		} catch( Exception e ) {
			tx.rollback();
			throw e;
		} finally {
			pm.close();
		}
	}

	public static Dictionary<String, List<String>> getContextParameters(
			BufferedReader reader ) throws IOException {
		Hashtable<String, List<String>> params = new Hashtable<String, List<String>>(
				4 );
		while( reader.ready() ) {
			String name = reader.readLine();
			params.put( name, parseParam( reader ) );
		}
		return params;
	}

	/**
	 * Reads in parameters relating to a specific entry. This assumes that the
	 * consecutive lines are parameters
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static List<String> parseParam( BufferedReader reader )
			throws IOException {
		List<String> list = new ArrayList<String>( 4 );
		// String name= reader.readLine();
		while( reader.ready() ) {
			String value = reader.readLine();
			// if(PARAM_END.equals( value )) {
			if( value.isEmpty() ) {
				break;
			}
			list.add( value );
		}

		return list;
	}
}
