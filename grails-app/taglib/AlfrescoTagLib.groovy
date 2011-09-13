import org.alfresco.model.*
import es.ineco.grails.alfresco.*;

class AlfrescoTagLib {
    static namespace = 'alfresco'
    def alfrescoUsersService
    def alfrescoConnectionService
    def alfrescoWorkflowService
    def contentService


    /**
     *
     * Genera un árbol con el contenido de la carpeta seleccionada.
     */
    def spaceTree = {attrs, body ->
        //1. Validar entrada
        def parentID = attrs.node
        def cssClass = attrs.cssClass

        if(!parentID){
            out << "<div class='error'>Falta el id de la carpeta a mostrar.</div>"
            return
        }
        if(!cssClass){
            out << "<div class='error'>Falta el estilo css para el contenedor</div>"
            return
        }

        //2. Si no existe la lista de nodos abiertos la creamos.
        if(!session.openNodes){
            session.openNodes = []
        }

        //3. Abrimos o cerramos los nodos solicitados.
        if(params.openNode && !session.openNodes.contains(params.openNode))
        session.openNodes<<params.openNode

        if(params.closeNode) session.openNodes.remove(params.closeNode)

        def baseURL= request.requestURL.toString()

        //4. Cargamos los datos desde Alfresco
        def ticket = alfrescoUsersService.openSession()        
        def fList = alfrescoUsersService.getFolders(ticket,parentID)
        out << "<div  class='${cssClass}'>"
        out << "<ul>"
        fList.each {
            //5. Para cada carpeta, invocamos la función recursiva.
            showFld(ticket,it, baseURL)
        }
        def files = alfrescoUsersService.getFiles(ticket,parentID)
        //Usamos ord para generar ids unicos en los formularios.
        def ord = System.currentTimeMillis()
        files.each{
            showFile(ticket,it,baseURL, ++ord)
        }
        out << "</ul>"
        out << "</div>"
        //alfrescoUsersService.closeSession(ticket)
    }

    /*
     *Genera una lista con el contenido de la carpeta del usuario.
     */
    def homeTree = {attrs, body ->
        //1. Validar entrada
        def userName = attrs.user
        def cssClass = attrs.cssClass

        if(!userName){
            out << "<div class='error'>Falta el nombre de usuario</div>"
            return
        }
        if(!cssClass){
            out << "<div class='error'>Falta el estilo css para el contenedor</div>"
            return
        }

        //2. Si no existe la lista de nodos abiertos la creamos.
        if(!session.openNodes){
            session.openNodes = []
        }

        //3. Abrimos o cerramos los nodos solicitados.
        if(params.openNode && !session.openNodes.contains(params.openNode))
        session.openNodes<<params.openNode

        if(params.closeNode) session.openNodes.remove(params.closeNode)

        def baseURL= request.requestURL.toString()        

        //4. Cargamos los datos desde Alfresco
        def ticket = alfrescoUsersService.openSession()
        def homeFolder = alfrescoUsersService.getUserHome(ticket,userName).toString()
        def fList = alfrescoUsersService.getFolders(ticket,homeFolder)
        out << "<div  class='${cssClass}'>"        
        out << "<ul>"
        fList.each {
            //5. Para cada carpeta, invocamos la función recursiva.
            showFld(ticket,it, baseURL)
        }
        def files = alfrescoUsersService.getFiles(ticket,homeFolder)
        def ord = System.currentTimeMillis()
        files.each{
            showFile(ticket,it,baseURL,++ord)
        }
        out << "</ul>"
        out << "</div>"
        //alfrescoUsersService.closeSession(ticket)
    }

    /**
    Función recursiva para mostrar los nodos del árbol.
     **/
    private showFld(ticket, node, baseURL){
    	//println node
        def actions = buildActions(ticket,node)
        def urlSepChar = baseURL.contains('?')?'&':'?'
        if(session.openNodes.contains(node.id.toString())){
            out << "<li class='openNode'><a href='${baseURL}${urlSepChar}closeNode=${node.id.encodeAsURL()}' title='${node.name}'>${node.name} ${actions}</a>"
            out << "<ul>"
            def subFlds = alfrescoUsersService.getFolders(ticket,node.id)
            subFlds.each{
                showFld(ticket,it, baseURL)
            }
            def files = alfrescoUsersService.getFiles(ticket,node.id)
            def ord = System.currentTimeMillis()
            files.each{
                showFile(ticket,it,baseURL,++ord)
            }
            out << "</ul>"
            out << "</li>"
        }
        else{
            out << "<li class='closedNode'><a href='${baseURL}${urlSepChar}openNode=${node.id.encodeAsURL()}' title='${node.name}'>${node.name} ${actions}</a></li>"
        }
    }

