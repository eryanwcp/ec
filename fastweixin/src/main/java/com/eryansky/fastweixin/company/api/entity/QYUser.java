package com.eryansky.fastweixin.company.api.entity;

import com.eryansky.fastweixin.api.entity.BaseModel;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

/**
 *
 * @author Eryan
 * @date 2016-03-15
 */
public class QYUser extends BaseModel {

    public static final class Gender{
        public static final String NO_DEF = "0";  // 0表示未定义
        public static final String MAN = "1";
        public static final String WOMAN = "2";
    }

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

    public QYUser() {
    }

    public QYUser(String userId, String name, String mobile, Integer[] department, Integer[] order, Integer[] isLeaderInDept, String position, String gender, String email, String avatar, String thumbAvatar, String telephone, String alias, Integer status, String openUserId, String qrCode, String address, Integer mainDepartment, Map<String, Object> extattr,Boolean toInvite, QYUserExternalProfile externalProfile, String externalPosition) {
        this.userId = userId;
        this.name = name;
        this.mobile = mobile;
        this.department = department;
        this.order = order;
        this.isLeaderInDept = isLeaderInDept;
        this.position = position;
        this.gender = gender;
        this.email = email;
        this.avatar = avatar;
        this.thumbAvatar = thumbAvatar;
        this.telephone = telephone;
        this.alias = alias;
        this.status = status;
        this.openUserId = openUserId;
        this.qrCode = qrCode;
        this.address = address;
        this.mainDepartment = mainDepartment;
        this.extattr = extattr;
        this.toInvite = toInvite;
        this.externalProfile = externalProfile;
        this.externalPosition = externalPosition;
    }

    public String getUserId() {
        return userId;
    }

    public QYUser setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getName() {
        return name;
    }

    public QYUser setName(String name) {
        this.name = name;
        return this;
    }

    public Integer[] getDepartment() {
        return department;
    }

    public QYUser setDepartment(Integer[] department) {
        if(department.length != 0) {
            this.department = department;
        }
        return this;
    }

    public String getPosition() {
        return position;
    }

    public QYUser setPosition(String position) {
        this.position = position;
        return this;
    }

    public String getMobile() {
        return mobile;
    }

    public QYUser setMobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public QYUser setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public QYUser setEmail(String email) {
        this.email = email;
        return this;
    }


    public String getAvatar() {
        return avatar;
    }

    public QYUser setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public QYUser setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Map<String, Object> getExtattr() {
        return extattr;
    }

    public QYUser setExtattr(Map<String, Object> extattr) {
        this.extattr = extattr;
        return this;
    }

    public Integer[] getOrder() {
        return order;
    }

    public QYUser setOrder(Integer[] order) {
        this.order = order;
        return this;
    }

    public Integer[] getIsLeaderInDept() {
        return isLeaderInDept;
    }

    public QYUser setIsLeaderInDept(Integer[] isLeaderInDept) {
        this.isLeaderInDept = isLeaderInDept;
        return this;
    }

    public String getThumbAvatar() {
        return thumbAvatar;
    }

    public QYUser setThumbAvatar(String thumbAvatar) {
        this.thumbAvatar = thumbAvatar;
        return this;
    }

    public String getTelephone() {
        return telephone;
    }

    public QYUser setTelephone(String telephone) {
        this.telephone = telephone;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public QYUser setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getOpenUserId() {
        return openUserId;
    }

    public QYUser setOpenUserId(String openUserId) {
        this.openUserId = openUserId;
        return this;
    }

    public String getQrCode() {
        return qrCode;
    }

    public QYUser setQrCode(String qrCode) {
        this.qrCode = qrCode;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public QYUser setAddress(String address) {
        this.address = address;
        return this;
    }

    public Integer getMainDepartment() {
        return mainDepartment;
    }

    public QYUser setMainDepartment(Integer mainDepartment) {
        this.mainDepartment = mainDepartment;
        return this;
    }

    public Boolean getToInvite() {
        return toInvite;
    }

    public QYUser setToInvite(Boolean toInvite) {
        this.toInvite = toInvite;
        return this;
    }
}
