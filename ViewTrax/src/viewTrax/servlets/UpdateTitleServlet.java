package viewTrax.servlets;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import viewTrax.GeneralHelper;
import viewTrax.QueryHelper;
import viewTrax.SingletonWrapper;
import viewTrax.GeneralHelper.RemoveRelationship;
import viewTrax.data.Title;
import viewTrax.data.TitleName;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings( "serial" )
public class UpdateTitleServlet extends HttpServlet {
	private static final Logger					log	= Logger.getLogger( UpdateTitleServlet.class.getName() );

	protected final List<FieldUpdaterAbstract>	fieldUpdaterList;

	public UpdateTitleServlet() {
		super();
		this.fieldUpdaterList = new ArrayList<FieldUpdaterAbstract>( 10 );

		initializeFieldUpdaterList( fieldUpdaterList );
	}

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
		Dictionary<String, String> params = GeneralHelper.getContextParameters( req.getReader() );

		Map<FieldUpdaterAbstract, String> updated = new Hashtable<FieldUpdaterAbstract, String>(
				fieldUpdaterList.size() );
		// Get Title to update
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Title title = pm.getObjectById( Title.class, Long.parseLong( key ) );
		Transaction tx = null;

		final String header = String.format( "[Title:{0}] ", title.getKey() );
		try {
			for( FieldUpdaterAbstract updater : fieldUpdaterList ) {
				String value = params.get( updater.fieldName );
				if( updater.attemptUpdate( title, value ) ) {
					updated.put( updater, value );
				}
			}

			if( !updated.isEmpty() ) {
				tx = pm.currentTransaction();
				tx.begin();

				for( Map.Entry<FieldUpdaterAbstract, String> updatedField : updated.entrySet() ) {
					updatedField.getKey().persist( pm, title,
							updatedField.getValue() );
				}
				pm.makePersistent( title );

				tx.commit();
				log.info( header + "Updated in DB" );

				// Set response
				PrintWriter writer = resp.getWriter();
				for( Map.Entry<FieldUpdaterAbstract, String> updatedField : updated.entrySet() ) {
					writeOutFieldUpdate( writer, updatedField );
				}
				log.info( header + "Response: \n" + writer.toString() );
			}
		} catch( IOException e ) {
			log.info( header + "IO Error" );
			throw e;
		} finally {
			if( tx != null && tx.isActive() ) {
				tx.rollback();
				log.info( header + "Transaction rolled back" );
			}
			pm.close();
		}
	}

	private void writeOutFieldUpdate( PrintWriter writer,
			Map.Entry<FieldUpdaterAbstract, String> updatedField ) {
		// TODO we don't really need this method
		String fieldName = updatedField.getKey().fieldName;
		String value = updatedField.getValue();

		writer.println( fieldName );
		writer.println( value );
	}

	protected abstract class FieldUpdaterAbstract {
		protected final String	fieldName;

		public FieldUpdaterAbstract( String fieldName ) {
			this.fieldName = fieldName;
		}

		/**
		 * Attempt to update the field specified by this class using the value
		 * provided.
		 * 
		 * @param title
		 * @param value
		 *            Value of the new field
		 * @return True of an update was made. False otherwise.
		 */
		public abstract boolean attemptUpdate( Title title, String value );

		@Override
		public String toString() {
			return fieldName;
		}


		/**
		 * This method should be called after attemptUpdate, and its purpose is
		 * to persist the updated field.
		 * 
		 * @param pm
		 *            {@link PersistenceManager} used for this transaction
		 * @param title
		 *            The {@link Title} which contains the field to be updated
		 * @param value
		 *            The original {@link String} representation of the field
		 *            value sent by the user.
		 */
		public void persist( PersistenceManager pm, Title title, String value ) {
			// Default to do nothing
		}
	}

	private void initializeFieldUpdaterList(
			List<FieldUpdaterAbstract> fieldUpdaterList ) {
		fieldUpdaterList.add( new FieldUpdaterAbstract( Title.DETAILS_PAGE ) {
			/**
			 * @param url
			 *            the detailsPage to set
			 * @return True if the value differed and was set, False if the
			 *         value was the same, and not set
			 */
			@Override
			public boolean attemptUpdate( Title title, String url ) {
				if( url == null )
					return false;

				Link detailsPage = title.getDetailsPage();
				if( detailsPage.getValue().equalsIgnoreCase( url ) )
					return false;

				title.setDetailsPage( new Link( url ) );
				return true;
			}
		} );

		fieldUpdaterList.add( new FieldUpdaterAbstract( Title.NAMES ) {
			/**
			 * Attempts to update the names provided to the {@link Title}
			 * 
			 * @param title
			 *            Title to add the names
			 * @param namesParam
			 *            CSV of names which are to be added
			 * @return True if the any of the names have been added to the title
			 */
			@Override
			public boolean attemptUpdate( Title title, String namesParam ) {
				if( namesParam == null )
					return false;

				String[] names = namesParam.split( "," );

				List<String> notFound = new ArrayList<String>( names.length );
				for( String name : names ) {
					if( !title.containsName( name ) ) {
						notFound.add( name );
					}
				}

				for( String name : notFound ) {
					title.addTitleName( name );
				}
				return notFound.size() > 0;
			}
		} );

		// TODO optimize this operation somehow
		fieldUpdaterList.add( new FieldUpdaterAbstract( Title.REMOVE_NAMES ) {
			/**
			 * Attempts to remove the names provided from the {@link Title}
			 * 
			 * @param title
			 *            Title to add the names
			 * @param namesParam
			 *            CSV of names which are to be removed
			 * @return True if the any of the names have been added to the title
			 */
			@Override
			public boolean attemptUpdate( Title title, String namesParam ) {
				if( namesParam == null )
					return false;

				String[] names = namesParam.split( "," );

				List<TitleName> list = title.getNames();
				List<TitleName> found = getTitleNames( list, names );
				return found.size() == names.length;
			}

			private List<TitleName> getTitleNames( List<TitleName> list,
					String[] names ) {
				List<TitleName> found = new ArrayList<TitleName>( names.length );

				for( String rmName : names ) {
					for( TitleName titleName : list ) {
						String name = titleName.getName();
						if( rmName.equalsIgnoreCase( name ) ) {
							found.add( titleName );
							break;
						}
					}
				}
				return found;
			}

			@Override
			public void persist( PersistenceManager pm, Title title,
					String value ) {

				String[] names = value.split( "," );
				List<TitleName> found = getTitleNames( title.getNames(), names );

				for( TitleName tname : found ) {
					// We need to manually remove the relationship
					tname.setTitle( null );
					pm.makePersistent( tname );
					pm.deletePersistent( tname );

					// We don't need to do this as it will be reflected in DB
					// title.getNames().remove( tname );
				}
			}
		} );
	}

}
