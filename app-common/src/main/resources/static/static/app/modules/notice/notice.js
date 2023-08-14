var noticeId = noticeId;
var objectType = objectType;
var objectId = objectId;
var fileIds = fileIds;
var title = title;
var content = content;
var receiveUserIds = receiveUserIds;
var receiveOrganIds = receiveOrganIds;
var receiveContactGroupIds = receiveContactGroupIds;
var hasRepeatPermission = hasRepeatPermission;
var isSuperUser = isSuperUser;


var $notice_datagrid;
var $notice_dialog;
var $notice_form;
var $notice_reply_dialog;
var $notice_reply_form;
var $form_noticeUser_MultiSelect;//notice-input.jsp

var $notice_search_form;
var read_linkbutton_text = "我的通知";
var $query_PublishUser_MultiSelect;

var _operateType = undefined;//全局变量
$(function () {
    if (isSuperUser) {//meta.jsp
        $("#publishUserIds_tr").show();
        $("#layout_north").panel("resize", {height: 156});
        $.parser.parse($(".easyui-layout").parent());
    }
    $notice_search_form = $('#notice_search_form').form();
    initReadDatagrid();
    initSelectUser();

    if (noticeId !== "" && objectId === '') {
        $.ajax({
            url: ctxAdmin + '/notice/noticeReceiveInfo/detail?noticeId='+noticeId,
            type: 'post',
            dataType: 'json',
            success: function (data) {
                if (data.code === 1) {
                    var m = data['obj'];
                    window.setTimeout(function () {
                        view(noticeId, hasRepeatPermission,m['id'],m['isNeedReply'],m['isReply']);
                    }, 500);
                }
            }
        });

    } else if (objectId) {
        window.setTimeout(function () {
            edit(noticeId, undefined, objectType, objectId, title, content, fileIds, receiveUserIds, receiveOrganIds, receiveContactGroupIds);
        }, 500);
    }


    mymessage();
});

function refreshMessage() {
    mymessage();
    //刷新 portal消息
    try {
        parent.refreshPortal();
    } catch (e) {
    }
}

function mymessage() {
    $.ajax({
        url: ctxAdmin + '/notice/myMessage',
        type: 'post',
        dataType: 'json',
        success: function (data) {
            if (data.code === 1) {
                var obj = data.obj;
                if (obj["noticeScopes"] > 0) {
                    var text = read_linkbutton_text + "&nbsp;<span style='color: red;'>（" + obj["noticeScopes"] + "）</span>";
                    $("#read_linkbutton").linkbutton({text: text});
                } else {
                    $("#read_linkbutton").linkbutton({text: read_linkbutton_text});
                }

            }
        }
    });
}

function initReadDatagrid() {
    $notice_datagrid = $('#notice_datagrid').datagrid({
        url: ctxAdmin + '/notice/noticeReceiveInfo/readInfoDatagrid',
        fit: true,
        pagination: true,//底部分页
        rownumbers: true,//显示行数
        fitColumns: false,//自适应列宽
        striped: true,//显示条纹
        pageSize: 20,//每页记录数
        checkOnSelect: false,
        selectOnCheck: false,
        idField: 'id',
        frozenColumns: [[{field: 'ck', checkbox: true},
            {
                field: 'title', title: '标题', width: 360,
                formatter: function (value, rowData, rowIndex) {
                    return "<a href='#' onclick='view(\"" + rowData["noticeId"] + "\"," + hasRepeatPermission + ",\"" + rowData["id"] + "\",\"" + rowData["isNeedReply"] + "\",\"" + rowData["isReply"] + "\")' >" + value + "</a>";
                }
            }
        ]],
        columns: [[{
            field: 'id',
            title: '主键',
            hidden: true,
            sortable: true,
            align: 'right',
            width: 80
        }, {
            field: 'publishUserName',
            title: '发布人',
            width: 100
        }, {
            field: 'publishTime',
            title: '发布时间',
            sortable: true,
            width: 146
        }, {
            field: 'isNeedReplyView',
            title: '是否需要回复',
            sortable: true,
            width: 100
        }, {
            field: 'isReplyView',
            title: '是否回复',
            sortable: true,
            width: 100
        }, {
            field: 'isReadView',
            title: '通知状态',
            sortable: true,
            width: 100
        }]],
        toolbar: [{
            text: '标记为已读',
            iconCls: 'eu-icon-mail_mark_read',
            handler: function () {
                markReaded();
            }
        }],
        onLoadSuccess: function () {
            $(this).datagrid('clearSelections');
            $(this).datagrid('clearChecked');
            $(this).datagrid('unselectAll');
            $(this).datagrid('uncheckAll');
        }
    }).datagrid('showTooltip');
}

