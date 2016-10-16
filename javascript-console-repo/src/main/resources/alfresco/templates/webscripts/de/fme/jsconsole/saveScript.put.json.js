<import resource="classpath:alfresco/extension/templates/webscripts/de/fme/jsconsole/listscripts.get.js">

var createFile = function createFile(parent, path) {
    var name = path.shift();
    if (path.length > 0) {
        return createFile(parent.childByNamePath(name) || parent.createFolder(name), path);
    }
    return parent.createFile(name);
};

var saveScript = function saveScript() {
    var scriptNode = null,
        addr = args.addr;
    if (addr) { // Update node or save to url
        if (addr.indexOf("workspace://") == 0) {
            scriptNode = search.findNode(args.addr);
        } else {
            jsConsoleResources.saveResource(addr, json.get('jsScript'));
        }
    } else { // Create in repo
        if (args.name.indexOf("file:/") === 0) { // Quick hack
            jsConsoleResources.saveResource(args.name, json.get('jsScript'));
        } else {
            var xpath = jsConsoleResources.getRepoXPaths()[0],
                scriptFolder = search.xpathSearch(xpath)[0];
            if (!scriptFolder) {
                throw('No script folder at ' + xpath);
            }
            scriptNode = createFile(scriptFolder, ('' + args.name).split(/\//));
        }
    }
    if (scriptNode) {
        scriptNode.content = json.get('jsScript');
        scriptNode.properties["jsc:freemarkerScript"].content = json.get('fmScript');
        scriptNode.save();
    }
};

saveScript();

// from listScripts.get.js
findAvailableScripts();
