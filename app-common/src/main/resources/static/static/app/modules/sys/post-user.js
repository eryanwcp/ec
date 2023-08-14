var postId = postId;


var $post_user_datagrid;
var $user_post_search_form;
$(function () {
    $user_post_search_form = $("#user_post_search_form").form();
    initPostUserDatagrid();
});

function initPostUserDatagrid() {
    $post_user_datagrid = $("#post_user_datagrid").datagrid({
        url: ctxAdmin + '/sys/post/userDatagrid?postId=' + postId,
        fit: true,
        pagination: true,
        rownumbers: true,
        fitColumns: false,
        striped: true,
        pageSize: 20,//每页记录数
        pageList: [10, 20, 50, 100, 1000, 99999],
        remoteSort: false,
        idField: 'id',
        frozenColumns: [
            [
                {field: 'ck', checkbox: true},
                {field: 'name', title: '姓名', width: 120}
            ]
        ],
        columns: [
            [
                {field: 'id', title: '主键', hidden: true, sortable: true, width: 80},
                {field: 'sexView', title: '性别', width: 60, hidden: true, sortable: true},
                {field: 'loginName', title: '账号', width: 60, hidden: true, sortable: true},
                {field: 'mobileSensitive', title: '手机号', width: 100, hidden: true, sortable: true},
                {field: 'sort', title: '排序号', width: 60, hidden: true, sortable: true},
                {field: 'defaultOrganName', title: '部门', width: 200, sortable: true},
                {field: 'companyName', title: '单位', width: 200, sortable: true}
            ]
        ],
        toolbar: [
            {
                text: '添加',
                iconCls: 'easyui-icon-add',
                handler: function () {
                    showAddPostUserDialog()
                }
            },
            {
                text: '移除',
                iconCls: 'easyui-icon-remove',
                handler: function () {
                    delPostUser()
                }
            },
            {
                text: '导出',
                iconCls: 'eu-icon-disk_download',
                handler: function () {
                    exportQuery();
                }
            }
        ]
    });
}

function showAddPostUserDialog() {
    _dialog = $("<div/>").dialog({
        title: "选择用户",
        top: 10,
        href: ctxAdmin + '/sys/post/select?dataScope=4&cascade=true&id=' + postId,
        width: '700',
        height: '450',
        maximizable: true,
        iconCls: 'eu-icon-user',
        modal: true,
        buttons: [
            {
                text: '确定',
                iconCls: 'easyui-icon-save',
                handler: function () {
                    addPostUser();
                    $post_user_datagrid.datagrid("reload");
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

function addPostUser() {
    var selectUserIds = [];
    $("#selectUser option").each(function () {
        var txt = $(this).val();
        selectUserIds.push($.trim(txt));
    });
    if (selectUserIds.length > 0) {
        $.ajax({
            url: ctxAdmin + '/sys/post/addPostUser?id=' + postId,
            type: 'post',
            data: {userIds: selectUserIds},
            traditional: true,
            dataType: 'json',
            success: function (data) {
                if (data.code === 1) {
                    $post_user_datagrid.datagrid('reload');
                    eu.showMsg(data.msg);
                } else {
                    eu.showAlertMsg(data.msg, 'error');
                }
            }
        });
    }

}

function delPostUser() {
    var rows = $post_user_datagrid.datagrid('getChecked');
    if (rows.length > 0) {
        $.messager.confirm('确认提示！', '您确定要移除选中的所有行？', function (r) {
            if (r) {
                var ids = [];
                $.each(rows, function (i, row) {
                    ids[i] = row.id;
                });
                $.ajax({
                    url: ctxAdmin + '/sys/post/removePostUser?id=' + postId,
                    type: 'post',
                    data: {userIds: ids},
                    traditional: true,
                    dataType: 'json',
                    success: function (data) {
                        if (data.code === 1) {
                            $post_user_datagrid.datagrid('reload');
                            eu.showMsg(data.msg);
                        } else {
                            eu.showAlertMsg(data.msg, 'error');
                        }
                    }
                });
            }
        });
    } else {
        eu.showMsg("您未选择任何操作对象，请选择一行数据！");
    }
}

function searchPostUser() {
    $post_user_datagrid.datagrid('load', $.serializeObject($user_post_search_form));
}

function exportQuery() {
    $('#annexFrame').attr('src', ctxAdmin + '/sys/post/userDatagrid?export=true&postId='+postId +"&"+ $.param($.serializeObject($user_post_search_form)));
}