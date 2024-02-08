<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>每日登陆次数分析</title>
    <meta name="decorator" content="default_sys"/>
    <!-- bootstrap条件框的收缩和展开 -->
    <link rel="stylesheet" href="${ctxStatic}/app/modules/sys/css/bootstrap_query_fold.min.css">
    <script type="text/javascript">
        $(function(){
            loadData();
            $("#btnSubmit").click(function(){
                $("#pageNo").val(1);
                loadData();
            });

            $("#btnReset").click(function(){
                $('#searchForm').find("input[type=hidden]").val("");
                $('#searchForm').find("select").val(null).trigger("change");
            });

            $("#btnExport").click(function(){
                var param = $.serializeObject($("#searchForm"));
                $('#annexFrame').attr('src', '${ctxAdmin}/sys/log/report/dayLoginStatistics?export=true&'+ $.param(param));
            });
        });
        function loadData(){
            var queryParam = $.serializeObject($("#searchForm"));
            $("#btnSubmit").attr("disabled",true);
            $.ajax({
                url: ctxAdmin + '/sys/log/report/dayLoginStatistics',
                type: 'post',
                dataType: "json",
                cache:false,
                data:queryParam,
                beforeSend: function (jqXHR, settings) {
                    $("#list").html("<div style='padding: 10px 30px;text-align:center;font-size: 16px;'><img src='${ctxStatic}/js/easyui/themes/bootstrap/images/loading.gif' />数据加载中...</div>");
                },
                success: function (data) {
                    $("#btnSubmit").attr("disabled",false);
                    if (data['totalCount'] > 0) {
                        var html = Mustache.render($("#list_template").html(), data);
                        $("#list").html(html);
                        $(".pagination").append(data['html']);
                    } else {
                        $("#list").html("<div style='color: red;padding: 10px 30px;text-align:center;font-size: 16px;'>查无数据</div>");
                    }
                }
            });
        }

        function page(n,s){
            $("#pageNo").val(n);
            $("#pageSize").val(s);
            loadData();
            return false;
        }
    </script>
    <script type="text/template" id="list_template">
        <table id="contentTable" class="table table-striped table-bordered table-condensed">
            <thead>
            <tr>
                <th>日期</th>
                <th>访问量</th>
            </tr>
            </thead>
            <tbody>
            {{#result}}
            <tr>
                <td>{{loginDate}}</td>
                <td>{{count}}</td>
            </tr>
            {{/result}}
            </tbody>
        </table>
        <div class="page pagination"></div>
    </script>
</head>
<body>
<div class="accordion" id="form_accordion">
    <div class="accordion-group">
        <div class="accordion-heading">
            <a class="accordion-toggle" data-toggle="collapse" data-parent="#form_accordion" href="#collapseOne">
                <i class="icon-filter"></i>
            </a>
        </div>
        <div id="collapseOne" class="accordion-body collapse in">
            <div class="accordion-inner">
                <form id="searchForm">
                    <input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
                    <input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
                    <div class="row-fluid">
                        <div class="span4">
                            <div class="span3"><label>时间：</label></div>
                            <div class="span9">
                                <input  name="startTime" type="text" readonly="readonly" maxlength="10" class="input-small Wdate"
                                        value="<fmt:formatDate value="${startTime}" pattern="yyyy-MM-dd"/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>
                                ~ <input  name="endTime" type="text" readonly="readonly" maxlength="10" class="input-small Wdate"
                                          value="<fmt:formatDate value="${endTime}" pattern="yyyy-MM-dd"/>" onclick="WdatePicker({dateFmt:'yyyy-MM-dd',isShowClear:true});"/>&nbsp;&nbsp;
                            </div>
                        </div>
                        <div class="span4">
                            <div class="span3"></div>
                            <div class="span9">
                                <input id="btnSubmit" class="btn btn-primary" type="button" value="查 询"/>&nbsp;&nbsp;
                                <input id="btnReset" class="btn btn-warning" type="reset" value="重 置"/>&nbsp;&nbsp;
                                <button id="btnExport" type="button" class="btn btn-primary" >导 出</button>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<tags:message content="${message}"/>
<div id="list"></div>
<iframe id="annexFrame" src="" frameborder="no" style="padding: 0;border: 0;width: 100%;height: 50px;display: none;"></iframe>
</body>
</html>


