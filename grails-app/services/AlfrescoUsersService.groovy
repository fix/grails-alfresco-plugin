import com.rivetlogic.core.cma.api.AuthenticationService;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.rivetlogic.core.cma.api.SearchService;
import com.rivetlogic.core.cma.api.ActionService;
import com.rivetlogic.core.cma.api.SecurityService;
import com.rivetlogic.core.cma.exception.AuthenticationFailure;
import com.rivetlogic.core.cma.exception.AuthorityExistsException;
import com.rivetlogic.core.cma.exception.CmaRuntimeException;
import com.rivetlogic.core.cma.exception.InvalidTicketException;
import com.rivetlogic.core.cma.impl.AuthenticationServiceImpl;
import com.rivetlogic.core.cma.impl.ContentServiceImpl;
import com.rivetlogic.core.cma.impl.PeopleServiceImpl;
import com.rivetlogic.core.cma.impl.SearchServiceImpl;
import com.rivetlogic.core.cma.repo.Node;
import com.rivetlogic.core.cma.repo.SortDefinition;
import com.rivetlogic.core.cma.repo.Ticket;

import es.ineco.grails.alfresco.*
import org.alfresco.service.cmr.search.ResultSet
import org.alfresco.service.cmr.search.ResultSet
import org.alfresco.service.cmr.repository.ContentData

class AlfrescoUsersService {
    def alfrescoConnectionService
    def authenticationService
    def peopleService
    def nodeService
    def searchService
    def workflowService
    def actionService
    
    boolean transactional = true

    /**
    /* Start a session trying to reuse an existing ticket. If not present or invalid, create a new one.
     **/
    Ticket getTicket(String servername, String username) {
        def alf = alfrescoConnectionService.getServer(servername, username)
		println alf
		def ticket=null;
		if(alf.ticket){
			ticket = AlfrescoUtils.unSerializeTicket(alf.ticket)
			//check if ticket is valid
			try{
				authenticationService.validate(ticket)
			}
			catch(Exception e){
				//Ticket not valid then we will create and save a new one
				ticket=null
			}
		}
        if(!ticket) {
			ticket = openSession(alf)
			alf.ticket=AlfrescoUtils.serializeTicket(ticket);
			alf.save()
        }
		
		return ticket
    }

    /**
    /* Start a session and open a ticket
     **/
    Ticket openSession(AlfrescoServer alf) {
        def url = "${alf.serverUrl}/alfresco/service"
        def ticket = authenticationService.authenticate(url, alf.username, alf.password.toCharArray());

    }

    /**
    /* Close the session releasing the ticket
     **/
    String closeTicket(ticket){
        authenticationService.invalidateTicket(ticket);
        'ok'
    }

    /**
     * find the id of the user home, for instance:
     * workspace://SpacesStore/c9fe7d30-d017-41bc-bedc-2d104be20ed3
     **/
    def getUserHome(ticket,userName){
        def maxResults = 1
        def userID = peopleService.getPerson(ticket,userName)
        def query = """
ID:"${userID}"
        """       
        def nodes = searchService.query(
            ticket,
            new StoreRef("workspace://SpacesStore"),
            SearchService.QueryLanguage.lucene,
            query,
            AlfrescoUtils.getPropertiesForUser(),
            true,
            true,
            true,
            true,
            AlfrescoUtils.getPermissionsForUserSearch(),
            maxResults,
            AlfrescoUtils.getSortDefinitions());

        if(nodes.size()){
            return nodes[0].properties[QName.createQName("{http://www.alfresco.org/model/content/1.0}homeFolder")]
        }
        else return null
    }

    def getNodeProperties(ticket,nodeRef){
        if(!(nodeRef instanceof NodeRef)){
            nodeRef = new NodeRef(nodeRef)
        }
        return nodeService.getProperties(ticket,nodeRef)
    }


    def getNode(ticket,ref){
        def maxResults = 100
        def query = "ID:\"${ref}\""
        def nodes = searchService.query(
            ticket,
            new StoreRef("workspace://SpacesStore"),
            SearchService.QueryLanguage.lucene,
            query,
            AlfrescoUtils.getPropertiesForUserSearch(),
            true,
            true,
            true,
            true,
            AlfrescoUtils.getPermissionsForUserSearch(),
            maxResults,
            AlfrescoUtils.getSortDefinitions());
        def nodeList = nodesToList(nodes)
        return nodeList[0]
    }

