var $session_datagrid;
var $session_search_form;
var isPermittedEdit = isPermittedEdit;
$(function () {
    $session_search_form = $('#session_search_form').form();

    var toolbar = [];
    if (isPermittedEdit) {
        toolbar.push({
            text: '下线',
            iconCls: 'easyui-icon-cancel',
            handler: function () {
                offline();
            }
        });
    }
    $session_datagrid = $('#session_datagrid').datagrid({
        url: ctxAdmin + '/sys/session/winthPermissionsOnLineSessions',
        fit: true,
        nowarp: false,
        border: false,
        idField: 'id',
        pagination: true,
        rownumbers: true,
        fitColumns: false,
        remoteSort: false,
        striped: true,
        pageSize: 20,
        pageList: [10, 20, 50, 100, 1000, 99999],
        frozenColumns: [[
            {field: 'ck', checkbox: true}
        ]],
        columns: [[{
            title: 'ID',
            field: 'id',
            width: 260,formatter: function (value, rowData, rowIndex) {
                return "<a target='_blank' href='" + ctxAdmin + "/sys/session/detail?id=" + rowData['id'] + "'>" + value + "</a>";
            }
        }, {
            title: 'HOST',
            field: 'host',
            width: 120,
            hidden: true
        }, {
            title: '用户类型',
            field: 'userType',
            width: 120,
            hidden: true
        }, {
            title: '名称',
            field: 'name',
            width: 160
        }, {
            title: '账号',
            field: 'loginName',
            width: 160
        }, {
            title: '手机号',
            field: 'mobileSensitive',
            width: 120
        }, {
            title: '真实手机号',
            field: 'mobile',
            width: 120,
            hidden: true
        }, {
            title: '部门',
            field: 'loginOrganName',
            width: 200,
            hidden: true
        }, {
            title: '登录IP',
            field: 'ip',
            width: 120,
            formatter: function (value, rowData, rowIndex) {
                return value;
            }
        }, {
            title: '设备类型',
            field: 'systemDeviceType',
            width: 100
        }, {
            title: '设备编码',
            field: 'deviceCode',
            width: 200
        }, {
            title: '客户端版本',
            field: 'appVersion',
            width: 120
        }, {
            title: '登录时间',
            field: 'loginTime',
            width: 146,
            sortable: true,
            formatter: function (value, rowData, rowIndex) {
                return value;
            }
        }, {
            title: '最后访问时间',
            field: 'updateTime',
            width: 146,
            sortable: true,
            formatter: function (value, rowData, rowIndex) {
                return value;
            }
        }, {
            title: '操作',
            field: 'operate',
            width: 100,
            formatter: function (value, rowData, rowIndex) {
                var operaterHtml = "";
                if (isPermittedEdit) {
                    var editHtml = "<a class='easyui-linkbutton' iconCls='easyui-icon-cancel'  href='#' " +
                        "onclick='offline(" + rowIndex + ");' >下线</a>";
                    operaterHtml += editHtml;
                }

                return operaterHtml;
            }
        }
        ]],
        toolbar: toolbar,
        onClickRow: function (rowIndex, rowData) {
        },
        onLoadSuccess: function (data) {
            $.parser.parse($(".easyui-linkbutton").parent());
            $(this).datagrid('clearSelections');
            $(this).datagrid('clearChecked');
            $(this).datagrid('unselectAll');
            $(this).datagrid('uncheckAll');
        }
    }).datagrid('showTooltip');
});

/**
 * 用户下线
 */
function offline(rowIndex) {
    var rows = [];
    var tipMsg = "您确定要将选中用户强制下线？";
    if (rowIndex != undefined) {
        $session_datagrid.datagrid('unselectAll');
        $session_datagrid.datagrid('selectRow', rowIndex);
        var rowData = $session_datagrid.datagrid('getSelected');
        rows.push(rowData);
        $session_datagrid.datagrid('unselectRow', rowIndex);
        tipMsg = "您确定要将用户[" + rowData["name"] + "]强制下线？";
    } else {
        rows = $session_datagrid.datagrid('getChecked');
    }
    if (rows.length > 0) {
        $.messager.confirm('确认提示！', tipMsg, function (r) {
            if (r) {
                var ids = [];
                $.each(rows, function (i, row) {
                    ids[i] = row.id;
                });
                $.ajax({
                    url: ctxAdmin + '/sys/session/offline',
                    type: 'post',
                    data: {sessionIds: ids},
                    traditional: true,
                    dataType: 'json',
                    success: function (data) {
                        if (data.code == 1) {
                            $session_datagrid.datagrid('load');	// reload the user data
                            eu.showMsg(data.msg);//操作结果提示
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

//搜索
function search() {
    $session_datagrid.datagrid('load', $.serializeObject($session_search_form));
}