function initDatagrid() {
    $notice_datagrid = $('#notice_datagrid').datagrid({
        url: ctxAdmin + '/notice/datagrid',
        fit: true,
        pagination: true,//底部分页
        fitColumns: false,//自适应列宽
        striped: true,//显示条纹
        pageSize: 20,//每页记录数
        singleSelect: false,//单选模式
        checkOnSelect: true,
        selectOnCheck: true,
        rownumbers: true,//显示行数
        checkbox: true,
        nowrap: true,
        border: false,
        idField: 'id',
        frozenColumns: [[
            {field: 'ck', checkbox: true},
            {
                field: 'title', title: '标题', width: 360,
                formatter: function (value, rowData, rowIndex) {
                    return "<a href='#' onclick='view(\"" + rowData["id"] + "\"," + hasRepeatPermission + ")' >" + value + "</a>";
                }
            }
        ]],
        columns: [[
            {field: 'id', title: '主键', hidden: true, sortable: true, width: 80},
            {field: 'typeView', title: '类型', width: 80},
            {field: 'publishUserName', title: '发布人', width: 80},
            {
                field: 'publishTime',
                title: '发布时间',
                width: 146,
                sortable: true
            },
            {
                field: 'effectTime',
                title: '生效时间',
                width: 136,
                sortable: true
            },
            {
                field: 'invalidTime',
                title: '终止时间',
                width: 136,
                sortable: true
            }, {
                field: 'isReplyView',
                title: '是否需要回复',
                sortable: true,
                width: 100
            },
            {
                field: 'modeView',
                title: '状态',
                width: 80
            },
            {
                field: 'operate',
                title: '操作',
                width: 260,
                formatter: function (value, rowData, rowIndex) {
                    var operateHtml = "";
                    var editHtml = "<a class='easyui-linkbutton' data-options='iconCls:\"easyui-icon-edit\"' onclick='edit(\"" + rowData["id"] + "\");' >编辑</a>";
                    if (rowData["bizMode"] === '0') {//未发布
                        operateHtml = editHtml + "&nbsp;<a class='easyui-linkbutton' data-options='iconCls:\"eu-icon-mail_forward\"' onclick='publish(\"" + rowData["id"] + "\");' >发布 </a>";
                    } else if (rowData["bizMode"] === "1") {//已发布
                        operateHtml += "&nbsp;<a class='easyui-linkbutton' data-options='iconCls:\"eu-icon-notice_stop\"' onclick='invalid(\"" + rowData["id"] + "\");' >终止</a>";
                        operateHtml += "&nbsp;<a class='easyui-linkbutton' data-options='iconCls:\"eu-icon-trip\"' onclick='push(\"" + rowData["id"] + "\");' >推送</a>";
                    }
                    if (rowData["isRecordRead"] === "1" && rowData["bizMode"] !== "0" && rowData["bizMode"] !== "3") {//记录查看情况
                        operateHtml += "&nbsp;<a class='easyui-linkbutton' data-options='iconCls:\"eu-icon-mail_find\"'  onclick='readInfo(\"" + rowData["id"] + "\");' >阅读情况</a>";
                    }

                    return operateHtml;
                }
            }
        ]
        ],
        toolbar: [{
            text: '新增',
            iconCls: 'easyui-icon-add',
            handler: function () {
                edit();
            }
        },
            '-',
            {
                text: '删除',
                iconCls: 'easyui-icon-remove',
                handler: function () {
                    del();
                }
            },
            '-',
            {
                text: '我的联系人',
                iconCls: 'eu-icon-user',
                handler: function () {
                    try {
                        parent.addTabs({
                            id: 'contactGroup',
                            title: '我的联系人',
                            close: true,
                            url: ctxAdmin + '/notice/contactGroup',
                            urlType: ''
                        });
                    } catch (e) {
                        eu.addTab(window.parent.layout_center_tabs, '我的联系人', ctxAdmin + '/notice/contactGroup', true, 'eu-icon-user', '', false);
                    }


                }
            }],
        onRowContextMenu: function (e, rowIndex, rowData) {
            e.preventDefault();
            $(this).datagrid('unselectAll');
            $(this).datagrid('selectRow', rowIndex);
        },
        onLoadSuccess: function () {
            $(this).datagrid('clearSelections');
            $(this).datagrid('clearChecked');
            $(this).datagrid('unselectAll');
            $(this).datagrid('uncheckAll');
        }
    }).datagrid("showTooltip");
}

