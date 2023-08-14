<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<script type="text/javascript">
    var noticeId = '${noticeId}';
    $(function() {
        $('#notice_readInfo_datagrid').datagrid({
                    url : ctxAdmin + '/notice/noticeReceiveInfo/readInfoDatagrid/'+noticeId,
                    fit:true,
                    pagination:true,//底部分页
                    rownumbers:true,//显示行数
                    fitColumns:false,//自适应列宽
                    striped:true,//显示条纹
                    nowrap : true,
                    pageSize:20,//每页记录数
                    pageList: [10, 20, 50, 100, 1000, 99999],
                    remoteSort: false,//是否通过远程服务器对数据排序
                    idField : 'id',
                    columns : [ [
                        {field:'id',title:'主键',hidden:true,sortable:true,align:'right',width:80},
                        {field : 'userName', title : '姓名', width : 80},
                        {field : 'organName',title : '部门',width : 260,sortable:true},
                        {field : 'companyName',title : '单位',width : 260,sortable:true, hidden: true},
                        {field : 'isReadView',title : '是否阅读',width : 80,sortable:true},
                        {field : 'readTime',title : '阅读时间',width : 146,sortable:true},
                        {field : 'isReplyView',title : '是否回复',width : 80,sortable:true},
                        {field : 'replyTime',title : '回复时间',width : 146,sortable:true},
                        {field : 'replyContent',title : '回复内容',width : 300,sortable:true},
                        {field : 'replyFileIds',title : '回复附件',width : 100,sortable:true,
                            formatter: function (value, rowData, rowIndex) {
                                if(!value){
                                    return "";
                                }
                                return "<a href='#' onclick='downloadReplyFile(\"" + rowData["replyFileIds"] + "\")' >下载附件</a>";
                            }},
                    ]]
                }).datagrid('showTooltip');
    });
    function downloadReplyFile(fileIds){
        console.log(fileIds);
        $('#annexFrame').attr('src', ctxAdmin + "/disk/downloadDiskFile?fileIds="+ fileIds);
    }
</script>
<table id="notice_readInfo_datagrid"></table>
<iframe id="annexFrame" style="display:none" src=""></iframe>
