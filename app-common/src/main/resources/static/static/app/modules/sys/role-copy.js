var $roleCombobox;

$(function () {
    loadRoles();
});

// 加載用户角色信息
function loadRoles() {
    $roleCombobox = $('#role_copy_form-roleIds').combobox({
        url: ctxAdmin + '/sys/role/comboboxWithPermission',
        multiple: true,
        editable: false
    });
}