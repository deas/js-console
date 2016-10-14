<#include "../../../../org/alfresco/components/component.head.inc">

<#assign codemirror_base = page.url.context + "/res/fme/components/jsconsole/codemirror"/>
<#--assign codemirror_base = page.url.context + "/res/bower_components/codemirror"/-->
<#assign tern_url =  page.url.context + "/res/fme/components/jsconsole/codemirror/addon/tern/defs/alfresco-json.js"/>
<#-- assign tern_url =  page.url.context + "/proxy/alfresco/javascript-console/tern-definitions/alfresco-script-api"/-->


<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/fme/components/jsconsole/javascript-console.css" />
<@script type="text/javascript" src="${page.url.context}/res/components/console/consoletool.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/javascript-console-wip.js"></@script>
<@link rel="stylesheet" type="text/css" href="${page.url.context}/res/modules/documentlibrary/global-folder.css" />
<@script type="text/javascript" src="${page.url.context}/res/modules/documentlibrary/global-folder.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/beautify.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/yui/resize/resize.js"></@script>

<#--link href=//fonts.googleapis.com/css?family=Source+Code+Pro:300" rel="stylesheet" type="text/css"/-->
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/common/map.js"></@script>


<!-------------------------->
<!-- Codemirror-->
<!-------------------------->
<@script type="text/javascript" src="${codemirror_base}/lib/codemirror.js"></@script>
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/lib/codemirror.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/default.css" />
<@script type="text/javascript" src="${codemirror_base}/keymap/extra.js"></@script>

<!-------------------------->
<!-- Codemirror Addons-->
<!-------------------------->

<!-- display addons -->
<@script type="text/javascript" src="${codemirror_base}/addon/display/placeholder.js"></@script>

<!-- edit addons -->
<@script type="text/javascript" src="${codemirror_base}/addon/edit/trailingspace.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/edit/matchbrackets.js"></@script>

<!-- hint addons-->
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/addon/hint/show-hint.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/addon/hint/show-hint-eclipse.css" />
<#--@script type="text/javascript" src="${codemirror_base}/addon/hint/show-hint.js"></@script-->
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/addon/hint/show-context-info.css" />
<@script type="text/javascript" src="${codemirror_base}/addon/hint/show-context-info.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/hint/templates-hint.js"></@script>
<@link rel="stylesheet" media="screen" type="text/css" href="${codemirror_base}/addon/hint/templates-hint.css" />
<@script type="text/javascript" src="${codemirror_base}/addon/hint/javascript/javascript-templates.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/hint/javascript/alfresco-templates.js"></@script>

<!-- hover addons-->
<@script type="text/javascript" src="${codemirror_base}/addon/hover/text-hover.js"></@script>
<@link rel="stylesheet" media="screen" type="text/css" href="${codemirror_base}/addon/hover/text-hover.css" />

<!-- hyperlink addons-->
<@script type="text/javascript" src="${codemirror_base}/addon/hyperlink/hyperlink.js"></@script>
<@link rel="stylesheet" media="screen" type="text/css" href="${codemirror_base}/addon/hyperlink/hyperlink.css" />

<!-- lint addons-->
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/jsonlint/jsonlint.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/jsonlint/json-formatter.js"></@script>
<@link rel="stylesheet" media="screen" type="text/css" href="${codemirror_base}/addon/lint/lint.css" />
<@script type="text/javascript" src="${codemirror_base}/addon/lint/lint.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/lint/json-lint.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/lint/javascript-lint.js"></@script>

<!-- search addons-->
<@script type="text/javascript" src="${codemirror_base}/addon/search/match-highlighter.js"></@script>
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/addon/search/match-highlighter.css" />
<@script type="text/javascript" src="${codemirror_base}/addon/search/searchcursor.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/search/search.js"></@script>
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/addon/search/matchesonscrollbar.css" />
<@script type="text/javascript" src="${codemirror_base}/addon/search/matchesonscrollbar.js"></@script>

<!-- selection addons-->
<@script type="text/javascript" src="${codemirror_base}/addon/selection/active-line.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/selection/mark-selection.js"></@script>

<!-- mode-->
<@script type="text/javascript" src="${codemirror_base}/mode/javascript/javascript.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/mode/htmlmixed/htmlmixed.js"></@script>
<@link rel="stylesheet" media="screen" type="text/css" href="${codemirror_base}/mode/freemarker/freemarkercolors.css" />

 <!-- Acorn / Tern JS -->
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/acorn/acorn.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/acorn/acorn_loose.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/acorn/util/walk.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/tern/lib/signal.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/tern/lib/tern.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/tern/lib/def.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/tern/lib/comment.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/ternjs/tern/lib/infer.js"></@script>

<!-- CodeMirror addon with Tern JS -->
<@link rel="stylesheet" media="screen" type="text/css" href="${codemirror_base}/addon/tern/tern.css" />
<@script type="text/javascript" src="${codemirror_base}/addon/tern/tern.js"></@script>

<!-- codemirror extension for ternjs by angelo zerr-->
<@link rel="stylesheet" media="screen" type="text/css" href="${codemirror_base}/addon/tern/tern-extension.css" />
<@script type="text/javascript" src="${codemirror_base}/addon/tern/tern-extension.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/tern/tern-hover.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/tern/tern-hyperlink.js"></@script>

<!-- tern definitions for ecma5 and alfresco-->
<@script type="text/javascript" src="${codemirror_base}/addon/tern/defs/ecma5.json.js"></@script>
<@script type="text/javascript" src="${tern_url}"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/tern/defs/alfresco-webscripts-tern.js"></@script>
<@script type="text/javascript" src="${codemirror_base}/addon/tern/defs/alfresco-batchprocessing-tern.js"></@script>


<!-- codemirror ui-->
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/codemirror-ui/js/codemirror-ui.js"></@script>
<@link rel="stylesheet" media="screen" type="text/css" href="${page.url.context}/res/fme/components/jsconsole/codemirror-ui/css/codemirror-ui.css" />
<@script type="text/javascript" src="${page.url.context}/res/fme/components/jsconsole/codemirror-ui/js/codemirror-ui-find.js"></@script>
<@link rel="stylesheet" media="screen" type="text/css" href="${page.url.context}/res/fme/components/jsconsole/codemirror-ui/css/codemirror-ui-find.css" />


<!-- codemirror themes -->
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/neat.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/elegant.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/erlang-dark.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/night.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/monokai.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/cobalt.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/eclipse.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/rubyblue.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/lesser-dark.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/xq-dark.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/ambiance.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/blackboard.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/vibrant-ink.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/solarized.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/twilight.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/zenburn.css" />
<@link rel="stylesheet" type="text/css" href="${codemirror_base}/theme/neo.css" />
