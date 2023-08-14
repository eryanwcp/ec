<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<script type="text/javascript">
    var folderId = "${model.id}";
    var sessionId = "${sessionInfo.sessionId}";
    var categorys_combotree;
    var fileSizeLimit = '<%=AppConstants.getDiskMaxUploadSize()%>';//附件上传大小限制
    $(function(){
        uploadify();

    });
    function loadcategoryType(){
        categorys_combotree = $("#categoryIds").combotree({
            url:'${ctxAdmin}/repository/categoryR/categoryComTree',
            multiple:true,//是否可多选
            editable:false,
            cascadeCheck:false
        });

    }

    var fileIdArray = [];
    var dataMap = new HashMap();
    /**
     * 文件上传
     */
    function uploadify() {
        $('#uploadify').Huploadify({
            auto:true,
            showUploadedPercent:true,
            showUploadedSize:true,
            uploader: ctxAdmin + '/disk/fileUpload',
            formData:{folderId:folderId},
            fileObjName: 'uploadFile',
            multi: true,
            fileSizeLimit: fileSizeLimit, //单个文件大小，0为无限制，可接受KB,MB,GB等单位的字符串值
            removeTimeout:24*60*60*1000,
            fileTypeExts: '*.*',
            onUploadStart:function(file){
            },
            onInit:function(obj){
            },
            onUploadComplete: function (file, data) {
                data = eval("(" + data + ")");
                if (data.code === 1) {
                    fileIdArray.push(data['obj']['id']);
                    dataMap.put(file.index,data.obj);
                } else {
                    $('#' + file.id).find('.data').html(' - ' + "<font color=#D94600>" + data.msg + "</font>");
                }
            },
            onCancel:function(file){
                var sf = dataMap.get(file['index']);
                delUpload(sf['id']);
                dataMap.remove(file['index']);
            }

        });
    }
    /**
     * 删除附件 页面删除
     * @param fileId 后台File ID
     */
    function delUpload(fileId) {
        fileIdArray.splice($.inArray(fileId,fileIdArray),1);
        delUploadFile(fileId);
    }

    /**
     * 删除附件 后台删除
     * @param fileId 后台File ID
     */
    function delUploadFile(fileId) {
        $.ajax({
            url: ctxAdmin + '/disk/delFolderFile',
            type: 'post',
            data: {fileIds: fileId},
            dataType: 'json',
            traditional: true,
            success: function(data) {
                if (1 === data.code) {
                    $("#"+fileId).remove();
                }
            }
        });
    }
</script>
<div>
    <form id="file_form" method="post"  class="dialog-form" novalidate>
        <div id="tip_msg">
        </div>
        <input type="hidden"  name="id" />
        <!-- 版本控制字段 version -->
        <input type="hidden" id="version" name="version" />
        <div>
            <%--提示小图标--%>
            <span class="tree-icon tree-file easyui-icon-tip easyui-tooltip"
                  title="单个文件上传限制1GB." ></span>
            <div id="uploadify"></div>
        </div>

    </form>
</div>