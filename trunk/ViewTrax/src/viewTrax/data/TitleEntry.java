package viewTrax.data;

import com.google.appengine.api.datastore.Key;

import java.util.LinkedList;
import java.util.List;

import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * This class contains information about an (individual) entry in the title. An
 * entry can be specified as a variety of types
 * <ul>
 * <li>Episode in a show</li>
 * <li>Chapter in a manga</li>
 * </ul>
 * 
 * @author ShihSam@gmail.com
 */
/**
 * @author ShihSam@gmail.com (Sam Shih)
 */
/**
 * @author sshih@google.com (Sam Shih)
 */
/**
 * @author sshih@google.com (Sam Shih)
 */
@PersistenceCapable( identityType = IdentityType.APPLICATION )
public class TitleEntry {
	/**
	 * Identifier for this object
	 */
	@PrimaryKey
	@Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
	private Key					key;

	/**
	 * {@link Title} for which this name is associated with
	 */
	@ForeignKey
	@Persistent
	private Title				title;

	/**
	 * Name of this entry. e.g. episode name chapter name
	 */
	@Persistent
	private String				name;

	/**
	 * Division which this entry belongs in. e.g. Season 1 Vol 2
	 */
	@Persistent
	private String				division;

	/**
	 * Identification Number of this entry in the {@link TitleEntry#division}
	 * e.g. Season x eps 5 -> 5 Vol x chapter 5 -> 5
	 */
	@Persistent
	private int					number;

	// TODO add fansubbers/scanlators
	private List<ReleaseGroups>	releaseGroups;

	public static TitleEntry createNext( Title title, String name ) {
		return createNext( title, name, new LinkedList<ReleaseGroups>() );
	}

	public static TitleEntry createNext( Title title, String name,
			List<ReleaseGroups> groups ) {
		List<TitleEntry> entries = title.getEntries();
		if( entries.size() == 0 ) {
			return new TitleEntry( title, name, "unknown", 1, groups );
		}

		TitleEntry last = entries.get( entries.size() - 1 );
		String division = last.division;
		int number = last.number;

		return new TitleEntry( title, name, division, number, groups );
	}

	public TitleEntry( Title title, String name, String division, int number ) {
		this( title, name, division, number, new LinkedList<ReleaseGroups>() );
	}

	public TitleEntry( Title title, String name, String division, int number,
			List<ReleaseGroups> translators ) {
		this.title = title;
		this.name = name;
		this.division = division;
		this.number = number;
		this.releaseGroups = translators;
	}

	/**
	 * @return the key
	 */
	public Key getKey() {
		return key;
	}

	/**
	 * @return the title
	 */
	public Title getTitle() {
		return title;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName( String name ) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param division
	 *            the division to set
	 */
	public void setDivision( String division ) {
		this.division = division;
	}

	/**
	 * @return the division
	 */
	public String getDivision() {
		return division;
	}

	/**
	 * @param number
	 *            the number to set
	 */
	public void setNumber( int number ) {
		this.number = number;
	}

	/**
	 * @return the number
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @param translators
	 *            the translators to set
	 */
	public void setReleaseGroups( List<ReleaseGroups> translators ) {
		this.releaseGroups = translators;
	}

	/**
	 * @return the translators
	 */
	public List<ReleaseGroups> getReleaseGroups() {
		return releaseGroups;
	}
}
