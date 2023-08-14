var $layout_portal_portal;
var $jplayer;
var protal_titles = ['&nbsp;我的通知', '&nbsp;我的邮件'];
$(function () {
    panels = [{
        id: 'p1',
        href: ctxAdmin + '/portal/notice',
        title: protal_titles[0],
        iconCls: 'eu-icon-notice_user_comment',
        height: 300,
        collapsible: true,
        tools: [{
            iconCls: 'eu-icon-more',
            handler: function () {
                var url = ctxAdmin + '/notice';
                try {
                    parent.addTabs({id: 'notice', title: '我的通知', close: true, url: url, urlType: ''});
                } catch (e) {
                    eu.addTab(window.parent.layout_center_tabs, "我的通知", url, true, "eu-icon-notice_user_comment");
                }


            }
        }]
    }];

    $('#layout_portal_portal').portal({
        border: false,
        fit: true,
        onStateChange: function () {
            $.cookie('portal-state', getPortalState(), {
                expires: 7
            });
        }
    });
    var state = $.cookie('portal-state');
    if (!state) {
        state = 'p1';/*冒号代表列，逗号代表行*/
    }
    addPortalPanels(state);
    $('#layout_portal_portal').portal('resize');

    initMedia();

    mymessages(false, true);
    window.setInterval('mymessages(true,true)', 5 * 60 * 1000);
});

function getPanelOptions(id) {
    for (var i = 0; i < panels.length; i++) {
        if (panels[i].id === id) {
            return panels[i];
        }
    }
    return undefined;
}

function getPortalState() {
    var aa = [];
    for (var columnIndex = 0; columnIndex < 2; columnIndex++) {
        var cc = [];
        var panels = $('#layout_portal_portal').portal('getPanels', columnIndex);
        for (var i = 0; i < panels.length; i++) {
            cc.push(panels[i].attr('id'));
        }
        aa.push(cc.join(','));
    }
    return aa.join(':');
}

function addPortalPanels(portalState) {
    var columns = portalState.split(':');
    for (var columnIndex = 0; columnIndex < columns.length; columnIndex++) {
        var cc = columns[columnIndex].split(',');
        for (var j = 0; j < cc.length; j++) {
            var options = getPanelOptions(cc[j]);
            if (options) {
                var p = $('<div/>').attr('id', options.id).appendTo('body');
                p.panel(options);
                $('#layout_portal_portal').portal('add', {
                    panel: p,
                    columnIndex: columnIndex
                });
            }
        }
    }
}

/**
 * 初始化声音提醒
 */
function initMedia() {
    $jplayer = $("#jplayer").jPlayer({
        swfPath: ctxStatic + "/js/jquery/jplayer/Jplayer.swf",
        ready: function () {
            $(this).jPlayer("setMedia", {
                mp3: ctxStatic + "/media/tip_new_msg.mp3"
            });
        },
        supplied: "mp3"
    });
}

/**
 * 声音提示
 */
function tipMsg() {
    var tipMessage = $.cookie('portal-tipMessage') !== "false";
    if (tipMessage) {
        $jplayer.jPlayer('play');
    }
    var tipMessageHtml = "<span>您有新的消息，请注意查收！</span>";
    tipMessageHtml += "<div style='margin-top: 10px;'><label><input id='tip_checkbox' type='checkbox' onclick='setTipMessage(this.checked);' ";
    if (tipMessage) {
        tipMessageHtml += " checked ";
    }
    tipMessageHtml += "/> 提示声音</label></div>";
    $.messager.show({
        title: '<span class="tree-icon tree-file easyui-icon-tip easyui-tooltip"></span><span style="color: red;"> 提示信息！</span>',
        msg: tipMessageHtml,
        height: 110,
        timeout: 5000,
        showType: 'slide' //null,slide,fade,show.
    });
}

function setTipMessage(tipMessage) {
    $.cookie('portal-tipMessage', tipMessage, {expires: 7});
}

/**
 * 更新portal消息
 * @param refreshPanel 是否需要刷新Panel
 * @param tipMessage 是否提示声音
 */
function mymessages(refreshPanel, tipMessage) {
    $.ajax({
        url: ctxAdmin + '/portal/mymessages',
        type: 'POST',
        dataType: 'json',
        success: function (data) {
            if (data.code === 1) {
                if (refreshPanel) {
                    $("#p1").panel("refresh");
                }

                var hashNewMessage = false;//是否提示声音
                var obj = data.obj;
                var messagesHtml = undefined;
                if (obj["noticeReceiveInfos"] && obj["noticeReceiveInfos"] > 0) {
                    hashNewMessage = true;
                    messagesHtml = "&nbsp;" + "<span  style='color: #FE6600;font-size: 16px;'>" + obj["noticeReceiveInfos"] + "</span>&nbsp条";
                } else {
                    messagesHtml = "&nbsp;" + "<span>" + (undefined != obj["noticeReceiveInfos"] ? obj["noticeReceiveInfos"]:0) + "</span>&nbsp;条";
                }
                $("#p1").panel("setTitle", protal_titles[0] + messagesHtml);

                if (obj["passwordTip"] !== undefined && obj["passwordTip"]['tip'] === true) {
                    var tipMsg = '';
                    if (obj["passwordTip"]['code']=== 1) {
                        tipMsg = "您从未修改过用户密码，请<a onclick='updateUserPassword(\""+obj["passwordTip"]['url']+"\");'>设置用户密码</a>！";
                    } else if (obj["passwordTip"]['code'] === 2) {
                        tipMsg = "距离上次修改密码已经很长时间了，请<a onclick='updateUserPassword(\""+obj["passwordTip"]['url']+"\");'>修改用户密码</a>！";
                    }
                    if(tipMsg !== ''){
                        $.messager.show({
                            title: '<span class="tree-icon tree-file easyui-icon-tip easyui-tooltip"></span><span style="color: red;"> 提示信息！</span>',
                            msg: tipMsg,
                            height: 110,
                            timeout: 30 * 1000,
                            showType: 'slide' //null,slide,fade,show.
                        });
                    }

                }

            } else {
            }
        }
    });
}


/**
 * 修改密码
 */
function updateUserPassword(serverUrl) {
    var url = serverUrl ? serverUrl: (ctx+"/a/index/password");
    parent.addTabs({id: 'index_password', title: '密码修改', close: true, url: url, urlType: ''});
}