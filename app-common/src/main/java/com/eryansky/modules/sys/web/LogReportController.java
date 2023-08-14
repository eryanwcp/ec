package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.DateUtils;
import com.eryansky.common.utils.PrettyMemoryUtils;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.excels.CsvUtils;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.core.excels.ExcelUtils;
import com.eryansky.core.excels.JsGridReportBase;
import com.eryansky.core.excels.TableData;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.sys.mapper.Organ;
import com.eryansky.modules.sys.service.LogService;
import com.eryansky.modules.sys.utils.OrganUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppDateUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志分析报表
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/log/report")
public class LogReportController extends SimpleController {

    @Autowired
    private LogService logService;

    /**
     * 登录统计
     *
     * @return
     */
    @Logging(value = "日志统计-登录统计", logType = LogType.access)
    @RequiresPermissions(value = "sys:log:loginStatistics")
    @GetMapping(value = {"loginStatistics"})
    public String loginStatistics() {
        return "modules/sys/log-loginStatistics";
    }

    @RequiresPermissions(value = "sys:log:loginStatistics")
    @PostMapping(value = {"loginStatisticsData"})
    @ResponseBody
    public Datagrid<Map<String, Object>> datagrid(String name, String startTime, String endTime, HttpServletRequest request) {
        Page<Map<String, Object>> page = new Page<>(request);
        page = logService.getLoginStatistics(page, name, startTime, endTime);
        Datagrid<Map<String, Object>> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        List<Map<String, Object>> footer = Lists.newArrayList();
        long totalSize = page.getResult().parallelStream().mapToLong(r-> (Long) r.get("count")).sum();
        Map<String, Object> map = Maps.newHashMap();
        map.put("name", "总计");
        map.put("count", totalSize);
        footer.add(map);
        dg.setFooter(footer);
        return dg;
    }

    @RequiresPermissions(value = "sys:log:loginStatistics")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"loginStatisticsExportExcel"})
    @ResponseBody
    public void loginStatisticsExportExcel(String name, String startTime, String endTime, HttpServletResponse response, HttpServletRequest request) throws Exception {
        Page<Map<String, Object>> page = new Page<>(1, -1);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();  //获取当前用户名
        String fileName = "登录次数统计";
        Page<Map<String, Object>> pageMap = logService.getLoginStatistics(page.pageSize(-1), name, startTime, endTime);
        List<Map<String, Object>> result = pageMap.getResult();
        String[] hearders = {"单位/部门", "部门", "姓名", "用户ID", "账号", "登录次数"};//表头数组
        String[] fields = new String[]{"company", "department", "name", "userId", "userName", "count"};//People对象属性数组

        if (page.getResult().size() < 65531) {
            //导出Excel
            try {
                TableData td = ExcelUtils.createTableData(result, ExcelUtils.createTableHeader(hearders, 0), fields);
                JsGridReportBase report = new JsGridReportBase(request, response);
                report.exportToExcel(fileName, sessionInfo.getName(), td);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            //导出CSV
            try {
                List<Object[]> data = Lists.newArrayList();
                page.getResult().forEach(o -> data.add(new Object[]{o.get(fields[0]), o.get(fields[1]), o.get(fields[2]), o.get(fields[3]), o.get(fields[4]), o.get(fields[5])}));
                CsvUtils.exportToCsv(fileName, hearders, data, request, response);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 每日登陆次数分析
     *
     * @return
     */
    @Logging(value = "日志统计-每日登陆次数分析", logType = LogType.access)
    @RequiresPermissions(value = "sys:log:dayLoginStatistics")
    @GetMapping(value = {"dayLoginStatistics"})
    public String dayLoginStatistics() {
        return "modules/sys/log-dayLoginStatistics";
    }

    /**
     * 每日登陆分析数据
     *
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    @RequiresPermissions(value = "sys:log:dayLoginStatistics")
    @PostMapping(value = {"dayLoginStatisticsData"})
    @ResponseBody
    public Result dayLoginStatisticsData(String startTime, String endTime) throws Exception {
        String _startTime = StringUtils.isBlank(startTime) ? DateUtils.formatDate(AppDateUtils.getCurrentYearStartTime()):startTime;
        List<Map<String, Object>> list = logService.getDayLoginStatistics(_startTime, endTime);
        Date date = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> map : list) {
            date = simpleDateFormat.parse(map.get("loginDate").toString());
            long timeStemp = date.getTime();
            map.put("loginDate", timeStemp);
        }
        return Result.successResult().setObj(list);
    }

    /**
     * 模块访问统计
     *
     * @param userId
     * @param organId
     * @param startTime
     * @param endTime
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @Logging(value = "日志统计-模块访问统计", logType = LogType.access)
    @RequiresPermissions(value = "sys:log:moduleStatistics")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"moduleStatistics"})
    public String moduleStatistics(String userId, String organId, String postCode, @RequestParam(defaultValue = "false") Boolean onlyCompany, String startTime, String endTime,
                                   HttpServletRequest request, HttpServletResponse response, Model model) throws Exception {
        Page<Map<String, Object>> page = new Page<>(request, response);
        HashMap<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("userId", userId);
        paramMap.put("organId", organId);
        paramMap.put("userName", UserUtils.getUserName(userId));
        paramMap.put("organName", OrganUtils.getOrganName(organId));
        paramMap.put("startTime", startTime);
        paramMap.put("endTime", endTime);
        paramMap.put("onlyCompany", onlyCompany);
        paramMap.put("postCode", postCode);
        if (StringUtils.isNotBlank(userId) || StringUtils.isNotBlank(organId)) {
            page = logService.getModuleStatistics(page, userId, organId, onlyCompany, startTime, endTime, postCode);
        }

        model.addAttribute("page", page);
        model.addAttribute("paramMap", paramMap);
        return "modules/sys/log-moduleStatistics";
    }

    @RequiresPermissions(value = "sys:log:moduleStatistics")
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"moduleStatisticsExportExcel"})
    public void moduleStatisticsExportExcel(String userId, String organId, String postCode, @RequestParam(defaultValue = "false") Boolean onlyCompany, String startTime, String endTime,
                                            HttpServletResponse response, HttpServletRequest request) throws Exception {
        Page<Map<String, Object>> page = new Page<Map<String, Object>>(request, response);
        page.setPageSize(Page.PAGESIZE_ALL);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();  //获取当前用户名
        String fileName = "模块访问统计";
        Organ organ = OrganUtils.getOrgan(organId);
        if (organ != null) {
            fileName += "-" + organ.getName();
        }
        if (onlyCompany) {
            fileName += "（本级）";
        }
        page = logService.getModuleStatistics(page.pageSize(Page.PAGESIZE_ALL), userId, organId, onlyCompany, startTime, endTime, postCode);
        List<Map<String, Object>> result = page.getResult();
        String[] hearders = new String[]{"模块", "访问次数"};//表头数组
        String[] fields = new String[]{"module", "moduleCount"};//People对象属性数组

        if (page.getResult().size() < 65531) {
            //导出Excel
            try {
                TableData td = ExcelUtils.createTableData(result, ExcelUtils.createTableHeader(hearders, 0), fields);
                JsGridReportBase report = new JsGridReportBase(request, response);
                report.exportToExcel(fileName, sessionInfo.getName(), td);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            //导出CSV
            try {
                List<Object[]> data = Lists.newArrayList();
                page.getResult().forEach(o -> {
                    data.add(new Object[]{o.get(fields[0]), o.get(fields[1])});
                });
                CsvUtils.exportToCsv(fileName, hearders, data, request, response);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
