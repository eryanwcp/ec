<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ include file="/common/taglibs.jsp"%>
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
</style>
<script type="text/javascript">
    var modelId = '${model.id}';
    var modelStatus = '${model.status}';
    var jsessionid = '${sessionInfo.sessionId}';
    var fileSizeLimit = '<%=AppConstants.getDiskMaxUploadSize()%>';
</script>
<script type="text/javascript" src="${ctxStatic}/app/modules/sys/user-input${yuicompressor}.js?_=${sysInitTime}" charset="utf-8"></script>
<div>
    <form id="user_form" class="dialog-form" method="post" novalidate>
        <input type="hidden" id="id" name="id" value="${model.id}"/>
        <!-- 用户版本控制字段 version -->
        <input type="hidden" id="version" name="version" value="${model.version}"/>
        <div>
            <label>用户类型：</label>
            <select id="userType" name="userType" class="easyui-combobox" style="width: 120px;height: 28px;" >
                <c:forEach items="${userTypes}" var="userType">
                    <option value="${userType.value}" <c:if test="${model.userType eq userType.value}">selected</c:if>>${userType.text}</option>
                </c:forEach>
            </select>
        </div>
        <div>
            <label>所属组织机构：</label>
            <input type="select" id="defaultOrganId" name="defaultOrganId" value="${model.defaultOrganId}"
                   style="width: 260px;height: 28px;"/>
        </div>
        <div>
            <label>登录名：</label>
            <input type="text" id="loginName" name="loginName" maxLength="36" value="${model.loginName}"
                   class="easyui-validatebox textbox"
                   data-options="required:true,missingMessage:'请输入登录名.',validType:['minLength[1]']"/>
        </div>
        <div>
            <label>员工编号：</label>
            <input type="text" id="code" name="code" maxLength="36" value="${model.code}"
                   class="easyui-validatebox textbox"/>
        </div>
        <div id="password_div">
            <label>密码：</label>
            <input type="password" id="password"
                   name="password" class="easyui-validatebox textbox" maxLength="36" value="${model.password}"
                   data-options="required:true,missingMessage:'请输入密码.',validType:['minLength[1]']">
        </div>
        <div id="repassword_div">
            <label>确认密码：</label>
            <input type="password" id="repassword"
                   name="repassword" class="easyui-validatebox textbox" required="true"
                   missingMessage="请再次填写密码." validType="equalTo['#password']"
                   invalidMessage="两次输入密码不匹配.">
        </div>

        <div>
            <label>姓名：</label>
            <input name="name" type="text" maxLength="12" value="${model.name}" class="easyui-validatebox textbox"
                   data-options="required:true,missingMessage:'请输入登录名.',validType:['length[2,12]']" />
        </div>
        <div>
            <label>头像：</label>
            <div id="image_div" style="margin-left: 96px;">
                <input id="image" name="photo" readonly="readonly" value="${model.photo}" type="hidden"/>
                <div class="img_div">
                    <img id="image_pre" class="img-rounded" src="${model.photoUrl}" alt="头像" style="max-height: 72px;display: none;" />
                    <div class="delete" onclick="delImageFile();" style="display: none;">x</div>
                </div>
                <div id="image_uploadify"></div>
            </div>
        </div>
        <div>
            <label>性别：</label>
            <input id="sex" name="sex" value="${model.sex}"  style="width: 120px;height: 28px;" />
        </div>
        <div>
            <label>出生日期：</label>
            <fmt:formatDate value="${model.birthday}" pattern="yyyy-MM-dd" var="birthdayStr"></fmt:formatDate>
            <input id="birthday" name="birthday" value="${birthdayStr}"  type="text" class="easyui-my97" />
        </div>
        <div>
            <label>公司邮箱：</label>
            <input name="email" type="text" value="${model.email}"  class="easyui-validatebox textbox" validType="email" maxLength="64" />
        </div>
        <div>
            <label>办公电话：</label>
            <input name="tel" type="text"  value="${model.tel}"  class="easyui-validatebox textbox" validType="phone">
        </div>
        <div>
            <label>手机号：</label>
            <input name="mobile" type="text" value="${model.mobile}" class="easyui-validatebox textbox" validType="mobile">
        </div>
        <div>
            <label>QQ：</label>
            <input name="qq" type="text" value="${model.qq}" class="easyui-numberbox textbox" validType="QQ" maxLength="64" style="height: 28px;" />
        </div>
        <div>
            <label>个人邮箱：</label>
            <input name="personEmail" type="text" value="${model.personEmail}" class="easyui-validatebox textbox" validType="email" maxLength="64" />
        </div>
        <div>
            <label>地址：</label>
            <input name="address" type="text" value="${model.address}" class="easyui-validatebox textbox" validType="legalInput" maxLength="255" />
        </div>
        <div>
            <label >备注：</label>
            <input name="remark" maxLength="1000" value="${model.remark}"  class="easyui-textbox" data-options="multiline:true" style="width:260px;height:75px;">
        </div>
        <div>
            <label>排序：</label>
            <input type="text" id="sort" name="sort" value="${model.sort}"  class="easyui-numberspinner"
                   data-options="min:1,max:99999999,size:9,maxlength:9,height:28" />
        </div>
        <div>
            <label>状态：</label>
            <label style="text-align: left;width: 60px;">
                <input type="radio" name="status" style="width: 20px;" value="0" /> 启用
            </label>
            <label style="text-align: left;width: 60px;">
                <input type="radio" name="status" style="width: 20px;" value="3" /> 停用
            </label>
        </div>
    </form>
</div>