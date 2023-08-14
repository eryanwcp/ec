package com.eryansky.common.orm.mybatis.type;

import com.eryansky.common.orm._enum.IGenericEnum;
import com.eryansky.common.orm._enum.GenericEnumUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 泛型枚举类型数据处理
 * @author Eryan
 * @version 2020-02-12
 */
public class GenericEnumTypeHandler<T extends Enum<T> & IGenericEnum<T>> extends BaseTypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(GenericEnumTypeHandler.class);

    private final Class<T> type;

    public GenericEnumTypeHandler(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }


    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i,parameter.getValue());
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return GenericEnumUtils.getByValue(type,value);
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return GenericEnumUtils.getByValue(type,value);
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return GenericEnumUtils.getByValue(type,value);
    }


}