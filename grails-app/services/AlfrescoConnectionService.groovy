class AlfrescoConnectionService {

    private static AlfrescoServer currentInstance

    boolean transactional = true

    /**
     * Devuelve la primera instancia activa del servicio.
     * TODO: Utilizar esta clase si en algún momento se desea implementar
     * un pseudo pool de servidores, o utilizar distintos servidores para
     * tareas específicas.
     */
    def getDefaultServer() {
        if(!currentInstance){
            //Look for the first server that is defined online
            currentInstance = AlfrescoServer.findByOnline(true)
            println("Starting alfresco server: ${currentInstance}")
        }
        return currentInstance
    }
	
	def getServer(String servername, String username) {
		return AlfrescoServer.findByUsernameAndName(username, servername)
	}

}

