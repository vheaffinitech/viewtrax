package viewTrax.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.users.User;

import viewTrax.SingletonWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.ForeignKey;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


/**
 * Contains information about a user for the purpose of the ViewTrax
 * application.
 * 
 * @author ShihSam@gmail.com
 */
@PersistenceCapable( identityType = IdentityType.APPLICATION )
public class ViewTraxUser {

	/**
	 * Identifier for this object
	 */
	@PrimaryKey
	@Persistent( valueStrategy = IdGeneratorStrategy.IDENTITY )
	private Key		key;

	private User	googleUser;

	public ViewTraxUser( User googleUser ) {
		this.googleUser = googleUser;
	}

	public String toString() {
		return "[" + key + "] " + googleUser;
	}

	public Key getKey() {
		return key;
	}

	/**
	 * @return the googleUser
	 */
	public User getGoogleUser() {
		return googleUser;
	}
}