function view(noticeId, isRepeat,noticeReceiveInfoId,isNeedReply,isReply) {
    var inputUrl = ctxAdmin + '/notice/view/' + noticeId;
    var toolbar = [];
    var closeToolbar = {
        text: '关闭',
        iconCls: 'easyui-icon-cancel',
        handler: function () {
            notice_view_dialog.dialog('destroy');
            $notice_datagrid.datagrid("reload");
            refreshMessage();
        }
    };

    // if ("1" === isNeedReply && !("1" === isReply)) {
    if ("1" === isNeedReply) {
        var replyToolbar = {
            text: '回复',
            iconCls: 'eu-icon-mail_reply_sender',
            handler: function () {
                notice_view_dialog.dialog('destroy');
                editReply(noticeId,noticeReceiveInfoId);
            }
        };
        toolbar.push(replyToolbar);
    }

    if (isRepeat !== undefined && isRepeat === true) {
        var repeatToolbar = {
            text: '转发',
            iconCls: 'eu-icon-mail_reply_sender',
            handler: function () {
                notice_view_dialog.dialog('destroy');
                edit(noticeId, "Repeat");
            }
        };
        toolbar.push(repeatToolbar);
    }
    toolbar.push(closeToolbar);

    var thisHeight = document.body.clientHeight - 50;
    var thisWidth = document.body.clientWidth * 0.9 - 50;
    var notice_view_dialog = $('<div/>').dialog({
        title: '查看通知',
        width: thisWidth,
        height: thisHeight,
        modal: true,
        maximizable: true,
        content: '<iframe id="notice_view_iframe" scrolling="no" frameborder="0"  src="' + inputUrl + '" ></iframe>',
        buttons: toolbar,
        onClose: function () {
            notice_view_dialog.dialog('destroy');
            $notice_datagrid.datagrid("reload");
            refreshMessage();
        }
    });
}

function readInfo(noticeId) {
    var inputUrl = ctxAdmin + '/notice/readInfo/' + noticeId;
    var thisHeight = document.body.clientHeight - 50;
    var thisWidth = document.body.clientWidth * 0.9 - 50;
    var _dialog = $('<div/>').dialog({
        title: '查看状态',
        // width: 600,
        // height: 400,
        width: thisWidth,
        height: thisHeight,
        modal: true,
        maximizable: true,
        href: inputUrl,
        buttons: [{
            text: '关闭',
            iconCls: 'easyui-icon-cancel',
            handler: function () {
                _dialog.dialog('destroy');
            }
        }],
        onClose: function () {
            _dialog.dialog('destroy');
        }
    });

}

function formInit() {
    $notice_form = $('#notice_form').form({
        url: ctxAdmin + '/notice/save',
        onSubmit: function (param) {
            $.messager.progress({
                title: '提示信息！',
                text: '数据处理中，请稍后....'
            });
            var isValid = $(this).form('validate');
            if (!isValid) {
                $.messager.progress('close');
            } else {
                var effectTime = $("#_effectTime").my97("getValue");
                var endTime = $("#_endTime").my97("getValue");
                if (endTime !== undefined && endTime !== "" && effectTime > endTime) {
                    eu.showAlertMsg("后者所填时间必须大于前者时间", 'warning');
                    isValid = false;
                }
                param.operateType = _operateType;
            }
            return isValid;
        },
        success: function (data) {
            $.messager.progress('close');
            var json = $.parseJSON(data);
            if (json.code === 1) {
                $notice_dialog.dialog('destroy');//销毁对话框
                $notice_datagrid.datagrid('reload');//重新加载列表数据
                eu.showMsg(json.msg);//操作结果提示
            } else if (json.code === 2) {
                $.messager.alert('提示信息！', json.msg, 'warning', function () {
                    if (json.obj) {
                        $('#$notice_form input[title="' + json.obj + '"]').focus();
                    }
                });
            } else {
                eu.showAlertMsg(json.msg, 'error');
            }
        }
    });
}

