package com.eryansky.fastweixin.company.api;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.eryansky.fastweixin.company.api.config.QYAPIConfig;
import com.eryansky.fastweixin.company.api.response.*;
import com.eryansky.fastweixin.util.JSONUtil;
import com.eryansky.fastweixin.util.StrUtil;
import com.eryansky.fastweixin.company.api.entity.QYUser;
import com.eryansky.fastweixin.company.api.enums.QYResultType;
import com.eryansky.fastweixin.util.BeanUtil;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYUserAPI extends QYBaseAPI {


    /**
     * 构造方法，设置apiConfig
     *
     * @param config 微信API配置对象
     */
    public QYUserAPI(QYAPIConfig config) {
        super(config);
    }

    /**
     * 创建一个新用户
     * @param user 用户
     * @return 创建结果
     */
    public QYResultType create(QYUser user){
        BeanUtil.requireNonNull(user, "user is null");
        String url = BASE_API_URL + "cgi-bin/user/create?access_token=#";
        BaseResponse response = executePost(url, user.toJsonString());
        return QYResultType.get(response.getErrcode());
    }

    /**
     * 更新用户信息
     * @param user 用户
     * @return 更新结果
     */
    public QYResultType update(QYUser user){
        BeanUtil.requireNonNull(user, "user is null");
        String url = BASE_API_URL + "cgi-bin/user/update?access_token=#";
        BaseResponse response = executePost(url, user.toJsonString());
        return QYResultType.get(response.getErrcode());
    }

    /**
     * 删除用户
     * @param userId 用户ID
     * @return 删除结果
     */
    public QYResultType delete(String userId){
        BeanUtil.requireNonNull(userId, "userId is null");
        String url = BASE_API_URL + "cgi-bin/user/delete?access_token=#&userid=" + userId;
        BaseResponse response = executeGet(url);
        return QYResultType.get(response.getErrcode());
    }

    /**
     * 批量删除用户
     * @param userIds 要删除的用户ID数组
     * @return 删除结果
     */
    public QYResultType batchdelete(String[] userIds){
        String url = BASE_API_URL + "cgi-bin/user/batchdelete?access_token=#";
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("useridlist", userIds);
        BaseResponse response = executePost(url, JSONUtil.toJson(params));
        return QYResultType.get(response.getErrcode());
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public GetQYUserInfoResponse get(String userId){
        BeanUtil.requireNonNull(userId, "userId is null");
        GetQYUserInfoResponse response;
        String url = BASE_API_URL + "cgi-bin/user/get?access_token=#&userid=" + userId;
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(resultJson, GetQYUserInfoResponse.class);
        return response;
    }

    /**
     * 通过部门列表获取部门成员摘要。仅包含userid与name
     * @param departmentId 部门ID
     * @param isLoop 是否递归获取子部门下面的成员
     * @return 部门成员
     */
    public GetQYUserInfo4DepartmentResponse simpleList(Integer departmentId, boolean isLoop){
        GetQYUserInfo4DepartmentResponse response;
        String url = BASE_API_URL + "cgi-bin/user/simplelist?access_token=#&department_id=" + departmentId + "&fetch_child=" + (isLoop? 1 : 0);
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(resultJson, GetQYUserInfo4DepartmentResponse.class);
        return response;
    }

    /**
     * 通过部门列表获取部门成员信息
     * @param departmentId 部门ID
     * @param isLoop 是否递归获取子部门下面的成员
     * @return 部门成员详情信息
     */
    public GetQYUserInfo4DepartmentResponse getList(Integer departmentId, boolean isLoop){
        GetQYUserInfo4DepartmentResponse response;
        String url = BASE_API_URL + "cgi-bin/user/list?access_token=#&department_id=" + departmentId + "&fetch_child=" + (isLoop? 1 : 0);
        BaseResponse r = executeGet(url);
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(resultJson, GetQYUserInfo4DepartmentResponse.class);
        return response;
    }

    /**
     * 用户踢下线 (仅限政务微信)
     * @param userid 成员UserID。对应管理端的账号
     * @param msg 用户踢下线提示语
     * @return
     */
    public QYUserOfflineResponse offline(String userid,String msg){
        return offline(Lists.newArrayList(userid),msg);
    }

    /**
     * 用户踢下线 (仅限政务微信)
     * @param userids 成员UserID。对应管理端的账号
     * @param msg 用户踢下线提示语
     * @return
     */
    public QYUserOfflineResponse offline(List<String> userids, String msg){
        BeanUtil.requireNonNull(userids, "userids is null");
        QYUserOfflineResponse response;
        String url = BASE_API_URL + "cgi-bin/user/offline?access_token=#";
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid_list", userids);
        params.put("msg", msg);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(resultJson, QYUserOfflineResponse.class);
        return response;
    }

    /**
     * 邀请成员关注。返回值type为1时表示微信邀请，2为邮件邀请
     * @param userid 用户ID
     * @return 邀请结果
     */
    public GetQYUserInviteResponse invite(String userid){
        BeanUtil.requireNonNull(userid, "userid is null");
        GetQYUserInviteResponse response;
        String url = BASE_API_URL + "cgi-bin/invite/send?access_token=#";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", userid);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(resultJson, GetQYUserInviteResponse.class);
        return response;
    }

    /**
     * 通过Oauth授权获得的CODE获取成员信息。仅包含UserId、OpenId、DeviceId三个。
     * 企业成员授权时会获得UserId，非企业成员授权会获得OpenId。
     * DeviceId为设备编号，重装微信时会发生变更，升级时不变
     * @param code
     * @return
     */
    public GetOauthUserInfoResponse getOauthUserInfo(String code){
        if(StrUtil.isBlank(code)){
            throw new NullPointerException("code is null");
        }
        GetOauthUserInfoResponse response;
        String url = BASE_API_URL + "cgi-bin/user/getuserinfo?access_token=#&code=" + code;
        BaseResponse r = executeGet(url);
        String jsonResult = isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        response = JSONUtil.toBean(jsonResult, GetOauthUserInfoResponse.class);
        return response;
    }


    /**
     * userid与openid互换
     * @param userid
     * @return
     */
    public GetUserConvertToOpenidResponse convertToOpenId(String userid) {
        BeanUtil.requireNonNull(userid, "userid is null");
        String url = BASE_API_URL + "cgi-bin/user/convert_to_openid?access_token=#";
        final Map<String, String> params = new HashMap<String, String>();
        params.put("userid", userid);
        BaseResponse r = executePost(url, JSONUtil.toJson(params));
        String resultJson = this.isSuccess(r.getErrcode()) ? r.getErrmsg() : r.toJsonString();
        GetUserConvertToOpenidResponse response = JSONUtil.toBean(resultJson, GetUserConvertToOpenidResponse.class);
        return response;
    }
}
