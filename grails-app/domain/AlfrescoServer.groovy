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
    String adminUsername
    String adminPassword

    boolean online = true
    int weight = 0

    static mapping = {
        name column:'alf_name'
        details column:'alf_details'
        serverUrl column:'alf_server_url'
        adminUsername column:'alf_admin_username'
        adminPassword column:'alf_admin_password'
        online column:'alf_online'
        weight column:'alf_weight'
    }

    static constraints = {
        name(size:3..150,unique:true,blank:false)
        serverUrl(url:true,blank:false)
        adminUsername(size:3..50,blank:false)
        adminPassword(size:5..50,blank:false)
        weight(range:0..100)
        details(maxSize:4000)
    }

    public String toString(){
        "${name} @ ${serverUrl}"
    }
}
