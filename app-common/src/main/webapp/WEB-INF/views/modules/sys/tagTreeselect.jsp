<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<!DOCTYPE html>
<html style="overflow-x:hidden;overflow-y:auto;">
<head>
	<title>数据选择</title>
	<%@include file="/WEB-INF/views/include/head.jsp" %>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		var key, lastValue = "", nodeList = [];
		var tree, setting = {
			view: {selectedMulti: false}, check: {enable: "${checked}", nocheckInherit: true},
			data: {simpleData: {enable: true}, key: {name: 'text'}},
			view: {
				fontCss: function (treeId, treeNode) {
					return (!!treeNode.highlight) ? {"color": "red", "font-weight": "bold"} : {
						"color": "#333",
						"font-weight": "normal"
					};
				}
			},
			callback: {
				beforeClick: function (id, node) {
					if ("${checked}" == "true") {
						tree.checkNode(node, !node.checked, true, true);
						return false;
					}
				},
				onDblClick: function () {
					$.jBox.getBox().find("button[value='ok']").trigger("click");
					//alert($("input[type='text']", top.mainFrame.document).val());
					//$("input[type='text']", top.mainFrame.document).focus();
				}
			}
		};
		$(document).ready(function () {
			$.get("${url}${fn:indexOf(url,'?')==-1?'?':'&'}&extId=${extId}&module=${module}&t=" + new Date().getTime(), function (zNodes) {
				// 初始化树结构
				var isString = Object.prototype.toString.call(zNodes) === "[object String]";
				tree = $.fn.zTree.init($("#tree"), setting, isString ? $.parseJSON(zNodes) : zNodes);

				// 默认展开一级节点
				var nodes = tree.getNodesByParam("level", 0);
				for (var i = 0; i < nodes.length; i++) {
					tree.expandNode(nodes[i], true, false, false);
				}
				// 默认选择节点
				var ids = "${selectIds}".split(",");
				for (var i = 0; i < ids.length; i++) {
					var node = tree.getNodeByParam("id", ids[i]);
					if ("${checked}" == "true") {
						try {
							tree.checkNode(node, true, true);
						} catch (e) {
						}
						tree.selectNode(node, false);
					} else {
						tree.selectNode(node, true);
					}
				}
			});
			key = $("#key");
			key.bind("focus", focusKey).bind("blur", blurKey).bind("change keydown cut input propertychange", searchNode);
		});

		function focusKey(e) {
			if (key.hasClass("empty")) {
				key.removeClass("empty");
			}
		}

		function blurKey(e) {
			if (key.get(0).value === "") {
				key.addClass("empty");
			}
			searchNode(e);
		}

		function searchNode(e) {
			// 取得输入的关键字的值
			var value = $.trim(key.get(0).value);
			// 按名字查询
			var keyType = "text";
			if (key.hasClass("empty")) {
				value = "";
			}
			// 如果和上次一样，就退出不查了。
			if (lastValue === value) {
				return;
			}
			// 保存最后一次
			lastValue = value;
			updateNodes(false);
			// 如果要查空字串，就退出不查了。
			if (value === "") {
				return;
			}
			tree.expandAll(false);
			nodeList = tree.getNodesByParamFuzzy(keyType, value);
			if (nodeList && nodeList.length > 0) {
				updateNodes(true);
			}
		}

		function updateNodes(highlight) {
			for (var i = 0, l = nodeList.length; i < l; i++) {
				nodeList[i].highlight = highlight;
				tree.updateNode(nodeList[i]);
				tree.expandNode(nodeList[i].getParentNode(), true, false, false);
			}
		}

		function search() {
			$("#search").slideToggle(200);
			$("#txt").toggle();
			$("#key").focus();
		}
	</script>
</head>
<body>
<div style="position:absolute;right:8px;top:5px;cursor:pointer;" onclick="search();">
	<i class="icon-search"></i><label id="txt">搜索</label>
</div>
<div id="search" class="control-group hide" style="padding:10px 0 0 15px;">
	<label for="key" class="control-label" style="float:left;padding:5px 5px 3px;">关键字：</label>
	<input type="text" class="empty" id="key" name="key" maxlength="64" style="width:180px;">
</div>
<div id="tree" class="ztree" style="padding:15px 20px;"></div>
</body>