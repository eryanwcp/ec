var modelId = modelId;
var modelStatus = modelStatus;
var jsessionid = jsessionid;

var organs_combotree;
$(function () {
    loadOrgan();
    loadSex();

    if ($('#image').val()) {
        addImageFile($('#image').val());
    }
    uploadifyImage();
    if (modelId === "") {  //新增
        setSortValue();
        $("input[name=status]:eq(0)").prop("checked", 'checked');//状态 初始化值

        $('#password_div').css('display', 'block');
        $('#repassword_div').css('display', 'block');
    } else {
        $('input[name=status][value=' + modelStatus + ']').prop("checked", 'checked');
        $('#password_div').css('display', 'none');
        $('#repassword_div').css('display', 'none');
        $('#password_div input').removeAttr('class');
        $('#repassword_div input').removeAttr('class');
    }
});

function loadOrgan() {
    organs_combotree = $("#defaultOrganId").combotree({
        url: ctxAdmin + '/sys/organ/tree?dataScope=4&cascade=true',
        multiple: false,
        editable: false
    });
}

//性别
function loadSex() {
    $('#sex').combobox({
        url: ctxAdmin + '/sys/user/sexTypeCombobox?selectType=select',
        editable: false
    });
}

//设置排序默认值
function setSortValue() {
    $.ajax({
        url: ctxAdmin + '/sys/user/maxSort',
        type: 'post',
        data: {},
        traditional: true,
        dataType: 'json',
        success: function (data) {
            if (data.code === 1) {
                $('#orderNo').numberspinner('setValue', data.obj + 30);
            }
        }
    });
}

function addImageFile(id, url) {
    $('#image').val(id);
    if (url) {
        $('#image_pre').attr("src", url);
    }
    $('#image_pre').show();
    $('#image_pre').next().show();
    var left = $('#image_pre').position().left;
    var top = $('#image_pre').position().top;
    $('#image_cencel').css({position: "absolute", left: left + 75, top: top + 10, display: "block"});
}

function delImageFile() {
    $('#image_pre').attr("src", "");
    $('#image_pre').hide();
    $('#image_pre').next().hide();
    $('#image').val("");
}

var imageDataMap = new HashMap();

function uploadifyImage() {
    $('#image_uploadify').Huploadify({
        auto: true,
        showUploadedPercent: true,
        showUploadedSize: true,
        uploader: ctxAdmin + '/sys/user/upload',
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
                imageDataMap.put(file.index, data.obj);
            } else {
                eu.showAlertMsg(data['msg']);
            }
        },
        onCancel: function (file) {
            var sf = imageDataMap.get(file['index']);
            delImageFile(sf['id']);
            imageDataMap.remove(file['index']);
        }

    });
}

//登录名 同步成员工编号
function sync() {
    $("#code").val($("#loginName").val());
}