<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>队列数据-${key}</title>
	<meta name="decorator" content="default_sys"/>
</head>
<body>
<ul class="nav nav-tabs">
	<li><a href="${ctxAdmin}/sys/systemMonitor/queue">队列管理</a></li>
	<li class="active"><a href="${ctxAdmin}/sys/systemMonitor/queueDetail?region=${region}">队列数据</a></li>
</ul>
<form:form id="searchForm" method="post" class="breadcrumb form-search">
	${region}
</form:form>
<tags:message content="${message}"/>
<div>${data}</div>
</body>
</html>