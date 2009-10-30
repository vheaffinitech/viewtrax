package viewTrax.data;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * This class contains information about an (individual) entry in the title.
 * An entry can be specified as a variety of types
 * <ul>
 * 	<li>Episode in a show</li>
 * 	<li>Chapter in a manga</li>
 * </ul>  
 * 
 * @author ShihSam@gmail.com
 * 
 */
/**
 * @author sshih@google.com (Sam Shih)
 * 
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class TitleEntry {
	/**
	 * Identifier for this object
	 */
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	/**
	 * {@link Title} for which this name is associated with
	 */
	@ForeignKey
	@Persistent
	private Title title;

	/**
	 * Name of this entry. e.g. episode name chapter name
	 */
	@Persistent
	private String name;

	/**
	 * Division which this entry belongs in. e.g. Season 1 Vol 2
	 */
	@Persistent
	private String division;

	/**
	 * Identification Number of this entry in the {@link TitleEntry#division}
	 * e.g. Season x eps 5 -> 5 Vol x chapter 5 -> 5
	 */
	@Persistent
	private int number;

	// TODO add fansubbers/scanlators
	private String[] translators;

	public TitleEntry(Title title, String name, String division, int number) {
		this(title, name, division, number, new String[] {});
	}

	public TitleEntry(Title title, String name, String division, int number,
			String[] translators) {
		this.title = title;
		this.name = name;
		this.division = division;
		this.number = number;
		this.translators = translators;
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
	public void setName(String name) {
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
	public void setDivision(String division) {
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
	public void setNumber(int number) {
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
	public void setTranslators(String[] translators) {
		this.translators = translators;
	}

	/**
	 * @return the translators
	 */
	public String[] getTranslators() {
		return translators;
	}
}
