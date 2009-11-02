package viewTrax.data;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Link;
import com.google.appengine.api.datastore.Rating;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * @author ShihSam@gmail.com(Sam Shih)
 */
@PersistenceCapable( identityType = IdentityType.APPLICATION )
public class Title {

	private static final int	DEFAULT_MAX_ENTRIES	= 0;

	public static final String	KEY					= "key";
	public static final String	NAMES				= "names";
	public static final String	ENTRIES				= "entries";
	public static final String	DETAILS_PAGE		= "detailsPage";
	public static final String	REMOVE_PREFIX		= "rm";
	public static final String	REMOVE_NAMES		= REMOVE_PREFIX + NAMES;
	public static final String	REMOVE_ENTRIES		= REMOVE_PREFIX + ENTRIES;

	@PrimaryKey
	@Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
	private Key					key;

	/**
	 * Hold a list of names for the title. Where the first entry in the list
	 * takes priority.
	 */
	@ForeignKey
	@Persistent( mappedBy = "title", defaultFetchGroup = "true" )
	private List<TitleName>		names;

	/**
	 * Hold a list of entries for the title. Entries may be defined as episodes
	 * or chapters
	 */
	@ForeignKey
	@Persistent( mappedBy = "title", defaultFetchGroup = "true" )
	private List<TitleEntry>	entries;

	/**
	 * Short description of the title
	 */
	@Persistent
	private String				description;

	/**
	 * Hold a list of feeds to check for this listing.
	 */
	@Persistent
	private String[]			feeds;

	/**
	 * Location where details may be obtained.
	 */
	@Persistent( defaultFetchGroup = "true" )
	private Link				detailsPage;

	/**
	 * Rating given to this title
	 */
	@Persistent
	private Rating				rating;

	@NotPersistent
	private int					primaryNameIndex	= 0;

	public static Title createTitle( String name, String description ) {
		List<TitleName> names = new ArrayList<TitleName>();
		Rating rating = new Rating( 0 );
		Title title = new Title( names, new ArrayList<TitleEntry>(),
				description, null, null, rating );
		title.addTitleName( name );

		return title;
	}

	protected Title( List<TitleName> names, List<TitleEntry> entries,
			String description, String[] feeds, Link detailsPage, Rating rating ) {
		this.names = names;
		this.entries = entries;
		this.description = description;
		this.feeds = feeds;
		this.detailsPage = detailsPage;
		this.rating = rating;
	}

	@Override
	public String toString() {
		return String.format( "[{0}]{1}", this.key,
				this.getPrimaryName().getName() );
	}

	public static Link getDefault( String name ) {
		return new Link( "http://en.wikipedia.org/wiki/" + name );
	}

	public Key getKey() {
		return key;
	}

	public void setNames( List<TitleName> names ) {
		this.names = names;
	}

	public List<TitleName> getNames() {
		return names;
	}

	public TitleName getPrimaryName() {
		return names.get( primaryNameIndex );
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setFeeds( String[] feeds ) {
		this.feeds = feeds;
	}

	public String[] getFeeds() {
		return feeds;
	}

	/**
	 * @param entries
	 *            the entries to set
	 */
	public void setEntries( List<TitleEntry> entries ) {
		this.entries = entries;
	}

	/**
	 * @return the entries
	 */
	public List<TitleEntry> getEntries() {
		return entries;
	}

	/**
	 * @param detailsPage
	 *            the detailsPage to set
	 */
	public void setDetailsPage( Link detailsPage ) {
		this.detailsPage = detailsPage;
	}

	/**
	 * @return the detailsPage
	 */
	public Link getDetailsPage() {
		return detailsPage;
	}

	public Link getDetailsPageOrDefault() {
		// we need this to allow JDO to do lazy get
		if( getDetailsPage() == null ) {
			detailsPage = getDefault( names.get( 0 ).getName() );
		}
		return detailsPage;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating( Rating rating ) {
		this.rating = rating;
	}

	/**
	 * @return the rating
	 */
	public Rating getRating() {
		return rating;
	}

	public void addTitleName( TitleName name ) {
		this.names.add( name );
	}

	public void addTitleName( String name ) {
		this.names.add( new TitleName( this, name ) );
	}

	/**
	 * Cycles through the list of stored names, and see is the title name that
	 * was provided is contained in the list
	 * <p>
	 * Note that this will do a comparision
	 * <ul>
	 * ignoring
	 * </ul>
	 * the case.
	 * 
	 * @param name
	 *            New alternative name for the title
	 * @return true if the name already exists as part of the list, false
	 *         otherwise
	 */
	public boolean containsName( String name ) {
		for( TitleName tName : names ) {
			if( tName.getName().equalsIgnoreCase( name ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the {@link Title} data matching the {@link TitleName} provided
	 * 
	 * @param pm
	 * @param name
	 *            Name of the title
	 * @return Title with the name provided.
	 */
	public static Title get( PersistenceManager pm, TitleName name ) {
		Title title = name.getTitle();
		// String filter = "id == titleIDparam";
		// Query query = pm.newQuery(Title.class);
		// query.setFilter(filter);
		// query.declareParameters("String titleIDParam");
		// Object obj = query.execute(title.getKey());
		//
		// List<Title> list = (List<Title>)obj;
		// title = list.get(0);
		//		
		// return title;
		return pm.getObjectById( Title.class, title.getKey() );
	}

}
