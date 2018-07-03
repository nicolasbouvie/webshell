<%@page import="com.nicolasbouvie.ws.terminal.action.ChangeDir"%>
<%@page import="com.nicolasbouvie.ws.terminal.action.ExecuteCommand"%>
<%@page import="com.nicolasbouvie.ws.terminal.action.AutoComplete"%>
<%@page import="com.nicolasbouvie.ws.terminal.ProcessExecutor"%>
<%@ page import="com.nicolasbouvie.ws.terminal.action.SaveTextFile" %>
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
			Upload area
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
		var bufIdx 		  = 0;
		var commandBuffer = [];
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
				{'<%=ExecuteCommand.PARAM %>': cmd}, 
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
                            var path = $("#path").html().slice(0, -1);
                            var filename = path+"/"+sp[1];
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
					if (cmd.indexOf("cd") == 0) {
						$.post("${pageContext.request.contextPath}/terminal/cd", 
							{'<%=ChangeDir.PARAM%>':cmd.substring(3)}, 
							function(d) {
								d = getObject(d);
								if (d.hasOwnProperty("workingDir")) {
									$("#path").html(d.workingDir + "$");
								} else {
									$("#terminal").append(d.message + "<br/>");
								}
								unlock(true);
								doScroll(true);
							}, "text"
						);
					} else {
						exec(cmd, isEndPage());
					}
				}
				commandBuffer[commandBuffer.length] = cmd;
				bufIdx = commandBuffer.length;
				return false;
			} else if (isTab(e)) {
				var sp = cmd.split(/\s+/);
				if (sp.length == 1) {
					var path = sp[0];
					$.post("${pageContext.request.contextPath}/terminal/complete", 
						{'<%=AutoComplete.PARAM_CMD %>': path}, 
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
						{'<%=AutoComplete.PARAM %>': path}, 
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
					$("#command").html(commandBuffer[--bufIdx]);
				}
				doScroll(true);
				return false;
			} else if (isDown(e)) {
				if (bufIdx < commandBuffer.length-1) {
					$("#command").html(commandBuffer[++bufIdx]);
				} else if (commandBuffer.length == bufIdx+1) {
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
        uploadForm.on('drag dragstart dragend dragover dragenter dragleave drop', function(e) {
            e.preventDefault();
            e.stopPropagation();
        }).on('drop', function(e) {
            droppedFiles = e.originalEvent.dataTransfer.files;

            e.preventDefault();

            var ajaxData = new FormData(uploadForm.get(0));

            if (droppedFiles) {
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
                            uploadForm.text(data.error);
						} else {
                            uploadForm.text("Success!");
						}
                        setTimeout(function() {uploadForm.text("Upload area");}, 5000);
                    },
                    error: function() {
                        // Log the error, show an alert, whatever works for you
                    }
                });
            }
        });

	</SCRIPT>
</body>
</html>