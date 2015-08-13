var prepareOutput = function prepareOutput(folder) {
    var scriptlist = [],
        children = folder.children;

    for (c in children) {
        var node = children[c];

        if (node.isContainer) {
            scriptlist.push({
                text: node.name,
                submenu: {
                    id: node.properties["sys:node-uuid"], itemdata: prepareOutput(node)
                }
            });
        }
        else {
            scriptlist.push({text: node.name, value: node.nodeRef});
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
        }
        buildResNode({path: s.path, url: s.url}, node[e]);
    }


};

var createResEntry = function createResEntry(n, s) {
    var ent = {text: n};
    if (typeof s === "string") {
        ent.value = s;
    } else {
        var id = [];
        for (var att in s) {
            if (s.hasOwnProperty(att)) {
                id.push(createResEntry(att, s[att]));
            }

            ent.submenu = {
                id: n,
                itemdata: id
            };

        }

    }
    return ent;

};

var addResourceScripts = function addResourceScripts() {
    var tree = {},
        scriptlist = [],
        sRes = jsConsoleResources.getScriptResources().map(function (s) {
            var js = ''+s;// != new String(s) != s;
            return {path: js.replace(/.*!\//, "").split("/"), url: js};
        });

    for (c in sRes) {
        buildResNode(sRes[c], tree);
    }
    for (var att in tree) {
        if (tree.hasOwnProperty(att)) {
            scriptlist.push(createResEntry(att, tree[att]));
        }

    }
    return scriptlist;
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
