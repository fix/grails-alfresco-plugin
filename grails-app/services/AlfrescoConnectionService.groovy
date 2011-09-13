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
            //Buscamos el primer servidor online, ordenados por peso asc.
            currentInstance = AlfrescoServer.findByOnline(
                true,
                [sort:'weight',order:'asc']
            )
            println("Starting alfresco server: ${currentInstance}")
        }
        return currentInstance
    }

}

