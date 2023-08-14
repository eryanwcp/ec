var organIds = organIds;
var dataOrganIds = dataOrganIds;
var isSystem = isSystem;

var $organ_combotree = undefined;
var $organIds_combotree = undefined;
var $data_organIds_combotree = undefined;
$(function () {
    loadOrgan();
    loadOrganIds();
    loadDataScope();
    loadDataOrganIds();
    //级联点击事件
    $('#changeMode').click(function () {
        var tempData = $data_organIds_combotree.combotree('getValues');
        $data_organIds_combotree.combotree({
            cascadeCheck: $(this).is(':checked'),
            onShowPanel: function () {
                var tree = $(this).combotree("tree");
                var checkeNodes = tree.tree("getChecked");
                var tempValues = [];
                $.each(checkeNodes, function (index, nodeData) {
                    tempValues.push(nodeData.id);
                });
                $data_organIds_combotree.combotree("setValues", tempValues);
            }
        });
        $data_organIds_combotree.combotree('setValues', tempData);
        $data_organIds_combotree.combotree("showPanel");
    });
    $('input[name=isSystem][value=' + isSystem + ']').prop("checked", 'checked');
    $("input[name='isSystem']").change(function () {
        var value = $(this).val();
        if ("1" === value) {
            $("#div_organId").hide();
            $("#div_organIds").hide();
            $organ_combotree.combotree("setValue", "");
        } else {
            $("#div_organId").show();
            $("#div_organIds").show();
        }
    });
});

function loadOrgan() {
    $organ_combotree = $("#organId").combotree({
        url: ctxAdmin + '/sys/organ/ownerAndChildsHomeCompanysData',
        missingMessage: '请选择所属机构.',
        multiple: false,
        editable: false
    });
}

function loadOrganIds() {
    $data_organIds_combotree = $("#organIds").combotree({
        url: ctxAdmin + '/sys/organ/ownerAndChildsHomeCompanysData',
        cascadeCheck: false,
        multiple: true,
        editable: false,
        value: organIds
    });
}

function loadDataScope(data) {
    $("#dataScope").combobox({
        url: ctxAdmin + '/sys/role/dataScopeWithPermission?selectType=select',
        editable: false,
        onSelect: function (record) {
            var dataScope = record['value'];
            if ("9" === dataScope) {
                $("#div_data_organIds").show();
            } else {
                $("#div_data_organIds").hide();
                if ($data_organIds_combotree) {
                    $data_organIds_combotree.combotree("setValue", "");
                }
            }
        }
    });
}
function loadDataOrganIds() {
    $data_organIds_combotree = $("#dataOrganIds").combotree({
        url: ctxAdmin + '/sys/organ/tree?dataScope=4&cascade=true',
        cascadeCheck: false,
        multiple: true,
        editable: false,
        value: dataOrganIds
    });
}


