package com.eryansky.modules.sys.mapper;

import java.sql.JDBCType;
import java.util.Date;

import com.eryansky.core.orm.mybatis.entity.DataSqlTable;
import org.mybatis.dynamic.sql.SqlColumn;

/**
 * 版本更新动态SQL支持类
 * @author Eryan
 * @date 2020-03-03
 */
public final class VersionLogDynamicSqlSupport {

    public static final VersionLogTable data = new VersionLogTable();

    public static final SqlColumn<String> id = data.id;
    public final SqlColumn<String> status = data.status;
    public final SqlColumn<Integer> version = data.version;
    public final SqlColumn<String> createUser = data.createUser;
    public final SqlColumn<Date> createTime = data.createTime;
    public final SqlColumn<String> updateUser = data.updateUser;
    public final SqlColumn<Date> updateTime = data.updateTime;

    public static final SqlColumn<String> versionName = data.versionName;
    public static final SqlColumn<String> versionCode = data.versionCode;
    public static final SqlColumn<String> fileId = data.fileId;
    public static final SqlColumn<String> app = data.app;
    public static final SqlColumn<String> versionLogType = data.versionLogType;
    public static final SqlColumn<String> isPub = data.isPub;
    public static final SqlColumn<String> isTip = data.isTip;
    public static final SqlColumn<String> isShelf = data.isShelf;
    public static final SqlColumn<Date> pubTime = data.pubTime;
    public static final SqlColumn<String> remark = data.remark;

    public static final class VersionLogTable extends DataSqlTable<VersionLogTable> {

        public final SqlColumn<String> versionName = column("version_name", JDBCType.VARCHAR);
        public final SqlColumn<String> versionCode = column("version_code", JDBCType.VARCHAR);
        public final SqlColumn<String> fileId = column("file_id", JDBCType.VARCHAR);
        public final SqlColumn<String> app = column("app", JDBCType.VARCHAR);
        public final SqlColumn<String> versionLogType = column("version_log_type", JDBCType.VARCHAR);
        public final SqlColumn<String> isPub = column("is_pub", JDBCType.VARCHAR);
        public final SqlColumn<String> isTip = column("is_tip", JDBCType.VARCHAR);
        public final SqlColumn<String> isShelf = column("is_shelf", JDBCType.VARCHAR);
        public final SqlColumn<Date> pubTime = column("pub_time", JDBCType.DATE);
        public final SqlColumn<String> remark = column("remark", JDBCType.VARCHAR);

        public VersionLogTable() {
            super("t_sys_version_log");
        }
    }
}