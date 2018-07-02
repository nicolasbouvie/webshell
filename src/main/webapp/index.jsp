<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Explorer</title>
		<link rel="stylesheet" href="jstree/themes/default/style.min.css"/>
	</head>
	<body>
		<script src="jstree/libs/jquery.js"></script>
		<script src="jstree/jstree.min.js"></script>
		<div id="jstree">
		</div>
	
		<script>
			$(function () {
				$('#jstree').jstree({
					"plugins" : [ "sort", "contextmenu" ],
					'core' : {
						'data' : {
							'url' : function (node) {
								return 'tree';
					    	},
					    	'data' : function (node) {
					      		return { 'id' : node.id };
					    	}
					  	}
					},
					'contextmenu' : {
						'items': { 
							'Cat': {
								'label': 'Cat',
								'action': function(obj){
									var id = obj.reference.parent().attr("id");
								}
							},
							'Tail': {
								'label': 'Tail',
								'action': function(obj){
									var id = obj.reference.parent().attr("id");
								}
							}
						}
					}
				});
			});
		</script>
	</body>
</html>