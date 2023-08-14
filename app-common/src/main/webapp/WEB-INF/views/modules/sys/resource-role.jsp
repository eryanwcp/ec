<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<script type="text/javascript">
    var modelId = "${resourceId}";
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/resource-role${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<link rel="stylesheet" href="${ctx}/js/iconfont/iconfont.css?_=${sysInitTime}">
<div class="easyui-layout" fit="true" style="margin: 0px;border: 0px;overflow: hidden;width:100%;height:100%;">
    <%-- 中间部分 列表 --%>
    <div data-options="region:'center',split:false,border:false"
         style="padding: 0px; height: 100%;width:100%; overflow-y: hidden;">
        <table id="resource_role_datagrid" ></table>

    </div>
</div>