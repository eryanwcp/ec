<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
<script type="text/javascript">
    var modelId = "${model.id}";
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/role-copy${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<div>
    <form id="role_copy_form" class="dialog-form" method="post" novalidate>
        <input type="hidden"  name="id" value="${model.id}"/>
        <div >
            <label >复制角色：</label><span>${model.name}</span>
        </div>
        <div >
            <label >来源角色：</label>
            <input id="role_copy_form-roleIds" name="roleIds"
                   style="width: 260px;height: 28px;"/>
        </div>
    </form>
</div>