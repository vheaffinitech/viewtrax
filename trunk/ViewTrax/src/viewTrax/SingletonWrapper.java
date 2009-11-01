package viewTrax;

import org.w3c.dom.Node;

import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

/**
 * Class to hold singleton instances of
 * 
 * @author ShihSam@gmail.com
 * 
 */
public final class SingletonWrapper {
	private static SingletonWrapper instance = new SingletonWrapper();

	/**
	 * Hold an inner instance to create {@link PersistenceManager} s
	 * 
	 * @see PersistenceManager
	 * @see PersistenceManagerFactory
	 */
	private final PersistenceManagerFactory pmf;

	private final Dictionary<String, CacheDatePair<List<Node>>> titleDetailsCache;
	
	private final List<String> emptyListString;

	private SingletonWrapper() {
		this.pmf = JDOHelper
				.getPersistenceManagerFactory("transactions-optional");
		this.titleDetailsCache = new Hashtable<String, CacheDatePair<List<Node>>>();
		this.emptyListString = Collections.unmodifiableList( new LinkedList<String>() );
	}

	public static SingletonWrapper get() {
		return instance;
	}

	/**
	 * Wrap call to get a {@link PersistenceManager} from inner factory.
	 * 
	 * @return Instance of a {@link PersistenceManager}
	 * @see PersistenceManager
	 * @see PersistenceManagerFactory
	 */
	public PersistenceManager getPersistenceManager() {
		return pmf.getPersistenceManager();
	}

	public static class CacheDatePair<T> {
		public T cache;
		public Date date;

		public CacheDatePair(T cache, Date date) {
			this.cache = cache;
			this.date = date;
		}
	}

	public Dictionary<String, CacheDatePair<List<Node>>> getTitleDetailsCache() {
		return titleDetailsCache;
	}

	public List<String> getEmptyListString() {
		return emptyListString;
	}
}
