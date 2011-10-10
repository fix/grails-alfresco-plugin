package org.grails.plugin.alfresco
import es.ineco.grails.alfresco.*
import org.alfresco.service.namespace.*
import org.alfresco.service.cmr.repository.*

class AlfrescoWorkflowController {
    def alfrescoUsersService
    def alfrescoWorkflowService
    def nodeService
    
    
    /**
    Env�a la se�al para ejecutar la transici�n elegida.
    **/
    def signal = {    	
    	if(params.pathId && params.transitionId && params.ticket && params.nextUrl){
    		def ticket = AlfrescoUtils.unSerializeTicket(params.ticket)
    		alfrescoWorkflowService.signal(ticket,params.pathId, params.transitionId)    		
    	}    	
    	else{
    		flash.message = "Faltaban parametros: ${params}"
    	}
    	redirect(url:params.nextUrl)
    }
    
    /**
    Comienza la ejecuci�n del workflow
    **/
    def startWorkflow = {
    	//flash.message = "${params}"
    	if(params.ticket && params.nextUrl && params.workflowId && params.nodeRef){
    		def ticket = AlfrescoUtils.unSerializeTicket(params.ticket)
    		def nodeRef = new NodeRef(params.nodeRef)
    		def asignee = ticket.userNodeRef    		    		
    		
    		alfrescoWorkflowService.startWorkflow(ticket,params.workflowId, asignee, nodeRef)
    	}
    	else{
    		flash.message = "Faltaban parametros: $params"
    	}    	
    	redirect(url:params.nextUrl)
    }
    
    
    
}