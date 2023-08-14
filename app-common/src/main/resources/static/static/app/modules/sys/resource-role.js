var modelId;
var $resource_role_datagrid;
$(function () {
    //数据列表
    $resource_role_datagrid = $('#resource_role_datagrid').datagrid({
        url: ctxAdmin + '/sys/resource/resourceRoleDatagrid/'+modelId,
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
                {field: 'name', title: '角色名称', width: 200},
                {field: 'code', title: '角色编码', width: 200}
            ]
        ],
        columns: [
            [
                {field: 'id', title: '主键', hidden: true, sortable: true, align: 'right', width: 80},
                {field: 'isSystemView', title: '系统角色', width: 60},
                {field: 'roleTypeView', title: '权限类型', width: 100},
                {field: 'dataScopeView', title: '数据范围', width: 200},
                {field: 'organName', title: '所属机构', width: 200},
                {field: 'remark', title: '备注', width: 260}
            ]
        ],
        toolbar: [
            {
                text: '删除关联',
                iconCls: 'easyui-icon-remove',
                handler: function () {
                    deleteRoles();
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
function deleteRoles() {
    var rows = $resource_role_datagrid.datagrid('getSelections');
    if (rows.length > 0) {
        $.messager.confirm('确认提示！', '您确定要删除选中的所有行？', function (r) {
            if (r) {
                var ids = [];
                $.each(rows, function (i, row) {
                    ids[i] = row.id;
                });
                $.ajax({
                    url: ctxAdmin + '/sys/resource/deleteRoles/'+modelId,
                    type: 'post',
                    data: {ids: ids},
                    traditional: true,
                    dataType: 'json',
                    success: function (data) {
                        if (data.code === 1) {
                            $resource_role_datagrid.datagrid('load');	// reload the user data
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

