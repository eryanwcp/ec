/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.service;

import com.eryansky.common.exception.DaoException;
import com.eryansky.common.exception.SystemException;
import com.eryansky.common.orm.Page;
import com.eryansky.common.orm.model.Parameter;
import com.eryansky.common.orm.mybatis.interceptor.BaseInterceptor;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.core.orm.mybatis.service.CrudService;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.modules.sys.dao.LogDao;
import com.eryansky.modules.sys.mapper.Log;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Eryan
 * @date 2015-09-26
 */
@Service
public class LogService extends CrudService<LogDao, Log> {

    @Override
    public Page<Log> findPage(Page<Log> page, Log entity) {
        entity.setEntityPage(page);
        page.setResult(dao.findList(entity));
        return page;
    }

    public Page<Log> findQueryPage(Page<Log> page, Log entity) {
        Parameter parameter = Parameter.newPageParameter(page, AppConstants.getJdbcType());
        page.setResult(dao.findQueryList(parameter));
        return page;
    }

    /**
     * 自定义查询（分页）
     *
     * @param page      分页对象
     * @param type      日志类型{@link com.eryansky.modules.sys._enum.LogType}
     * @param query     关键字
     * @param userInfo  用户信息
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    public Page<Log> findQueryPage(Page<Log> page,String type, String userInfo, String query, Date startTime, Date endTime) {
        return findQueryPage(page, type,userInfo, query, startTime, endTime,true);
    }

    /**
     * 自定义查询（分页）
     *
     * @param page              分页对象
     * @param type              日志类型{@link com.eryansky.modules.sys._enum.LogType}
     * @param userInfo          用户信息
     * @param query             关键字
     * @param startTime         开始时间
     * @param endTime           结束时间
     * @param isDataScopeFilter 分级数据权限
     * @return
     */
    public Page<Log> findQueryPage(Page<Log> page, String type, String userInfo, String query, Date startTime, Date endTime, boolean isDataScopeFilter) {
        Parameter parameter = Parameter.newPageParameter(page, AppConstants.getJdbcType());
        parameter.put("type", type);
        parameter.put("userInfo", userInfo);
        parameter.put("query", query);
        parameter.put("startTime", null != startTime ? DateUtils.formatDate(startTime) : null);
        parameter.put("endTime", null != endTime ? DateUtils.formatDate(endTime) : null);
        Map<String, String> sqlMap = Maps.newHashMap();
        sqlMap.put("dsf", "");
        if (isDataScopeFilter) {
            sqlMap.put("dsf", dataScopeFilter(SecurityUtils.getCurrentUser(), "o", "u"));
        }
        parameter.put("sqlMap", sqlMap);
        page.setResult(dao.findQueryList(parameter));
        return page;
    }

