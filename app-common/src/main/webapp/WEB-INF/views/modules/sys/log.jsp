<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<%@ include file="/common/meta.jsp" %>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/log${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<%-- 列表右键 --%>
<div id="log_menu" class="easyui-menu" style="width:120px;display: none;">
    <e:isAdmin>
        <div onclick="del()" data-options="iconCls:'easyui-icon-remove'">删除</div>&nbsp;
    </e:isAdmin>

</div>
<div class="easyui-layout" fit="true" style="margin: 0px;border: 0px;overflow: hidden;width:100%;height:100%;">
    <div data-options="region:'north',title:'过滤条件',collapsed:false,split:false,border:false"
         style="padding: 0px; height: 100px;width:100%; overflow-y: hidden;">
        <form id="log_search_form" style="padding: 5px;">
            &nbsp;日志类型：<input id="_type" name="type" />
            &nbsp;账号或姓名：<input type="text" name="userInfo"
                               class="easyui-validatebox textbox eu-input" placeholder="账号或姓名..."  onkeydown="if(event.keyCode==13)search()"
                               maxLength="25" style="width: 160px" />
            &nbsp;关键字：<input type="text" name="query"
                             class="easyui-validatebox textbox eu-input" placeholder="关键字..."  onkeydown="if(event.keyCode==13)search()"
                             maxLength="30" style="width: 160px" />
            &nbsp;时间：<input type="text" id="startTime" name="startTime" class="easyui-my97" value="${startTime}" placeholder="起始时间..."/>~<input type="text" id="endTime" name="endTime" class="easyui-my97"  placeholder="结束时间..."/>
            &nbsp;<a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-search',width:100,height:28,onClick:search">查 询</a>
            <a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-no',width:100,height:28" onclick="$log_search_form.form('reset');">重置</a>
        </form>
    </div>

    <%-- 中间部分 列表 --%>
    <div data-options="region:'center',split:false,border:false"
         style="padding: 0px; height: 100%;width:100%; overflow-y: hidden;">
        <table id="log_datagrid"></table>
    </div>

</div>
<iframe id="annexFrame" style="display:none" src=""></iframe>