package viewTrax.servlets;

import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import viewTrax.GeneralHelper;
import viewTrax.SingletonWrapper;
import viewTrax.data.Title;
import viewTrax.data.TitleEntry;
import viewTrax.data.TitleName;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	private List<FieldUpdaterAbstract> createFieldUpdaterList() {
		List<FieldUpdaterAbstract> fieldUpdaterList = new LinkedList<FieldUpdaterAbstract>();
		fieldUpdaterList.add( new DetailsPageFieldUpdater() );
		fieldUpdaterList.add( new AddNamesFieldUpdater() );
		fieldUpdaterList.add( new RemoveNamesFieldUpdater() );
		fieldUpdaterList.add( new AddTitleEntryFieldUpdater() );
		fieldUpdaterList.add( new RemoveTitleEntryFieldUpdater() );

		return Collections.unmodifiableList( fieldUpdaterList );
	}

	public UpdateTitleServlet() {
		super();
		this.fieldUpdaterList = createFieldUpdaterList();
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
		Dictionary<String, List<String>> params = GeneralHelper.getContextParameters( req.getReader() );

		Map<FieldUpdaterAbstract, List<String>> updated = new Hashtable<FieldUpdaterAbstract, List<String>>(
				fieldUpdaterList.size() );
		// Get Title to update
		PersistenceManager pm = SingletonWrapper.get().getPersistenceManager();
		Title title = pm.getObjectById( Title.class, Long.parseLong( key ) );
		Transaction tx = null;

		final String header = String.format( "[Title:{0}] ", title.getKey() );
		try {
			List<FieldUpdaterAbstract> ful = createFieldUpdaterList();
			for( FieldUpdaterAbstract updater : ful ) {
				List<String> value = params.get( updater.fieldName );
				if( value != null && value.size() != 0 && updater.attemptUpdate( title, value ) ) {
					updated.put( updater, value );
				}
			}

			if( !updated.isEmpty() ) {
				tx = pm.currentTransaction();
				tx.begin();

				for( Map.Entry<FieldUpdaterAbstract, List<String>> updatedFields : updated.entrySet() ) {
					updatedFields.getKey().persist( pm, title,
							updatedFields.getValue() );
				}
				pm.makePersistent( title );

				tx.commit();
				log.info( header + "Updated in DB" );

				// Set response
				PrintWriter writer = resp.getWriter();
				for( Map.Entry<FieldUpdaterAbstract, List<String>> updatedFields : updated.entrySet() ) {
					writeOutFieldUpdate( writer, updatedFields );
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
			Entry<FieldUpdaterAbstract, List<String>> updatedField ) {
		// TODO we don't really need this method
		String fieldName = updatedField.getKey().fieldName;
		List<String> values = updatedField.getValue();

		writer.println( fieldName );
		for( String value : values ) {
			writer.println( value );
		}
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
		 * @param values
		 *            Value of the new field
		 * @return True of an update was made. False otherwise.
		 */
		public abstract boolean attemptUpdate( Title title, List<String> values );

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
		 * @param list
		 *            The original {@link String} representation of the field
		 *            value sent by the user.
		 */
		public void persist( PersistenceManager pm, Title title,
				List<String> list ) {
			// Default to do nothing
		}
	}

	public class DetailsPageFieldUpdater extends FieldUpdaterAbstract {
		public DetailsPageFieldUpdater() {
			super( Title.DETAILS_PAGE );
		}

		/**
		 * @param value
		 *            the detailsPage to set
		 * @return True if the value differed and was set, False if the value
		 *         was the same, and not set
		 */
		@Override
		public boolean attemptUpdate( Title title, List<String> value ) {
			Link detailsPage = title.getDetailsPageOrDefault();
			String url = value.get( 0 );
			if( detailsPage.getValue().equalsIgnoreCase( url ) )
				return false;

			title.setDetailsPage( new Link( url ) );
			return true;
		}
	}

	public class AddNamesFieldUpdater extends FieldUpdaterAbstract {
		public AddNamesFieldUpdater() {
			super( Title.NAMES );
		}

		/**
		 * Attempts to update the names provided to the {@link Title}
		 * 
		 * @param title
		 *            Title to add the names
		 * @param names
		 *            {@link List} of names which are to be added
		 * @return True if the any of the names have been added to the title
		 */
		@Override
		public boolean attemptUpdate( Title title, List<String> names ) {
			List<String> notFound = new ArrayList<String>( names.size() );
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
	}

	public class RemoveNamesFieldUpdater extends FieldUpdaterAbstract {

		// TODO optimize this operation somehow
		public RemoveNamesFieldUpdater() {
			super( Title.REMOVE_NAMES );
		}

		/**
		 * Attempts to remove the names provided from the {@link Title}
		 * 
		 * @param title
		 *            Title to add the names
		 * @param names
		 *            {@link List} of names which are to be removed
		 * @return True if the any of the names have been added to the title
		 */
		@Override
		public boolean attemptUpdate( Title title, List<String> names ) {
			List<TitleName> list = title.getNames();
			List<TitleName> found = getTitleNames( list, names );
			return found.size() == names.size();
		}

		private List<TitleName> getTitleNames( List<TitleName> list,
				List<String> names ) {
			List<TitleName> found = new ArrayList<TitleName>( names.size() );

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
				List<String> names ) {

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
	}

	public class AddTitleEntryFieldUpdater extends FieldUpdaterAbstract {

		public AddTitleEntryFieldUpdater() {
			super( Title.ENTRIES );
		}

		@Override
		public boolean attemptUpdate( Title title, List<String> value ) {
			List<TitleEntry> entries = title.getEntries();
			// Only allow one name update for now
			switch( value.size() ) {
			case 1:
				entries.add( TitleEntry.createNext( title, value.get( 0 ) ) );
				break;
			case 3:
				// entries.add( TitleEntry.createNext( title, value.get( 0 ), )
				// );
				// break;
			default:
				return false;
			}

			return true;
		}
	}

	public class RemoveTitleEntryFieldUpdater extends FieldUpdaterAbstract {

		public RemoveTitleEntryFieldUpdater() {
			super( Title.REMOVE_ENTRIES );
		}

		@Override
		public boolean attemptUpdate( Title title, List<String> value ) {
			List<TitleEntry> entries = title.getEntries();
			// Only allow one name update for now
			switch( value.size() ) {
			case 1:
				for( TitleEntry titleEntry : entries ) {
					if( value.get( 0 ).equals( titleEntry.getName() )) {
						entries.remove( titleEntry );
						return true;
					}
				}
				// Fall through
			case 3:
				// entries.add( TitleEntry.createNext( title, value.get( 0 ), )
				// );
				// break;
			default:
				break;
			}

			return false;
		}
	}

}
