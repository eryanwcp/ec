package com.eryansky.core.orm.mybatis.type;

import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.modules.sys.vo.ExtendAttr;
import com.eryansky.modules.sys.vo.ExtendAttrItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义json类型数据处理
 * {@link com.eryansky.common.orm.mybatis.type.JsonTypeHandler}
 * @author Eryan
 * @version 2023-12-18
 */
public class ExtendAttrJsonTypeHandler extends BaseTypeHandler<ExtendAttr> {

    private static  final Logger logger = LoggerFactory.getLogger(ExtendAttrJsonTypeHandler.class);

    private static final JsonMapper jsonMapper;
    private Class<ExtendAttr> type;

    static {
        jsonMapper = JsonMapper.getInstance();
    }

    public ExtendAttrJsonTypeHandler(Class<ExtendAttr> type) {
        if (logger.isTraceEnabled()) {
            logger.trace("JsonTypeHandler(" + type + ")");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    public ExtendAttrJsonTypeHandler() {

    }

    private ExtendAttr parse(String json) {
        try {
            if (json == null || json.length() == 0) {
                return null;
            }
            Map<String,Object> map = jsonMapper.readValue(json, HashMap.class);
            return new ExtendAttr().setItems(map.entrySet().stream().map(v-> new ExtendAttrItem(v.getKey(),v.getValue())).collect(Collectors.toSet()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJsonString(ExtendAttr obj) {
        try {
            return jsonMapper.writeValueAsString(obj.toMap());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExtendAttr getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public ExtendAttr getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public ExtendAttr getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int columnIndex, ExtendAttr parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(columnIndex, toJsonString(parameter));

    }

}