This plugin will hopefully help you to integrate Alfresco 3.4 into your grails Application. Note that I have tested it only to integrate repository documents.
Also i commit to sustain the plugin until the end of my project (middle 2012). After, future is unclear. Don't hesistate to collaborate.


As of now, the plugin as committed does not work. I am doing the necessary modifications to have it working out of the scope of my project. Stay tuned.

# Introduction

In order to install the plugin do
grails install-plugin alfresco-grails //TODO provide definitive link, this one does not work yet

After installation you can create an alfresco server connector on http://host:port/myapp/alfrescoServer

As soon as done, you can use the tag lib `<alfresco:homeTree node="workspace://SpaceNodes/your-node-here">` to render the folders and documents inside the node

you can use also `<alfresco:spaceTree node="">`

# CSS Considerations

TODO

# Workflows

TODO
