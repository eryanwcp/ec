var modelIsTop = modelIsTop;
var modelIsReply = modelIsReply;
var modelTipMessage = modelTipMessage;
var modelType = modelType;
var dictionaryTypeCode = dictionaryTypeCode;
var modelFileIds = modelFileIds;
var noticeReceiveUserIds = noticeReceiveUserIds;
var noticeReceiveOrganIds = noticeReceiveOrganIds;
var noticeReceiveContactGroupIds = noticeReceiveContactGroupIds;
var jsessionid = jsessionid;
var fileSizeLimit = fileSizeLimit;

var $form_noticeUser_MultiSelect = undefined;
var $form_noticeOrgan_combotree = undefined;
var $form_contactGroup_combotree = undefined;
$(function () {
    loadReceiveScope();

    var $isTop = $("input[name=isTop]:eq(" + modelIsTop + ")");
    $isTop.prop("checked", 'checked');
    toggoleIsTop($isTop.val());
    $("input[name=isTop]").bind("click", function () {
        toggoleIsTop($(this).val());
    });
    var $isReply = $("input[name=isReply]:eq(" + modelIsReply + ")");
    $isReply.prop("checked", 'checked');

    if (modelTipMessage) {
        var val = modelTipMessage.split(",");
        var boxes = document.getElementsByName("tipMessage");
        for (i = 0; i < boxes.length; i++) {
            for (j = 0; j < val.length; j++) {
                if (boxes[i].value === val[j]) {
                    boxes[i].checked = true;
                    break
                }
            }
        }
    }
    uploadify();
    editor();

    if ($('#head_image').val()) {
        addImageFile($('#head_image').val());
    }
    uploadifyHeadImage();
    loadContactGroup();
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

/**
 * 数据字典 Javascript调用方式
 */
function loadNoticeType() {
    $('#notice_type').combobox({
        url: ctxAdmin + '/sys/dictionary/combobox?dictionaryTypeCode=' + dictionaryTypeCode + '&selectType=select',
        multiple: false,//是否可多选
        editable: false,//是否可编辑
        width: 120,
        height: 28,
        value: modelType,
        validType: ['comboboxRequired[\'#notice_type\']']
    });
}


function loadReceiveScope() {
    $('#receiveScope').combobox({
        url: ctxAdmin + '/notice/receiveScopeCombobox',
        multiple: false,
        editable: false,
        required: true,
        missingMessage: '请选择接收范围.',
        onChange: function (newValue, oldValue) {
            toggoleReceiveScope(newValue);
        },
        onLoadSuccess: function () {
            var value = $(this).combobox("getValue");
            toggoleReceiveScope(value);
        }
    });
}


//按自定义组
function loadContactGroup() {
    $form_contactGroup_combotree = $("#_noticeContactGroupIds").combotree({
        url: ctxAdmin + "/notice/contactGroup/groupTree?contactGroupType=0",
        multiple: true,//是否可多选
        editable: false,
        value: noticeReceiveContactGroupIds
    });
}

function toggoleReceiveScope(receiveScope) {
    if ("0" === receiveScope) {
        $("#noticeUserIds_div").show();
        $("#noticeOrganIds_div").show();
        $("#noticeContactGroupIds_div").show();
        if ($form_noticeUser_MultiSelect === undefined) {
            loadNoticeUser();
        }
        if ($form_noticeOrgan_combotree === undefined) {
            loadNoticeOrgan();
        }
    } else {
        $("#noticeUserIds_div").hide();
        if ($form_noticeUser_MultiSelect !== undefined) {
            $form_noticeUser_MultiSelect.value(" ");
        }
        $("#noticeOrganIds_div").hide();
        if ($form_noticeOrgan_combotree !== undefined) {
            $form_noticeOrgan_combotree.combotree("setValue", "");
        }
        $("#noticeContactGroupIds_div").hide();
        if ($form_contactGroup_combotree !== undefined) {
            $form_contactGroup_combotree.combotree("setValue", "");
        }
    }
}

function toggoleIsTop(isTop) {
    if ("0" === isTop) {//不置顶
        $("#endTopDay_span").hide();
        //TODO 清空#endTopDay
    } else {
        $("#endTopDay_span").show();
        if (!$("#endTopDay").numberspinner().numberspinner("getValue")) {
            $("#endTopDay").numberspinner("setValue", 7);
        }
    }
}

function loadNoticeOrgan() {
    $form_noticeOrgan_combotree = $("#_noticeOrganIds").combotree({
        url: ctxAdmin + '/sys/organ/tree?dataScope=4&cascade=true',
        multiple: true,//是否可多选
        editable: false,
        value: noticeReceiveOrganIds
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
        uploader: ctxAdmin + '/notice/upload',
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


function addImageFile(id, url) {
    $('#head_image').val(id);
    if (url) {
        $('#head_image_pre').attr("src", url);
    }
    $('#head_image_pre').show();
    $('#head_image_pre').next().show();
    var left = $('#head_image_pre').position().left;
    var top = $('#head_image_pre').position().top;
    $('#head_image_cencel').css({position: "absolute", left: left + 75, top: top + 10, display: "block"});
}

function delImageFile() {
    $('#head_image_pre').attr("src", "");
    $('#head_image_pre').hide();
    $('#head_image_pre').next().hide();
    $('#head_image').val("");
}

var photoDataMap = new HashMap();

function uploadifyHeadImage() {
    $('#head_image_uploadify').Huploadify({
        auto: true,
        showUploadedPercent: true,
        showUploadedSize: true,
        uploader: ctxAdmin + '/notice/upload',
        formData: {},
        fileObjName: 'uploadFile',
        buttonText: '浏 览',
        multi: false,
        fileSizeLimit: fileSizeLimit, //单个文件大小，0为无限制，可接受KB,MB,GB等单位的字符串值
        removeTimeout: 24 * 60 * 60 * 1000,
        fileTypeExts: '*.gif; *.jpg; *.png; *.bmp',  //上传的文件后缀过滤器
        //上传到服务器，服务器返回相应信息到data里
        onUploadSuccess: function (file, data, response) {
            data = eval("(" + data + ")");
            if (1 === data['code']) {
                addImageFile(data['obj']['id'], data['obj']['url']);
                photoDataMap.put(file.index, data.obj);
            } else {
                eu.showAlertMsg(data['msg']);
            }
        },
        onCancel: function (file) {
            var sf = photoDataMap.get(file['index']);
            delImageFile(sf['id']);
            photoDataMap.remove(file['index']);
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

/**
 *
 */
function loadNoticeUser() {
    var dataSource = {data: [], group: {field: "defaultOrganName"}};
    $form_noticeUser_MultiSelect = $("#noticeUserIds").kendoMultiSelect({
        dataTextField: "name",
        dataValueField: "id",
        dataSource: dataSource,
        value: noticeReceiveUserIds,
        dataBound: function (e) {

        }

    }).data("kendoMultiSelect");
    $.ajax({
        type: "post",
        dataType: 'json',
        contentType: "application/json",
        url: ctxAdmin + '/sys/user/userList?dataScope=4',
        //async: false,
        success: function (data) {
            var dataSource = {data: data, group: {field: "defaultOrganName"}};
            $form_noticeUser_MultiSelect.setDataSource(dataSource);
            $form_noticeUser_MultiSelect.value(noticeReceiveUserIds);
        }
    });

}

function _selectUser() {
    var userIds = "";
    var dataItems = $form_noticeUser_MultiSelect.dataItems();
    if (dataItems && dataItems.length > 0) {
        var num = dataItems.length;
        $.each(dataItems, function (n, value) {
            if (n === num - 1) {
                userIds += value.id;
            } else {
                userIds += value.id + ",";
            }

        });

    }
    var input_selectUser_dialog = $("<div/>").dialog({
        title: "选择用户",
        top: 10,
        href: ctxAdmin + '/sys/user/select?dataScope=4&cascade=true&userIds=' + userIds,
        width: '700',
        height: '450',
        maximizable: true,
        iconCls: 'easyui-icon-edit',
        modal: true,
        buttons: [
            {
                text: '确定',
                iconCls: 'easyui-icon-save',
                handler: function () {
                    _setSelectUser();
                    input_selectUser_dialog.dialog('destroy');
                }
            },
            {
                text: '关闭',
                iconCls: 'easyui-icon-cancel',
                handler: function () {
                    input_selectUser_dialog.dialog('destroy');

                }
            }
        ],
        onClose: function () {
            input_selectUser_dialog.dialog('destroy');
        }
    });
}

function _setSelectUser() {
    var selectUserIds = [];
    $("#selectUser option").each(function () {
        var txt = $(this).val();
        selectUserIds.push($.trim(txt));
    });
    $form_noticeUser_MultiSelect.value(selectUserIds);
}