var modelId = modelId;
var postIds = postIds;

var $post_combobox;
$(function () {
    loadPost();
});

//加载用户可选岗位
function loadPost() {
    $post_combobox = $("#postIds").combobox({
        url: ctxAdmin + '/sys/post/userPostCombobox?userId=' + modelId ,
        multiple: true,
        editable: false
    });
    if (postIds) {
        $post_combobox.combobox('setValues', postIds);
    }
}