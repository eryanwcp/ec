<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>队列管理</title>
	<meta name="decorator" content="default_sys"/>
	<script type="text/javascript">
		$(function(){
		    resetTip();
			loadData();
		});
		function loadData(){
            var queryParam = $.serializeObject($("#searchForm"));
			$.ajax({
				url: ctxAdmin + '/sys/systemMonitor/queue',
				type: 'post',
                dataType: "json",
				cache:false,
				data:queryParam,
                beforeSend: function (jqXHR, settings) {
                    $("#list").html("<div style='padding: 10px 30px;text-align:center;font-size: 16px;'><img src='${ctxStatic}/js/easyui/themes/bootstrap/images/loading.gif' />数据加载中...</div>");
                },
                success: function (data) {
                    if (data['totalCount'] > 0) {
                        var html = Mustache.render($("#list_template").html(), data);
                        $("#list").html(html);
                        $(".pagination").append(data['html']);
                    } else {
                        $("#list").html("<div style='color: red;padding: 10px 30px;text-align:center;font-size: 16px;'>暂无数据</div>");
                    }
                }
            });
		}
        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            loadData(n,s);
            return false;
        }
	</script>
	<script type="text/template" id="list_template">
		<table id="contentTable" class="table table-striped table-bordered table-condensed">
			<thead>
			<tr>
				<th>队列</th>
				<th>KEYS</th>
				<th>操作</th>
			</tr>
			</thead>
			<tbody>
			{{#result}}
			<tr>
				<td>{{name}}</td>
				<td>{{keys}}</td>
				<td>
					<a href="${ctxAdmin}/sys/systemMonitor/queueDetail?region={{name}}">查看</a>&nbsp;
					<e:hasPermission name="sys:systemMonitor:edit">
						<a href="${ctxAdmin}/sys/systemMonitor/queueClear?region={{name}}"  onclick="return confirmx('确认要清除队列数据吗？', this.href)">清除队列</a>
					</e:hasPermission>
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
	<li><a href="${ctxAdmin}/sys/systemMonitor">系统监控</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/log">系统日志</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/cache">缓存管理</a></li>
	<li class="active"><a href="${ctxAdmin}/sys/systemMonitor/queue">队列管理</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/asyncTask">异步任务</a></li>
	<li><a href="${ctxAdmin}/sys/job">调度任务</a></li>
</ul>
<form:form id="searchForm" method="post" class="breadcrumb form-search">
	<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
	<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	<e:hasPermission name="sys:systemMonitor:edit">
		<a class="btn btn-link" href="${ctxAdmin}/sys/systemMonitor/clearAllQueue">清空队列</a>&nbsp;&nbsp;
	</e:hasPermission>
</form:form>
<tags:message content="${message}"/>
<div id="list"></div>
</body>
</html>