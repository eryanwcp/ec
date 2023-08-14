<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<script type="text/javascript">
    var modelId= '${model.id}';
    var postIds = ${fns:toJson(postIds)};
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/user-post${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<div>
    <form id="user_post_form" class="dialog-form" method="post">
        <input type="hidden" id="id" name="id" value="${model.id}"/>
        <!-- 用户版本控制字段 version -->
        <input type="hidden" id="version" name="version" value="${model.version}"/>
        <div>
            <label>关联岗位：</label>
            <input type="select" id="postIds" name="postIds"
                   style="width: 260px;height: 28px;"/>
        </div>
    </form>
</div>