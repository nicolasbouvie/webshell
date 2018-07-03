function newEditor(appendTo, params) {
    function defVal(param, defVal) {
        return param !== undefined && param !== null ? param : defVal;
    }
    var options = {
        lineNumbers: defVal(params.lineNumbers, true),
        readOnly:    defVal(params.readOnly,    false),
        filename:    defVal(params.filename,    ""),
        showModes:   defVal(params.showModes,   true),
        showThemes:  defVal(params.showThemes,  true),
        showKeymap:  defVal(params.showKeymap,  false),
        onclose:     defVal(params.onclose,     function() {}),
        onsave:      defVal(params.onsave,      function () {})
    };
    var self = this;
    this.init = init;
    this.destroy = destroy;

    var priv = {
        newDropdown: newDropdown,
        selectTheme: selectTheme,
        selectMode: selectMode,
        selectKeymap: selectKeymap,
        appendControllers: appendControllers,
        modes: [{"name":"APL","mime":"text/apl","mode":"apl","ext":["dyalog","apl"]},{"name":"PGP","mimes":["application/pgp","application/pgp-encrypted","application/pgp-keys","application/pgp-signature"],"mode":"asciiarmor","ext":["asc","pgp","sig"],"mime":"application/pgp"},{"name":"ASN.1","mime":"text/x-ttcn-asn","mode":"asn.1","ext":["asn","asn1"]},{"name":"Asterisk","mime":"text/x-asterisk","mode":"asterisk","file":{}},{"name":"Brainfuck","mime":"text/x-brainfuck","mode":"brainfuck","ext":["b","bf"]},{"name":"C","mime":"text/x-csrc","mode":"clike","ext":["c","h","ino"]},{"name":"C++","mime":"text/x-c++src","mode":"clike","ext":["cpp","c++","cc","cxx","hpp","h++","hh","hxx"],"alias":["cpp"]},{"name":"Cobol","mime":"text/x-cobol","mode":"cobol","ext":["cob","cpy"]},{"name":"C#","mime":"text/x-csharp","mode":"clike","ext":["cs"],"alias":["csharp"]},{"name":"Clojure","mime":"text/x-clojure","mode":"clojure","ext":["clj","cljc","cljx"]},{"name":"ClojureScript","mime":"text/x-clojurescript","mode":"clojure","ext":["cljs"]},{"name":"Closure Stylesheets (GSS)","mime":"text/x-gss","mode":"css","ext":["gss"]},{"name":"CMake","mime":"text/x-cmake","mode":"cmake","ext":["cmake","cmake.in"],"file":{}},{"name":"CoffeeScript","mimes":["application/vnd.coffeescript","text/coffeescript","text/x-coffeescript"],"mode":"coffeescript","ext":["coffee"],"alias":["coffee","coffee-script"],"mime":"application/vnd.coffeescript"},{"name":"Common Lisp","mime":"text/x-common-lisp","mode":"commonlisp","ext":["cl","lisp","el"],"alias":["lisp"]},{"name":"Cypher","mime":"application/x-cypher-query","mode":"cypher","ext":["cyp","cypher"]},{"name":"Cython","mime":"text/x-cython","mode":"python","ext":["pyx","pxd","pxi"]},{"name":"Crystal","mime":"text/x-crystal","mode":"crystal","ext":["cr"]},{"name":"CSS","mime":"text/css","mode":"css","ext":["css"]},{"name":"CQL","mime":"text/x-cassandra","mode":"sql","ext":["cql"]},{"name":"D","mime":"text/x-d","mode":"d","ext":["d"]},{"name":"Dart","mimes":["application/dart","text/x-dart"],"mode":"dart","ext":["dart"],"mime":"application/dart"},{"name":"diff","mime":"text/x-diff","mode":"diff","ext":["diff","patch"]},{"name":"Django","mime":"text/x-django","mode":"django"},{"name":"Dockerfile","mime":"text/x-dockerfile","mode":"dockerfile","file":{}},{"name":"DTD","mime":"application/xml-dtd","mode":"dtd","ext":["dtd"]},{"name":"Dylan","mime":"text/x-dylan","mode":"dylan","ext":["dylan","dyl","intr"]},{"name":"EBNF","mime":"text/x-ebnf","mode":"ebnf"},{"name":"ECL","mime":"text/x-ecl","mode":"ecl","ext":["ecl"]},{"name":"edn","mime":"application/edn","mode":"clojure","ext":["edn"]},{"name":"Eiffel","mime":"text/x-eiffel","mode":"eiffel","ext":["e"]},{"name":"Elm","mime":"text/x-elm","mode":"elm","ext":["elm"]},{"name":"Embedded Javascript","mime":"application/x-ejs","mode":"htmlembedded","ext":["ejs"]},{"name":"Embedded Ruby","mime":"application/x-erb","mode":"htmlembedded","ext":["erb"]},{"name":"Erlang","mime":"text/x-erlang","mode":"erlang","ext":["erl"]},{"name":"Esper","mime":"text/x-esper","mode":"sql"},{"name":"Factor","mime":"text/x-factor","mode":"factor","ext":["factor"]},{"name":"FCL","mime":"text/x-fcl","mode":"fcl"},{"name":"Forth","mime":"text/x-forth","mode":"forth","ext":["forth","fth","4th"]},{"name":"Fortran","mime":"text/x-fortran","mode":"fortran","ext":["f","for","f77","f90"]},{"name":"F#","mime":"text/x-fsharp","mode":"mllike","ext":["fs"],"alias":["fsharp"]},{"name":"Gas","mime":"text/x-gas","mode":"gas","ext":["s"]},{"name":"Gherkin","mime":"text/x-feature","mode":"gherkin","ext":["feature"]},{"name":"GitHub Flavored Markdown","mime":"text/x-gfm","mode":"gfm","file":{}},{"name":"Go","mime":"text/x-go","mode":"go","ext":["go"]},{"name":"Groovy","mime":"text/x-groovy","mode":"groovy","ext":["groovy","gradle"],"file":{}},{"name":"HAML","mime":"text/x-haml","mode":"haml","ext":["haml"]},{"name":"Haskell","mime":"text/x-haskell","mode":"haskell","ext":["hs"]},{"name":"Haskell (Literate)","mime":"text/x-literate-haskell","mode":"haskell-literate","ext":["lhs"]},{"name":"Haxe","mime":"text/x-haxe","mode":"haxe","ext":["hx"]},{"name":"HXML","mime":"text/x-hxml","mode":"haxe","ext":["hxml"]},{"name":"ASP.NET","mime":"application/x-aspx","mode":"htmlembedded","ext":["aspx"],"alias":["asp","aspx"]},{"name":"HTML","mime":"text/html","mode":"htmlmixed","ext":["html","htm","handlebars","hbs"],"alias":["xhtml"]},{"name":"HTTP","mime":"message/http","mode":"http"},{"name":"IDL","mime":"text/x-idl","mode":"idl","ext":["pro"]},{"name":"Pug","mime":"text/x-pug","mode":"pug","ext":["jade","pug"],"alias":["jade"]},{"name":"Java","mime":"text/x-java","mode":"clike","ext":["java"]},{"name":"Java Server Pages","mime":"application/x-jsp","mode":"htmlembedded","ext":["jsp"],"alias":["jsp"]},{"name":"JavaScript","mimes":["text/javascript","text/ecmascript","application/javascript","application/x-javascript","application/ecmascript"],"mode":"javascript","ext":["js"],"alias":["ecmascript","js","node"],"mime":"text/javascript"},{"name":"JSON","mimes":["application/json","application/x-json"],"mode":"javascript","ext":["json","map"],"alias":["json5"],"mime":"application/json"},{"name":"JSON-LD","mime":"application/ld+json","mode":"javascript","ext":["jsonld"],"alias":["jsonld"]},{"name":"JSX","mime":"text/jsx","mode":"jsx","ext":["jsx"]},{"name":"Jinja2","mime":"null","mode":"jinja2"},{"name":"Julia","mime":"text/x-julia","mode":"julia","ext":["jl"]},{"name":"Kotlin","mime":"text/x-kotlin","mode":"clike","ext":["kt"]},{"name":"LESS","mime":"text/x-less","mode":"css","ext":["less"]},{"name":"LiveScript","mime":"text/x-livescript","mode":"livescript","ext":["ls"],"alias":["ls"]},{"name":"Lua","mime":"text/x-lua","mode":"lua","ext":["lua"]},{"name":"Markdown","mime":"text/x-markdown","mode":"markdown","ext":["markdown","md","mkd"]},{"name":"mIRC","mime":"text/mirc","mode":"mirc"},{"name":"MariaDB SQL","mime":"text/x-mariadb","mode":"sql"},{"name":"Mathematica","mime":"text/x-mathematica","mode":"mathematica","ext":["m","nb"]},{"name":"Modelica","mime":"text/x-modelica","mode":"modelica","ext":["mo"]},{"name":"MUMPS","mime":"text/x-mumps","mode":"mumps","ext":["mps"]},{"name":"MS SQL","mime":"text/x-mssql","mode":"sql"},{"name":"mbox","mime":"application/mbox","mode":"mbox","ext":["mbox"]},{"name":"MySQL","mime":"text/x-mysql","mode":"sql"},{"name":"Nginx","mime":"text/x-nginx-conf","mode":"nginx","file":{}},{"name":"NSIS","mime":"text/x-nsis","mode":"nsis","ext":["nsh","nsi"]},{"name":"NTriples","mimes":["application/n-triples","application/n-quads","text/n-triples"],"mode":"ntriples","ext":["nt","nq"],"mime":"application/n-triples"},{"name":"Objective-C","mime":"text/x-objectivec","mode":"clike","ext":["m","mm"],"alias":["objective-c","objc"]},{"name":"OCaml","mime":"text/x-ocaml","mode":"mllike","ext":["ml","mli","mll","mly"]},{"name":"Octave","mime":"text/x-octave","mode":"octave","ext":["m"]},{"name":"Oz","mime":"text/x-oz","mode":"oz","ext":["oz"]},{"name":"Pascal","mime":"text/x-pascal","mode":"pascal","ext":["p","pas"]},{"name":"PEG.js","mime":"null","mode":"pegjs","ext":["jsonld"]},{"name":"Perl","mime":"text/x-perl","mode":"perl","ext":["pl","pm"]},{"name":"PHP","mimes":["text/x-php","application/x-httpd-php","application/x-httpd-php-open"],"mode":"php","ext":["php","php3","php4","php5","php7","phtml"],"mime":"text/x-php"},{"name":"Pig","mime":"text/x-pig","mode":"pig","ext":["pig"]},{"name":"Plain Text","mime":"text/plain","mode":"","ext":["txt","text","conf","def","list","log"]},{"name":"PLSQL","mime":"text/x-plsql","mode":"sql","ext":["pls"]},{"name":"PowerShell","mime":"application/x-powershell","mode":"powershell","ext":["ps1","psd1","psm1"]},{"name":"Properties files","mime":"text/x-properties","mode":"properties","ext":["properties","ini","in"],"alias":["ini","properties"]},{"name":"ProtoBuf","mime":"text/x-protobuf","mode":"protobuf","ext":["proto"]},{"name":"Python","mime":"text/x-python","mode":"python","ext":["BUILD","bzl","py","pyw"],"file":{}},{"name":"Puppet","mime":"text/x-puppet","mode":"puppet","ext":["pp"]},{"name":"Q","mime":"text/x-q","mode":"q","ext":["q"]},{"name":"R","mime":"text/x-rsrc","mode":"r","ext":["r","R"],"alias":["rscript"]},{"name":"reStructuredText","mime":"text/x-rst","mode":"rst","ext":["rst"],"alias":["rst"]},{"name":"RPM Changes","mime":"text/x-rpm-changes","mode":"rpm"},{"name":"RPM Spec","mime":"text/x-rpm-spec","mode":"rpm","ext":["spec"]},{"name":"Ruby","mime":"text/x-ruby","mode":"ruby","ext":["rb"],"alias":["jruby","macruby","rake","rb","rbx"]},{"name":"Rust","mime":"text/x-rustsrc","mode":"rust","ext":["rs"]},{"name":"SAS","mime":"text/x-sas","mode":"sas","ext":["sas"]},{"name":"Sass","mime":"text/x-sass","mode":"sass","ext":["sass"]},{"name":"Scala","mime":"text/x-scala","mode":"clike","ext":["scala"]},{"name":"Scheme","mime":"text/x-scheme","mode":"scheme","ext":["scm","ss"]},{"name":"SCSS","mime":"text/x-scss","mode":"css","ext":["scss"]},{"name":"Shell","mimes":["text/x-sh","application/x-sh"],"mode":"shell","ext":["sh","ksh","bash"],"alias":["bash","sh","zsh"],"file":{},"mime":"text/x-sh"},{"name":"Sieve","mime":"application/sieve","mode":"sieve","ext":["siv","sieve"]},{"name":"Slim","mimes":["text/x-slim","application/x-slim"],"mode":"slim","ext":["slim"],"mime":"text/x-slim"},{"name":"Smalltalk","mime":"text/x-stsrc","mode":"smalltalk","ext":["st"]},{"name":"Smarty","mime":"text/x-smarty","mode":"smarty","ext":["tpl"]},{"name":"Solr","mime":"text/x-solr","mode":"solr"},{"name":"SML","mime":"text/x-sml","mode":"mllike","ext":["sml","sig","fun","smackspec"]},{"name":"Soy","mime":"text/x-soy","mode":"soy","ext":["soy"],"alias":["closure template"]},{"name":"SPARQL","mime":"application/sparql-query","mode":"sparql","ext":["rq","sparql"],"alias":["sparul"]},{"name":"Spreadsheet","mime":"text/x-spreadsheet","mode":"spreadsheet","alias":["excel","formula"]},{"name":"SQL","mime":"text/x-sql","mode":"sql","ext":["sql"]},{"name":"SQLite","mime":"text/x-sqlite","mode":"sql"},{"name":"Squirrel","mime":"text/x-squirrel","mode":"clike","ext":["nut"]},{"name":"Stylus","mime":"text/x-styl","mode":"stylus","ext":["styl"]},{"name":"Swift","mime":"text/x-swift","mode":"swift","ext":["swift"]},{"name":"sTeX","mime":"text/x-stex","mode":"stex"},{"name":"LaTeX","mime":"text/x-latex","mode":"stex","ext":["text","ltx","tex"],"alias":["tex"]},{"name":"SystemVerilog","mime":"text/x-systemverilog","mode":"verilog","ext":["v","sv","svh"]},{"name":"Tcl","mime":"text/x-tcl","mode":"tcl","ext":["tcl"]},{"name":"Textile","mime":"text/x-textile","mode":"textile","ext":["textile"]},{"name":"TiddlyWiki ","mime":"text/x-tiddlywiki","mode":"tiddlywiki"},{"name":"Tiki wiki","mime":"text/tiki","mode":"tiki"},{"name":"TOML","mime":"text/x-toml","mode":"toml","ext":["toml"]},{"name":"Tornado","mime":"text/x-tornado","mode":"tornado"},{"name":"troff","mime":"text/troff","mode":"troff","ext":["1","2","3","4","5","6","7","8","9"]},{"name":"TTCN","mime":"text/x-ttcn","mode":"ttcn","ext":["ttcn","ttcn3","ttcnpp"]},{"name":"TTCN_CFG","mime":"text/x-ttcn-cfg","mode":"ttcn-cfg","ext":["cfg"]},{"name":"Turtle","mime":"text/turtle","mode":"turtle","ext":["ttl"]},{"name":"TypeScript","mime":"application/typescript","mode":"javascript","ext":["ts"],"alias":["ts"]},{"name":"TypeScript-JSX","mime":"text/typescript-jsx","mode":"jsx","ext":["tsx"],"alias":["tsx"]},{"name":"Twig","mime":"text/x-twig","mode":"twig"},{"name":"Web IDL","mime":"text/x-webidl","mode":"webidl","ext":["webidl"]},{"name":"VB.NET","mime":"text/x-vb","mode":"vb","ext":["vb"]},{"name":"VBScript","mime":"text/vbscript","mode":"vbscript","ext":["vbs"]},{"name":"Velocity","mime":"text/velocity","mode":"velocity","ext":["vtl"]},{"name":"Verilog","mime":"text/x-verilog","mode":"verilog","ext":["v"]},{"name":"VHDL","mime":"text/x-vhdl","mode":"vhdl","ext":["vhd","vhdl"]},{"name":"Vue.js Component","mimes":["script/x-vue","text/x-vue"],"mode":"vue","ext":["vue"],"mime":"script/x-vue"},{"name":"XML","mimes":["application/xml","text/xml"],"mode":"xml","ext":["xml","xsl","xsd","svg"],"alias":["rss","wsdl","xsd"],"mime":"application/xml"},{"name":"XQuery","mime":"application/xquery","mode":"xquery","ext":["xy","xquery"]},{"name":"Yacas","mime":"text/x-yacas","mode":"yacas","ext":["ys"]},{"name":"YAML","mimes":["text/x-yaml","text/yaml"],"mode":"yaml","ext":["yaml","yml"],"alias":["yml"],"mime":"text/x-yaml"},{"name":"Z80","mime":"text/x-z80","mode":"z80","ext":["z80"]},{"name":"mscgen","mime":"text/x-mscgen","mode":"mscgen","ext":["mscgen","mscin","msc"]},{"name":"xu","mime":"text/x-xu","mode":"mscgen","ext":["xu"]},{"name":"msgenny","mime":"text/x-msgenny","mode":"mscgen","ext":["msgenny"]}],
        themes: ["default", "3024-day", "3024-night", "abcdef", "ambiance", "base16-dark", "base16-light", "bespin", "blackboard", "cobalt", "colorforth", "darcula", "dracula", "duotone-dark", "duotone-light", "eclipse", "elegant", "erlang-dark", "gruvbox-dark", "hopscotch", "icecoder", "idea", "isotope", "lesser-dark", "liquibyte", "lucario", "material", "mbo", "mdn-like", "midnight", "monokai", "neat", "neo", "night", "oceanic-next", "panda-syntax", "paraiso-dark", "paraiso-light", "pastel-on-dark", "railscasts", "rubyblue", "seti", "shadowfox", "solarized dark", "solarized light", "the-matrix", "tomorrow-night-bright", "tomorrow-night-eighties", "ttcn", "twilight", "vibrant-ink", "xq-dark", "xq-light", "yeti", "zenburn"],
        keymaps: ["vim", "emacs", "sublime"]
    };

    return this;

    function init(text) {
        priv.editorArea = document.createElement("textarea");
        priv.editorArea.textContent = text;
        appendTo.append(priv.editorArea);

        priv.themeInput = priv.newDropdown(options.showThemes, priv.themes.map(function(t) {return {name: t, value: t}}), priv.selectTheme);
        priv.modeInput = priv.newDropdown(options.showModes, priv.modes.map(function(m) {return {name: m.name, value: m.name}}), priv.selectMode);
        priv.keysInput = priv.newDropdown(options.showKeymap, priv.keymaps.map(function(k) {return {name: k, value: k}}), priv.selectKeymap);

        priv.editor = null;

        loadCss("codemirror/lib/codemirror.css");
        loadCss("codemirror/addon/dialog/dialog.css");

        require(["codemirror/lib/codemirror",
                "codemirror/keymap/vim",

                "codemirror/addon/edit/matchbrackets",
                "codemirror/addon/dialog/dialog",
                "codemirror/addon/search/searchcursor"],
            function (CodeMirror) {
                CodeMirror.commands.save = options.onsave;

                CodeMirror.Vim.defineEx("q", false, function() {
                    self.destroy();
                    options.onclose();
                });

                priv.editor = CodeMirror.fromTextArea(priv.editorArea, {
                    lineNumbers: options.lineNumbers,
                    keyMap: "vim",
                    matchBrackets: true,
                    showCursorWhenSelecting: true,
                    autofocus: true,
                    dragDrop: false,
                    readOnly: options.readOnly,
                    inputStyle: "contenteditable"
                });

                priv.appendControllers();
                priv.selectTheme();
                priv.selectMode();
            });
        delete self.init;
    }

    function destroy() {
        priv.modeInput.remove();
        priv.themeInput.remove();
        priv.editorArea.nextSibling.remove();
        priv.editorArea.remove();
        for (var p in priv) {
            priv[p] = null;
            delete priv[p];
        }
        priv = null;
        delete self.destroy;
    }

    function appendControllers() {
        var controllers = document.createElement("div");
        controllers.style = "height: 20px;";

        if ((options.filename||"").length > 0) {
            var filenameInput = document.createElement("span");
            filenameInput.textContent = options.filename;

            var fileExt = filenameInput.textContent.substring(filenameInput.textContent.lastIndexOf(".") + 1);
            var mode = priv.modes.find(function (m) {
                return (m.ext || []).find(function (ext) {
                    return ext.toLowerCase() === fileExt.toLowerCase()
                }) !== undefined;
            });
            controllers.append(filenameInput);
        }
        if (mode === undefined) {
            mode = {name:"Plain Text"};
        }

        priv.themeInput.value = localStorage.getItem("theme") || "base16-dark";
        priv.modeInput.value = mode.name;
        priv.keysInput.value = localStorage.getItem("keymap") || "vim";

        controllers.append(priv.themeInput);
        controllers.append(priv.modeInput);
        controllers.append(priv.keysInput);

        priv.editorArea.nextSibling.prepend(controllers);
    }

    function selectTheme() {
        var theme = priv.themeInput.options[priv.themeInput.selectedIndex].textContent;
        loadThemeCss(theme);
        priv.editor.setOption("theme", theme);
        localStorage.setItem("theme", theme);
    }

    function selectMode() {
        var mode = priv.modes.find(function(m) {return m.name === priv.modeInput.value;});
        if (mode === undefined || mode.mime === 'text/plain') {
            priv.editor.setOption("mode", "text/plain");
        } else {
            require(["codemirror/mode/" + mode.mode + "/" + mode.mode], function () {
                priv.editor.setOption("mode", mode.mime);
            });
        }
    }
    function selectKeymap() {
        var keymap = priv.keysInput.options[priv.keysInput.selectedIndex].textContent;
        require(["codemirror/keymap/" + keymap], function () {
            priv.editor.setOption("keyMap", keymap);
        });
        localStorage.setItem("keymap", keymap);
    }

    function newDropdown(visible, opts, onChange) {
        var select = document.createElement("select");
        select.style = "float: right"+(!visible ? "; display:none" : "");
        select.onchange = onChange;
        for (var i = 0; i < opts.length; i++) {
            var opt = opts[i];
            select.options[select.options.length] = new Option(opt.name, opt.value);
        }
        return select;
    }
}

var loadedCss = {};
var loadThemeCss = function(theme) {
    loadCss("codemirror/theme/"+theme+".css");
};
var loadCss = function(href) {
    if (loadedCss.hasOwnProperty(href)) {
        return;
    }
    loadedCss[href] = true;
    var link = document.createElement("link");
    link.type = "text/css";
    link.rel = "stylesheet";
    link.href = href;
    document.getElementsByTagName("head")[0].appendChild(link);
}
