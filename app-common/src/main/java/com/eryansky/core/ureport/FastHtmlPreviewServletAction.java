/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.eryansky.core.ureport;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.util.StrUtil;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.build.ReportBuilder;
import com.bstek.ureport.build.paging.Page;
import com.bstek.ureport.cache.CacheUtils;
import com.bstek.ureport.chart.ChartData;
import com.bstek.ureport.console.MobileUtils;
import com.bstek.ureport.console.RenderPageServletAction;
import com.bstek.ureport.console.cache.TempObjectCache;
import com.bstek.ureport.console.exception.ReportDesignException;
import com.bstek.ureport.console.html.Tools;
import com.bstek.ureport.definition.Paper;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.definition.searchform.FormPosition;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.export.*;
import com.bstek.ureport.export.html.HtmlProducer;
import com.bstek.ureport.export.html.HtmlReport;
import com.bstek.ureport.export.html.SearchFormData;
import com.bstek.ureport.model.Report;
import com.eryansky.common.exception.ServiceException;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * 修改支持多租户
 * @author Jacky.gao
 * @since 2017年2月15日
 * @author ZhouZhou
 * @date 2021-09-15
 */
public class FastHtmlPreviewServletAction extends RenderPageServletAction {

    private static final Logger log = LoggerFactory.getLogger(FastHtmlPreviewServletAction.class);

    private ExportManager exportManager;
    private ReportBuilder reportBuilder;
    private ReportRender reportRender;
    private HtmlProducer htmlProducer=new HtmlProducer();

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method=retriveMethod(req);
        if(method!=null){
            invokeMethod(method, req, resp);
        }else{
            VelocityContext context = new VelocityContext();
            HtmlReport htmlReport=null;
            String errorMsg=null;
            try{
                htmlReport=loadReport(req);
            }catch (ServiceException e){
                errorMsg = "错误代码:"+e.getCode() +" 错误信息:" + e.getMessage();
            }catch(Exception ex){
                log.error("报表计算出错，错误信息如下：",ex);
                errorMsg ="报表内部异常,请查看后台日志.";
            }
            String title = "异常请求";
            try{
                title = buildTitle(req);
            }catch (Exception e){
            }
            context.put("title", title);
            if(htmlReport==null){
                context.put("content", "<div style='width: 550px;margin: 7% auto;margin-top: 0;padding-top: 20%;'><div style='color:red;font-size:25px;text-align: center;'><strong>"+errorMsg+"</strong></div></div>");
                context.put("error", true);
                context.put("searchFormJs", "");
                context.put("downSearchFormHtml", "");
                context.put("upSearchFormHtml", "");
            }else{
                String file=req.getParameter("_u");
                if(file.indexOf("fast-") == -1 && !file.equals(PREVIEW_KEY)){
                    file = "fast-" + file + ".xml";
                }
                SearchFormData formData=htmlReport.getSearchFormData();
                if(formData!=null){
                    context.put("searchFormJs", formData.getJs());
                    if(formData.getFormPosition().equals(FormPosition.up)){
                        context.put("upSearchFormHtml", formData.getHtml());
                        context.put("downSearchFormHtml", "");
                    }else{
                        context.put("downSearchFormHtml", formData.getHtml());
                        context.put("upSearchFormHtml", "");
                    }
                }else{
                    context.put("searchFormJs", "");
                    context.put("downSearchFormHtml", "");
                    context.put("upSearchFormHtml", "");
                }
                context.put("content", htmlReport.getContent());
                context.put("style", htmlReport.getStyle());
                if(Boolean.parseBoolean(AppConstants.getAppConfig("ec.csrf.enabled"))){
                    context.put("FAST_LOGIN_CSRF_TOKEN", Base64Encoder.encode(SecurityUtils.getCurrentSessionInfo().getToken()));
                }else {
                    context.put("FAST_LOGIN_CSRF_TOKEN", "");
                }
                context.put("reportAlign", htmlReport.getReportAlign());
                context.put("totalPage", htmlReport.getTotalPage());
                context.put("totalPageWithCol", htmlReport.getTotalPageWithCol());
                context.put("pageIndex", htmlReport.getPageIndex());
                context.put("chartDatas", convertJson(htmlReport.getChartDatas()));
                context.put("echartDatas", convertJson(htmlReport.getEchartDatas()));
                context.put("error", false);
                context.put("pageAll", htmlReport.isPage());
                context.put("file", file);
                context.put("__f", StrUtil.nullToDefault(req.getParameter("__f"),""));
                context.put("intervalRefreshValue",htmlReport.getHtmlIntervalRefreshValue());
                String customParameters=buildCustomParameters(req);
                context.put("customParameters", customParameters);
                context.put("_t", "");
                Tools tools=null;

                if(MobileUtils.isMobile(req)){
                    tools=new Tools(false);
                    tools.setShow(false);
                }else{
                    file=decode(file);
                    if(file.equals(PREVIEW_KEY)){
                        tools=new Tools(true);
                        file = StrUtil.nullToDefault(req.getParameter("__f"),file);
                    }else{
                        tools=new Tools(false);
                        tools.doInit("1");
                        tools.doInit("9");
                        if(UreportUtils.isFunc(file,"dcexcel")) {
                            tools.doInit("6");
                            tools.doInit("7");
                            tools.doInit("8");
                        }
                        if(UreportUtils.isFunc(file,"dcword")) {
                            tools.doInit("5");
                        }
                        if(UreportUtils.isFunc(file,"dcpdf")) {
                            tools.doInit("4");
                        }
                        if(UreportUtils.isFunc(file,"ylpdf")) {
                            tools.doInit("2");
                            tools.doInit("3");
                        }
                    }
                    context.put("watermark", StrUtil.nullToDefault(htmlReport.getWatermark(),""));
                }
                context.put("tools", tools);
            }
            context.put("contextPath", req.getContextPath());
            resp.setContentType("text/html");
            resp.setCharacterEncoding("utf-8");
            VelocityEngine veTemp = new VelocityEngine();
            veTemp.setProperty(Velocity.RESOURCE_LOADERS, "class");
            veTemp.setProperty("resource.loader.class.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            veTemp.init();
            Template template= veTemp.getTemplate("ureport/html/preview.html","utf-8");
            PrintWriter writer=resp.getWriter();
            template.merge(context, writer);
            writer.close();
        }
    }

