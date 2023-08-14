<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp" %>
<html>
<head>
    <title>我的消息</title>
    <meta name="decorator" content="default_sys"/>
    <script type="text/javascript">
        $(function(){
            $("#btnSubmit").click(function(){
                $("#pageNo").val(1);
                loadData();
            });
            $("#btnReset").click(function(){
                $('#searchForm').find("input[type=hidden]").val("");
                // $('#searchForm').find("select").val(null).trigger("change");
            });
            loadData();
            $("#setReadAll").click(function(){
                $.ajax({
                    url: appURL + '/a/notice/messageReceive/setReadAll',
                    type: 'post',
                    dataType: 'json',
                    cache:false,
                    success: function (data) {
                        if (data.code == 1) {
                            $('#pageNo').val(1);
                            loadData();
                        }
                    }
                });
            });
        });
        function page(n, s) {
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            loadData();
            return false;
        }
        function loadData() {
            var queryParam = $.serializeObject($("#searchForm"));
            $.ajax({
                url: ctxAdmin + "/notice/messageReceive?_=" + new Date().getTime(),
                type: "post",
                dataType: "json",
                data: queryParam,
                cache: false,
                beforeSend: function (jqXHR, settings) {
                    $("#list").html("<div style='padding: 10px 30px;text-align:center;font-size: 16px;'>数据加载中...</div>");
                },
                success: function (data) {
                    data = data['obj'];
                    if (data['totalCount'] > 0) {
                        var html = Mustache.render($("#list_template").html(), data);
                        $("#list").html(html);
                        $(".pagination").append(data['html']);

                    } else {
                        $("#list").html("<div style='color: red;padding: 10px 30px;text-align:center;font-size: 16px;'>暂无数据</div>");
                    }
                }
            });
        }

        /**
         * 设置已读 （异步）
         * @param id
         */
        function setRead(id,messageId,linkUrl){
            $.ajax({
                url: '${ctxAdmin}/notice/messageReceive/setRead?id='+id,
                type: 'post',
                dataType: 'json',
                success: function (data) {}
            });
            if(linkUrl){
                try {
                    $.ajax({
                        url: '${ctx}/f/getMessageSSOUrl?messageId=' + messageId,
                        type: 'post',
                        dataType: 'json',
                        success: function (data) {
                            if (1 === data['code']) {
                                window.location.assign(data['obj']);
                            }
                        }
                    });
                } catch (e) {
                    window.location.assign(linkUrl);
                }
            }
        }
    </script>
    <script type="text/template" id="list_template">
        <table id="contentTable" class="table table-striped table-bordered table-condensed">
            <thead>
            <tr>
                <th>消息内容</th>
                <th>发送者</th>
                <th>发送时间</th>
                <th>推送状态</th>
                <th>阅读状态</th>
            </tr>
            </thead>
            <tbody>
            {{#result}}
                <tr>
                    <td><a href="javascript:" onclick="setRead('{{id}}','{{message.id}}','{{message.url}}');">{{message.content}}</a></td>
                    <td>{{message.senderName}}</td>
                    <td>{{message.sendTime}}</td>
                    <td>{{isSendView}}</td>
                    <td>{{isReadView}}</td>
                </tr>
            {{/result}}
            </tbody>
        </table>
        <div class="page pagination"></div>
    </script>
</head>
<body>
<ul class="nav nav-tabs">
    <li class="active"><a href="${ctxAdmin}/notice/messageReceive">消息列表</a></li>
</ul>
<form:form id="searchForm" modelAttribute="model" action="${ctxAdmin}/notice/messageReceive" method="post" class="breadcrumb form-search">
    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
    <form:radiobutton path="isRead" value="0" label="未阅" />
    <form:radiobutton path="isRead" value="1" label="已阅" />&nbsp;&nbsp;
    <input id="btnSubmit" class="btn btn-primary" type="button" value="查 询"/>&nbsp;
    <input id="btnReset" class="btn btn-warning" type="reset" value="重 置"/>&nbsp;
    <input id="setReadAll" class="btn btn-primary" type="button" value="全部标记为已读"/>&nbsp;
</form:form>
<tags:message content="${message}"/>
<div id="list"></div>
</body>
</html>