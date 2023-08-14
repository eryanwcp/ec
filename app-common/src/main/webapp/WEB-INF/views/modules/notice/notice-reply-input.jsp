<%@ page import="com.eryansky.modules.notice.utils.NoticeUtils" %>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="/common/taglibs.jsp" %>
<script type="text/javascript">
    var modelIsReply = '${model.isReply}';
    var modelFileIds = ${fns:toJson(fileIds)};
    var jsessionid = '${sessionInfo.sessionId}';
    var fileSizeLimit = '<%=AppConstants.getNoticeMaxUploadSize()%>';//附件上传大小限制
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/notice/notice-reply-input${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<style type="text/css">
    .img_div{
        display : inline-block;
        position : relative;
    }

    .img_div .delete{
        position : absolute;
        top : 5px;
        right : 5px;
        color: red;
        border: 1px solid red;
        text-align: center;
        cursor: pointer;
        border-radius: 20px;
        width : 20px;
        height : 20px;
        line-height: 20px;
    }
    #notice_form div label{text-align: right;}
</style>
<div>
    <form id="notice_reply_form" class="dialog-form"  method="post" novalidate>
        <input type="hidden" name="id" value="${model.id}"/>
        <!-- 用户版本控制字段 version -->
        <input type="hidden" id="noticeId" name="noticeId" value="${model.noticeId}"/>

        <table style="border: 0px;width: 100%;">
            <tr>
                <td style="width: 96px; vertical-align: top;text-align: right;">回复内容：&nbsp;</td>
                <td><textarea id="editor" name="replyContent">${model.replyContent}</textarea></td>
            </tr>
        </table>
        <div>
            <label>附件：</label>
            <div style="margin-left: 96px;">
                <div id="uploadify"></div>
                <input id="fileIds" name="fileIds" type="hidden" />
                <c:if test="${not empty files}">
                    <div>
                        <c:forEach items="${files}" begin="0" var="file" varStatus="i">
                            <div id='${file.id}' style="font-size: 14px;">${i.index +1}、<a href="#" onclick="loadOrOpen('${file.id}');" style="color: #0000ff;">${file.name}</a>&nbsp;&nbsp;
                                <a href="#" onclick="delUpload('${file.id}');" style="color: red;">删除</a></div>
                        </c:forEach>
                    </div>
                </c:if>
            </div>

        </div>
    </form>
    <iframe id="annexFrame" style="display:none" src=""></iframe>
</div>
