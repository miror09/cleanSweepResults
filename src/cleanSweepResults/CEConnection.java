/**
 * 
 */
package cleanSweepResults;

import java.util.Iterator;
import java.util.Vector;
import javax.security.auth.Subject;
import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;
/**
 * @author Miroslav Richter - 4.B
 *
 */
public class CEConnection {

	private Connection con;
	private Domain dom;
	private String domainName;
	private ObjectStoreSet ost;
	private Vector osnames;
	private boolean isConnected;
	private UserContext uc;
	private Subject subject;
		
	public CEConnection()
	{
		con = null;
		uc = UserContext.get();
		dom = null;
		domainName = null;
		ost = null;
		osnames = new Vector();
		isConnected = false;
	}
	
	/*
	 * The connection to the Content Engine using username, password, JAAS stanza and CE Uri
	 */

	public void establishConnection(String userName, String password, String stanza, String uri)
	{
        	con = Factory.Connection.getConnection(uri);
	        Subject sub = UserContext.createSubject(con,userName,password,stanza);
	        setSubject(sub);
	        uc.pushSubject(sub);
	        dom = fetchDomain();
	        domainName = dom.get_Name();
	        ost = getOSSet();
	        isConnected = true;
	}

	public Domain fetchDomain()
	{
        	dom = Factory.Domain.fetchInstance(con, null, null);
	        return dom;
	}
    
	/*
	 * Returns ObjectStoreSet from Domain.
	 */

	public ObjectStoreSet getOSSet()
	{
		ost = dom.get_ObjectStores();
		return ost;
	}
    
    /*
     * Returns vector containing object stores 
     * names from object stores available in
     * ObjectStoreSet.
     */
	public Vector getOSNames()
    {
    	if(osnames.isEmpty())
    	{
    		Iterator it = ost.iterator();
    		while(it.hasNext())
    		{
    			ObjectStore os = (ObjectStore) it.next();
    			osnames.add(os.get_DisplayName());
    		}
    	}
        return osnames;
    }

	/*
	 * Checks whether JAAS login has been performed
	 * with the Content Engine or not.
	 */
	public boolean isConnected() 
	{
		return isConnected;
	}
	
	/*
	 * Returns ObjectStore object for supplied
	 * object store name.
	 */
	public ObjectStore fetchOS(String name)
    {
        ObjectStore os = Factory.ObjectStore.fetchInstance(dom, name, null);
        return os;
    }
	
	/*
	 * Returns the domain name.
	 */
	public String getDomainName()
    {
        return domainName;
    }

	/*
	 * Returns the subject.
	 */
	public Subject getSubject()
	{
		return subject;
	}

	/*
	 * Sets the subject to the Subject object created using
	 * the username and password passed to the establishConnection method.
	 */
	public void setSubject(Subject sub)
	{
		this.subject = sub;
	}	
}
