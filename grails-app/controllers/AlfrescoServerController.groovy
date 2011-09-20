

class AlfrescoServerController {
	
	def scaffold=true
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ alfrescoServerInstanceList: AlfrescoServer.list( params ), alfrescoServerInstanceTotal: AlfrescoServer.count() ]
    }

    def show = {
        def alfrescoServerInstance = AlfrescoServer.get( params.id )

        if(!alfrescoServerInstance) {
            flash.message = "AlfrescoServer not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ alfrescoServerInstance : alfrescoServerInstance ] }
    }

    def delete = {
        def alfrescoServerInstance = AlfrescoServer.get( params.id )
        if(alfrescoServerInstance) {
            try {
                alfrescoServerInstance.delete()
                flash.message = "AlfrescoServer ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "AlfrescoServer ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "AlfrescoServer not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def alfrescoServerInstance = AlfrescoServer.get( params.id )

        if(!alfrescoServerInstance) {
            flash.message = "AlfrescoServer not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ alfrescoServerInstance : alfrescoServerInstance ]
        }
    }

    def update = {
        def alfrescoServerInstance = AlfrescoServer.get( params.id )
        if(alfrescoServerInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(alfrescoServerInstance.version > version) {
                    
                    alfrescoServerInstance.errors.rejectValue("version", "alfrescoServer.optimistic.locking.failure", "Another user has updated this AlfrescoServer while you were editing.")
                    render(view:'edit',model:[alfrescoServerInstance:alfrescoServerInstance])
                    return
                }
            }
            alfrescoServerInstance.properties = params
            if(!alfrescoServerInstance.hasErrors() && alfrescoServerInstance.save()) {
                flash.message = "AlfrescoServer ${params.id} updated"
                redirect(action:show,id:alfrescoServerInstance.id)
            }
            else {
                render(view:'edit',model:[alfrescoServerInstance:alfrescoServerInstance])
            }
        }
        else {
            flash.message = "AlfrescoServer not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def alfrescoServerInstance = new AlfrescoServer()
        alfrescoServerInstance.properties = params
        return ['alfrescoServerInstance':alfrescoServerInstance]
    }

    def save = {
        def alfrescoServerInstance = new AlfrescoServer(params)
        if(!alfrescoServerInstance.hasErrors() && alfrescoServerInstance.save()) {
            flash.message = "AlfrescoServer ${alfrescoServerInstance.id} created"
            redirect(action:show,id:alfrescoServerInstance.id)
        }
        else {
            render(view:'create',model:[alfrescoServerInstance:alfrescoServerInstance])
        }
    }
}