    private showFile(ticket, node, baseURL, ord){

        //Ver en el navegador:
        //http://[host]/alfresco/d/d/workspace/SpacesStore/[id nodo]/[nombre archivo]
        //Download:                |
        //http://[host]/alfresco/d/a/workspace/SpacesStore/[id nodo]/[nombre archivo]

        def alf = alfrescoConnectionService.getDefaultServer()
        def url = "${alf.serverUrl}/alfresco/d/d/${node.id.replaceAll('://','/')}/${node.name}"
        def workflows = alfrescoWorkflowService.getWorkflows(ticket)
        def serializedTicket = AlfrescoUtils.serializeTicket(ticket)
        
        def li = "<li class='file'>"
        
        //1. Formulario para iniciar un workflow sobre este nodo.
        li += " <form action='${createLink(controller:'alfrescoWorkflow',action:'startWorkflow')}' name='alf_workflow_start_${ord}'>"
        li += " ${node.name} <a onclick=\"window.open(this.href);return false;\" href='${url}'>[Download]</a>"
        li += " <input type='hidden' name='nextUrl' value='${request.forwardURI}'/>"
        li += " <input type='hidden' name='nodeRef' value='${node.id}'/>\n"
        li += " <input type='hidden' name='ticket' value='${serializedTicket}'/>\n"
        
        //li += " <select name='workflowId' onchange='if(this.value.length > 0){document.alf_workflow_start_${ord}.submit();}'>"
        //li += " <option value=''>Iniciar Workflow</option>"
        //workflows.each{w ->
        //	li += "<option value='${w.id}'>${w.title}</option>"
        //}
        //li += "</select></form>"
        
        /*
        Mostramos información sobre workflows activos para este nodo, si los tiene.
        */
//        def flows = alfrescoWorkflowService.getWorkflowsForContent(ticket,node.id)
//        if(flows?.size()){
//        	li += '<div style="width:450px;border: 1px solid #ccc;background-color:#f5f5f5">Workflows: <ul>'
//        	flows.each{flow ->
//        		def initiator = alfrescoUsersService.getNodeProperties(ticket,flow.initiator)[ContentModel.PROP_USERNAME]
//        		def paths = alfrescoWorkflowService.getPaths(ticket,flow.id)
//        		
//        		li += "<li><strong>${flow.definition.title}</strong>: <br />"
//        		li += "[Descripción: <strong>${flow.description}</strong>, Activado el <strong>${flow.startDate.format('dd/MM/yyyy')}</strong> por <strong>${initiator}</strong>]"
//        		li += "<br />Acciones: "
//        		paths.each{path ->        					
//        			if(path.node?.transitions){
//        				path.node.transitions.each{transition->
//        					if(transition.id){
//	        					li += "<form method='post' action='${createLink(controller:'alfrescoWorkflow',action:'signal')}'>"
//	        					li += "<input type='hidden' name='pathId' value='${path.id}'/>"
//	        					li += "<input type='hidden' name='transitionId' value='${transition.id}'/>"
//	        					li += "<input type='hidden' name='ticket' value='${serializedTicket}'/>"
//	        					li += "<input type='hidden' name='nextUrl' value='${request.forwardURI}'/>"
//	        					li += "<input style='width:80px' type='submit' name='s' value='${transition.title}'/></form>"    						
//        					}        					
//        					else{
//        						li += "(${transition.title})"
//        					}
//        				}
//        			}
//        		}
//        		li += '</li>'
//        	}
//        	li += '</div></ul>'
//        }
        
        li += '</li>'
        out << li
    }

    private buildActions(ticket,node){
        def actions = alfrescoUsersService.getActions(ticket,node.id.toString())
        def str = ""
        actions.each {
            str += "$it, "
        }
        return str
    }

}
