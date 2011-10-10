package org.grails.plugin.alfresco
/**
 * El plugin soporta varias instalaciones de Alfresco.
 * Por defecto, al buscar un servidor consultará las entidades
 * con online true ordenadas por weight ascendente. La primnera que
 * le conteste será almacenada.
 */
class AlfrescoServer {
    String name
    String details
    String serverUrl
    String username
    String password
	String ticket
    boolean online = true

    static constraints = {
        name(size:3..150,unique:true,blank:false)
        serverUrl(url:true,blank:false)
        username(size:3..50,blank:false)
        password(size:5..50,blank:false)
		ticket(maxSize:8184)
        details(maxSize:4000)
    }

    public String toString(){
        "${name} @ ${serverUrl}"
    }
}
