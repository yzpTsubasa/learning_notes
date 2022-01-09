## chrome snippets
``` js
// getting snippets
var scriptSnippets;
InspectorFrontendHost.getPreferences(data => {
    scriptSnippets = data.scriptSnippets; 
    console.log(scriptSnippets);
});
```
``` js
// setting snippets
var scriptSnippets = JSON.stringify(temp1);
InspectorFrontendHost.setPreference("scriptSnippets", scriptSnippets);
```