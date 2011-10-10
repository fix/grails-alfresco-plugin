/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ineco.grails.alfresco;

import com.rivetlogic.core.cma.repo.*;
import java.util.ArrayList;
import java.util.*;
import java.io.*;
import sun.misc.*;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author Miguel
 */
public class AlfrescoUtils {
	
	/**
	
	**/
	public static String serializeTicket(Ticket t) throws Exception{
		ByteArrayOutputStream bs= new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream (bs);
		os.writeObject(t);
		os.close();
		byte[] bytes =  bs.toByteArray();
		BASE64Encoder b64enc = new BASE64Encoder();
    	return b64enc.encode(bytes); 
	}
	
	/**
	
	**/
	public static Ticket unSerializeTicket(String s) throws Exception{
		Ticket t = null;
		BASE64Decoder b64dec = new BASE64Decoder();
    	byte[] bytes = b64dec.decodeBuffer(s);
		ByteArrayInputStream bs= new ByteArrayInputStream(bytes); // bytes es el byte[]
		ObjectInputStream is = new ObjectInputStream(bs);
		t = (Ticket)is.readObject();
		is.close();        	

		return t;		
	}

    /**
     * 
     * @return
     */
    public static List<SortDefinition> getSortDefinitions() {
        List<SortDefinition> sortDefinitions = new Vector<SortDefinition>();
        sortDefinitions.add(new SortDefinition(SortDefinition.SortType.FIELD, "@{http://www.alfresco.org/model/content/1.0}name", true));
        return sortDefinitions;
    }

    /**
     * 
     * @return
     */
    public static List<String> getPermissionsForUserSearch() {
        List<String> requiredPermissions = new ArrayList<String>();
        //requiredPermissions.add(SecurityService.READ_CONTENT);
        /*
        requiredPermissions.add(SecurityService.WRITE_CONTENT);
        
        requiredPermissions.add(SecurityService.WRITE_PROPERTIES);
        requiredPermissions.add(SecurityService.DELETE);
         */
        return requiredPermissions;
    }

    /**
     * 
     * @return
     */
    public static List<QName> getPropertiesForUserSearch() {
        QName name = QName.createQName("{http://www.alfresco.org/model/content/1.0}name");
        QName created = QName.createQName("{http://www.alfresco.org/model/content/1.0}created");

        List<QName> properties = new Vector<QName>();
        properties.add(name);
        properties.add(created);
        return properties;
    }

    /**
     *
     * @return
     */
    public static List<QName> getPropertiesForUser() {
        QName p1 = QName.createQName("{http://www.alfresco.org/model/content/1.0}userName");
        QName p2 = QName.createQName("{http://www.alfresco.org/model/content/1.0}homeFolder");

        List<QName> properties = new Vector<QName>();
        properties.add(p1);
        properties.add(p2);
        return properties;
    }
}
