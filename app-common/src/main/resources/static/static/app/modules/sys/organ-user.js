var organUserCombogridData = organUserCombogridData;
var usersCombogridData = usersCombogridData;
var managerUserId = managerUserId;

var $managerUser_combogrid;
$(function () {
    loadManagerUser();
});

//加载主管用户
function loadManagerUser() {
    $managerUser_combogrid = $('#managerUserId').combogrid({
        width: 360,
        panelWidth: 360,
        panelHeight: 360,
        idField: 'id',
        textField: 'name',
        data: organUserCombogridData,
        fitColumns: true,
        striped: true,
        editable: true,
        rownumbers: true,//序号
        collapsible: false,//是否可折叠的
        method: 'post',
        columns: [[
            {field: 'id', title: '主键', hidden: true, sortable: true, width: 200},
            {field: 'name', title: '姓名', width: 100, sortable: true},
            {field: 'sexView', title: '性别', width: 60, hidden: true, sortable: true},
            {field: 'loginName', title: '账号', width: 120, hidden: true, sortable: true},
            {field: 'code', title: '编码', width: 160, hidden: true, sortable: true},
            {field: 'bizCode', title: '信息分类编码', width: 160, hidden: true, sortable: true},
            {field: 'mobile', title: '手机号', width: 120, hidden: true, sortable: true},
            {field: 'sort', title: '排序号', width: 60, align: 'right', hidden: true, sortable: true},
            {field: 'defaultOrganName', title: '部门', width: 200, sortable: true},
            {field: 'companyName', title: '单位', width: 200, sortable: true}
        ]]
    });
    if (managerUserId) {
        $managerUser_combogrid.combogrid("setValue", managerUserId);
    }

}

//加载分管用户
function selectUser() {
    var checkedUserIds = $("#superManagerUserId").val();
    _dialog = $("<div/>").dialog({
        title: "选择用户",
        top: 10,
        href: ctxAdmin + '/sys/user/organUserTreePage?checkbox=false&checkedUserIds=' + checkedUserIds,
        width: '500',
        height: '360',
        maximizable: true,
        iconCls: 'eu-icon-user',
        modal: true,
        buttons: [{
            text: '确定',
            iconCls: 'easyui-icon-save',
            handler: function () {
                setSelectUser();
                _dialog.dialog('destroy');
            }
        }, {
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

function setSelectUser() {
    var node = $("#organUserTree").tree("getSelected");
    if (node) {
        if ("u" === node.attributes.nType) {
            $("#superManagerUserId").val(node['id']);
            $("#superManagerUserName").textbox('setValue', node['text']);
        } else {
            $("#superManagerUserId").val('');
            $("#superManagerUserName").textbox('setValue', '');
        }

    }
}