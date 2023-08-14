<%@ page import="com.eryansky.utils.AppConstants" %>
<%@ page import="com.eryansky.core.security.SecurityUtils" %>
<%@ include file="/common/taglibs.jsp"%>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<c:set var="isCheckPasswordPolicy" value="<%=AppConstants.isCheckPasswordPolicy()%>"></c:set>
<c:set var="isCurrentUserAdmin" value="<%=SecurityUtils.isCurrentUserAdmin()%>"></c:set>
<div>
	<form id="user_password_form" class="dialog-form" method="post">
		<input type="hidden" id="user_password_form_id" name="id" value="${model.id}"/>
		<div>
			<label>新密码：</label>
			<input type="password" id="newPassword" name="newPassword"
				class="easyui-validatebox textbox"
			<c:choose>
				<c:when test="${isCurrentUserAdmin}">
				   data-options="required:true,missingMessage:'请输入新密码.',validType:['minLength[1]']"/>
				</c:when>
				<c:when test="${isCheckPasswordPolicy || !isCurrentUserAdmin}">
					data-options="required:true,missingMessage:'请输入新密码.',validType:['safepass_yc','minLength[1]']"/>
				</c:when>
				<c:otherwise>
					data-options="required:true,missingMessage:'请输入新密码.',validType:['minLength[1]']"/>
				</c:otherwise>
			</c:choose>
		</div>
		<div>
			<label>确认新密码：</label>
			<input type="password" id="newPassword2" name="newPassword2"
				class="easyui-validatebox textbox" required="true"
				missingMessage="请再次输入新密码." validType="equalTo['#newPassword']"
				invalidMessage="两次输入密码不匹配." />
		</div>
	</form>
</div>