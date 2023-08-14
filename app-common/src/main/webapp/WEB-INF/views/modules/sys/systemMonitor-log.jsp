<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>系统监控-系统日志</title>
	<meta name="decorator" content="default_sys"/>
	<script type="text/javascript">
		$(function(){
			$("#showTotal").change(function(){
				reLoad();
			});
			$("#pretty").change(function(){
				reLoad();
			});

			$("#fileName").change(function(){
				reLoad();
			});
			reLoad();
		});
        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            reLoad();
            return false;
        }
		function reLoad(){
			$.ajax({
				url: '${ctxAdmin}/sys/systemMonitor/log',
				type: 'post',
				data:$.serializeObject($("#searchForm")),
				cache:false,
				dataType: 'json',
				success: function (data) {
					if (data.code === 1) {
						var html = Mustache.render($("#list_template").html(), data['obj']);
						$("#list").html(html);
						$(".pagination").append(data['obj']['html']);
						$(".pagination").find("ul").append("<li class=\"disabled controls\"><a>"+data['data']+"</a></li>");
					} else {
						$("#list").html(data['msg']);
					}
				}
			});
		}
        function _download() {
            var annexFrame = document.getElementById("annexFrame");
            annexFrame.src = '${ctxAdmin}/sys/systemMonitor/downloadLogFile?fileName='+$("#fileName").val();
        }
	</script>
	<script type="text/template" id="list_template">
		<div class="page pagination"></div>
		<div style="padding: 10px;">
			{{#result}}
			{{&.}}</br>
			{{/result}}
		</div>
		<div class="page pagination"></div>
	</script>
</head>
<body>
<ul class="nav nav-tabs">
	<li><a href="${ctxAdmin}/sys/systemMonitor">系统监控</a></li>
	<li class="active"><a href="${ctxAdmin}/sys/systemMonitor/log">系统日志</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/cache">缓存管理</a></li>
	<li><a href="${ctxAdmin}/sys/systemMonitor/queue">队列管理</a></li>
</ul>
<form:form id="searchForm" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
	<select id="fileName" name="fileName">
		<c:forEach items="${fileNames}" var="item">
			<option value="${item}" <c:if test="${item eq fileName}">selected</c:if>>< >${item}</option>
		</c:forEach>
	</select>
	<label> <input id="showTotal" name="showTotal" type="checkbox" value="true">全部显示</label>&nbsp;
	<label> <input id="pretty" name="pretty" type="checkbox" value="true" checked>彩色日志</label>&nbsp;
	&nbsp;&nbsp;<input id="btnSubmit" class="btn btn-primary" type="button" value="刷新" onclick="reLoad();"/>&nbsp;&nbsp;
	&nbsp;&nbsp;<input class="btn btn-primary" type="button" value="下载" onclick="_download();"/>&nbsp;&nbsp;
</form:form>
<div id="list"></div>
<iframe id="annexFrame" src="" frameborder="no" style="padding: 0;border: 0;width: 100%;height: 50px;"></iframe>
</body>
</html>