function replyFormInit() {
    $notice_reply_form = $('#notice_reply_form').form({
        url: ctxAdmin + '/notice/noticeReceiveInfo/replySave',
        onSubmit: function (param) {
            $.messager.progress({
                title: '提示信息！',
                text: '数据处理中，请稍后....'
            });
            var isValid = $(this).form('validate');
            if (!isValid) {
                $.messager.progress('close');
            }
            return isValid;
        },
        success: function (data) {
            $.messager.progress('close');
            var json = $.parseJSON(data);
            if (json.code === 1) {
                $notice_reply_dialog.dialog('destroy');//销毁对话框
                $notice_datagrid.datagrid('reload');//重新加载列表数据
                eu.showMsg(json.msg);//操作结果提示
            }  else {
                eu.showAlertMsg(json.msg, 'error');
            }
        }
    });
}

//新增 编辑 转发
function edit(noticeId, operateType, objectType, objectId, title, content, fileIds, receiveUserIds, receiveOrganIds, receiveContactGroupIds) {
    var inputUrl = ctxAdmin + '/notice/input?operateType=';
    if (operateType !== undefined) {
        inputUrl += operateType;
    }
    if (noticeId !== undefined) {
        inputUrl += "&id=" + noticeId;
    }
    if (objectId != undefined) {
        inputUrl += "&objectType=" + objectType + "&objectId=" + objectId;
    }
    if (objectId != undefined && !noticeId) {
        inputUrl +="&title=" + title + "&content=" + content + "&fileIds=" + fileIds + "&receiveUserIds=" + receiveUserIds + "&receiveOrganIds=" + receiveOrganIds + "&receiveContactGroupIds=" + receiveContactGroupIds;
    }

    $notice_dialog = $('<div/>').dialog({
        title: '通知信息',
        width: document.body.clientWidth,
        height: document.body.clientHeight,
        modal: true,
        maximizable: true,
        href: inputUrl,
        buttons: [{
            text: '保存',
            iconCls: 'easyui-icon-save',
            handler: function () {
                _operateType = "Save";
                $notice_form.submit();
            }
        }, {
            text: '发布',
            iconCls: 'eu-icon-mail_forward',
            handler: function () {
                _operateType = "Publish";
                $notice_form.submit();
            }
        }, {
            text: '关闭',
            iconCls: 'easyui-icon-cancel',
            handler: function () {
                $notice_dialog.dialog('destroy');
            }
        }
        ],
        onClose: function () {
            $(this).dialog('destroy');
        },
        onLoad: function () {
            formInit();
        }
    });


}

//新增 编辑 转发
function editReply(noticeId, noticeReceiveInfoId) {
    var inputUrl = ctxAdmin + '/notice/noticeReceiveInfo/replyInput?id='+noticeReceiveInfoId+"&noticeId="+noticeId;

    $notice_reply_dialog = $('<div/>').dialog({
        title: '回复信息',
        width: document.body.clientWidth,
        height: document.body.clientHeight,
        modal: true,
        maximizable: true,
        href: inputUrl,
        buttons: [{
            text: '回复',
            iconCls: 'easyui-icon-save',
            handler: function () {
                $notice_reply_form.submit();
            }
        }, {
            text: '关闭',
            iconCls: 'easyui-icon-cancel',
            handler: function () {
                $notice_reply_dialog.dialog('destroy');
            }
        }
        ],
        onClose: function () {
            $(this).dialog('destroy');
        },
        onLoad: function () {
            replyFormInit();
        }
    });


}



/**
 * 发布
 * @param noticeId
 */
function publish(noticeId) {
    $.post(ctxAdmin + '/notice/publish/' + noticeId, {}, function (data) {
        if (data.code === 1) {
            $notice_datagrid.datagrid('reload');	// reload the user data
            eu.showMsg(data.msg);//操作结果提示
        } else {
            eu.showAlertMsg(data.msg, 'error');
        }
    });
}

/**
 * 推送
 * @param noticeId
 */
function push(noticeId) {
    $.post(ctxAdmin + '/notice/push/' + noticeId, {}, function (data) {
        if (data.code === 1) {
            eu.showMsg(data.msg);//操作结果提示
        } else {
            eu.showAlertMsg(data.msg, 'error');
        }
    });
}

/**
 * 失效
 * @param noticeId
 */
function invalid(noticeId) {
    $.post(ctxAdmin + '/notice/invalid/' + noticeId, {}, function (data) {
        if (data.code === 1) {
            $notice_datagrid.datagrid('reload');	// reload the user data
            eu.showMsg(data.msg);//操作结果提示
        } else {
            eu.showAlertMsg(data.msg, 'error');
        }
    });
}


/**
 * 标记为已读
 * @param rowIndex
 */
