<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
    <title>定时任务列表</title>
    <meta name="decorator" content="default_sys"/>
    <link href="${ctxStatic}/js/toastr/toastr.min.css" type="text/css" rel="stylesheet" />
    <script src="${ctxStatic}/js/toastr/toastr.min.js" type="text/javascript"></script>
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
                $('#annexFrame').attr('src', '${ctxAdmin}/sys/job/getJobList?export=true&'+ $.param(param));
            });
        });
        function loadData(){
            var queryParam = $.serializeObject($("#searchForm"));
            $("#btnSubmit").attr("disabled",true);
            $.ajax({
                url: ctxAdmin + '/sys/job/list',
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

        function executeJob(jobClassName,jobGroupName) {
            $("#"+jobClassName).attr("disabled",true).css({"pointer-events":"none","opacity":"0.4"});
            $.ajax({
                type : 'POST',
                url :'${ctxAdmin}'+'/sys/job/triggerJob',
                data: {"jobClassName":jobClassName,"jobGroupName":jobGroupName},
                dataType:"JSON",
                success : function(data) {
                    toastr.info(data.msg);
                    setTimeout("location.reload()",3000);
                }
            });
        }

        function pauseJob(jobClassName,jobGroupName) {
            $("#"+jobClassName+"_1").attr("disabled",true).css({"pointer-events":"none","opacity":"0.4"});
            $.ajax({
                type : 'POST',
                url :'${ctxAdmin}'+'/sys/job/pauseJob',
                data: {"jobClassName":jobClassName,"jobGroupName":jobGroupName},
                dataType:"JSON",
                success : function(data) {
                    toastr.info(data.msg);
                    setTimeout("location.reload()",3000);
                }
            });
        }

        function resumeJob(jobClassName,jobGroupName) {
            $("#"+jobClassName+"_2").attr("disabled",true).css({"pointer-events":"none","opacity":"0.4"});
            $.ajax({
                type : 'POST',
                url :'${ctxAdmin}'+'/sys/job/resumeJob',
                data: {"jobClassName":jobClassName,"jobGroupName":jobGroupName},
                dataType:"JSON",
                success : function(data) {
                    toastr.info(data.msg);
                    setTimeout("location.reload()",3000);
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
                <th>任务名</th>
                <th>任务状态</th>
                <%--<th>任务组名</th>
                <th>所属类名</th>--%>
                <th>执行实例</th>
                <th>时间表达式</th>
                <th>上一次执行时间</th>
                <th>下一次执行时间</th>
                <th>操作</th>
            </tr>
            </thead>
            <tbody>
            {{#result}}
            <tr>
                <td>{{jobName}}</td>
                <td>{{triggerStateView}}</td>
                <%--<td>{{jobGroup}}</td>
                <td>{{jobClassName}}</td>--%>
                <td>{{instanceName}}</td>
                <td>{{cronExpression}}</td>
                <td>{{prevFireTime}}</td>
                <td>{{nextFireTime}}</td>
                <td>
                    <a id="{{jobName}}" style="cursor:pointer;" onclick="executeJob('{{jobName}}','{{jobGroup}}');">立即执行</a>
                    <a id="{{jobName}}_1" style="cursor:pointer;" onclick="pauseJob('{{jobName}}','{{jobGroup}}');">暂停任务</a>
                    <a id="{{jobName}}_2" style="cursor:pointer;" onclick="resumeJob('{{jobName}}','{{jobGroup}}');">继续任务</a>

                </td>
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
                            <div class="span3"><label>任务名称：</label></div>
                            <div class="span9"><input name="jobName" type="text" value="${model.jobName}" class="input-large" style="width: 146px;" /></div>
                        </div>
                        <div class="span4">
                            <div class="span3"><label>任务状态：</label></div>
                            <div class="span9">
                                <select name="triggerState" id="triggerState"  class="selectStyle" style="width: 160px">
                                    <option value="">全部</option>
                                    <c:forEach  items="${states}" var="modes">
                                        <option value="${modes.value}"
                                                <c:if test='${not empty model and not empty model.triggerState and model.triggerState==modes.value}'>
                                                    selected='selected'
                                                </c:if>>
                                                ${modes.description}
                                        </option>
                                    </c:forEach>
                                </select>
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
