This plugin will hopefully help you to integrate Alfresco 3.4 into your grails Application. Note that I have tested it only to integrate repository documents.
Also i commit to sustain the plugin until the end of my project (middle 2012). After, future is unclear. Don't hesistate to collaborate.


As of now, the plugin as committed does not work. I am doing the necessary modifications to have it working out of the scope of my project. Stay tuned.

## Introduction
In order to install the plugin do

    grails install-plugin https://github.com/fix/grails-alfresco-plugin/raw/master/grails-alfresco-0.5.zip //TODO provide definitive link, this one does not work yet

## Alfresco server configuration
The plugin is using Remote Alfresco API rivet (http://wiki.rivetlogic.com/display/RAAR/Home) to communicate with Alfresco. To enable it on your Alfresco, you need to deploy the amp file downloaded from here: http://wiki.rivetlogic.com/display/RAAR/Downloads. Choose the version according to your server installation (Community or Enterprise).

    cd /path/to/alfresco/bin
    java -jar alfresco-mgt.jar install cma-alfresco-your_version.amp
    
## Registering different instances of Alfresco Servers
After installation you can create an alfresco server connector on http://host:port/myapp/alfrescoServer.
You can create several AlfrescoServer pointing on the same url with different username/password settings. The most important thing is to give a unique pair name/username. For instance you can have several servers with the same name, but with different usernames.

As soon as done, you can use the tag lib `<alfresco:spaceTree username="registered_username" servername="registered_name" node="workspace://StoresSpace/your-node">` to render the folders and documents inside the node `your-node`. Of course the rendering depends on the right of the username on the server.

You can use also `<alfresco:homeTree username="registered_username" servername="registered_name">` to render personal repoitory of `registered_username`.


## How it works
Basically when you use the taglib, it will try to use the ticket stored in the AlfrescoServer Instance. If not valid, it will open a new session and save the new ticket in the AlfrescoServer Instance again.

## CSS Considerations
The taglib renders the node as a tree using \<ul\> \<li\> tags. There are 3 classes in order to customize tree styling:

* `<li class="file">` rendering a file in the tree
* `<li class="closedNode">` rendering a closed folder in the tree
* `<li class="openNode">` rendering an open folder in the tree

you can use famfamfam plugin to render it nicely. As an example here are my custom CSS, that may be included in future release. My div class surrounding the tree is called "repo-list":

```css
.repo-list li{
    margin:4px;
	margin-left:15px;
	list-style-type: none;
}

.repo-list{
	padding-top:20px;
	padding-bottom:20px;
	padding-left: 30px;
}

.openNode {
	list-style-image: url('/projectname/plugins/famfamfam-1.0.1/images/icons/folder.png')
}

.closedNode {
	list-style-image: url('/projectname/plugins/famfamfam-1.0.1/images/icons/folder.png')
}

.file {
	list-style-image: url('/projectname/plugins/famfamfam-1.0.1/images/icons/page.png')
}

```

## Workflows
TODO

## Roadmap
* Create a default css to style the tree (using famfamfam plugin?)
* Separate rendering of the Workflow from the Tree
* Create a document view/taglib