    private String buildTitle(HttpServletRequest req){
        String title=req.getParameter("_title");
        if(StringUtils.isBlank(title)){
            title=req.getParameter("_u");
            title=decode(title);
            int point=title.lastIndexOf(".ureport.xml");
            if(point>-1){
                title=title.substring(0,point);
            }
            if(title.equals("p")){
                title="设计中报表";
            }
        }else{
            title=decode(title);
        }
        return title+"-ureport";
    }

    private String convertJson(Collection<ChartData> data){
        if(data==null || data.size()==0){
            return "";
        }
        ObjectMapper mapper=new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(data);
            return json;
        } catch (Exception e) {
            throw new ReportComputeException(e);
        }
    }

    public void loadData(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HtmlReport htmlReport=loadReport(req);
        writeObjectToJson(resp, htmlReport);
    }

    /**
     * 在线打印
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    public void loadPrintPages(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long start=System.currentTimeMillis();
        String file=req.getParameter("_u");
        String fpage = req.getParameter("_i");
        String f = req.getParameter("__f");
        // 是否打印所有  0 当前  1所有
        String all = req.getParameter("__a");
        file=decode(file);
        if(StringUtils.isBlank(file)){
            throw new ReportComputeException("Report file can not be null.");
        }



        Map<String, Object> parameters = buildParameters(req);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        // J2eeFAST 整合系统 支持多租户模式
        parameters.put("tenantId",sessionInfo.getLoginCompanyId());
        //操作用户
        parameters.put("__userName",sessionInfo.getName());

        ReportDefinition reportDefinition=null;



        String _file= file;
        if(file.equals(PREVIEW_KEY)){
            _file=req.getParameter("__f");
            _file=decode(_file);
        }

        if(file.indexOf("fast-") == -1 && !file.equals(PREVIEW_KEY)){
            file = "fast-" + file + ".xml";
            _file = "fast-" + _file + ".xml";
        }

        //已经设计完成
        if(!UreportUtils.isPermissions(_file)){
            throw new ServiceException("无权限访问!");
        }

        //数据权限
        parameters.put("sqlFilter",UreportUtils.getSQLFilter(_file));

        //分页 当前页
        if(StringUtils.isNotEmpty(fpage) &&
                StringUtils.isNotEmpty(all)
                && all.equals("0")){
//            Map<String, Object> mapLsit = UreportUtils.isLimit(_file);
//            if((Boolean) mapLsit.get("isLimit")){
//                //当前页
//                parameters.put("__page", Integer.parseInt(fpage));
//                parameters.put("__paging", true);
//                // 分页大小
//                parameters.put("__pageSize", mapLsit.get("pageSize"));
//                // 需要分页的对象
//                parameters.put("__objList", mapLsit.get("ObjList"));
//            }
            parameters.put("__page", Integer.parseInt(fpage));

            if(file.equals(PREVIEW_KEY)){
                reportDefinition=(ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
                if(reportDefinition==null){
                    throw new ReportDesignException("Report data has expired,can not do export excel.");
                }
            }else{
                reportDefinition=reportRender.getReportDefinition(file);
            }


            Report report=reportBuilder.buildReport(reportDefinition, parameters);
            Map<String, ChartData> chartMap=report.getContext().getChartDataMap();
            if(chartMap.size()>0){
                CacheUtils.storeChartDataMap(chartMap);
            }
            FullPageData pageData= PageBuilder.buildFullPageData(report);
            StringBuilder sb=new StringBuilder();
            List<List<Page>> list=pageData.getPageList();
            Context context=report.getContext();
            if(list.size()>0){
                for(int i=0;i<list.size();i++){
                    List<Page> columnPages=list.get(i);
                    if(i==0){
                        String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                        sb.append(html);
                    }else{
                        String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                        sb.append(html);
                    }
                }
            }else{
                List<Page> pages=report.getPages();
                for(int i=0;i<pages.size();i++){
                    Page page=pages.get(i);
                    if(i==0){
                        String html=htmlProducer.produce(context,page, false);
                        sb.append(html);
                    }else{
                        String html=htmlProducer.produce(context,page, true);
                        sb.append(html);
                        break;
                    }
                }
            }
            Map<String,String> map=new HashMap<String,String>();
            map.put("html", sb.toString());
            map.put("echarts",convertJson(report.getContext().getEchartDataMap().values()));
            map.put("watermark", reportDefinition.getPaper().getWatermark());
            writeObjectToJson(resp, map);
        }else{

            if(StringUtils.isNotEmpty(fpage) &&
                    StringUtils.isNotEmpty(all)
                    && all.equals("1")){
//                Map<String, Object> mapLsit = UreportUtils.isLimit(_file);
//
//                if((Boolean) mapLsit.get("isLimit")){
//                    //当前页
//                    parameters.put("__page", 1);
//                    parameters.put("__paging", true);
//                    // 分页大小
//                    parameters.put("__pageSize", mapLsit.get("pageSize"));
//                    // 需要分页的对象
//                    parameters.put("__objList", mapLsit.get("ObjList"));
//                }
                //当前页
                parameters.put("__page", 1);
                if(file.equals(PREVIEW_KEY)){
                    reportDefinition=(ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
                    if(reportDefinition==null){
                        throw new ReportDesignException("Report data has expired,can not do export excel.");
                    }
                }else{
                    reportDefinition=reportRender.getReportDefinition(file);
                }
                Report report=reportBuilder.buildReport(reportDefinition, parameters);
                Map<String, ChartData> chartMap=report.getContext().getChartDataMap();
                if(chartMap.size()>0){
                    CacheUtils.storeChartDataMap(chartMap);
                }
                FullPageData pageData= PageBuilder.buildFullPageData(report);
                StringBuilder sb=new StringBuilder();
                List<List<Page>> list=pageData.getPageList();
                Context context=report.getContext();
                if(list.size()>0){
                    for(int i=0;i<list.size();i++){
                        List<Page> columnPages=list.get(i);
                        if(i==0){
                            String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                            sb.append(html);
                        }else{
                            String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                            sb.append(html);
                        }
                    }
                }else{
                    List<Page> pages=report.getPages();
                    for(int i=0;i<pages.size();i++){
                        Page page=pages.get(i);
                        String html=htmlProducer.produce(context,page, false);
                        sb.append(html);
                        break;
                    }
                }

                long __totalPage = (long) parameters.get("__totalPage");

                for(int k=2; k<=__totalPage;k++){
                    //当前页
                    parameters.put("__page", k);
                    if(file.equals(PREVIEW_KEY)){
                        reportDefinition=(ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
                        if(reportDefinition==null){
                            throw new ReportDesignException("Report data has expired,can not do export excel.");
                        }
                    }else{
                        reportDefinition=reportRender.getReportDefinition(file);
                    }
                    report=reportBuilder.buildReport(reportDefinition, parameters);
                    chartMap=report.getContext().getChartDataMap();
                    if(chartMap.size()>0){
                        CacheUtils.storeChartDataMap(chartMap);
                    }
                    pageData= PageBuilder.buildFullPageData(report);
                    list=pageData.getPageList();
                    context=report.getContext();
                    if(list.size()>0){
                        for(int i=0;i<list.size();i++){
                            List<Page> columnPages=list.get(i);
                            if(i==0){
                                String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                                sb.append(html);
                            }else{
                                String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                                sb.append(html);
                            }
                        }
                    }else{
                        List<Page> pages=report.getPages();
                        for(int i=0;i<pages.size();i++){
                            Page page=pages.get(i);
                            if(i==0){
                                String html=htmlProducer.produce(context,page, true);
                                sb.append(html);
                            }else{
                                String html=htmlProducer.produce(context,page, true);
                                sb.append(html);
                                break;
                            }
                        }
                    }
                }

                Map<String,String> map=new HashMap<String,String>();
                map.put("html", sb.toString());
                map.put("echarts",convertJson(report.getContext().getEchartDataMap().values()));
                map.put("watermark", reportDefinition.getPaper().getWatermark());
                writeObjectToJson(resp, map);
            }else{
                if(file.equals(PREVIEW_KEY)){
                    reportDefinition=(ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
                    if(reportDefinition==null){
                        throw new ReportDesignException("Report data has expired,can not do export excel.");
                    }
                }else{
                    reportDefinition=reportRender.getReportDefinition(file);
                }
                Report report=reportBuilder.buildReport(reportDefinition, parameters);
                Map<String, ChartData> chartMap=report.getContext().getChartDataMap();
                if(chartMap.size()>0){
                    CacheUtils.storeChartDataMap(chartMap);
                }
                FullPageData pageData= PageBuilder.buildFullPageData(report);
                StringBuilder sb=new StringBuilder();
                List<List<Page>> list=pageData.getPageList();
                Context context=report.getContext();
                if(list.size()>0){
                    for(int i=0;i<list.size();i++){
                        List<Page> columnPages=list.get(i);
                        if(i==0){
                            String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                            sb.append(html);
                        }else{
                            String html=htmlProducer.produce(context,columnPages,pageData.getColumnMargin(),false);
                            sb.append(html);
                        }
                    }
                }else{
                    List<Page> pages=report.getPages();
                    for(int i=0;i<pages.size();i++){
                        Page page=pages.get(i);
                        if(i==0){
                            String html=htmlProducer.produce(context,page, false);
                            sb.append(html);
                        }else{
                            String html=htmlProducer.produce(context,page, true);
                            sb.append(html);
                            break;
                        }
                    }
                }
                Map<String,String> map=new HashMap<String,String>();
                map.put("html", sb.toString());
                System.out.println(sb.toString());
                map.put("echarts",convertJson(report.getContext().getEchartDataMap().values()));
                map.put("watermark", reportDefinition.getPaper().getWatermark());
                writeObjectToJson(resp, map);
            }
        }
        long end=System.currentTimeMillis();
        String msg="~~~ 打印传输数据总耗时:"+(end-start)+"ms";
        log.info(msg);
    }

    public void loadPagePaper(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String file=req.getParameter("_u");
        if(file.indexOf("fast-") == -1 && !file.equals(PREVIEW_KEY)){
            file = "fast-" + file + ".xml";
        }
        file=decode(file);
        if(StringUtils.isBlank(file)){
            throw new ReportComputeException("Report file can not be null.");
        }
        ReportDefinition report=null;
        if(file.equals(PREVIEW_KEY)){
            report=(ReportDefinition)TempObjectCache.getObject(PREVIEW_KEY);
            if(report==null){
                throw new ReportDesignException("Report data has expired.");
            }
        }else{
            report=reportRender.getReportDefinition(file);
        }
        Paper paper=report.getPaper();
        writeObjectToJson(resp, paper);
    }

    private HtmlReport loadReport(HttpServletRequest req) {
        Map<String, Object> parameters = buildParameters(req);
        String pageIndex=req.getParameter("_i");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        // J2eeFAST 整合系统 支持多租户模式
        parameters.put("tenantId",sessionInfo.getLoginHomeCompanyId());

        //操作用户
        parameters.put("__userName",sessionInfo.getName());

        HtmlReport htmlReport=null;
        String file=req.getParameter("_u");
        file=decode(file);
        if(StringUtils.isBlank(file)){
            //throw new ReportComputeException("Report file can not be null.");
            throw new ServiceException("链接有误,请确认访问链接!");
        }
        String _file= file;

        if(file.equals(PREVIEW_KEY)){
            _file=req.getParameter("__f");
            _file=decode(_file);
        }

        if(file.indexOf("fast-") == -1 && !file.equals(PREVIEW_KEY)){
            file = "fast-" + file + ".xml";
            _file = "fast-" + _file + ".xml";
        }

        //分页 当前页
        if(StringUtils.isNotEmpty(pageIndex)){
            //当前页
            parameters.put("__page", Integer.parseInt(pageIndex));
        }

        if(!UreportUtils.isFunc(_file,"preview")){
            throw new ServiceException("未开放此功能!");
        }

        //已经设计完成
        if(!UreportUtils.isPermissions(_file)){
            throw new ServiceException("无权限访问!");
        }

        //数据权限
        parameters.put("sqlFilter",UreportUtils.getSQLFilter(_file));


        //在线设计查看 -- 使用会话内存数据-- 不做数据权限拦截
        if(file.equals(PREVIEW_KEY)){

            ReportDefinition reportDefinition=(ReportDefinition)TempObjectCache.getObject(PREVIEW_KEY);
            if(reportDefinition==null){
                throw new ReportDesignException("Report data has expired,can not do preview.");
            }
            Report report = reportBuilder.buildReport(reportDefinition, parameters);
            Map<String, ChartData> chartMap=report.getContext().getChartDataMap();
            if(chartMap.size()>0){
                CacheUtils.storeChartDataMap(chartMap);
            }
            htmlReport=new HtmlReport();
            String html=null;
            if(StringUtils.isNotBlank(pageIndex) && !pageIndex.equals("0")){
                Context context=report.getContext();
                int index=Integer.valueOf(pageIndex);
                SinglePageData pageData=PageBuilder.buildSinglePageData(index, report);
                List<Page> pages=pageData.getPages();
                if(pages.size()==1){
                    Page page=pages.get(0);
                    html=htmlProducer.produce(context,page,false);
                }else{
                    html=htmlProducer.produce(context,pages,pageData.getColumnMargin(),false);
                }
                htmlReport.setTotalPage(pageData.getTotalPages());
                htmlReport.setPageIndex(index);
            }else{
                html=htmlProducer.produce(report);
            }
            if(report.getPaper().isColumnEnabled()){
                htmlReport.setColumn(report.getPaper().getColumnCount());
            }
            htmlReport.setWatermark(reportDefinition.getPaper().getWatermark());
            htmlReport.setChartDatas(report.getContext().getChartDataMap().values());
            htmlReport.setEchartDatas(report.getContext().getEchartDataMap().values());
            htmlReport.setContent(html);
            htmlReport.setTotalPage(report.getPages().size());
            htmlReport.setStyle(reportDefinition.getStyle());
            htmlReport.setSearchFormData(reportDefinition.buildSearchFormData(report.getContext().getDatasetMap(),parameters));
            htmlReport.setReportAlign(report.getPaper().getHtmlReportAlign().name());
            htmlReport.setHtmlIntervalRefreshValue(report.getPaper().getHtmlIntervalRefreshValue());
        }else{

            TempObjectCache.removeObject(PREVIEW_KEY);
            if(StringUtils.isNotBlank(pageIndex) && !pageIndex.equals("0")){
                int index=Integer.valueOf(pageIndex);
                htmlReport=exportManager.exportHtml(file,req.getContextPath(),parameters,index);
            }else{
                htmlReport=exportManager.exportHtml(file,req.getContextPath(),parameters);
            }
        }
        return htmlReport;
    }


    private String buildCustomParameters(HttpServletRequest req){
        StringBuilder sb=new StringBuilder();
        Enumeration<?> enumeration=req.getParameterNames();
        while(enumeration.hasMoreElements()){
            Object obj=enumeration.nextElement();
            if(obj==null){
                continue;
            }
            String name=obj.toString();
            String value=req.getParameter(name);
            if(name==null || value==null || (name.startsWith("_") && !name.equals("_n"))){
                continue;
            }
            if(sb.length()>0){
                sb.append("&");
            }
            sb.append(name);
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }

    private String buildExceptionMessage(Throwable throwable){
        Throwable root=buildRootException(throwable);
        StringWriter sw=new StringWriter();
        PrintWriter pw=new PrintWriter(sw);
        root.printStackTrace(pw);
        String trace=sw.getBuffer().toString();
        trace=trace.replaceAll("\n", "<br>");
        pw.close();
        return trace;
    }

    public void setExportManager(ExportManager exportManager) {
        this.exportManager = exportManager;
    }

    public void setReportBuilder(ReportBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
    }
    public void setReportRender(ReportRender reportRender) {
        this.reportRender = reportRender;
    }

    @Override
    public String url() {
        return "/preview";
    }
}
