<import resource="classpath:alfresco/extension/templates/webscripts/de/fme/jsconsole/listscripts.get.js">

var addr  = url.templateArgs['addr'],
    removed  = false;
logger.log("Remove at " + addr);

if (addr) {
    if (addr.indexOf("workspace/SpacesStore") === 0) {
        var nodeRef = "workspace://SpacesStore" + addr.replace("workspace/SpacesStore", ""),
            node = search.findNode(nodeRef);
        if (node) {
            removed = node.remove();
        }
    } else  {
        var file  = new Packages.java.io.File('/' + addr);
        if (file.exists()) {
            removed = file.delete();
        }
    }
}

if (!removed) {
    status.setCode(status.STATUS_BAD_REQUEST, "Unabled to remove file at " + addr);
    status.redirect = true;
} else {
    findAvailableScripts();
}
