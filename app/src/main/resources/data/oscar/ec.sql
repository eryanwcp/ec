---------------------TABLE DDL: QRTZ_BLOB_TRIGGERS---------------------
CREATE TABLE SYSDBA.QRTZ_BLOB_TRIGGERS
(
    BLOB_DATA blob DEFAULT NULL ,
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL, 
    CONSTRAINT QRTZ_BLOB_TRIGGERS_PK PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_BLOB_TRIGGERS---------------------
---------------------PRIMARY KEY: QRTZ_BLOB_TRIGGERS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_BLOB_TRIGGERS  add constraint QRTZ_BLOB_TRIGGERS_PK primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
---------------------TABLE DDL: QRTZ_CALENDARS---------------------
CREATE TABLE SYSDBA.QRTZ_CALENDARS
(
    SCHED_NAME character varying(120) NOT NULL,
    CALENDAR_NAME character varying(200) NOT NULL,
    CALENDAR blob NOT NULL, 
    CONSTRAINT QRTZ_CALENDARS_PK PRIMARY KEY (SCHED_NAME, CALENDAR_NAME)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_CALENDARS---------------------
---------------------PRIMARY KEY: QRTZ_CALENDARS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_CALENDARS  add constraint QRTZ_CALENDARS_PK primary key(SCHED_NAME,CALENDAR_NAME);
---------------------TABLE DDL: QRTZ_CRON_TRIGGERS---------------------
CREATE TABLE SYSDBA.QRTZ_CRON_TRIGGERS
(
    TIME_ZONE_ID character varying(80) ,
    TRIGGER_NAME character varying(200) NOT NULL,
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    CRON_EXPRESSION character varying(200) NOT NULL
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_CRON_TRIGGERS---------------------
INSERT INTO SYSDBA.QRTZ_CRON_TRIGGERS ( TIME_ZONE_ID,TRIGGER_NAME,SCHED_NAME,TRIGGER_GROUP,CRON_EXPRESSION) VALUES ('Asia/Shanghai','NoticeJob_trigger','DefaultQuartzScheduler','DEFAULT_GROUP','0 0/10 * * * ?' ); 

INSERT INTO SYSDBA.QRTZ_CRON_TRIGGERS ( TIME_ZONE_ID,TRIGGER_NAME,SCHED_NAME,TRIGGER_GROUP,CRON_EXPRESSION) VALUES ('Asia/Shanghai','SyncOrganToExtend_trigger','DefaultQuartzScheduler','DEFAULT_GROUP','0 0 0,13 * * ?' ); 

INSERT INTO SYSDBA.QRTZ_CRON_TRIGGERS ( TIME_ZONE_ID,TRIGGER_NAME,SCHED_NAME,TRIGGER_GROUP,CRON_EXPRESSION) VALUES ('Asia/Shanghai','LogJob_trigger','DefaultQuartzScheduler','DEFAULT_GROUP','0 0 0 * * ?' ); 

INSERT INTO SYSDBA.QRTZ_CRON_TRIGGERS ( TIME_ZONE_ID,TRIGGER_NAME,SCHED_NAME,TRIGGER_GROUP,CRON_EXPRESSION) VALUES ('Asia/Shanghai','SystemSerialNumerJob_trigger','DefaultQuartzScheduler','DEFAULT_GROUP','0 0 0 * * ?' ); 

---------------------TABLE DDL: QRTZ_FIRED_TRIGGERS---------------------
CREATE TABLE SYSDBA.QRTZ_FIRED_TRIGGERS
(
    INSTANCE_NAME character varying(200) NOT NULL,
    JOB_NAME character varying(200) ,
    JOB_GROUP character varying(200) ,
    IS_NONCONCURRENT character varying(1) ,
    REQUESTS_RECOVERY character varying(1) ,
    FIRED_TIME bigint NOT NULL,
    SCHED_NAME character varying(120) NOT NULL,
    ENTRY_ID character varying(95) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    SCHED_TIME bigint NOT NULL,
    "PRIORITY" integer NOT NULL,
    STATE character varying(16) NOT NULL, 
    CONSTRAINT QRTZ_FIRED_TRIGGERS_PK PRIMARY KEY (SCHED_NAME, ENTRY_ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_FIRED_TRIGGERS---------------------
---------------------PRIMARY KEY: QRTZ_FIRED_TRIGGERS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_FIRED_TRIGGERS  add constraint QRTZ_FIRED_TRIGGERS_PK primary key(SCHED_NAME,ENTRY_ID);
---------------------TABLE DDL: QRTZ_JOB_DETAILS---------------------
CREATE TABLE SYSDBA.QRTZ_JOB_DETAILS
(
    SCHED_NAME character varying(120) NOT NULL,
    IS_DURABLE character varying(1) NOT NULL,
    JOB_NAME character varying(200) NOT NULL,
    JOB_GROUP character varying(200) NOT NULL,
    JOB_CLASS_NAME character varying(250) NOT NULL,
    IS_NONCONCURRENT character varying(1) NOT NULL,
    IS_UPDATE_DATA character varying(1) NOT NULL,
    REQUESTS_RECOVERY character varying(1) NOT NULL,
    "DESCRIPTION" character varying(250) ,
    JOB_DATA blob DEFAULT NULL , 
    CONSTRAINT QRTZ_JOB_DETAILS_PK PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_JOB_DETAILS---------------------
---------------------PRIMARY KEY: QRTZ_JOB_DETAILS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_JOB_DETAILS  add constraint QRTZ_JOB_DETAILS_PK primary key(SCHED_NAME,JOB_NAME,JOB_GROUP);
---------------------TABLE DDL: QRTZ_LOCKS---------------------
CREATE TABLE SYSDBA.QRTZ_LOCKS
(
    SCHED_NAME character varying(120) NOT NULL,
    LOCK_NAME character varying(40) NOT NULL, 
    CONSTRAINT QRTZ_LOCKS_PK PRIMARY KEY (SCHED_NAME, LOCK_NAME)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_LOCKS---------------------
INSERT INTO SYSDBA.QRTZ_LOCKS ( SCHED_NAME,LOCK_NAME) VALUES ('DefaultQuartzScheduler','TRIGGER_ACCESS' ); 

---------------------PRIMARY KEY: QRTZ_LOCKS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_LOCKS  add constraint QRTZ_LOCKS_PK primary key(SCHED_NAME,LOCK_NAME);
---------------------TABLE DDL: QRTZ_PAUSED_TRIGGER_GRPS---------------------
CREATE TABLE SYSDBA.QRTZ_PAUSED_TRIGGER_GRPS
(
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL, 
    CONSTRAINT QRTZ_PAUSED_TRIGGER_GRPS_PK PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_PAUSED_TRIGGER_GRPS---------------------
---------------------PRIMARY KEY: QRTZ_PAUSED_TRIGGER_GRPS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_PAUSED_TRIGGER_GRPS  add constraint QRTZ_PAUSED_TRIGGER_GRPS_PK primary key(SCHED_NAME,TRIGGER_GROUP);
---------------------TABLE DDL: QRTZ_SCHEDULER_STATE---------------------
CREATE TABLE SYSDBA.QRTZ_SCHEDULER_STATE
(
    SCHED_NAME character varying(120) NOT NULL,
    INSTANCE_NAME character varying(200) NOT NULL,
    LAST_CHECKIN_TIME bigint NOT NULL,
    CHECKIN_INTERVAL bigint NOT NULL, 
    CONSTRAINT QRTZ_SCHEDULER_STATE_PK PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_SCHEDULER_STATE---------------------
---------------------PRIMARY KEY: QRTZ_SCHEDULER_STATE_PK---------------------
ALTER TABLE SYSDBA.QRTZ_SCHEDULER_STATE  add constraint QRTZ_SCHEDULER_STATE_PK primary key(SCHED_NAME,INSTANCE_NAME);
---------------------TABLE DDL: QRTZ_SIMPLE_TRIGGERS---------------------
CREATE TABLE SYSDBA.QRTZ_SIMPLE_TRIGGERS
(
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    REPEAT_COUNT bigint NOT NULL,
    REPEAT_INTERVAL bigint NOT NULL,
    TIMES_TRIGGERED bigint NOT NULL, 
    CONSTRAINT QRTZ_SIMPLE_TRIGGERS_PK PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_SIMPLE_TRIGGERS---------------------
---------------------PRIMARY KEY: QRTZ_SIMPLE_TRIGGERS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_SIMPLE_TRIGGERS  add constraint QRTZ_SIMPLE_TRIGGERS_PK primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
---------------------TABLE DDL: QRTZ_SIMPROP_TRIGGERS---------------------
CREATE TABLE SYSDBA.QRTZ_SIMPROP_TRIGGERS
(
    INT_PROP_1 integer DEFAULT NULL ,
    INT_PROP_2 integer DEFAULT NULL ,
    LONG_PROP_1 bigint DEFAULT NULL ,
    LONG_PROP_2 bigint DEFAULT NULL ,
    DEC_PROP_1 numeric(13,4) DEFAULT NULL ,
    DEC_PROP_2 numeric(13,4) DEFAULT NULL ,
    STR_PROP_1 character varying(512) DEFAULT 'NULL' ,
    STR_PROP_2 character varying(512) DEFAULT 'NULL' ,
    STR_PROP_3 character varying(512) DEFAULT 'NULL' ,
    BOOL_PROP_1 character varying(1) DEFAULT 'NULL' ,
    BOOL_PROP_2 character varying(1) DEFAULT 'NULL' ,
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL, 
    CONSTRAINT QRTZ_SIMPROP_TRIGGERS_PK PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_SIMPROP_TRIGGERS---------------------
---------------------PRIMARY KEY: QRTZ_SIMPROP_TRIGGERS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_SIMPROP_TRIGGERS  add constraint QRTZ_SIMPROP_TRIGGERS_PK primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
---------------------TABLE DDL: QRTZ_TRIGGERS---------------------
CREATE TABLE SYSDBA.QRTZ_TRIGGERS
(
    SCHED_NAME character varying(120) NOT NULL,
    TRIGGER_NAME character varying(200) NOT NULL,
    TRIGGER_GROUP character varying(200) NOT NULL,
    JOB_NAME character varying(200) NOT NULL,
    JOB_GROUP character varying(200) NOT NULL,
    "DESCRIPTION" character varying(250) DEFAULT 'NULL' ,
    NEXT_FIRE_TIME bigint DEFAULT NULL ,
    PREV_FIRE_TIME bigint DEFAULT NULL ,
    "PRIORITY" integer DEFAULT NULL ,
    TRIGGER_STATE character varying(16) NOT NULL,
    TRIGGER_TYPE character varying(8) NOT NULL,
    START_TIME bigint NOT NULL,
    END_TIME bigint DEFAULT NULL ,
    CALENDAR_NAME character varying(200) DEFAULT 'NULL' ,
    MISFIRE_INSTR smallint DEFAULT NULL ,
    JOB_DATA blob DEFAULT NULL , 
    CONSTRAINT QRTZ_TRIGGERS_PK PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: QRTZ_TRIGGERS---------------------
---------------------PRIMARY KEY: QRTZ_TRIGGERS_PK---------------------
ALTER TABLE SYSDBA.QRTZ_TRIGGERS  add constraint QRTZ_TRIGGERS_PK primary key(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
---------------------TABLE DDL: T_DISK_FILE---------------------
CREATE TABLE SYSDBA.T_DISK_FILE
(
    "VERSION" integer DEFAULT NULL ,
    FILE_SIZE bigint DEFAULT NULL ,
    FOLDER_ID character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    CREATE_USER character varying(36) ,
    STATUS character varying(1) ,
    UPDATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    FILE_PATH character varying(1024) ,
    FILE_SUFFIX character varying(36) ,
    FILE_TYPE character varying(36) ,
    KEYWORD character varying(128) ,
    REMARK character varying(255) ,
    SHARE_USER_ID character varying(36) ,
    USER_ID character varying(36) ,
    ID character varying(36) NOT NULL,
    CODE character varying(128) NOT NULL,
    "NAME" character varying(512) NOT NULL, 
    CONSTRAINT T_DISK_FILE_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_DISK_FILE---------------------
---------------------PRIMARY KEY: T_DISK_FILE_PK---------------------
ALTER TABLE SYSDBA.T_DISK_FILE  add constraint T_DISK_FILE_PK primary key(ID);
---------------------TABLE DDL: T_DISK_FOLDER---------------------
CREATE TABLE SYSDBA.T_DISK_FOLDER
(
    CREATE_TIME timestamp without time zone ,
    CREATE_USER character varying(36) ,
    STATUS character varying(1) ,
    UPDATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    "VERSION" integer ,
    CODE character varying(64) ,
    FOLDER_AUTHORIZE character varying(36) ,
    LIMIT_SIZE integer ,
    "NAME" character varying(255) ,
    SORT integer ,
    PARENT_ID character varying(36) ,
    "PATH" character varying(512) ,
    REMARK character varying(255) ,
    "TYPE" character varying(36) ,
    USER_ID character varying(36) ,
    PARENT_IDS character varying(255) ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_DISK_FOLDER_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_DISK_FOLDER---------------------
INSERT INTO SYSDBA.T_DISK_FOLDER ( CREATE_TIME,CREATE_USER,STATUS,UPDATE_TIME,UPDATE_USER,VERSION,CODE,FOLDER_AUTHORIZE,LIMIT_SIZE,NAME,SORT,PARENT_ID,PATH,REMARK,TYPE,USER_ID,PARENT_IDS,ID) VALUES ('2019-09-09 18:08:00.443','1','0','2019-09-09 18:08:00.443','1','1',null,'0',null,'����','30',null,null,null,'1','1','0,','5e01661ad7554459b6137d88a465b1a2' ); 

---------------------PRIMARY KEY: T_DISK_FOLDER_PK---------------------
ALTER TABLE SYSDBA.T_DISK_FOLDER  add constraint T_DISK_FOLDER_PK primary key(ID);
---------------------TABLE DDL: T_NOTICE---------------------
CREATE TABLE SYSDBA.T_NOTICE
(
    "VERSION" integer DEFAULT NULL ,
    END_TOP_DAY integer DEFAULT NULL ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    TITLE character varying(512) ,
    HEAD_IMAGE character varying(36) ,
    "TYPE" character varying(64) ,
    "CONTENT" clob ,
    USER_ID character varying(36) ,
    ORGAN_ID character varying(36) ,
    IS_TOP character(1) ,
    BIZ_MODE character(1) ,
    PUBLISH_TIME timestamp without time zone ,
    EFFECT_TIME timestamp without time zone ,
    INVALID_TIME timestamp without time zone ,
    IS_RECORD_READ character(1) ,
    RECEIVE_SCOPE character varying(36) ,
    APP_ID character varying(64) ,
    TIP_MESSAGE character varying(64) ,
    ID character varying(36) NOT NULL,
    CONSTRAINT T_NOTICE_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_NOTICE---------------------
---------------------PRIMARY KEY: T_NOTICE_PK---------------------
ALTER TABLE SYSDBA.T_NOTICE  add constraint T_NOTICE_PK primary key(ID);
---------------------TABLE DDL: T_NOTICE_FILE---------------------
CREATE TABLE SYSDBA.T_NOTICE_FILE
(
    NOTICE_ID character varying(36) ,
    FILE_ID character varying(36) 
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_NOTICE_FILE---------------------
---------------------TABLE DDL: T_NOTICE_MESSAGE---------------------
CREATE TABLE SYSDBA.T_NOTICE_MESSAGE
(
    "VERSION" integer DEFAULT NULL ,
    TIP_MESSAGE character varying(36) ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    TITLE character varying(512) ,
    "CONTENT" clob ,
    CATEGORY character varying(36) ,
    URL character varying(512) ,
    IMAGE character varying(512) ,
    SEND_TIME timestamp without time zone ,
    SENDER character varying(36) ,
    ORGAN_ID character varying(36) ,
    BIZ_MODE character varying(36) ,
    APP_ID character varying(64) ,
    ID character varying(36) NOT NULL,
    CREATE_TIME timestamp without time zone NOT NULL, 
    CONSTRAINT T_NOTICE_MESSAGE_PK PRIMARY KEY (ID, CREATE_TIME)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_NOTICE_MESSAGE---------------------
---------------------PRIMARY KEY: T_NOTICE_MESSAGE_PK---------------------
ALTER TABLE SYSDBA.T_NOTICE_MESSAGE  add constraint T_NOTICE_MESSAGE_PK primary key(ID,CREATE_TIME);
---------------------TABLE DDL: T_NOTICE_MESSAGE_RECEIVE---------------------
CREATE TABLE SYSDBA.T_NOTICE_MESSAGE_RECEIVE
(
    IS_SEND character(1) ,
    MESSAGE_ID character varying(36) ,
    USER_ID character(32) ,
    IS_READ character(1) ,
    READ_TIME timestamp without time zone ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_NOTICE_MESSAGE_RECEIVE_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_NOTICE_MESSAGE_RECEIVE---------------------
---------------------PRIMARY KEY: T_NOTICE_MESSAGE_RECEIVE_PK---------------------
ALTER TABLE SYSDBA.T_NOTICE_MESSAGE_RECEIVE  add constraint T_NOTICE_MESSAGE_RECEIVE_PK primary key(ID);
---------------------TABLE DDL: T_NOTICE_MESSAGE_SENDER---------------------
CREATE TABLE SYSDBA.T_NOTICE_MESSAGE_SENDER
(
    OBJECT_ID character varying(36) ,
    OBJECT_TYPE character varying(12) ,
    MESSAGE_ID character varying(36) ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_NOTICE_MESSAGE_SENDER_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_NOTICE_MESSAGE_SENDER---------------------
---------------------PRIMARY KEY: T_NOTICE_MESSAGE_SENDER_PK---------------------
ALTER TABLE SYSDBA.T_NOTICE_MESSAGE_SENDER  add constraint T_NOTICE_MESSAGE_SENDER_PK primary key(ID);
---------------------TABLE DDL: T_NOTICE_RECEIVE_INFO---------------------
CREATE TABLE SYSDBA.T_NOTICE_RECEIVE_INFO
(
    READ_TIME timestamp without time zone ,
    NOTICE_ID character varying(36) ,
    USER_ID character(32) ,
    IS_READ character(1) ,
    IS_SEND character(1) ,
    ID character varying(36) NOT NULL,
    CONSTRAINT T_NOTICE_RECEIVE_INFO_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_NOTICE_RECEIVE_INFO---------------------
---------------------PRIMARY KEY: T_NOTICE_RECEIVE_INFO_PK---------------------
ALTER TABLE SYSDBA.T_NOTICE_RECEIVE_INFO  add constraint T_NOTICE_RECEIVE_INFO_PK primary key(ID);
---------------------TABLE DDL: T_NOTICE_SEND_INFO---------------------
CREATE TABLE SYSDBA.T_NOTICE_SEND_INFO
(
    ID character varying(36) NOT NULL,
    NOTICE_ID character varying(36) ,
    RECEIVE_OBJECT_TYPE character(1) ,
    RECEIVE_OBJECT_ID character varying(36) , 
    CONSTRAINT T_NOTICE_SEND_INFO_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_NOTICE_SEND_INFO---------------------
---------------------PRIMARY KEY: T_NOTICE_SEND_INFO_PK---------------------
ALTER TABLE SYSDBA.T_NOTICE_SEND_INFO  add constraint T_NOTICE_SEND_INFO_PK primary key(ID);
---------------------TABLE DDL: T_SYS_AREA---------------------
CREATE TABLE SYSDBA.T_SYS_AREA
(
    "VERSION" integer DEFAULT NULL ,
    SORT integer DEFAULT NULL ,
    PARENT_ID character varying(36) ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    "NAME" character varying(255) ,
    SHORT_NAME character varying(255) ,
    CODE character varying(36) ,
    "TYPE" character varying(36) ,
    PARENT_IDS character varying(512) ,
    REMARK character varying(255) ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_SYS_AREA_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_AREA---------------------
---------------------PRIMARY KEY: T_SYS_AREA_PK---------------------
ALTER TABLE SYSDBA.T_SYS_AREA  add constraint T_SYS_AREA_PK primary key(ID);
---------------------TABLE DDL: T_SYS_CONFIG---------------------
CREATE TABLE SYSDBA.T_SYS_CONFIG
(
    "VALUE" character varying(512) ,
    REMARK character varying(255) ,
    ID character varying(36) NOT NULL,
    CODE character varying(128) NOT NULL, 
    CONSTRAINT T_SYS_CONFIG_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_CONFIG---------------------
---------------------PRIMARY KEY: T_SYS_CONFIG_PK---------------------
ALTER TABLE SYSDBA.T_SYS_CONFIG  add constraint T_SYS_CONFIG_PK primary key(ID);
---------------------TABLE DDL: T_SYS_DICTIONARY---------------------
CREATE TABLE SYSDBA.T_SYS_DICTIONARY
(
    "VERSION" integer DEFAULT NULL ,
    ORDER_NO integer DEFAULT NULL ,
    REMARK character varying(255) ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    "NAME" character varying(100) ,
    CODE character varying(36) ,
    GROUP_ID character varying(36) ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_SYS_DICTIONARY_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_DICTIONARY---------------------
---------------------PRIMARY KEY: T_SYS_DICTIONARY_PK---------------------
ALTER TABLE SYSDBA.T_SYS_DICTIONARY  add constraint T_SYS_DICTIONARY_PK primary key(ID);
---------------------TABLE DDL: T_SYS_DICTIONARY_ITEM---------------------
CREATE TABLE SYSDBA.T_SYS_DICTIONARY_ITEM
(
    "VERSION" integer DEFAULT NULL ,
    ORDER_NO integer DEFAULT NULL ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    DICTIONARY_ID character varying(36) ,
    CODE character varying(64) ,
    "VALUE" character varying(255) ,
    PARENT_ID character varying(36) ,
    REMARK character varying(255) ,
    ID character varying(36) NOT NULL,
    "NAME" character varying(255) NOT NULL, 
    CONSTRAINT T_SYS_DICTIONARY_ITEM_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_DICTIONARY_ITEM---------------------
---------------------PRIMARY KEY: T_SYS_DICTIONARY_ITEM_PK---------------------
ALTER TABLE SYSDBA.T_SYS_DICTIONARY_ITEM  add constraint T_SYS_DICTIONARY_ITEM_PK primary key(ID);
---------------------TABLE DDL: T_SYS_LOG---------------------
CREATE TABLE SYSDBA.T_SYS_LOG
(
    "VERSION" integer DEFAULT NULL ,
    LONGITUDE numeric(10,6) DEFAULT NULL ,
    LATITUDE numeric(10,6) DEFAULT NULL ,
    ACCURACY numeric(10,2) DEFAULT NULL ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone NOT NULL,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    TITLE character varying(255) ,
    "TYPE" character(1) ,
    USER_ID character(32) ,
    IP character varying(64) ,
    OPER_TIME timestamp without time zone ,
    MODULE character varying(255) ,
    "ACTION" character varying(255) ,
    BROWSER_TYPE character varying(128) ,
    DEVICE_TYPE character varying(128) ,
    USER_AGENT clob ,
    ACTION_TIME character varying(20) ,
    REMARK clob ,
    EXCEPTION clob ,
    ID character(32) NOT NULL, 
    CONSTRAINT T_SYS_LOG_PK PRIMARY KEY (CREATE_TIME, ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_LOG---------------------
---------------------PRIMARY KEY: T_SYS_LOG_PK---------------------
ALTER TABLE SYSDBA.T_SYS_LOG  add constraint T_SYS_LOG_PK primary key(CREATE_TIME,ID);
---------------------TABLE DDL: T_SYS_LOG_HISTORY---------------------
CREATE TABLE SYSDBA.T_SYS_LOG_HISTORY
(
    "VERSION" integer DEFAULT NULL ,
    LONGITUDE numeric(10,6) DEFAULT NULL ,
    LATITUDE numeric(10,6) DEFAULT NULL ,
    ACCURACY numeric(10,2) DEFAULT NULL ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    TITLE character varying(512) ,
    "TYPE" character(1) ,
    USER_ID character varying(36) ,
    IP character varying(64) ,
    OPER_TIME timestamp without time zone ,
    MODULE character varying(255) ,
    "ACTION" character varying(255) ,
    BROWSER_TYPE character varying(128) ,
    DEVICE_TYPE character varying(128) ,
    USER_AGENT clob ,
    ACTION_TIME character varying(20) ,
    REMARK clob ,
    EXCEPTION clob ,
    ID character varying(36) NOT NULL,
    CREATE_TIME timestamp without time zone NOT NULL, 
    CONSTRAINT T_SYS_LOG_HISTORY_PK PRIMARY KEY (ID, CREATE_TIME)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_LOG_HISTORY---------------------
---------------------PRIMARY KEY: T_SYS_LOG_HISTORY_PK---------------------
ALTER TABLE SYSDBA.T_SYS_LOG_HISTORY  add constraint T_SYS_LOG_HISTORY_PK primary key(ID,CREATE_TIME);
---------------------TABLE DDL: T_SYS_ORGAN---------------------
CREATE TABLE SYSDBA.T_SYS_ORGAN
(
    ID character varying(36) NOT NULL,
    STATUS character(1) ,
    "VERSION" integer DEFAULT NULL ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    "NAME" character varying(255) NOT NULL,
    SHORT_NAME character varying(255) ,
    "TYPE" character varying(36) ,
    CODE character varying(36) ,
    SYS_CODE character varying(36) ,
    ADDRESS character varying(255) ,
    MOBILE character varying(64) ,
    PHONE character varying(64) ,
    FAX character varying(64) ,
    MANAGER_USER_ID character varying(36) ,
    DEPUTY_MANAGER_USER_ID character varying(128) ,
    SUPER_MANAGER_USER_ID character varying(36) ,
    SORT integer ,
    PARENT_ID character varying(36) ,
    PARENT_IDS character varying(2048) ,
    AREA_ID character varying(36) ,
    REMARK character varying(255) , 
    CONSTRAINT t_sys_organ_pk PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_ORGAN---------------------
INSERT INTO SYSDBA.T_SYS_ORGAN ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,SHORT_NAME,TYPE,CODE,SYS_CODE,ADDRESS,MOBILE,PHONE,FAX,MANAGER_USER_ID,DEPUTY_MANAGER_USER_ID,SUPER_MANAGER_USER_ID,SORT,PARENT_ID,PARENT_IDS,AREA_ID,REMARK) VALUES ('1','0','1','1','2014-09-17 14:08:54','1','2018-05-28 16:05:25','XXX�Ƽ����޹�˾','','0','1','360000','','','','','1',null,'','1','0','0,','',null ); 

---------------------PRIMARY KEY: t_sys_organ_pk---------------------
ALTER TABLE SYSDBA.T_SYS_ORGAN  add constraint t_sys_organ_pk primary key(ID);
---------------------TABLE DDL: T_SYS_ORGAN_EXTEND---------------------
CREATE TABLE SYSDBA.T_SYS_ORGAN_EXTEND
(
    ID character varying(36) NOT NULL,
    STATUS character(1) ,
    "VERSION" integer DEFAULT NULL ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    "NAME" character varying(255) NOT NULL,
    SHORT_NAME character varying(255) ,
    "TYPE" character varying(36) ,
    CODE character varying(36) ,
    SYS_CODE character varying(36) ,
    ADDRESS character varying(255) ,
    MOBILE character varying(64) ,
    PHONE character varying(64) ,
    FAX character varying(64) ,
    MANAGER_USER_ID character varying(36) ,
    DEPUTY_MANAGER_USER_ID character varying(128) ,
    SUPER_MANAGER_USER_ID character varying(36) ,
    SORT integer ,
    PARENT_ID character varying(36) ,
    PARENT_IDS character varying(2048) ,
    AREA_ID character varying(36) ,
    REMARK character varying(255) ,
    COMPANY_ID character varying(36) ,
    COMPANY_CODE character varying(36) ,
    HOME_COMPANY_ID character varying(36) ,
    HOME_COMPANY_CODE character varying(36) ,
    TREE_LEVEL integer , 
    CONSTRAINT t_sys_organ_extend_pk PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_ORGAN_EXTEND---------------------
INSERT INTO SYSDBA.T_SYS_ORGAN_EXTEND ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,SHORT_NAME,TYPE,CODE,SYS_CODE,ADDRESS,MOBILE,PHONE,FAX,MANAGER_USER_ID,DEPUTY_MANAGER_USER_ID,SUPER_MANAGER_USER_ID,SORT,PARENT_ID,PARENT_IDS,AREA_ID,REMARK,COMPANY_ID,COMPANY_CODE,HOME_COMPANY_ID,HOME_COMPANY_CODE,TREE_LEVEL) VALUES ('1','0','1','1','2014-09-17 14:08:54','1','2018-05-28 16:05:25','XXX�Ƽ����޹�˾','','0','1','360000','','','','','1',null,'','1','0','0,','',null,'1','1','1','1','1' ); 

---------------------PRIMARY KEY: t_sys_organ_extend_pk---------------------
ALTER TABLE SYSDBA.T_SYS_ORGAN_EXTEND  add constraint t_sys_organ_extend_pk primary key(ID);
---------------------TABLE DDL: T_SYS_POST---------------------
CREATE TABLE SYSDBA.T_SYS_POST
(
    "VERSION" integer DEFAULT NULL ,
    SORT integer DEFAULT NULL ,
    ORGAN_ID character varying(36) ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    "NAME" character varying(255) ,
    CODE character varying(36) ,
    REMARK character varying(255) ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_SYS_POST_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_POST---------------------
---------------------PRIMARY KEY: T_SYS_POST_PK---------------------
ALTER TABLE SYSDBA.T_SYS_POST  add constraint T_SYS_POST_PK primary key(ID);
---------------------TABLE DDL: T_SYS_POST_ORGAN---------------------
CREATE TABLE SYSDBA.T_SYS_POST_ORGAN
(
    POST_ID character varying(36) ,
    ORGAN_ID character varying(36) 
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_POST_ORGAN---------------------
---------------------TABLE DDL: T_SYS_RESOURCE---------------------
CREATE TABLE SYSDBA.T_SYS_RESOURCE
(
    ID character varying(36) NOT NULL,
    STATUS character(1) ,
    "VERSION" integer DEFAULT NULL ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    "NAME" character varying(255) NOT NULL,
    CODE character varying(64) ,
    URL character varying(512) ,
    SORT integer ,
    ICON_CLS character varying(128) ,
    ICON character varying(255) ,
    MARK_URL character varying(1024) ,
    "TYPE" character varying(36) ,
    PARENT_ID character varying(36) ,
    PARENT_IDS character varying(1024) , 
    CONSTRAINT t_sys_resource_pk PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_RESOURCE---------------------
INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('01c3cce8ab694f0cbd18ef4c9747f9f5','0','2','1','2017-10-12 08:26:04','1','2018-05-28 15:53:51','�����û�','sys:session:view','/a/sys/session','80','',null,'','0','c02bcd465534419f8bbeb3eb1965f5ad','null1,c02bcd465534419f8bbeb3eb1965f5ad,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('0611b11c98d64146877c6c980f482454','0','1','1','2018-05-28 15:53:18','1','2018-05-28 15:53:18','�༭','sys:role:edit','','440','',null,'','1','3','null1,3,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('075453bed9a245489162bf3a7aec4740','3','3','1','2018-05-28 16:05:16','1','2018-05-28 16:08:01','�ļ�����','disk:disk:search','/a/disk/search','650','',null,'','0','80c14d6e60f94c67b26b5f44bd2bf309','null1,80c14d6e60f94c67b26b5f44bd2bf309,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('1','0','6',null,null,'admin','2015-01-27 15:46:10','ϵͳ����','','','9','eu-icon-application',null,'','0',null,null ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('155d9fa508a3497686a31b1c8c68a718','0','4','1','2016-03-28 21:43:47','1','2018-05-28 16:09:41','��������','sys:config:view','/a/sys/config','100','',null,'','0','c02bcd465534419f8bbeb3eb1965f5ad','null1,c02bcd465534419f8bbeb3eb1965f5ad,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('1918c2e5f04547b180891cf2fe7488bb','0','2','ac9c62d1646942348eac686a7d41b0dc','2015-08-04 10:01:26','1','2018-05-09 16:53:36','����','notice:publish','','1','',null,'','1','78c495a1eab4439fb64b1ac5d568f4b0','null1,78c495a1eab4439fb64b1ac5d568f4b0,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('1c8d27bab3b34151929e9d36fc304847','0','4','1','2018-05-25 15:42:17','1','2018-05-28 16:10:35','ϵͳ����','sys:versionLog:view','/a/sys/versionLog','260','',null,'','0','c02bcd465534419f8bbeb3eb1965f5ad','null1,c02bcd465534419f8bbeb3eb1965f5ad,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('1f6dc7330e224ee6b902a78cdcc847d3','0','4','1','2018-05-25 15:50:30','1','2018-05-25 16:14:40','���кŹ���','sys:systemSerialNumber:view','/a/sys/systemSerialNumber','290','',null,'','0','c02bcd465534419f8bbeb3eb1965f5ad','null1,c02bcd465534419f8bbeb3eb1965f5ad,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('2','0','16',null,null,'1','2019-09-09 18:54:28.827','��Դ����','sys:resource:view','/a/sys/resource','2','eu-icon-link',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('21','0','7','admin','2013-12-08 17:26:38','1','2019-09-04 09:25:49','��־����','sys:log:view','/a/sys/log','10','eu-icon-server',null,'','0','b6a78f488c614d479cd95dbd176dcaab','null1,b6a78f488c614d479cd95dbd176dcaab,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('26','0','7','admin','2014-06-11 19:43:48','1','2018-05-28 16:01:25','��λ����','sys:post:view','/a/sys/post','5','eu-icon-vcard',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('2e0a307dea114a4b809e6d8111365b80','0','2','1','2018-05-28 16:02:44','1','2018-05-28 16:02:54','�༭','sys:log:edit','','620','',null,'','1','21','null1,21,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('3','0','15','admin','2013-11-12 22:13:42','1','2018-05-28 15:53:09','��ɫ����','sys:role:view','/a/sys/role','3','eu-icon-group',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('301d6fcef1594dcd82786afe4d59272b','0','3','1','2017-10-12 08:26:40','1','2018-05-28 15:54:59','ϵͳ���','sys:systemMonitor:view','/a/sys/systemMonitor','110','',null,'','0','c02bcd465534419f8bbeb3eb1965f5ad','null1,c02bcd465534419f8bbeb3eb1965f5ad,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('4','0','7','admin','2013-11-12 22:14:10','1','2018-05-28 15:55:43','��������','sys:organ:view','/a/sys/organ','4','eu-icon-gears',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('4ec49ed312da4ca9a4b74a0a3fd40dc2','0','1','1','2018-05-28 15:52:30','1','2018-05-28 15:52:30','�༭','sys:resource:edit','','410','',null,'','1','2','null1,2,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('5','0','6','admin','2013-11-12 22:14:28','1','2018-05-28 15:51:04','�û�����','sys:user:view','/a/sys/user','6','eu-icon-user',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('5e141f0314114e25b67d4bc28d906309','0','1','1','2018-05-28 16:11:14','1','2018-05-28 16:11:14','�༭','sys:versionLog:edit','','740','',null,'','1','1c8d27bab3b34151929e9d36fc304847','null1,c02bcd465534419f8bbeb3eb1965f5ad,1c8d27bab3b34151929e9d36fc304847,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('6e4535a55d544d69aed7692010d17291','0','1','1','2018-05-25 16:14:29','1','2018-05-25 16:14:29','�༭','sys:systemSerialNumber:edit','','350','',null,'','1','1f6dc7330e224ee6b902a78cdcc847d3','null1,c02bcd465534419f8bbeb3eb1965f5ad,1f6dc7330e224ee6b902a78cdcc847d3,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('78c495a1eab4439fb64b1ac5d568f4b0','0','2','1','2016-03-28 21:42:46','1','2016-03-28 21:43:55','�ҵ�֪ͨ','','/a/notice','50','',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('8','0','10','admin','2013-11-12 22:15:40','1','2018-05-28 16:08:19','�����ֵ�','sys:dictionary:view','/a/sys/dictionary','8','eu-icon-book',null,'','0','c02bcd465534419f8bbeb3eb1965f5ad','null1,c02bcd465534419f8bbeb3eb1965f5ad,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('80c14d6e60f94c67b26b5f44bd2bf309','0','3','1','2018-05-04 09:40:03','1','2018-05-28 16:04:29','�ҵ�����','disk:disk:view','/a/disk','200','',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('824214907b0e4070921c5425f5eceb3d','0','1','1','2018-05-28 16:09:51','1','2018-05-28 16:09:51','�༭','sys:config:edit','','710','',null,'','1','155d9fa508a3497686a31b1c8c68a718','null1,c02bcd465534419f8bbeb3eb1965f5ad,155d9fa508a3497686a31b1c8c68a718,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('8382c51e177d49019c7cecd870a9aaea','0','3','1','2018-06-01 15:48:41','1','2019-09-04 09:26:00','��¼ͳ��','sys:log:loginStatistics','/a/sys/log/report/loginStatistics','770','',null,'','0','b6a78f488c614d479cd95dbd176dcaab','null1,b6a78f488c614d479cd95dbd176dcaab,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('93191289514b4a5ea00140bada7903ad','0','1','1','2018-04-12 08:56:40','1','2018-05-09 16:53:36','�༭','sys:area:edit','','170','',null,'','1','9d97b7d7222f4a698a1858ac76013370','null1,9d97b7d7222f4a698a1858ac76013370,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('9d97b7d7222f4a698a1858ac76013370','0','2','1','2018-04-12 08:51:43','1','2018-05-25 16:11:45','�������','sys:area:view','/a/sys/area','140','',null,'','0','c02bcd465534419f8bbeb3eb1965f5ad','null1,c02bcd465534419f8bbeb3eb1965f5ad,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('a3a942fb7f5d444a968e1659b645083a','0','1','1','2018-05-28 16:01:35','1','2018-05-28 16:01:35','�༭','sys:post:edit','','590','',null,'','1','26','null1,26,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('b0b955c8379f46eca3cb0e25a7854246','0','2','1','2018-05-28 15:54:31','1','2018-05-28 16:13:51','�༭','sys:session:edit','','470','',null,'','1','01c3cce8ab694f0cbd18ef4c9747f9f5','null1,c02bcd465534419f8bbeb3eb1965f5ad,01c3cce8ab694f0cbd18ef4c9747f9f5,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('b6a78f488c614d479cd95dbd176dcaab','0','2','1','2019-09-04 09:25:17','1','2019-09-04 09:25:39','��־����','','','860','',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('b6db739c282945d9a32725b1d79fa85c','0','1','1','2018-05-28 15:51:25','1','2018-05-28 15:51:25','�༭','sys:user:edit','','380','',null,'','1','5','null1,5,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('c02bcd465534419f8bbeb3eb1965f5ad','0','1','1','2018-05-25 16:11:23','1','2018-05-25 16:11:23','��������','','','320','',null,'','0','1','null1,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('c0644579ba7f47db8be51e3ef72008d2','0','4','1','2018-06-01 15:50:00','1','2019-09-04 09:26:08','ÿ�յ�½��������','sys:log:dayLoginStatistics','/a/sys/log/report/dayLoginStatistics','800','',null,'','0','b6a78f488c614d479cd95dbd176dcaab','null1,b6a78f488c614d479cd95dbd176dcaab,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('c1bcad5f18844d139a47fe9190920d5c','3','2','1','2018-05-28 16:00:53','1','2018-05-28 16:17:25','��ʼ������ϵͳ����','sys:organ:sync','/a/sys/organ/initSysCode','560','',null,'','0','4','null1,4,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('c28a601cfb45474eac5698fafc802d25','0','1','1','2018-05-28 16:08:58','1','2018-05-28 16:08:58','�༭','sys:dictionary:edit','','680','',null,'','1','8','null1,c02bcd465534419f8bbeb3eb1965f5ad,8,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('c843600b13aa4a8a95891919e9b895c3','0','1','1','2018-05-28 15:56:35','1','2018-05-28 15:56:35','�༭','sys:organ:edit','','500','',null,'','1','4','null1,4,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('cc2f06b68653441bb84698befb02aa67','0','3','1','2018-06-01 15:50:46','1','2019-09-04 09:26:13','ģ�����ͳ��','sys:log:moduleStatistics','/a/sys/log/report/moduleStatistics','830','',null,'','0','b6a78f488c614d479cd95dbd176dcaab','null1,b6a78f488c614d479cd95dbd176dcaab,' ); 

INSERT INTO SYSDBA.T_SYS_RESOURCE ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,NAME,CODE,URL,SORT,ICON_CLS,ICON,MARK_URL,TYPE,PARENT_ID,PARENT_IDS) VALUES ('e5d1933bb1c64ca3b76969a0b14e9c2f','3','2','1','2018-05-28 15:57:36','1','2018-05-28 16:17:23','ͬ������ID','sys:organ:sync','/a/sys/organ/syncAllParentIds','530','',null,'','0','4','null1,4,' ); 

---------------------PRIMARY KEY: t_sys_resource_pk---------------------
ALTER TABLE SYSDBA.T_SYS_RESOURCE  add constraint t_sys_resource_pk primary key(ID);
---------------------TABLE DDL: T_SYS_ROLE---------------------
CREATE TABLE SYSDBA.T_SYS_ROLE
(
    "VERSION" integer DEFAULT NULL ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    CODE character varying(36) ,
    ORGAN_ID character varying(36) ,
    IS_SYSTEM character(1) ,
    IS_ACTIVITY character(1) ,
    ROLE_TYPE character varying(36) ,
    DATA_SCOPE character(1) ,
    REMARK character varying(255) ,
    ID character varying(36) NOT NULL,
    "NAME" character varying(255) NOT NULL, 
    CONSTRAINT T_SYS_ROLE_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_ROLE---------------------
---------------------PRIMARY KEY: T_SYS_ROLE_PK---------------------
ALTER TABLE SYSDBA.T_SYS_ROLE  add constraint T_SYS_ROLE_PK primary key(ID);
---------------------TABLE DDL: T_SYS_ROLE_ORGAN---------------------
CREATE TABLE SYSDBA.T_SYS_ROLE_ORGAN
(
    ROLE_ID character varying(36) ,
    ORGAN_ID character varying(36)
)
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_ROLE_ORGAN---------------------
                               ---------------------TABLE DDL: T_SYS_DATA_ROLE_ORGAN---------------------
CREATE TABLE SYSDBA.T_SYS_DATA_ROLE_ORGAN
(
    ROLE_ID character varying(36) ,
    ORGAN_ID character varying(36)
)
BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_DATA_ROLE_ORGAN---------------------
---------------------TABLE DDL: T_SYS_ROLE_RESOURCE---------------------
CREATE TABLE SYSDBA.T_SYS_ROLE_RESOURCE
(
    ROLE_ID character varying(36) ,
    RESOURCE_ID character varying(36) 
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_ROLE_RESOURCE---------------------
---------------------TABLE DDL: T_SYS_SERIAL_NUMBER---------------------
CREATE TABLE SYSDBA.T_SYS_SERIAL_NUMBER
(
    "VERSION" integer DEFAULT NULL ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    APP character varying(36) ,
    MODULE_NAME character varying(255) ,
    MODULE_CODE character varying(64) ,
    CONFIG_TEMPLATE character varying(128) ,
    MAX_SERIAL character varying(128) ,
    IS_AUTO_INCREMENT character(1) ,
    PRE_MAX_NUM character varying(36) ,
    REMARK character varying(255) ,
    RESET_TYPE character(1) ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_SYS_SERIAL_NUMBER_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_SERIAL_NUMBER---------------------
---------------------PRIMARY KEY: T_SYS_SERIAL_NUMBER_PK---------------------
ALTER TABLE SYSDBA.T_SYS_SERIAL_NUMBER  add constraint T_SYS_SERIAL_NUMBER_PK primary key(ID);
---------------------TABLE DDL: T_SYS_USER---------------------
CREATE TABLE SYSDBA.T_SYS_USER
(
    ID character varying(36) NOT NULL,
    STATUS character(1) ,
    "VERSION" integer DEFAULT NULL ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    LOGIN_NAME character varying(36) NOT NULL,
    CODE character varying(64) ,
    ORIGINAL_PASSWORD character varying(128) ,
    "PASSWORD" character varying(128) ,
    "NAME" character varying(36) ,
    SEX character varying(36) ,
    BIRTHDAY date ,
    PHOTO character varying(512) ,
    EMAIL character varying(64) ,
    PERSON_EMAIL character varying(64) ,
    MOBILE character varying(36) ,
    TEL character varying(36) ,
    QQ character varying(36) ,
    ADDRESS character varying(255) ,
    DEFAULT_ORGAN_ID character varying(36) ,
    SORT integer ,
    USER_TYPE character varying(36) ,
    REMARK character varying(255) ,
    WEIXIN character varying(64) , 
    CONSTRAINT t_sys_user_pk PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_USER---------------------
INSERT INTO SYSDBA.T_SYS_USER ( ID,STATUS,VERSION,CREATE_USER,CREATE_TIME,UPDATE_USER,UPDATE_TIME,LOGIN_NAME,CODE,ORIGINAL_PASSWORD,PASSWORD,NAME,SEX,BIRTHDAY,PHOTO,EMAIL,PERSON_EMAIL,MOBILE,TEL,QQ,ADDRESS,DEFAULT_ORGAN_ID,SORT,USER_TYPE,REMARK,WEIXIN) VALUES ('1','0','17',null,null,'1','2018-05-28 16:18:29','admin',null,'7e0cd7be3e66d4a8','79b2cf0337180351d2dcc5ee9d625481','����Ա','1','2018-05-10 00:00:00','/a/disk/file/f6c3178ee45a4ec6a5b24bb26c036fa4.png','','','','','','','1','1','0','',null ); 

---------------------PRIMARY KEY: t_sys_user_pk---------------------
ALTER TABLE SYSDBA.T_SYS_USER  add constraint t_sys_user_pk primary key(ID);
---------------------TABLE DDL: T_SYS_USER_ORGAN---------------------
CREATE TABLE SYSDBA.T_SYS_USER_ORGAN
(
    USER_ID character varying(36) ,
    ORGAN_ID character varying(36) 
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_USER_ORGAN---------------------
INSERT INTO SYSDBA.T_SYS_USER_ORGAN ( USER_ID,ORGAN_ID) VALUES ('1','1' ); 

---------------------TABLE DDL: T_SYS_USER_PASSWORD---------------------
CREATE TABLE SYSDBA.T_SYS_USER_PASSWORD
(
    "VERSION" integer DEFAULT NULL ,
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    USER_ID character varying(36) ,
    MODIFY_TIME timestamp without time zone ,
    ORIGINAL_PASSWORD character varying(128) ,
    "PASSWORD" character varying(128) ,
    ID character varying(36) NOT NULL, 
    CONSTRAINT T_SYS_USER_PASSWORD_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_USER_PASSWORD---------------------
---------------------PRIMARY KEY: T_SYS_USER_PASSWORD_PK---------------------
ALTER TABLE SYSDBA.T_SYS_USER_PASSWORD  add constraint T_SYS_USER_PASSWORD_PK primary key(ID);
---------------------TABLE DDL: T_SYS_USER_POST---------------------
CREATE TABLE SYSDBA.T_SYS_USER_POST
(
    ORGAN_ID character varying(36) ,
    USER_ID character varying(36) ,
    POST_ID character varying(36) 
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_USER_POST---------------------
---------------------TABLE DDL: T_SYS_USER_RESOURCE---------------------
CREATE TABLE SYSDBA.T_SYS_USER_RESOURCE
(
    RESOURCE_ID character varying(36) ,
    USER_ID character varying(36) 
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_USER_RESOURCE---------------------
---------------------TABLE DDL: T_SYS_USER_ROLE---------------------
CREATE TABLE SYSDBA.T_SYS_USER_ROLE
(
    USER_ID character varying(36) ,
    ROLE_ID character varying(36) 
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_USER_ROLE---------------------
---------------------TABLE DDL: T_SYS_VERSION_LOG---------------------
CREATE TABLE SYSDBA.T_SYS_VERSION_LOG
(
    STATUS character(1) ,
    CREATE_USER character varying(36) ,
    CREATE_TIME timestamp without time zone ,
    UPDATE_USER character varying(36) ,
    UPDATE_TIME timestamp without time zone ,
    "VERSION" character varying(128) ,
    APP character varying(36) ,
    VERSION_LOG_TYPE character varying(36) ,
    VERSION_CODE character varying(128) ,
    VERSION_NAME character varying(128) ,
    IS_PUB character(1) ,
    PUB_TIME timestamp without time zone ,
    REMARK clob ,
    FILE_ID character varying(36) ,
    IS_TIP character(1) ,
    IS_SHELF character(1) ,
    ID character varying(36) NOT NULL,
    CONSTRAINT T_SYS_VERSION_LOG_PK PRIMARY KEY (ID)
)  
 BINLOG ON ;
---------------------TABLE INSERT SQL: T_SYS_VERSION_LOG---------------------
---------------------PRIMARY KEY: T_SYS_VERSION_LOG_PK---------------------
ALTER TABLE SYSDBA.T_SYS_VERSION_LOG  add constraint T_SYS_VERSION_LOG_PK primary key(ID);
