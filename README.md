This plugin will hopefully help you to integrate Alfresco 3.4 into your grails Application. Note that I have tested it only to integrate repository documents.
Also i commit to sustain the plugin until the end of my project (middle 2012). After, future is unclear. Don't hesistate to collaborate.


As of now, the plugin as committed does not work. I am doing the necessary modifications to have it working out of the scope of my project. Stay tuned.

## Introduction
In order to install the plugin do
`grails install-plugin alfresco-grails` //TODO provide definitive link, this one does not work yet

## Alfresco server configuration
The plugin is using Remote Alfresco API rivet (http://wiki.rivetlogic.com/display/RAAR/Home) to communicate with Alfresco. To enable it on your Alfresco, you need to deploy the amp file downloaded from here: http://wiki.rivetlogic.com/display/RAAR/Downloads. Choose the version according to your server installation (Community or Enterprise).

`cd /path/to/alfresco/bin`

`java -jar alfresco-mgt.jar install cma-alfresco-your_version.amp`

After installation you can create an alfresco server connector on http://host:port/myapp/alfrescoServer.
As soon as done, you can use the tag lib `<alfresco:homeTree node="workspace://StoresSpace/your-node">` to render the folders and documents inside the node `your-node`.

you can use also `<alfresco:spaceTree user="the_username">` to render personal repoitory of `the_username`.

## CSS Considerations
There are 3 classes in order to customize tree styling:

* `<li class="file">` rendering a file in the tree
* `<li class="closedNode">` rendering a closed folder in the tree
* `<li class="openNode">` rendering an open folder in the tree

## Workflows
TODO

## Roadmap
* Move AlfrescoServer to a non static context, to allow several instances concurrently
* Create a default css to style the tree (using famfamfam plugin)
* Separate rendering of the Workflow from the Tree
* Create a document view/taglib
