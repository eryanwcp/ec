<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>系统监控</title>
	<meta name="decorator" content="default_sys"/>
	<style type="text/css">
		pre {
			white-space: pre-wrap;       /* css-3 */
			white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */
			white-space: -pre-wrap;      /* Opera 4-6 */
			white-space: -o-pre-wrap;    /* Opera 7 */
			word-wrap: break-word;       /* Internet Explorer 5.5+ */
		}
	</style>
	<script type="text/javascript">
		$(function(){
			reLoad();
            window.setInterval('reLoad()',60*1000);
		});
		function reLoad(){
			$.ajax({
				url: ctxAdmin + '/sys/systemMonitor',
				type: 'post',
				cache:false,
				dataType: 'json',
				beforeSend: function (jqXHR, settings) {
					$("#systemInfo_div").html("<div style='padding: 10px 30px;text-align:center;font-size: 16px;'><img src='${ctxStatic}/js/easyui/themes/bootstrap/images/loading.gif' />数据加载中...</div>");
				},
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
			<tr><td>名称</td><td>{{sys.computerName}}</td></tr>
			<tr><td>IP地址</td><td>{{sys.computerIp}}</td></tr>
			<tr><td>操作系统</td><td>{{sys.osName}} {{sys.osArch}}</td></tr>
			<tr><td>服务器时间</td><td>{{sys.serverTime}}</td></tr>
			<tr><td>在线会话数</td><td><a href="${ctxAdmin}/sys/systemMonitor/sessionCache">{{sessionSize}}</a></td></tr>
			<tr><td>CPU型号</td><td>{{cpu.cpuModel}}</td></tr>
			<tr><td>CPU核心数</td><td>{{cpu.cpuNum}}</td></tr>
			<tr><td>CPU用户使用率</td><td>{{cpu.used}} %</td></tr>
			<tr><td>CPU系统使用率</td><td>{{cpu.sys}} %</td></tr>
			<tr><td>CPU当前等待率</td><td>{{cpu.wait}} %</td></tr>
			<tr><td>CPU当前空闲率</td><td>{{cpu.free}} %</td></tr>
			<tr><td>内存总量</td><td>{{mem.total}} GB</td></tr>
			<tr><td>已用内存</td><td>{{mem.used}} GB</td></tr>
			<tr><td>剩余内存</td><td>{{mem.free}} GB</td></tr>
			<tr><td>内存使用率</td><td>{{mem.usage}} %</td></tr>
			<tr><td>javaVersion</td><td>{{jvm.name}} {{jvm.version}}</td></tr>
			<tr><td>javaHome</td><td>{{jvm.home}}</td></tr>
			<tr><td>javaTmpPath</td><td>{{jvm.tmpPath}}</td></tr>
			<tr><td>当前JVM占用的内存总数</td><td>{{jvm.total}} M</td></tr>
			<tr><td>JVM最大可用内存总数</td><td>{{jvm.max}} M</td></tr>
			<tr><td>JVM使用</td><td>{{jvm.used}} M</td></tr>
			<tr><td>JVM使用率</td><td>{{jvm.usage}} %</td></tr>
			<tr><td>JVM空闲内存</td><td>{{jvm.free}} M</td></tr>
			<tr><td>jvm运行时间</td><td>{{jvm.startTime}} {{jvm.runTime}}</td></tr>
			<tr><td>文件系统</td><td>
				<table class="table table-striped table-bordered table-condensed">
					<tbody>
					{{#sysFiles}}
					<tr><td>{{typeName}} {{dirName}}    {{used}} / {{total}}  剩余{{free}}  {{sysTypeName}}</td></tr>
					{{/sysFiles}}
					</tbody>
				</table>
			</td></tr>
			</tbody>
		</table>
	</script>
</head>
<body>
<ul class="nav nav-tabs">
	<li class="active"><a href="${ctxAdmin}/sys/systemMonitor">系统监控</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/log">系统日志</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/sessionCache">会话监控</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/cache">缓存管理</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/queue">队列管理</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/asyncTask">异步任务</a></li>
	<li><a href="${ctxAdmin}/sys/job">调度任务</a></li>
</ul>
<form:form id="searchForm" method="post" class="breadcrumb form-search">
	&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" value="刷新" onclick="reLoad();"/>&nbsp;&nbsp;
</form:form>
<div id="systemInfo_div"></div>
</body>
</html>