var modelId = modelId;
var modelIsPub = modelIsPub;
var modelIsTip = modelIsTip;
var modelIsShelf = modelIsShelf;
var modelFileId = modelFileId;
var fileSizeLimit = fileSizeLimit;
var $versionLogType_combobox = undefined;
$(function () {
    if (modelId === "") {  //新增
        $("input[name=isPub]:eq(0)").prop("checked", 'checked');//状态 初始化值
        $("input[name=isTip]:eq(0)").prop("checked", 'checked');//状态 初始化值
        $("input[name=isShelf]:eq(1)").prop("checked", 'checked');//状态 初始化值
    } else {
        $('input[name=isPub][value=' + modelIsPub + ']').prop("checked", 'checked');
        $('input[name=isTip][value=' + modelIsTip + ']').prop("checked", 'checked');
        $('input[name=isShelf][value=' + modelIsShelf + ']').prop("checked", 'checked');
    }
    uploadify();
    loadVersionLogType();
});

function loadVersionLogType(){
    //日志类型 搜索选项
    $versionLogType_combobox= $('#version_log_type').combobox({
        url: ctxAdmin + '/sys/versionLog/versionLogTypeCombobox?selectType=select',
        multiple: false,
        editable: false,
        width: 120
    });
}
var dataMap = new HashMap();

function uploadify() {
    $('#uploadify').Huploadify({
        auto: true,
        showUploadedPercent: true,
        showUploadedSize: true,
        uploader: ctxAdmin + '/sys/versionLog/upload',
        formData: {},
        fileObjName: 'uploadFile',
        multi: false,
        fileSizeLimit: fileSizeLimit, //单个文件大小，0为无限制，可接受KB,MB,GB等单位的字符串值
        removeTimeout: 24 * 60 * 60 * 1000,
        fileTypeExts: '*.*',
        onUploadStart: function (file) {
        },
        onInit: function (obj) {
        },
        onUploadComplete: function (file, data) {
            data = eval("(" + data + ")");
            if (data.code === 1) {
                $("#fileId").val(data['obj']['id']);
                dataMap.put(file.index, data.obj);
            } else {
                eu.showAlertMsg(data['msg']);
            }
        },
        onCancel: function (file) {
            var sf = dataMap.get(file['index']);
            delUpload(sf['id']);
            dataMap.remove(file['index']);
        }

    });
}

function loadOrOpen(fileId) {
    $('#annexFrame').attr('src', ctxAdmin + '/disk/fileDownload/' + fileId);
}

/**
 * 删除附件 页面删除
 * @param fileId 后台File ID
 */
function delUpload(fileId) {
    $("#fileId").val('');
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
        success: function (data) {
            if (1 === data.code) {
                $("#" + fileId).remove();
            }
        }
    });
}