<%@page import="com.nicolasbouvie.ws.terminal.action.ChangeDir"%>
<%@page import="com.nicolasbouvie.ws.terminal.action.ExecuteCommand"%>
<%@page import="com.nicolasbouvie.ws.terminal.action.AutoComplete"%>
<%@page import="com.nicolasbouvie.ws.terminal.ProcessExecutor"%>
<%@ page import="com.nicolasbouvie.ws.terminal.action.SaveTextFile" %>
<%@ page import="com.nicolasbouvie.ws.terminal.Terminal" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Web Shell</title>
	<STYLE type="text/css">
		.terminal {
			background-color: #000000; 
			color: #00FF00;
			width: 99%;
			border:none;
			outline: none;
		}
		#upload {
			position: fixed;
			top: 0;
			right: 0;
			line-height: 100px;
			width: 100px;
			text-align: center;
			border: #00FF00 dashed;
		}
		.CodeMirror {
			width: 100% !important;
			height: 100% !important;
		}
	</STYLE>
	<script src="jstree/libs/jquery.js"></script>
	<script src="require.js"></script>
	<script src="editor.js"></script>
</head>
<body class="terminal">
	<div id="terminalDiv">
		<form id="upload" method="post" action="terminal/upload" enctype="multipart/form-data">
			<input class="box__file" type="file" name="files[]" id="file" data-multiple-caption="{count} files selected" style="display: none" multiple/>
			<input name="<%=Terminal.WORKING_DIR_PARAM%>" id="uploadWD" style="display: none;" value="<%=request.getAttribute("workingDir") %>"/>
			<span>Upload area</span>
		</form>
<pre id="terminal">
<%@ include file="welcome.txt" %>

