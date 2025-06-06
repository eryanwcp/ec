<%@ page import="com.eryansky.core.security.SecurityUtils" %>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<%@ include file="/common/meta.jsp" %>
<script type="text/javascript">
    var sessionInfoUserId = "${sessionInfo.userId}";//当前的登录用户id
    var hasPermissionDictionaryEdit = <%= SecurityUtils.isPermitted("sys:dictionary:edit")%>;
    var toolbar = [];
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/dictionary${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<%-- 数据字典右键操作 --%>
<div id="treeMenu" class="easyui-menu" style="width:120px;">
    <e:hasPermission name="sys:dictionary:edit">
        <div name="edit" data-options="iconCls:'easyui-icon-edit'">编辑</div>
        <div name="delete" data-options="iconCls:'easyui-icon-remove'">删除</div>
    </e:hasPermission>
</div>
<div class="easyui-layout" fit="true" style="margin: 0;border: 0;overflow: hidden;width:100%;height:100%;">

    <%-- 左边部分 数据字典树形 --%>
    <div data-options="region:'west',title:'数据字典',split:true,collapsed:false,border:false"
         style="width: 180px; text-align: left; padding: 2px;">
        <div style="padding: 5px;">
            <a onclick="showDictionaryDialog();" class="easyui-linkbutton"
               data-options="iconCls:'easyui-icon-add',toggle:true,selected:true"
               style="width:120px;">新增数据字典</a>
            <span class="tree-icon tree-file easyui-icon-tip easyui-tooltip" data-options="position:'right'"
                  title="点击鼠标右键."></span>
        </div>
        <ul id="dictionary_tree"></ul>
    </div>

    <!-- 中间部分 列表 -->
    <div data-options="region:'center',split:true"
         style="overflow: hidden;">
        <div class="easyui-layout" fit="true"
             style="margin: 0; border: 0; overflow: hidden; width: 100%; height: 100%;">
            <div data-options="region:'north',title:'过滤条件',collapsed:false,split:false,border:false"
                 style="padding: 0px; height: 70px;width:100%; overflow-y: hidden;">
                <span style="display: none;">&nbsp;</span><%--兼容IE8--%>
                <form id="dictionaryItem_search_form" style="padding: 5px;">
                    关键字: &nbsp;<input type="text" id="query" name="query" placeholder="关键字..."
                                  class="easyui-validatebox textbox eu-input"
                                  onkeydown="if(event.keyCode==13)search()" maxLength="36" style="width: 160px"/>
                    &nbsp;<a class="easyui-linkbutton" href="#"
                             data-options="iconCls:'easyui-icon-search',width:100,height:28,onClick:search">查询</a>
                    <a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-no',width:100,height:28"
                       onclick="$dictionaryItem_search_form.form('reset');">重置</a>
                </form>
            </div>

            <%-- 中间部分 列表 --%>
            <div data-options="region:'center',split:false,border:false"
                 style="padding: 0; height: 100%;width:100%; overflow-y: hidden;">
                <table id="dictionaryItem_datagrid"></table>
            </div>
        </div>

    </div>


</div>