-- V4.2.20231124.11 用户、组织增加信息分类编码以及自定义扩展数据；日志增加自定义扩展数据。
ALTER TABLE t_sys_user ADD COLUMN biz_code varchar(64) COMMENT '信息分类编码';
ALTER TABLE t_sys_user ADD COLUMN extend_attr text COMMENT '自定义扩展数据 {\'key1\':Object,\'key2\':Object}';
ALTER TABLE t_sys_organ ADD COLUMN biz_code varchar(64) COMMENT '信息分类编码';
ALTER TABLE t_sys_organ ADD COLUMN extend_attr text COMMENT '自定义扩展数据 {\'key1\':Object,\'key2\':Object}';
ALTER TABLE t_sys_organ_extend ADD COLUMN biz_code varchar(64) COMMENT '信息分类编码';
ALTER TABLE t_sys_organ_extend ADD COLUMN extend_attr text COMMENT '自定义扩展数据 {\'key1\':Object,\'key2\':Object}';
ALTER TABLE t_sys_log ADD COLUMN extend_attr text COMMENT '自定义扩展数据 {\'key1\':Object,\'key2\':Object}';
ALTER TABLE t_sys_log_history ADD COLUMN extend_attr text COMMENT '自定义扩展数据 {\'key1\':Object,\'key2\':Object}';

-- V4.2.20231124.02 消息管理增加消息类型
ALTER TABLE t_notice_message ADD COLUMN msg_type varchar(32) COMMENT '消息类型 文本：text；文本卡片：textcard';

-- V4.2.20230921.04 用户管理增加“职务”字段
ALTER TABLE t_sys_user ADD COLUMN position varchar(128) COMMENT '职务';

-- V4.2.20230521.26
ALTER TABLE t_sys_user_password ADD COLUMN type char(1) COMMENT '修改类型 重置：0 用户初始化：1 用户安全修改：2';

-- V4.2.20230224.10
DROP TABLE IF EXISTS `t_sys_role_data_organ`;
CREATE TABLE `t_sys_role_data_organ`  (
                                          `ORGAN_ID` varchar(36) COMMENT '机构ID',
                                          `ROLE_ID` varchar(36) COMMENT '角色ID',
                                          INDEX `ROLE_ID`(`ROLE_ID`) USING BTREE,
                                          INDEX `ORGAN_ID`(`ORGAN_ID`) USING BTREE
) ENGINE = InnoDB COMMENT = '角色数据权限机构范围表';

-- 4.1.20191115
ALTER TABLE `t_sys_serial_number`
ADD COLUMN `APP` varchar(36) COMMENT 'APP标识' ;

ALTER TABLE `t_sys_version_log`
ADD COLUMN `IS_SHELF` varchar(36) COMMENT 'APP标识' ;