    def sizeOfNode(ticket, oNodeRef){
        ContentData contentData = (ContentData)nodeService.getProperty(ticket, oNodeRef, ContentModel.PROP_CONTENT);
        if (contentData != null && contentData.size > 0){
            return contentData.size
        }else
        return 0
    }

    def moveNode(ticket,originalNodeID,destinationContainerID){
        nodeService.moveNode(ticket,new NodeRef(originalNodeID),new NodeRef(destinationContainerID));
    }


    def describeNode(ticket, oNodeRef){
        def description = nodeService.getProperty(ticket, oNodeRef, ContentModel.PROP_DESCRIPTION);
        return description
    }
    /**
     *
     **/
    def getFiles(ticket, parentFolderID) {
        return getChildren(ticket,parentFolderID,["{http://www.alfresco.org/model/content/1.0}content"])
    }

    def getFolders(ticket,parentFolderID){
        return getChildren(ticket,parentFolderID,["{http://www.alfresco.org/model/content/1.0}folder"])
    }
	
    def getChildren(ticket,parentFolderID,types){
        def maxResults = 100
        def query = "PARENT:\"${parentFolderID}\""
        if(types?.size()){
            query += " AND ("
            types.each {
                query += "TYPE:\"${it}\" OR "
            }
            query = "${query[0..query.size() - 4]})"
        }        
        ////println query
        def nodes = searchService.query(
            ticket,
            new StoreRef("workspace://SpacesStore"),
            SearchService.QueryLanguage.lucene,
            query,
            AlfrescoUtils.getPropertiesForUserSearch(),
            true,
            true,
            true,
            true,
            AlfrescoUtils.getPermissionsForUserSearch(),
            maxResults,
            AlfrescoUtils.getSortDefinitions());

        return nodesToList(nodes)
    }

    def findChildren(ticket,parentFolderID,text){

        def maxResults = 100
        String query = ''
        
        //if(text) query +=  "TEXT:\"*${text}*\" OR NAME:\"*${text}*\""
        if(text) query = "TEXT:\"${text}\""
        //if(parentFolderID) query = "PARENT:\"${parentFolderID}\" AND ${query}"
        
        //println query
        def nodes = searchService.query(
            ticket,
            new StoreRef("workspace://SpacesStore"),
            SearchService.QueryLanguage.lucene,
            query,
            AlfrescoUtils.getPropertiesForUserSearch(),
            true,
            true,
            true,
            true,
            AlfrescoUtils.getPermissionsForUserSearch(),
            maxResults,
            AlfrescoUtils.getSortDefinitions());

        return nodesToList(nodes)
    }

    
    /**
     * Guardar el archivo proporcionado (ruta local) en el repositorio.
     *
     */
    String storeFile(ticket,parentID,fileName,fileDescription, localFilePath) {
        def StoreRef store = new StoreRef('workspace://SpacesStore')
        NodeRef file
        NodeRef parentNode = new NodeRef(parentID)
        Map<QName,Serializable> props = new HashMap<QName,Serializable>(2)
        props.put(ContentModel.PROP_NAME, fileName);
        props.put(ContentModel.PROP_TITLE, fileName);
        props.put(ContentModel.PROP_ARCHIVED_DATE, new Date());        
        props.put(ContentModel.PROP_DESCRIPTION, fileDescription);

        file = nodeService.createFile(ticket,"test",parentNode,props,localFilePath)
        nodeService.addAspect(ticket,file,ContentModel.ASPECT_TITLED)        

        return file
    }

    /**
     * Guarda el archivo proporcionado (ruta local) en el repositorio.
     * Intentará usar / crear la carpeta folderName en la home del usuario.
     *
     **/
    String storeFileInUserHome(ticket,userName,folderName,localFilePath) {
        def StoreRef store = new StoreRef('workspace://SpacesStore')
        NodeRef usrNode = getUser(ticket,userName)
        NodeRef file

        Map<QName,Serializable> props = new HashMap<QName,Serializable>(2)
        props.put(ContentModel.PROP_NAME, folderName);
        props.put(ContentModel.PROP_DESCRIPTION, "Creada desde Grails");
        NodeRef parentNode = getUserHome(ticket,userName)

        file = nodeService.createFile(ticket,"test",parentNode,props,localFilePath)
        return file
    }

