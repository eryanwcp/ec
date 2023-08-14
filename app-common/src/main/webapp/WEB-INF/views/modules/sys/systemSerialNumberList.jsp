<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>序列号管理</title>
	<meta name="decorator" content="default_sys"/>
	<script type="text/javascript">
		$(function(){
			resetTip();
			loadData();
			$("#btnSubmit").click(function(){
				$("#pageNo").val(1);
				loadData();
			});
			$("#btnReset").click(function(){
				$('#searchForm').find("input[type=hidden]").val("");
				// $('#searchForm').find("select").val(null).trigger("change");
			});
			$("#btnExport").click(function(){
				var param = $.serializeObject($("#searchForm"));
				$('#annexFrame').attr('src', '${ctxAdmin}/sys/systemSerialNumber?export=true&'+ $.param(param));
			});
		});

		function loadData(){
			var queryParam = $.serializeObject($("#searchForm"));
			$.ajax({
				url: ctxAdmin + '/sys/systemSerialNumber',
				type: 'get',
				dataType: "json",
				cache:false,
				data:queryParam,
				beforeSend: function (jqXHR, settings) {
					$("#list").html("<div style='padding: 10px 30px;text-align:center;font-size: 16px;'>数据加载中...</div>");
				},
				success: function (data) {
					if (data['totalCount'] > 0) {
						var html = Mustache.render($("#list_template").html(), data);
						$("#list").html(html);
						$(".pagination").append(data['html']);
					} else {
						$("#list").html("<div style='color: red;padding: 10px 30px;text-align:center;font-size: 16px;'>查无数据</div>");
					}
				}
			});
		}
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			loadData();
			return false;
		}
	</script>
	<script type="text/template" id="list_template">
		<table id="contentTable" class="table table-striped table-bordered table-condensed">
			<thead>
			<tr>
				<th>APP标识</th>
				<th>模块名称</th>
				<th>模块编码</th>
				<th>重置类型</th>
				<th>是否自动增长</th>
				<th>预生成流水号数量</th>
				<th>备注</th>
				<th>操作</th>
			</tr>
			</thead>
			<tbody>
			{{#result}}
			<tr>
				<td>{{app}}</td>
				<td>{{moduleName}}</td>
				<td>{{moduleCode}}</td>
				<td>{{resetTypeView}}</td>
				<td>{{isAutoIncrementView}}</td>
				<td>{{preMaxNum}}</td>
				<td>{{remark}}</td>
				<td>
					<a class="btn-link" href="${ctxAdmin}/sys/systemSerialNumber/form?id={{id}}">修改</a>&nbsp;
					<a class="btn-link" onclick="return confirmx('要删除数据吗？', this.href)" href="${ctxAdmin}/sys/systemSerialNumber/delete?id={{id}}">删除</a>&nbsp;
					{{#resetType}}
					<a class="btn-link" target="_blank" href="${ctxAdmin}/sys/systemSerialNumber/resetSerialNumber?id={{id}}">重置</a>&nbsp;
					{{/resetType}}
				</td>
			</tr>
			{{/result}}
			</tbody>
		</table>
		<div class="page pagination"></div>
	</script>
</head>
<body>
<ul class="nav nav-tabs">
	<li class="active"><a href="${ctxAdmin}/sys/systemSerialNumber">序列号列表</a></li>
	<e:hasPermission name="sys:systemSerialNumber:edit"><li><a href="${ctxAdmin}/sys/systemSerialNumber/form">序列号添加</a></li></e:hasPermission>
</ul>
<form:form id="searchForm" modelAttribute="model" action="${ctxAdmin}/sys/systemSerialNumber" method="post" class="breadcrumb form-search">
	<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
	<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	<label>关键字：</label><input name="query" type="text" maxlength="50" class="input-large" placeholder="关键字"/>&nbsp;&nbsp;
	&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" value="查 询"/>&nbsp;
	<input id="btnReset" class="btn btn-warning" type="reset" value="重 置"/>&nbsp;&nbsp;&nbsp;
	<button id="btnExport" type="button" class="btn btn-primary" >导出</button>
</form:form>
<tags:message content="${message}"/>
<div id="list"></div>
</body>
</html>