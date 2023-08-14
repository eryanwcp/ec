var modelId;
var $resource_user_datagrid;
$(function () {
    //数据列表
    $resource_user_datagrid = $('#resource_user_datagrid').datagrid({
        url: ctxAdmin + '/sys/resource/resourceUserDatagrid/'+modelId,
        fit: true,
        pagination: true,//底部分页
        rownumbers: true,//显示行数
        fitColumns: false,//自适应列宽
        striped: true,//显示条纹
        pageSize: 20,//每页记录数
        pageList: [10, 20, 50, 100, 1000, 99999],
        remoteSort: false,//是否通过远程服务器对数据排序
        idField: 'id',
        frozen: true,
        collapsible: true,
        frozenColumns: [
            [
                {field: 'ck', checkbox: true},
                {field: 'loginName', title: '登录名', width: 120, sortable: true},
                {field: 'name', title: '姓名',width: 100,sortable: true}
            ]
        ],
        columns: [
            [
                {field: 'code', title: '员工编号', width: 80, hidden: true, sortable: true},
                {field: 'id', title: '主键', hidden: true, sortable: true, align: 'right', width: 80},
                {field: 'tel', title: '办公电话', width: 100},
                {field: 'mobile', title: '手机号', width: 100},
                {field: 'qq', title: 'QQ', width: 100},
                {field: 'email', title: '公司邮箱', width: 160},
                {field: 'personEmail', title: '个人邮箱', width: 160},
                {field: 'defaultOrganId', title: '默认机构ID', width: 80, hidden: true},
                {field: 'defaultOrganName', title: '部门', width: 160, sortable: true},
                {field: 'companyName', title: '单位', width: 200, sortable: true, hidden: true},
                {field: 'sexView', title: '性别', width: 60, align: 'center', sortable: true},
                {field: 'birthday', title: '出生日期', width: 80, sortable: true},
                {field: 'address', title: '地址', width: 200},
                {field: 'remark', title: '备注', width: 200},
                {field: 'sort', title: '排序', width: 60, sortable: true},
                {
                    field: 'statusView',
                    title: '状态',
                    align: 'center',
                    width: 60,
                    formatter: function (value, rowData, rowIndex) {
                        if (rowData['status'] !== 0) {
                            return $.formatString('<span  style="color:red">{0}</span>', value);
                        }
                        return value;
                    }
                }
            ]
        ],
        toolbar: [
            {
                text: '删除关联',
                iconCls: 'easyui-icon-remove',
                handler: function () {
                    deleteUsers();
                }
            },
        ],
        onLoadSuccess: function () {
            $(this).datagrid('clearSelections');//取消所有的已选择项
            $(this).datagrid('unselectAll');//取消全选按钮为全选状态
        },
        onRowContextMenu: function (e, rowIndex, rowData) {
            e.preventDefault();
        }
    }).datagrid('showTooltip');

});


//删除
function deleteUsers() {
    var rows = $resource_user_datagrid.datagrid('getSelections');
    if (rows.length > 0) {
        $.messager.confirm('确认提示！', '您确定要删除选中的所有行？', function (r) {
            if (r) {
                var ids = [];
                $.each(rows, function (i, row) {
                    ids[i] = row.id;
                });
                $.ajax({
                    url: ctxAdmin + '/sys/resource/deleteUsers/'+modelId,
                    type: 'post',
                    data: {ids: ids},
                    traditional: true,
                    dataType: 'json',
                    success: function (data) {
                        if (data.code === 1) {
                            $resource_user_datagrid.datagrid('load');	// reload the user data
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

