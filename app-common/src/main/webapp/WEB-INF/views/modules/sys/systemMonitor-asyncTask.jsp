<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>系统监控-异步任务</title>
	<meta name="decorator" content="default_sys"/>
	<script type="text/javascript">
		$(function(){
			reLoad();
		});
		function reLoad(){
			$.ajax({
				url: ctxAdmin + '/sys/systemMonitor/asyncTask',
				type: 'post',
				cache:false,
				dataType: 'json',
				success: function (data) {
					if (data.code === 1) {
						var html = Mustache.render($("#systemList").html(),data['data']);
						$("#systemInfo_div").html(html);
					} else {
						$("#systemInfo_div").html(data['msg'] || "加载异常");
					}
				}
			});
		}
	</script>
	<script type="text/template" id="systemList">
		<table class="table table-striped table-bordered table-condensed">
			<tbody>
			<tr>
				<td>默认线程数</td>
				<td> {{corePoolSize}}</td>
			</tr>
			<tr>
				<td>最大线程数</td>
				<td style="color: red;"> {{maxPoolSize}}</td>
			</tr>
			<tr>
				<td>执行中线程数</td>
				<td style="color: red;">{{activeCount}}</td>
			</tr>
			<tr>
				<td>待执行队列数</td>
				<td>{{queueSize}}</td>
			</tr>
			<tr>
				<td>提交任务数</td>
				<td>{{taskCount}}</td>
			</tr>
			<tr>
				<td>完成任务数</td>
				<td>{{completedTaskCount}}</td>
			</tr>

			<tr>
				<td>可用队列长度</td>
				<td style="color: green;"> {{queueRemainingCapacity}}</td>
			</tr>
			</tbody>
		</table>
	</script>
</head>
<body>
<ul class="nav nav-tabs">
	<li><a href="${ctxAdmin}/sys/systemMonitor">系统监控</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/log">系统日志</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/sessionCache">会话监控</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/cache">缓存管理</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/queue">队列管理</a></li>
	<li class="active"><a href="${ctxAdmin}/sys/systemMonitor/asyncTask">异步任务</a></li>
	<li><a href="${ctxAdmin}/sys/job">调度任务</a></li>
</ul>
<form:form id="searchForm" method="post" class="breadcrumb form-search">
	&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" value="刷新" onclick="reLoad();"/>&nbsp;&nbsp;
</form:form>
<div id="systemInfo_div"></div>
</body>
</html>