    /**
     * 删除
     *
     * @param ids
     */
    public void deleteByIds(Collection<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            for (String id : ids) {
                deleteById(id);
            }
        } else {
            logger.warn("参数[ids]为空.");
        }
    }

    /**
     * 删除
     *
     * @param id
     */
    public void deleteById(String id) {
        dao.delete(new Log(id));
    }

    /**
     * 删除日志（物理删除）
     *
     * @return
     */
    public int clear(String id) {
        int reslutCount = super.clear(id);
        logger.debug("清除日志：{}", reslutCount);
        return reslutCount;
    }

    /**
     * 清空所有日志物理删除）
     *
     * @return
     */
    public int clearAll() {
        int reslutCount = super.clearAll();
        logger.debug("清空日志：{}", reslutCount);
        return reslutCount;
    }

    /**
     * 清空有效期之外的日志
     *
     * @param day 保留时间 （天）
     * @throws DaoException
     * @throws SystemException
     */
    public int clearInvalidLog(int day) {
        if (day < 0) {
            throw new SystemException("参数[day]不合法，需要大于0.输入为：" + day);
        }
        Date now = new Date();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(now); // 得到gc格式的时间
        gc.add(5, -day); // 2表示月的加减，年代表1依次类推(３周....5天。。)
        // 把运算完的时间从新赋进对象
        gc.set(gc.get(gc.YEAR), gc.get(gc.MONTH), gc.get(gc.DATE));

        Parameter parameter = new Parameter();
        parameter.put("operTime", DateUtils.format(gc.getTime(), DateUtils.DATE_FORMAT));
        int result = dao.clearInvalidLog(parameter);
        return result;
    }


    /**
     * 清空有效期之外的日志
     *
     * @param day 保留时间 （天）
     * @throws DaoException
     * @throws SystemException
     */
    public void insertToHistoryAndClear(int day) {
        if (day < 0) {
            throw new SystemException("参数[day]不合法，需要大于0.输入为：" + day);
        }
        Date now = new Date();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(now); // 得到gc格式的时间
        gc.add(Calendar.DAY_OF_MONTH, -day); // 2表示月的加减，年代表1依次类推(３周....5天。。)
        // 把运算完的时间从新赋进对象
        gc.set(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH), gc.get(Calendar.DATE));

        Parameter parameter = new Parameter();
        parameter.put("createTime", DateUtils.format(gc.getTime(), DateUtils.DATE_FORMAT));


        logger.info("备份日志表开始：{}", DateUtils.format(gc.getTime(), DateUtils.DATE_FORMAT));
        int countInsert = dao.insertToHistory(parameter);//插入历史表
        logger.info("备份日志表结束:{}", new Object[]{countInsert});
        logger.info("删除日志表开始：{}", DateUtils.format(gc.getTime(), DateUtils.DATE_FORMAT));
        int countDelete = dao.clearInvalidLog(parameter);//删除数据
        logger.info("删除日志表结束:{}", new Object[]{countDelete});

        parameter = new Parameter();
        gc.add(Calendar.DAY_OF_MONTH, -day);
        parameter.put("createTime", DateUtils.format(gc.getTime(), DateUtils.DATE_FORMAT));
        logger.info("删除历史日志表开始：{}", DateUtils.format(gc.getTime(), DateUtils.DATE_FORMAT));
        int countClearHistory = dao.clearHistoryLog(parameter);//删除历史日志数据
        logger.info("删除历史日志表结束:{}", new Object[]{countClearHistory});
    }

    /**
     * 查询用户登录次数
     *
     * @param userId
     * @return
     */
    public Long getUserLoginCount(String userId) {
        return getUserLoginCount(userId, null, null);
    }

    /**
     * 查询用户登录次数
     *
     * @param userId
     * @param startTime
     * @param endTime
     * @return
     */
    public Long getUserLoginCount(String userId, Date startTime, Date endTime) {
        Parameter parameter = new Parameter();
        parameter.put("userId", userId);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        if (startTime != null) {
            parameter.put("startTime", DateUtils.format(startTime, DateUtils.DATE_TIME_FORMAT));
        }
        if (startTime != null) {
            parameter.put("endTime", DateUtils.format(endTime, DateUtils.DATE_TIME_FORMAT));
        }
        return dao.getUserLoginCount(parameter);
    }

    /**
     * 数据修复 title
     */
    public void dataAutoFix() {
        List<Log> list = dao.findNullData();
        if (Collections3.isNotEmpty(list)) {
            for (Log log : list) {
                Log _log = dao.getNotNullData(log.getModule());
                if (_log != null && StringUtils.isBlank(log.getTitle())) {
                    log.setTitle(_log.getTitle());
                    dao.update(log);
                }
            }
        }

    }

    /**
     * 员工登录统计
     */
    public Page<Map<String, Object>> getLoginStatistics(Page pg, String name, String startTime, String endTime) {
        Parameter parameter = Parameter.newParameter();
        if (StringUtils.isNotBlank(startTime)) {
            parameter.put("startTime", startTime + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endTime)) {
            parameter.put("endTime", endTime + " 23:59:59");
        }
        parameter.put("name", name);
        parameter.put(BaseInterceptor.PAGE, pg);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        parameter.put(Log.FIELD_STATUS, Log.STATUS_NORMAL);
        List<Map<String, Object>> mapList = dao.getLoginStatistics(parameter);
        pg.setResult(mapList);
        return pg;
    }

    /**
     * 模块访问统计
     */
    public Page<Map<String, Object>> getModuleStatistics(Page pg, String objectIds, String organId, Boolean onlyCompany, String startTime, String endTime, String postCode) {
        Parameter parameter = new Parameter();
        if (StringUtils.isNotBlank(startTime)) {
            parameter.put("startTime", startTime + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endTime)) {
            parameter.put("endTime", endTime + " 23:59:59");
        }
        if (StringUtils.isNotBlank(objectIds)) {
            String[] objectId = objectIds.split(",");
            parameter.put("objectIds", objectId);
        }
        parameter.put("organId", organId);
        parameter.put("onlyCompany", onlyCompany);
        parameter.put("postCode", postCode);
        parameter.put(BaseInterceptor.PAGE, pg);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        List<Map<String, Object>> mapList = dao.getModuleStatistics(parameter);
        pg.setResult(mapList);
        return pg;
    }

    /**
     * 每天访问数据
     *
     * @return
     */
    public List<Map<String, Object>> getDayLoginStatistics(String startTime, String endTime) {
        Parameter parameter = new Parameter();
        if (StringUtils.isNotBlank(startTime)) {
            startTime += " 00:00:00";
            parameter.put("startTime", startTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            endTime += " 23:59:59";
            parameter.put("endTime", endTime);
        }
//        parameter.put(BaseInterceptor.PAGE,pg);
        parameter.put(BaseInterceptor.DB_NAME, AppConstants.getJdbcType());
        List<Map<String, Object>> list = dao.getDayLoginStatistics(parameter);
        return list;
    }
}