</pre>
		<label id="path" style="float:left; margin-right: 5px"><%=request.getAttribute("workingDir") %>$</label>

		<span contenteditable="true" id="command" class="terminal">&nbsp;</span>
	</div>

	<SCRIPT type="text/javascript">
		var timer;
		var locked 		  = false;
        var commandBuffer = localStorage.getItem("commandBuffer");
        commandBuffer = commandBuffer !== null ? commandBuffer.split(",") : [];
		var bufIdx 		  = commandBuffer.length;
		var currentPath   = "<%=Terminal.HOME.getCanonicalPath()%>";
        var previousPath  = "<%=Terminal.HOME.getCanonicalPath()%>";
		var isUp    = function(e) { return e.keyCode == 38;              };
		var isDown  = function(e) { return e.keyCode == 40;              };
		var isEnter = function(e) { return e.keyCode == 13;              };
		var isTab   = function(e) { return e.keyCode == 9;               };
		var isCtrlC = function(e) { return e.keyCode == 67 && (e.ctrlKey || e.metaKey); };

		var getObject = function(txt) {
			return eval("("+txt+")");
		};
		
		var isEndPage = function() {
			return $(window).scrollTop() + document.body.clientHeight == $(document).height();
		};
		
		var doScroll = function(force) {
			if (force) {
				$("body").scrollTop($("body").height());
			}
		};
		
		var lock = function() {
			locked = true;
			$("#path, #command").hide();
		};
		
		var unlock = function(focus) {
			locked = false;
			$("#path, #command").show();
            if (focus) commandFocus();
		};
		
		var commandFocus = function() {
			$("#command").focus();
            var cmd = $("#command").html().trim().replace(/\&nbsp\;/g, "");
			var char = cmd.length;
			if (document.selection) {
				var sel = document.selection.createRange();
				sel.moveStart('character', char);
				sel.select();
			} else {
				sel = window.getSelection();
				sel.collapse(document.getElementById("command").firstChild, char);
			}
		};

		var exec = function(cmd, scroll) {
			$.post("${pageContext.request.contextPath}/terminal/exec", 
				{'<%=ExecuteCommand.PARAM %>': cmd, '<%=Terminal.WORKING_DIR_PARAM%>': currentPath},
				function(d) {
					d = getObject(d);
					if (d.hasOwnProperty("out")) {
					    if (d.out.startsWith("wsget://")) {
							var sp = d.out.split("//");
                            download(sp[1], "data:application/octet-stream;base64,"+sp[2]);
						}
                        if (d.out.startsWith("wsedit://")) {
                            var sp = d.out.split("//");
                            $("#terminalDiv").hide();
                            var filename = currentPath+"/"+sp[1];
                            newEditor(document.body,
								{
								    filename: filename,
									onclose: function() {
                                		$("#terminalDiv").show();
                                        commandFocus();
                            		},
									onsave: function(cm) {
                                		var base64val = btoa(cm.getValue());
                                		$.post("${pageContext.request.contextPath}/terminal/saveTextFile",
                                    		{'<%=SaveTextFile.FILE_PARAM %>': filename, '<%=SaveTextFile.CONTENT_PARAM %>': base64val},
                                    		function(d) {
												alert("File saved");
                                    		}, "text"
                                		);
									}
								}
							).init(atob(sp[2]));
                            $("#terminal").append("");
                            unlock(false);
                            return;
                        }
						$("#terminal").append(d.out);
					}
					if (d.hasOwnProperty("error")) {
						$("#terminal").append("<ERROR><br/>");
						$("#terminal").append(d.error);
						$("#terminal").append("</ERROR><br/>");
					}
					
					if (d.finished) {
						unlock(true);
					} else if (locked) {
						timer = setTimeout(function() {exec(cmd, isEndPage());}, 500);
					}
					doScroll(scroll);
				}, "text"
			);
		};

		$("html").click(function(){
			$("#command").focus();
		});
		
		$("html").keydown(function(e){
			if (isCtrlC(e)) {
				clearTimeout(timer);
				unlock(true);
				$("#command").html("&nbsp;");
				$.post("${pageContext.request.contextPath}/terminal/kill");
				bufIdx = commandBuffer.length;
				doScroll(true);
				return false;
			}
		});
		
		$("#command").keydown(function(e){
			if (locked) return false;
			var cmd = $(this).html().trim().replace(/\&nbsp\;/g, "");
			if (isEnter(e)) {
				$("#command").html("&nbsp;");
				if (cmd == "exit") {
					window.location = "${pageContext.request.contextPath}";
				} else if (cmd == "clear") {
					$("#terminal").html("");
				} else {
					$("#terminal").append($("#path").html() + cmd + "<br/>");
					lock();
					if (cmd.indexOf("cd") === 0) {
					    if (cmd.substring(3).trim() === "-") {
                            $("#path").html(previousPath + "$");
                            var aux = previousPath;
                            previousPath = currentPath;
                            currentPath = aux;
                            unlock(true);
                            doScroll(true);
						} else {
							$.post("${pageContext.request.contextPath}/terminal/cd",
								{'<%=ChangeDir.PARAM%>':cmd.substring(3), '<%=Terminal.WORKING_DIR_PARAM%>': currentPath},
								function(d) {
									d = getObject(d);
									if (d.hasOwnProperty("workingDir")) {
										previousPath = currentPath;
										currentPath = d.workingDir;

										var formatWD = d.workingDir.split('/');
										if (formatWD.length > 4) {
                                            formatWD.splice(2,formatWD.length-4, "...");
										}
										formatWD = formatWD.join("/");
										$("#path").html(formatWD + "$");
									} else {
										$("#terminal").append(d.message + "<br/>");
									}
									unlock(true);
									doScroll(true);
								}, "text"
							);
						}
					} else {
						exec(cmd, isEndPage());
					}
				}
                if (cmd.trim().length > 0 && commandBuffer[commandBuffer.length-1] !== btoa(cmd)) {
                    commandBuffer[commandBuffer.length] = btoa(cmd);
                    localStorage.setItem("commandBuffer", commandBuffer.slice(commandBuffer.length-20));
                }
                bufIdx = commandBuffer.length;
				return false;
			} else if (isTab(e)) {
				var sp = cmd.split(/\s+/);
				if (sp.length == 1) {
					var path = sp[0];
					$.post("${pageContext.request.contextPath}/terminal/complete", 
						{'<%=AutoComplete.PARAM_CMD %>': path, '<%=Terminal.WORKING_DIR_PARAM%>': currentPath},
						function(d) {
							d = getObject(d);
							$("#terminal").append($("#path").html() + cmd + "<br/>");
							if (d.hasOwnProperty("complete")) {
								$("#command").html(cmd + d.complete);
							} else if (d.hasOwnProperty("options")) {
								$("#terminal").append(d.options+"<br/>");
							}
							commandFocus();
							doScroll(true);
						}, "text"
					);
				} else {
					//path
					var path = sp[sp.length-1];
					$.post("${pageContext.request.contextPath}/terminal/complete", 
						{'<%=AutoComplete.PARAM %>': path, '<%=Terminal.WORKING_DIR_PARAM%>': currentPath},
						function(d) {
							d = getObject(d);
							$("#terminal").append($("#path").html() + cmd + "<br/>");
							if (d.hasOwnProperty("complete")) {
								$("#command").html(cmd + d.complete);
							} else if (d.hasOwnProperty("options")) {
								$("#terminal").append(d.options+"<br/>");
							}
							commandFocus();
							doScroll(true);
						}, "text"
					);
				}
				return false;
			} else if (isUp(e)) {
				if (bufIdx > 0) {
					$("#command").html(atob(commandBuffer[--bufIdx]));
				}
				doScroll(true);
				return false;
			} else if (isDown(e)) {
				if (bufIdx < commandBuffer.length-1) {
					$("#command").html(atob(commandBuffer[++bufIdx]));
				} else if (commandBuffer.length == bufIdx+1) {
                    bufIdx = commandBuffer.length;
					$("#command").html("&nbsp;");
				}
				doScroll(true);
				return false;
			}
		});


        function download(filename, file) {
            var element = document.createElement('a');
            element.setAttribute('href', file);
            element.setAttribute('download', filename);

            element.style.display = 'none';
            document.body.appendChild(element);

            element.click();

            document.body.removeChild(element);
        }

        var droppedFiles = false;
        var uploadForm = $("#upload");
        var uploadText = $("#upload span");
        uploadForm.on('drag dragstart dragend dragover dragenter dragleave drop', function(e) {
            e.preventDefault();
            e.stopPropagation();
        }).on('drop', function(e) {
            droppedFiles = e.originalEvent.dataTransfer.files;

            e.preventDefault();

            $("#uploadWD").val(currentPath);
            var ajaxData = new FormData(uploadForm.get(0));

            if (droppedFiles) {
                lock();
                uploadText.text("Uploading...");
                $.each( droppedFiles, function(i, file) {
                    ajaxData.append( $("#input").attr('name'), file );
                });

                $.ajax({
                    url: uploadForm.attr('action'),
                    type: uploadForm.attr('method'),
                    data: ajaxData,
                    dataType: 'json',
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function(data) {
                        if (data.error) {
                            uploadText.text(data.error);
						} else {
                            uploadText.text("Success!");
						}
                        setTimeout(function() {uploadText.text("Upload area");}, 5000);
                        unlock(true);
                    },
                    error: function(e) {
                        uploadText.text(e);
                        setTimeout(function() {uploadText.text("Upload area");}, 5000);
                        unlock(true);
                    }
                });
            }
        });

	</SCRIPT>
</body>
</html>