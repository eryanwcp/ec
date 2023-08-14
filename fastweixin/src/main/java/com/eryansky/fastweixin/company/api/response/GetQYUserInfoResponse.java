package com.eryansky.fastweixin.company.api.response;

import com.eryansky.fastweixin.api.response.BaseResponse;
import com.alibaba.fastjson.annotation.JSONField;
import com.eryansky.fastweixin.company.api.entity.QYUser;
import com.eryansky.fastweixin.company.api.entity.QYUserExternalProfile;

import java.util.Map;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class GetQYUserInfoResponse extends BaseResponse {

    /**
     * 成员UserID。对应管理端的帐号
     */
    @JSONField(name = "userid")
    private String userId;
    /**
     * 成员名称；第三方不可获取，调用时返回userid以代替name；代开发自建应用需要管理员授权才返回；对于非第三方创建的成员，第三方通讯录应用也不可获取；未返回名称的情况需要通过通讯录展示组件来展示名字
     */
    private String name;
    /**
     * 手机号码，代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private String mobile;
    /**
     * 成员所属部门id列表，仅返回该应用有查看权限的部门id
     */
    private Integer[] department;
    /**
     * 部门内的排序值，默认为0。数量必须和department一致，数值越大排序越前面。值范围是[0, 2^32)
     */
    private Integer[] order;
    /**
     * 表示在所在的部门内是否为上级。0-否；1-是。是一个列表，数量必须与department一致。第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    @JSONField(name = "is_leader_in_dept")
    private Integer[] isLeaderInDept;
    /**
     * 职务信息；代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private String position;
    /**
     * 性别。0表示未定义，1表示男性，2表示女性
     */
    private String gender;
    /**
     * 邮箱，代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private String email;
    /**
     * 头像url。 第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private String avatar;
    /**
     * 头像缩略图url。第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    @JSONField(name = "thumb_avatar")
    private String thumbAvatar;
    /**
     * 座机。代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private String telephone;
    /**
     * 别名；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private String alias;
    /**
     * 激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业。
     * 已激活代表已激活企业微信或已关注微工作台（原企业号）。未激活代表既未激活企业微信又未关注微工作台（原企业号）。
     */
    private Integer status;
    /**
     * 全局唯一。对于同一个服务商，不同应用获取到企业内同一个成员的open_userid是相同的，最多64个字节。仅第三方应用可获取
     */
    @JSONField(name = "open_userid")
    private String openUserId;
    /**
     * 员工个人二维码，扫描可添加为外部联系人(注意返回的是一个url，可在浏览器上打开该url以展示二维码)；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    @JSONField(name = "qr_code")
    private String qrCode;
    /**
     * 地址。代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private String address;
    /**
     * 主部门
     */
    @JSONField(name = "main_department")
    private Integer mainDepartment;
    /**
     * 扩展属性，代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    private Map<String, Object> extattr;
    /**
     * 是否邀请该成员使用企业微信，默认值为true
     */
    @JSONField(name = "to_invite")
    private Boolean toInvite;
    /**
     * 成员对外属性，字段详情见对外属性；代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     * {@link QYUserExternalProfile}
     */
    @JSONField(name = "external_profile")
    private QYUserExternalProfile  externalProfile;
    /**
     * 对外职务，如果设置了该值，则以此作为对外展示的职务，否则以position来展示。代开发自建应用需要管理员授权才返回；第三方仅通讯录应用可获取；对于非第三方创建的成员，第三方通讯录应用也不可获取
     */
    @JSONField(name = "external_position")
    private String externalPosition;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer[] getDepartment() {
        return department;
    }

    public void setDepartment(Integer[] department) {
        this.department = department;
    }

    public Integer[] getOrder() {
        return order;
    }

    public void setOrder(Integer[] order) {
        this.order = order;
    }

    public Integer[] getIsLeaderInDept() {
        return isLeaderInDept;
    }

    public void setIsLeaderInDept(Integer[] isLeaderInDept) {
        this.isLeaderInDept = isLeaderInDept;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getThumbAvatar() {
        return thumbAvatar;
    }

    public void setThumbAvatar(String thumbAvatar) {
        this.thumbAvatar = thumbAvatar;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOpenUserId() {
        return openUserId;
    }

    public void setOpenUserId(String openUserId) {
        this.openUserId = openUserId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getMainDepartment() {
        return mainDepartment;
    }

    public void setMainDepartment(Integer mainDepartment) {
        this.mainDepartment = mainDepartment;
    }

    public Map<String, Object> getExtattr() {
        return extattr;
    }

    public void setExtattr(Map<String, Object> extattr) {
        this.extattr = extattr;
    }

    public Boolean getToInvite() {
        return toInvite;
    }

    public void setToInvite(Boolean toInvite) {
        this.toInvite = toInvite;
    }

    public QYUserExternalProfile getExternalProfile() {
        return externalProfile;
    }

    public void setExternalProfile(QYUserExternalProfile externalProfile) {
        this.externalProfile = externalProfile;
    }

    public String getExternalPosition() {
        return externalPosition;
    }

    public void setExternalPosition(String externalPosition) {
        this.externalPosition = externalPosition;
    }

    public QYUser getUser(){
        return new QYUser(userId,name,mobile,department,order,isLeaderInDept,position,gender,email,avatar,thumbAvatar,telephone,alias,status,openUserId,qrCode,address,mainDepartment,extattr,toInvite,externalProfile,externalPosition);
    }
}
