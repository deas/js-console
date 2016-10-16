/**
 * Admin Console Javascript Console component
 */
var cfgEl = config.scoped["Contentreich"]["javascript-console"],
    ternUrl = (cfgEl && cfgEl.getChild("tern-url")) ? cfgEl.getChild("tern-url").value
        : page.url.context + "/res/fme/components/jsconsole/codemirror/addon/tern/defs/alfresco-json.js",
    codeMirrorBase = (cfgEl && cfgEl.getChild("codemirror-base")) ? cfgEl.getChild("codemirror-base").value
    : page.url.context + "/res/fme/components/jsconsole/codemirror";
// "/res/bower_components/codemirror"
// "/proxy/alfresco/javascript-console/tern-definitions/alfresco-script-api";

// Beware !
// Does not show up in .head.ftl
model.codemirror = {
    "base_url": codeMirrorBase,
    "tern_url": ternUrl
};