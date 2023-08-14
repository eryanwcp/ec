<%@ page import="com.eryansky.core.security.SecurityUtils" %>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<%@ include file="/common/meta.jsp"%>
<script type="text/javascript">
    var hasPermissionResourceEdit = <%= SecurityUtils.isPermitted("sys:resource:edit")%>;
    var hasPermissionResourceRoleEdit = <%= SecurityUtils.isPermitted("sys:resource:role:edit")%>;
    var hasPermissionResourceUserEdit = <%= SecurityUtils.isPermitted("sys:resource:user:edit")%>;
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/resource${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<link rel="stylesheet" href="${ctx}/js/iconfont/iconfont.css?_=${sysInitTime}">
<div class="easyui-layout" fit="true" style="margin: 0px;border: 0px;overflow: hidden;width:100%;height:100%;">

    <%-- 列表右键 --%>
    <div id="resource_menu" class="easyui-menu" style="width:120px;display: none;">
        <e:hasPermission name="sys:resource:edit">
            <div onclick="showDialog();" data-options="iconCls:'easyui-icon-add'">新增</div>
            <div onclick="edit();" data-options="iconCls:'easyui-icon-edit'">编辑</div>
            <div onclick="del();" data-options="iconCls:'easyui-icon-remove'">删除</div>
        </e:hasPermission>
        <e:hasPermission name="sys:resource:role:edit">
            <div onclick="resourceRole();" data-options="iconCls:'eu-icon-lock'">角色</div>
        </e:hasPermission>
        <e:hasPermission name="sys:resource:user:edit">
            <div onclick="resourceUser();" data-options="iconCls:'eu-icon-user'">用户</div>
        </e:hasPermission>
    </div>

    <%-- 中间部分 列表 --%>
    <div data-options="region:'center',split:false,border:false"
         style="padding: 0px; height: 100%;width:100%; overflow-y: hidden;">
        <table id="resource_treegrid" ></table>

    </div>
</div>