var prepareOutput = function prepareOutput(folder) {
    var scriptlist = [],
        children = folder.children;

    for (c in children) {
        var node = children[c];

        if (node.isContainer) {
            scriptlist.push({
                text: node.name,
                canSave: true,
                submenu: {
                    // id: node.properties["sys:node-uuid"],
                    itemdata: prepareOutput(node)
                }
            });
        }
        else {
            scriptlist.push({text: node.name, value: node.nodeRef, canSave: true });
        }
    }

    return scriptlist;
};

var buildResNode = function buildResNode(s, node) {
    var e = s.path.shift();

    if (s.path.length == 0) {
        node[e] = s.url;
    } else {
        if (!node[e]) {
            node[e] = {};
            node[e] = { entries: {}, canSave: s.canSave };
        }
        buildResNode({path: s.path, url: s.url, canSave: s.canSave }, node[e].entries );
    }

};

var createResEntry = function createResEntry(n, s) {
    // logger.log("Create entry " + JSON.stringify(s, null, 2));
    var ent = { text: n, url:true, canSave: s.canSave };
    if (typeof s === "string") {
        ent.value = s;
    } else {
        var id = [],
            ents = s.entries;

        for (var att in ents) {
            if (ents.hasOwnProperty(att)) {
                id.push(createResEntry(att, ents[att] ));
            }

            ent.submenu = {
                // id: n,
                itemdata: id
            };

        }

    }
    return ent;

};

var trimPaths = function trimPaths(entry) {
    var contentFolders = [];
    if (entry.submenu) {
        var id = entry.submenu.itemdata;
        var hasContent = id.filter(function(s) { return s.value; }).length > 0;
        if  (hasContent) {
            contentFolders.push(entry);
        } else {
            for (var i=0;i<id.length; i++) {
                contentFolders = contentFolders.concat(trimPaths(id[i]));
            }
        }
    } else {
        contentFolders.push(entry);
    }
    return contentFolders;
};

var addResourceScripts = function addResourceScripts() {
    var tree = {};
        scriptlist = [],
        sRes = jsConsoleResources.getAllResources().map(function (s) {
            var js = ''+s;// != new String(s) != s;
            // file:/opt/alfresco-5.0/tomcat/shared/classes/alfresco/extension/custom-web-context.xml
            // return {path: js.replace(/.*!|.*\/classes/, "").split("/"), url: js};
            // 3 : file:/opt/alfresco-5.0/tomcat/shared/classes/alfresco/extension/contentreich-connector-context.xml
            // 4 : jar:file:/opt/alfresco-5.0/tomcat/webapps/alfresco/WEB-INF/lib/alfresco-messaging-repo-1.2.4.jar!/alfresco/extension/messaging-context.xml
            return {path: js.replace(/.*!/, "").split("/"), url: js, canSave: js.indexOf("file:/")  === 0 };
        });

    for (c in sRes) {
        buildResNode(sRes[c], tree);
    }

    for (var att in tree) {
        if (tree.hasOwnProperty(att)) {
            scriptlist.push(createResEntry(att, tree[att]));
        }

    }
    return scriptlist.reduce(function(previousValue, currentValue, index, array) {
        return previousValue.concat(trimPaths(currentValue));
    }, []);
};

var findAvailableScripts = function findAvailableScripts() {
    var scriptFolder = companyhome.childrenByXPath("app:dictionary/app:scripts")[0],
        scripts = [];

    if (scriptFolder) {
        scripts = scripts.concat(prepareOutput(scriptFolder));
    }

    scripts = scripts.concat(addResourceScripts());
    scripts.sort(function(a,b) { return a.text.localeCompare(b.text); });

    model.scripts = jsonUtils.toJSONString(scripts);
}

findAvailableScripts();
