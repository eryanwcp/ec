<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<%@ include file="/common/meta.jsp"%>
<%@ include file="/common/huploadify.jsp"%>
<script type="text/javascript">
    var isAdmin = ${not empty isAdmin ? isAdmin: false};
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/disk/disk${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<div id="folder_treeMenu_all" class="easyui-menu" style="width:120px;">
    <div name="addFolder" data-options="iconCls:'easyui-icon-add'">新建文件夹</div>
    <div name="editFolder" data-options="iconCls:'easyui-icon-edit'">编辑</div>
    <div name="deleteFolder" data-options="iconCls:'easyui-icon-remove'">删除</div>
</div>
<div id="folder_treeMenu_add" class="easyui-menu" style="width:120px;">
    <div name="addFolder" data-options="iconCls:'easyui-icon-add'">新建文件夹</div>
</div>
<div class="easyui-layout" fit="true" style="margin: 0px;border: 0px;overflow: hidden;width:100%;height:100%;">
    <%-- 左边部分 菜单树形 --%>
    <div data-options="region:'west',title:'我的云盘',split:true,collapsed:false,border:false"
         style="width:180px; text-align: left;padding:5px;">
           <div style="padding: 5px;">
            <a onclick="try {
                        parent.addTabs({id:'diskSearch',title: '文件检索',close: true,url: '${ctxAdmin}/disk/search',urlType: ''});
                    } catch(e) {
                        eu.addTab(window.parent.layout_center_tabs, '文件检索','${ctxAdmin}/disk/search', true,'eu-icon-disk_search','',false);
                    }" class="easyui-linkbutton"
               data-options="iconCls:'eu-icon-disk_search'" style="width: 152px;">文件检索</a>
           </div>
      <hr>
      <div style="padding: 5px;">
         <a onclick="showFolderDialog();" class="easyui-linkbutton"
               data-options="iconCls:'eu-icon-disk_folder_add',toggle:true,selected:true"
               style="width:110px;">新建文件夹</a>
            <span class="tree-icon tree-file easyui-icon-tip easyui-tooltip" data-options="position:'right'"
                  title="点击鼠标右键,可新建文件夹." ></span>
      </div>
        <ul id="folder_tree"></ul>
    </div>
    <!-- 中间部分 列表 -->
    <div data-options="region:'center',split:true" style="overflow: hidden;">
        <div class="easyui-layout" fit="true" style="margin: 0;border: 0;overflow: hidden;width:100%;height:100%;">
            <div data-options="region:'center',split:true" style="overflow: hidden;">
                <table id="folder_file_datagrid" ></table>
            </div>

            <div data-options="region:'north',title:'过滤条件',split:false,collapsed:false,border:false"
                 style="width: 100%;height:70px;overflow-y: hidden;">
                <form id="folder_file_search_form" style="padding: 5px;">
                    &nbsp;&nbsp;文件名：<input type="text" id="fileName" name="fileName" placeholder="文件名..." class="easyui-validatebox textbox eu-input"
                                           onkeydown="if(event.keyCode==13)search()"  maxLength="25" style="width: 160px"/>
                    <a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-search',width:100,height:28,onClick:search">查 询</a>
                    <a class="easyui-linkbutton" href="#" data-options="iconCls:'easyui-icon-no',width:100,height:28" onclick="$user_search_form.form('reset');">重置</a>
                </form>
            </div>
        </div>
    </div>
</div>
<iframe id="annexFrame" src="" frameborder="no" style="padding: 0;border: 0;width: 0;height: 0;display: none;"></iframe>