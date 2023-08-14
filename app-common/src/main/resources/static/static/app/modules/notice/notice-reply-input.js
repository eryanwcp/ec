var modelIsReply = modelIsReply;
var jsessionid = jsessionid;
var modelFileIds = modelFileIds;
var fileSizeLimit = fileSizeLimit;

$(function () {
    uploadify();
    editor();

});

function editor() {
    $("#editor").kendoEditor({
        encoded: false,
        resizable: {
            content: true,
            toolbar: true
        }
    });
}



var fileIdArray = modelFileIds;
var fileIds = fileIdArray.join(",");
$("#fileIds").val(fileIds);
var dataMap = new HashMap();

function uploadify() {
    $('#uploadify').Huploadify({
        auto: true,
        showUploadedPercent: true,
        showUploadedSize: true,
        uploader: ctxAdmin + '/notice/noticeReceiveInfo/upload',
        formData: {},
        fileObjName: 'uploadFile',
        multi: true,
        fileSizeLimit: fileSizeLimit, //单个文件大小，0为无限制，可接受KB,MB,GB等单位的字符串值
        removeTimeout: 24 * 60 * 60 * 1000,
        fileTypeExts: '*.*',
        onUploadStart: function (file) {
        },
        onInit: function (obj) {
        },
        onUploadComplete: function (file, data) {
            data = eval("(" + data + ")");
            if (data.code !== undefined && data.code === 1) {
                fileIdArray.push(data['obj']['id']);
                var _fileIds = fileIdArray.join(",");
                $("#fileIds").val(_fileIds);
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
    fileIdArray.splice($.inArray(fileId, fileIdArray), 1);
    var fileIds = fileIdArray.join(",");
    $("#fileIds").val(fileIds);
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