function markReaded(rowIndex) {
    var rows = [];
    var tipMsg = "您确定要删除选中的所有通知标记为已读？";
    if (rowIndex !== undefined) {
        $notice_datagrid.datagrid('unselectAll');
        $notice_datagrid.datagrid('selectRow', rowIndex);
        var rowData = $notice_datagrid.datagrid('getSelected');
        rows.push(rowData);
        $notice_datagrid.datagrid('unselectRow', rowIndex);
        tipMsg = "您确定要将通知[" + rowData["title"] + "]标记为已读？";
    } else {
        rows = $notice_datagrid.datagrid('getChecked');
    }

    if (rows.length > 0) {
        $.messager.confirm('确认提示！', '您确定要标记所有行为已读？', function (r) {
            if (r) {
                var ids = [];
                $.each(rows, function (i, row) {
                    ids[i] = row.noticeId;
                });
                $.ajax({
                    url: ctxAdmin + '/notice/markReaded',
                    type: 'post',
                    data: {ids: ids},
                    dataType: 'json',
                    traditional: true,
                    success: function (data) {
                        if (data.code === 1) {
                            $notice_datagrid.datagrid('reload');
                            eu.showMsg(json.msg);
                            refreshMessage();
                        } else {
                            eu.showAlertMsg(data.msg, 'error');
                        }
                    }
                });
            }
        });
    } else {
        eu.showMsg("请选择要操作的对象！");
    }

}


//删除
function del() {
    var rows = $notice_datagrid.datagrid('getChecked');
    if (rows.length > 0) {
        $.messager.confirm('确认提示！', '您确定要删除选中的所有行?', function (r) {
            if (r) {
                var ids = [];
                $.each(rows, function (i, row) {
                    ids[i] = row.id;
                });
                $.ajax({
                    url: ctxAdmin + '/notice/remove',
                    type: 'post',
                    data: {ids: ids},
                    dataType: 'json',
                    traditional: true,
                    success: function (data) {
                        if (data.code === 1) {
                            $notice_datagrid.datagrid('load');	// reload the user data
                            eu.showMsg(data.msg);//操作结果提示
                        } else {
                            eu.showAlertMsg(data.msg, 'error');
                        }
                    }
                });
            }
        });
    } else {
        eu.showMsg("请选择要操作的对象！");
    }
}

function convertValues(MultiSelect) {
    var query = "";
    if (MultiSelect !== undefined) {
        query = MultiSelect.input.val();

    }
    var data = {};
    data["query"] = query;
    return data;
}

function initSelectUser() {
    $query_PublishUser_MultiSelect = $("#publishUserIds").kendoMultiSelect({
        dataTextField: "name",
        dataValueField: "id",
        groupTemplate: "#: data #",
        maxSelectedItems: 1,
        dataSource: {
            transport: {
                read: {
                    url: ctxAdmin + '/sys/user/customUserList',
                    dataType: 'json'
                },
                parameterMap: function (data, type) {
                    if (type === "read") {
                        return convertValues($query_PublishUser_MultiSelect);
                    }
                }
            },
            serverFiltering: false,
            group: {field: "defaultOrganName"}
        }
    }).data("kendoMultiSelect");

}

function selectQueryUser() {
    var userIds = "";
    var dataItems = $query_PublishUser_MultiSelect.dataItems();
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
    var _dialog = $("<div/>").dialog({
        title: "选择用户",
        top: 10,
        href: ctxAdmin + '/sys/user/organUserTreePage?checkedUserIds=' + userIds,
        width: '500',
        height: '360',
        maximizable: true,
        iconCls: 'easyui-icon-user',
        modal: true,
        buttons: [
            {
                text: '确定',
                iconCls: 'easyui-icon-save',
                handler: function () {
                    setQuerySelectUser();
                    _dialog.dialog('destroy');
                }
            },
            {
                text: '关闭',
                iconCls: 'easyui-icon-cancel',
                handler: function () {
                    _dialog.dialog('destroy');

                }
            }
        ],
        onClose: function () {
            _dialog.dialog('destroy');
        }
    });
}


function setQuerySelectUser() {
    var selectUserIds = [];
    var checkNodes = $("#organUserTree").tree("getChecked");
    $.each(checkNodes, function (i, node) {
        if ("u" === node.attributes.nType) {
            selectUserIds.push(node.id);
        }
    });
    $query_PublishUser_MultiSelect.value(selectUserIds);
}

//搜索
function search() {
    $notice_datagrid.datagrid('load', $.serializeObject($notice_search_form));
}