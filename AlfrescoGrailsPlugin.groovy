class AlfrescoGrailsPlugin {
// the plugin version
def version = "0.6"
// the version or versions of Grails the plugin is designed for
def grailsVersion = "1.3 > *"
// the other plugins this plugin depends on
def dependsOn = [:]

// resources that are excluded from plugin packaging
def pluginExcludes = [
"grails-app/views/error.gsp",
"grails-app/views/test/**"
]

def author = "INECO-TIFSA, Francois-Xavier Thoorens"
def authorEmail = "fx.thoorens@gmail.com"
def title = "Plugin to integrate alfresco 3.4 into your grails application"
def description = '''\\
Integrate Alfresco in your grails Application. You need to install RAAr package to your alfresco server in order to expose the cma API the plugin use.
It is based on the original alfresco grails plugin, but considerably updated to work with alfresco 3.4
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/fix/grails-alfresco-plugin"

    def doWithSpring = {
	cmaUnmarshaller(com.rivetlogic.core.cma.mapping.impl.CmaCastorUnmarshaller)

	cmaMappingService(com.rivetlogic.core.cma.mapping.impl.CmaMappingServiceImpl){bean ->
		bean.initMethod = "init"
		mappingFile="castor/mapping/mappingservice.mapping.xml"
		mappingConfiguration="castor/mapping/mappingservice.configuration.xml"
		cmaUnmarshaller=cmaUnmarshaller
        }


	restExecuter(com.rivetlogic.core.cma.rest.impl.RestExecuterImpl){bean ->
		bean.initMethod = "init"
		cmaUnmarshaller=cmaUnmarshaller
        }

	actionService(com.rivetlogic.core.cma.impl.ActionServiceImpl){
		serviceUri="/cma/actionservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	authenticationService(com.rivetlogic.core.cma.impl.AuthenticationServiceImpl){
		serviceUri="/cma/authenticationservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	classificationService(com.rivetlogic.core.cma.impl.ClassificationServiceImpl){
		serviceUri="/cma/classificationservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }


	contentService(com.rivetlogic.core.cma.impl.ContentServiceImpl){
		serviceUri="/cma/contentservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	dictionaryService(com.rivetlogic.core.cma.impl.DictionaryServiceImpl){
		serviceUri="/cma/dictionaryservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	nodeService(com.rivetlogic.core.cma.impl.NodeServiceImpl){
		serviceUri="/cma/nodeservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	libraryService(com.rivetlogic.core.cma.impl.LibraryServiceImpl){
		serviceUri="/cma/libraryservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	mimetypeService(com.rivetlogic.core.cma.impl.MimetypeServiceImpl){
		serviceUri="/cma/mimetypeservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	peopleService(com.rivetlogic.core.cma.impl.PeopleServiceImpl){
		serviceUri="/cma/peopleservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	searchService(com.rivetlogic.core.cma.impl.SearchServiceImpl){
		serviceUri="/cma/searchservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	securityService(com.rivetlogic.core.cma.impl.SecurityServiceImpl){
		serviceUri="/cma/securityservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	transformationService(com.rivetlogic.core.cma.impl.TransformationServiceImpl){
		serviceUri="/cma/transformationservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	versionService(com.rivetlogic.core.cma.impl.VersionServiceImpl){
		serviceUri="/cma/versionservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }

	workflowService(com.rivetlogic.core.cma.impl.WorkflowServiceImpl){
		serviceUri="/cma/workflowservice"
		restExecuter=restExecuter
		cmaMappingService=cmaMappingService
        }
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}

