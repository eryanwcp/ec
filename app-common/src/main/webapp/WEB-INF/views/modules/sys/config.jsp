<%@ page import="com.eryansky.core.security.SecurityUtils" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<%@ include file="/common/meta.jsp"%>
<script type="text/javascript">
    var sessionInfoUserId = "${sessionInfo.userId}";//当前的登录用户ID
    var hasPermissionConfigEdit= <%= SecurityUtils.isPermitted("sys:config:edit")%>;
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/config${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<%-- 列表右键 --%>
<div id="config_datagrid_menu" class="easyui-menu" style="width:120px;display: none;">
    <e:hasPermission name="sys:config:edit">
        <div onclick="showDialog();" data-options="iconCls:'easyui-icon-add'">新增</div>
        <div onclick="edit();" data-options="iconCls:'easyui-icon-edit'">编辑</div>
        <div onclick="del();" data-options="iconCls:'easyui-icon-remove'">删除</div>
    </e:hasPermission>
</div>

<div class="easyui-layout" fit="true" style="margin: 0px;border: 0px;overflow: hidden;width:100%;height:100%;">
    <div data-options="region:'north',title:'过滤条件',collapsed:false,split:false,border:false"
         style="padding: 0px; height: 70px;width:100%; overflow-y: hidden;">
        <form id="config_search_form" style="padding: 5px;">
            &nbsp;关键字： &nbsp;<input type="text" class="easyui-validatebox textbox eu-input" name="query" placeholder="请输入关键字..."
                              onkeydown="if(event.keyCode==13)search()"  maxLength="36" style="width: 160px" />
            &nbsp;<a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-search',width:100,height:28,onClick:search">查 询</a>
            <%--<a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-no',width:100,height:28" onclick="javascript:$config_search_form.form('reset');">重置</a>--%>
        </form>
    </div>
    <%-- 中间部分 列表 --%>
    <div data-options="region:'center',split:false,border:false"
         style="padding: 0px; height: 100%;width:100%; overflow-y: hidden;">
        <table id="config_datagrid"></table>
    </div>
</div>


   