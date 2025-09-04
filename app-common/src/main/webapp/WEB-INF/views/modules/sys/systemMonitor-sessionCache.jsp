<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>会话监控</title>
	<meta name="decorator" content="default_sys"/>
	<script type="text/javascript">
		$(function(){
			resetTip();
			loadData();
		});
		function loadData(){
			var queryParam = $.serializeObject($("#searchForm"));
			$.ajax({
				url: ctxAdmin + '/sys/systemMonitor/sessionCache',
				type: 'post',
				dataType: "json",
				cache:false,
				data:queryParam,
				beforeSend: function (jqXHR, settings) {
					$("#list").html("<div style='padding: 10px 30px;text-align:center;font-size: 16px;'><img src='${ctxStatic}/js/easyui/themes/bootstrap/images/loading.gif' />数据加载中...</div>");
				},
				success: function (data) {
					if (data['totalCount'] > 0) {
						$.each(data['result'],function (i,r){
							delete r['data']['loginUser'];
							r['sessionData'] = typeof r['data']['SESSION_DATA'] === 'object' ? JSON.stringify(r['data']['SESSION_DATA']) : r['data']['SESSION_DATA'];
							delete r['data']['SESSION_DATA'];
							r['dataList'] = Object.entries(r['data']).map(([key, value]) => ({
								key,
								value: typeof value === 'object' ? JSON.stringify(value) : value,
								type: typeof value
							}));

						});
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
			loadData();
			return false;
		}
	</script>
	<script type="text/template" id="list_template">
		<table id="contentTable" class="table table-striped table-bordered table-condensed">
			<thead>
			<tr>
				<th>主机</th>
				<th>会话ID</th>
				<th>用户标识</th>
				<th>客户端IP</th>
				<th>数据</th>
				<th>一级TTL</th>
				<th>二级TTL</th>
				<th>创建时间</th>
				<th>更新时间</th>
				<th>操作</th>
			</tr>
			</thead>
			<tbody>
			{{#result}}
			<tr>
				<td>{{host}}</td>
				<td><a href="javascript:" onclick="$('#c_{{key}}').toggle()">{{key}}</a></td>
				<td>{{loginUser}}</td>
				<td>{{clientIP}}</td>
				<td>
					<table class="table table-striped table-bordered table-condensed">
						<tbody>
						{{#dataList}}
						<tr>
							<td>{{key}}</td>
							<td>{{value}}</td>
						</tr>
						{{/dataList}}
						</tbody>
					</table>
				</td>
				<td>{{ttl1}}</td>
				<td>{{ttl2}}</td>
				<td>{{created_at}}</td>
				<td>{{lastAccess_at}}</td>
				<td>
					<e:hasPermission name="sys:systemMonitor:edit">
						<a href="${ctxAdmin}/sys/systemMonitor/clearSessionCacheKey?key={{keyEncodeUrl}}"  onclick="return confirmx('确认要清除缓存KEY吗？', this.href)">删除</a>
					</e:hasPermission>
				</td>
			</tr>
			<tr id="c_{{key}}" style="background:#fdfdfd;display:none;"><td colspan="10"><pre>{{sessionData}}</pre></td></tr>
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
	<li class="active"><a href="${ctxAdmin}/sys/systemMonitor/sessionCache">会话监控</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/cache">缓存管理</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/queue">队列管理</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/asyncTask">异步任务</a></li>
	<li><a href="${ctxAdmin}/sys/job">调度任务</a></li>
</ul>
<form:form id="searchForm" method="post" class="breadcrumb form-search">
	<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
	<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	${region}
</form:form>
<tags:message content="${message}"/>
<div id="list"></div>
</body>
</html>