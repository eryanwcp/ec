var modelId;
var $resource_treegrid;
$(function () {
    //数据列表
    $resource_treegrid = $('#resource_treegrid').treegrid({
        url: ctxAdmin + '/sys/role/viewRoleResources?roleId=' + modelId,
        fit: true,
        fitColumns: false,//自适应列宽
        striped: true,//显示条纹
        rownumbers: true,//显示行数
        nowrap: false,
        border: false,
        singleSelect: true,
        remoteSort: false,//是否通过远程服务器对数据排序
        sortName: 'sort',//默认排序字段
        sortOrder: 'asc',//默认排序方式 'desc' 'asc'
        idField: 'id',
        treeField: "name",
        frozenColumns: [[
            {field: 'name', title: '资源名称', width: 300},
            {field: 'code', title: '资源编码', width: 260}
        ]],
        columns: [[
            {field: 'id', title: '主键', hidden: true, sortable: true, align: 'right', width: 80},
            {field: 'url', title: '链接地址', width: 260},
            {field: 'markUrl', title: '标识地址', width: 260},
            {field: 'sort', title: '排序', align: 'right', width: 60, sortable: true},
            {field: 'typeView', title: '资源类型', align: 'center', width: 100},
            {field: 'statusView', title: '状态', align: 'center', width: 60}
        ]]
    }).datagrid('showTooltip');

});
