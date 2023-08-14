<%@ page import="com.eryansky.core.security.SecurityUtils" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<%@ include file="/common/meta.jsp"%>
<%@ include file="/common/autocomplete.jsp"%>
<%@ include file="/common/huploadify.jsp"%>
<link href="${ctxStatic}/js/bootstrap/2.3.2/custom/bootstrap-img.css" rel="stylesheet" type="text/css"/>
<%--<%@ include file="/common/zTree.jsp"%>--%>
<script type="text/javascript">
    var sessionInfoUserId = "${sessionInfo.userId}";//当前的登录用户ID
    var hasPermissionUserAdd = <%= SecurityUtils.isPermitted("sys:user:add")%>;
    var hasPermissionUserDelete = <%= SecurityUtils.isPermitted("sys:user:delete")%>;
    var hasPermissionUserEdit = <%= SecurityUtils.isPermitted("sys:user:edit")%>;
    var hasPermissionUserPasswordEdit = <%= SecurityUtils.isPermitted("sys:user:password:edit")%>;
    var hasPermissionUserOrganEdit = <%= SecurityUtils.isPermitted("sys:user:organ:edit")%>;
    var hasPermissionUserPostEdit = <%= SecurityUtils.isPermitted("sys:user:post:edit")%>;
    var hasPermissionUserRoleEdit = <%= SecurityUtils.isPermitted("sys:user:role:edit")%>;
    var hasPermissionUserResourceEdit = <%= SecurityUtils.isPermitted("sys:user:resource:edit")%>;
    var toolbar = [];
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/user-extend-before${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/user${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/user-extend-after${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<%-- 列表右键 --%>
<div id="user_datagrid_menu" class="easyui-menu" style="width:120px;display: none;">
    <e:hasPermission name="sys:user:add">
        <div onclick="showDialog();" data-options="iconCls:'easyui-icon-add'">新增</div>
    </e:hasPermission>
    <e:hasPermission name="sys:user:edit">
        <div onclick="edit();" data-options="iconCls:'easyui-icon-edit'">编辑</div>
    </e:hasPermission>
    <e:hasPermission name="sys:user:delete">
        <div onclick="del();" data-options="iconCls:'easyui-icon-remove'">删除</div>
    </e:hasPermission>
    <e:hasPermission name="sys:user:password:edit">
        <div onclick="editPassword();" data-options="iconCls:'eu-icon-lock'">修改密码</div>
    </e:hasPermission>
    <e:hasPermission name="sys:user:organ:edit">
        <div onclick="editUserOrgan();" data-options="iconCls:'eu-icon-group'">设置机构</div>
    </e:hasPermission>
    <e:hasPermission name="sys:user:post:edit">
        <div onclick="editUserPost();" data-options="iconCls:'eu-icon-group'">设置岗位</div>
    </e:hasPermission>
    <e:hasPermission name="sys:user:role:edit">
        <div onclick="editUserRole();" data-options="iconCls:'eu-icon-group'">设置角色</div>
    </e:hasPermission>
    <e:hasPermission name="sys:user:resource:edit">
        <div onclick="editUserResource();" data-options="iconCls:'eu-icon-folder'">设置资源</div>
    </e:hasPermission>
    <div onclick="viewUserResources();" data-options="iconCls:'easyui-icon-search'">查看权限</div>
    <div onclick="move(true);" data-options="iconCls:'eu-icon-up'">上移</div>
    <div onclick="move();" data-options="iconCls:'eu-icon-down'">下移</div>
    <e:hasPermission name="sys:user:edit">
        <div onclick="lock(false);" data-options="iconCls:'eu-icon-user'">启用</div>
        <div onclick="lock(true);" data-options="iconCls:'eu-icon-lock'">停用</div>
    </e:hasPermission>
</div>
<%-- easyui-layout布局 --%>
<div class="easyui-layout" fit="true" style="margin: 0px;border: 0px;overflow: hidden;width:100%;height:100%;">

    <%-- 左边部分 菜单树形 --%>
    <div data-options="region:'west',title:'组织机构列表',split:false,collapsed:false,border:false"
         style="width: 180px; text-align: left;padding:5px;">
        <ul id="organ_tree"></ul>
    </div>

    <!-- 中间部分 列表 -->
    <div data-options="region:'center',split:true" style="overflow: hidden;">
        <div class="easyui-layout" fit="true" style="margin: 0px;border: 0px;overflow: hidden;width:100%;height:100%;">
            <div data-options="region:'center',split:true" style="overflow: hidden;">
                <table id="user_datagrid" ></table>
            </div>

            <div data-options="region:'north',title:'过滤条件',split:false,collapsed:false,border:false"
                 style="width: 100%;height:70px;overflow-y: hidden; ">
                <form id="user_search_form" style="padding: 5px;">
                    &nbsp;关键字：<input type="text" id="query" name="query"
                                        class="easyui-validatebox textbox eu-input" placeholder="关键字..."
                                        onkeydown="if(event.keyCode==13)search()" maxLength="25" style="width: 160px;height: 28px;"/>
                    &nbsp;<a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-search',width:100,height:28,onClick:search">查 询</a>
                     <a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-no',width:100,height:28" onclick="$user_search_form.form('reset');">重置</a>
                </form>
            </div>
        </div>
    </div>
</div>