import org.alfresco.service.cmr.workflow.WorkflowTaskState
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef
import org.alfresco.service.namespace.QName
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

import org.alfresco.service.cmr.workflow.*
import com.rivetlogic.core.cma.rest.api.*
import com.rivetlogic.core.cma.rest.api.RestExecuter.HttpMethod

import com.rivetlogic.core.cma.api.*
import com.rivetlogic.core.cma.exception.*
import com.rivetlogic.core.cma.impl.*
import com.rivetlogic.core.cma.repo.*
import com.rivetlogic.core.cma.mapping.*

import es.ineco.grails.alfresco.*
import org.alfresco.service.cmr.search.ResultSet
import org.alfresco.service.namespace.*

class AlfrescoWorkflowService {
    def alfrescoConnectionService
    def authenticationService
    def peopleService
    def nodeService
    def searchService
    def workflowService
    def actionService
    def cmaMappingService
    def restExecuter
    
    boolean transactional = true
    
    //La lista de workflows se cachea.
    private flows
    
    /**
    Devuelve la lista completa de workflows desplegados.
    **/
    def getWorkflows(ticket){
    	if(!flows) flows = workflowService.getDefinitions(ticket,true)
    	
    	return flows
    }
    
    /**
    Comienza la ejecución de un workflow sobre el nodeRef.
    **/
    def startWorkflow(ticket, workflowID, asignee, nodeRef){  
    	
    	// create workflow package to contain actioned upon node
    	def workflowPackage = workflowService.createPackage(ticket,null)
    	def childAssoc = nodeService.getPrimaryParent(ticket,nodeRef)
    	nodeService.addChild(ticket, workflowPackage, nodeRef,ContentModel.ASSOC_CONTAINS, childAssoc.getQName())
    	
    	// build map of workflow start task parameters
		def workflowParams = new HashMap<QName,Serializable>()
		workflowParams.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "assignee"), asignee)
    	workflowParams.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "context"), childAssoc.parentRef)    	
    	workflowParams.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package"), workflowPackage)
    	
    	println workflowParams
    	// start the workflow
    	def path = workflowService.startWorkflow(ticket, workflowID, workflowParams)       	
    	
    	// end the start task
    	def tasks = workflowService.getTaskForWorkflowPath(ticket, path.id)
    	tasks.each{task ->
    		task = workflowService.updateTask(ticket,task.id,workflowParams,null,null)
    		endTask(ticket, task.id)
    	}
    	
    }
    
    /**
    Este método está copiado del código fuente de raar, eliminando la validación de parámetros.
    Más info en: http://issues.rivetlogic.com/browse/CMA-45
    **/
	private endTask(Ticket ticket, String taskId) {

		String methodName = CmaConstants.METHOD_WORKFLOW_ENDTASK;
		// create parameters
		Vector<NameValuePair> parameters = new Vector<NameValuePair>();
		parameters.add(new NameValuePair(CmaConstants.PARAM_VERSION, CmaConstants.CMA_VERSION));
		parameters.add(new NameValuePair(CmaConstants.PARAM_SERVICE, CmaConstants.SERVICE_WORKFLOW));
		parameters.add(new NameValuePair(CmaConstants.PARAM_METHOD, methodName));
		parameters.add(new NameValuePair(CmaConstants.PARAM_ALFRESCO_TICKET, ticket.getTicket()));
		parameters.add(new NameValuePair(CmaConstants.PARAM_TASKID, taskId));
		//parameters.add(new NameValuePair(CmaConstants.PARAM_TRANSITIONID, transitionId));
		parameters.add(new NameValuePair(CmaConstants.PARAM_USETRANSACTION, true));

		def task = execute(ticket, methodName, parameters);			
		
	}
    /**
    Este método está copiado sin modificar del fuente de Raar, porque al ser privado el anterior
    no lo ve...    
    **/
	private Object execute(Ticket ticket, String methodName, Vector<NameValuePair> parameters) throws InvalidTicketException, CmaRuntimeException {
		String serviceUri="/cma/workflowservice"
		String targetUri = ticket.getRepositoryUri() + serviceUri;
		String mappingFile = cmaMappingService.getMapping(CmaConstants.CMA_VERSION,CmaConstants.SERVICE_WORKFLOW, methodName);		
		try {
			bigPrint('ENVIANDO PETICION')
			CmaResult result = restExecuter.execute(HttpMethod.POST, targetUri, mappingFile, parameters);
			bigPrint(result)
			return result.getResult();
		}		
		catch(Throwable t){
			/**
			07/08/2009 - Hay un bug en algún sitio que hace que la respuesta recibida desde Alfresco
			en esta llamada produzca una excepción. Podemos ignorarla siempre que no necesitemos
			manejar el objeto task devuelto.
			**/
			println "Ignorando: ${t}"
		}
	}    
	
	private bigPrint(txt){
		println '***********************************************************************************'
		println txt
		println '***********************************************************************************'
		
	}
    
    /**
    Devuelve la lista de workflows (http://dev.alfresco.com/resource/docs/java/repository/org/alfresco/service/cmr/workflow/WorkflowInstance.html)
    activos para el nodo proporcionado.
    **/
    def getWorkflowsForContent(ticket, nodeRef) {    	
        if(!(nodeRef instanceof NodeRef)){
            nodeRef = new NodeRef(nodeRef)
        }
        def flows = workflowService.getWorkflowsForContent(ticket, nodeRef,true)
        return flows
    }
    
    /**
    Devuelve las acciones disponibles para ese workflow
    **/
    def getPaths(ticket, workflowID){
    	def paths = workflowService.getWorkflowPaths(ticket, workflowID)	
    }
    
    def signal(ticket, pathId, transitionId){
    	def path = workflowService.signal(ticket,pathId,transitionId)
    	
    }
}