    /**
     *
     *
     */
    void editNode(ticket, nodeRef, newName, newDescription){
        if(!(nodeRef instanceof NodeRef)){
            nodeRef = new NodeRef(nodeRef)
        }
        Map<QName,Serializable> props = new HashMap<QName,Serializable>(2)
        props.put(ContentModel.PROP_NAME, newName);
        props.put(ContentModel.PROP_TITLE, newName);
        props.put(ContentModel.PROP_DESCRIPTION, newDescription);

        nodeService.setProperties(ticket,nodeRef,props)
    }
	
	
    String createFolder(ticket, folderName, folderDesc, parentNodeRef){
        //GStrings to Strings
        String fName = folderName.toString()
        String fDesc = folderDesc.toString()

        def StoreRef store = new StoreRef('workspace://SpacesStore')

        Map<QName,Serializable> props = new HashMap<QName,Serializable>(2)
        props.put(ContentModel.PROP_NAME, fName);
        props.put(ContentModel.PROP_DESCRIPTION, fDesc);
        if(!(parentNodeRef instanceof NodeRef)){
            parentNodeRef = new NodeRef(parentNodeRef)
        }

        NodeRef fld = nodeService.createFolder(ticket,fName,parentNodeRef,props)
        "${fld}"
    }

    /**
     * Change username
     **/
    def editUser(ticket,userName, newUserName){
        Map<QName,Serializable> props = new HashMap<QName,Serializable>()
        props[ContentModel.PROP_USERNAME] = newUserName
        NodeRef usr = getUser(ticket,userName)
        if(usr){
            peopleService.setPersonProperties(ticket,usr,props)
        }
    }


    /**
     * Add a user to a group
     */
    def addUserToGroup(ticket,userName,groupName, createGroupIfNotExists = false){
        NodeRef usr = getUser(ticket,userName)
        NodeRef grp = getGroup(ticket, groupName,createGroupIfNotExists)
        if(usr && grp){
            peopleService.addAuthority(ticket,grp,usr)
        }
    }
    /**
    /* Create a new user and put in in a new group
     **/
    String createUser(ticket, userName, password, groupName, createGroupIfNotExists = false){        
        NodeRef usr = getUser(ticket,userName)
        def props = [:]
        if(!usr){
            //Crear el usuario:
            usr = peopleService.createPerson(ticket,userName,password.toCharArray(),props)
            //Añadirlo al grupo:
            addUserToGroup(ticket,userName,groupName,createGroupIfNotExists)
        }
        else{     
            throw new AlfrescoIntegrationException("Username already existing \"${userName}\": ${usr}")
        }
        return usr
    }

    void deleteNode(ticket, nodeRef){
        if(!(nodeRef instanceof NodeRef)){
            nodeRef = new NodeRef(nodeRef)
        }
        try{
            nodeService.deleteNode(ticket,nodeRef)
        }
        catch(Exception x){
            log.error x.message
        }
    }
    /**
    /* delete the user
     **/
    void deleteUser(ticket, userName, deleteHome=true){
        NodeRef usr = getUser(ticket,userName)
        if(usr){
            try{
                def home = getUserHome(ticket,userName)
                peopleService.deletePerson(ticket,usr)
                deleteNode(ticket,home)
            }
            catch(Exception x){
                log.error x.message
            }
        }
    }

    def getActions(ticket, contentID){
        //println "Acciones para $contentID:"
        def l = actionService.getActions(ticket,new NodeRef(contentID))
        //println l
        return l
    }

    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // PRIVATE
    // :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


    /**
    /* Find the user
     **/
    private NodeRef getUser(ticket,userName){
        NodeRef usr
        try{
            usr = peopleService.getPerson(ticket,userName)
        }
        catch(Exception x){
            //log.error x.message
        }
        return usr
    }

    /**
    /* Gets the group "groupName", and if it does not exits and "create"=true, then create a new one
     **/
    private NodeRef getGroup(ticket,groupName,create){
        NodeRef grp
        try{
            grp = peopleService.getGroup(ticket,groupName)
        }
        catch(com.rivetlogic.core.cma.exception.AuthorityNotFoundException e){
            log.error(e.message)
        }
        if(!grp){
            if(create){
                //println "Creando grupo ${groupName}"
                grp = peopleService.createGroup(ticket,groupName)
            }
            else{
                throw new AlfrescoIntegrationException("El grupo ${groupName} no existe, y no se ha solicitado la creación automática.")
            }
        }
        return grp
    }


    private nodesToList(nodes){
        def res = []
        def item
        nodes.each{
            item = [:]
            item['id'] = "${it}"
            it.properties.each{k,v ->
                item."${k.localName}" = v
            }
            res << item
        }
        //println res
        return res
    }

}
