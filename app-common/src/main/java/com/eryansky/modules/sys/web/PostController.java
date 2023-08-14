/**
 * Copyright (c) 2012-2022 https://www.eryansky.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.eryansky.modules.sys.web;

import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.excels.CsvUtils;
import com.eryansky.core.excels.ExcelUtils;
import com.eryansky.core.excels.JsGridReportBase;
import com.eryansky.core.excels.TableData;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security._enum.Logical;
import com.eryansky.core.security.annotation.RequiresPermissions;
import com.eryansky.modules.sys._enum.DataScope;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.mapper.Post;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.*;
import com.eryansky.modules.sys.utils.PostUtils;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 岗位管理 Controller
 *
 * @author Eryan
 * @date : 2014-06-09 14:07
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/post")
public class PostController extends SimpleController {

    @Autowired
    private PostService postService;
    @Autowired
    private OrganService organService;
    @Autowired
    private UserService userService;

    @ModelAttribute("model")
    public Post get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return postService.get(id);
        } else {
            return new Post();
        }
    }

    @RequiresPermissions("sys:post:view")
    @Logging(value = "岗位管理", logType = LogType.access)
    @GetMapping(value = {""})
    public String list() {
        return "modules/sys/post";
    }

    @PostMapping(value = {"datagrid"})
    @ResponseBody
    public String datagrid(String organId, String query, HttpServletRequest request) {
        Page<Post> page = new Page<>(request);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Post model = new Post();
        model.setOrganId(organId);
        model.setQuery(query);
        if (StringUtils.isBlank(model.getOrganId())) {
            model.setOrganId(sessionInfo.getLoginOrganId());
        }

        page = postService.findPage(page, model);
        Datagrid<Post> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        return JsonMapper.getInstance().toJson(dg);
    }

    /**
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping(value = {"input"})
    public ModelAndView input(@ModelAttribute("model") Post model) throws Exception {
        ModelAndView modelAndView = new ModelAndView("modules/sys/post-input");
        modelAndView.addObject("model", model);
        List<String> organIds = organService.findAssociationOrganIdsByPostId(model.getId());
        modelAndView.addObject("organIds", organIds);
        return modelAndView;
    }

    @RequiresPermissions("sys:post:edit")
    @Logging(value = "岗位管理-保存岗位", logType = LogType.access)
    @PostMapping(value = {"save"}, produces = {MediaType.TEXT_HTML_VALUE})
    @ResponseBody
    public Result save(@ModelAttribute("model") Post model, String organId) {
        Result result;
        Validate.notNull(organId, "参数[organId]不能为null");
        // 编码重复校验
        if (StringUtils.isNotBlank(model.getCode())) {
            Post checkPost = postService.getPostByOC(organId, model.getCode());
            if (checkPost != null && !checkPost.getId().equals(model.getId())) {
                result = new Result(Result.WARN, "编码为[" + model.getCode() + "]已存在,请修正!", "code");
                logger.debug("{}",result);
                return result;
            }
        }

        postService.savePost(model);
        userService.clearCache();
        result = Result.successResult();
        return result;
    }

    /**
     * 根据ID集合批量删除.
     *
     * @param ids 主键ID集合
     * @return
     */
    @RequiresPermissions("sys:post:edit")
    @Logging(value = "岗位管理-删除岗位", logType = LogType.access)
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        postService.deleteByIds(ids);
        userService.clearCache();
        return Result.successResult();
    }

    /**
     * @param model 岗位
     * @return
     */
    @GetMapping(value = {"select"})
    public ModelAndView selectPage(@ModelAttribute("model") Post model) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-select");
        List<User> users = null;
        String dataScope = DataScope.COMPANY_AND_CHILD.getValue();
        List<String> excludeUserIds =  userService.findUserIdsByPost(model.getId(),null);
        modelAndView.addObject("users", users);
        modelAndView.addObject("excludeUserIds", excludeUserIds);
        if (Collections3.isNotEmpty(excludeUserIds)) {
            modelAndView.addObject("excludeUserIdStrs", Collections3.convertToString(excludeUserIds, ","));
        }
        modelAndView.addObject("postId", model.getId());
        modelAndView.addObject("dataScope", dataScope);//不分级授权
        modelAndView.addObject("cascade", "true");//不分级授权
        modelAndView.addObject("multiple", "");
        modelAndView.addObject("userDatagridData", JsonMapper.getInstance().toJson(new Datagrid()));
        return modelAndView;
    }


    /**
     * 岗位用户数据
     *
     * @param postId 岗位ID
     * @param query 关键字 匹配用户
     * @param export 是否导出
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"userDatagrid"})
    public String userDatagrid(String postId,
                               String query,
                               @RequestParam(value = "export", defaultValue = "false") Boolean export,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        Page<User> page = new Page<>(SpringMVCHolder.getRequest());
        //分级授权
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        String parentOrganId = SecurityUtils.isPermittedMaxRoleDataScope() ? null:sessionInfo.getLoginCompanyId();
        if(export){
            page.setPageSize(Page.PAGESIZE_ALL);
        }

        page = userService.findPageByPost(page, postId, null,parentOrganId,query);

        if (export) {
            List<Object[]> data = Lists.newArrayList();
            page.getResult().forEach(u -> data.add(new Object[]{u.getName(), u.getLoginName(),u.getMobile(), u.getDefaultOrganName(), u.getCompanyName()}));

            String title = "岗位用户信息-" + PostUtils.getPostName(postId);
            //Sheet2
            String[] hearders = new String[]{"姓名", "账号", "手机号", "部门", "单位"};//表头数组

            if (page.getResult().size() < 65531) {
                //导出Excel
                try {
                    TableData td = ExcelUtils.createTableData(data, ExcelUtils.createTableHeader(hearders, 0), null);
                    td.setSheetTitle(title);
                    JsGridReportBase report = new JsGridReportBase(request, response);
                    report.exportToExcel(title, sessionInfo.getName(), td);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                //导出CSV
                try {
                    CsvUtils.exportToCsv(title, hearders, data, request, response);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return null;
        }
        Datagrid<User> dg = new Datagrid<>(page.getTotalCount(), page.getResult());
        return renderString(response, JsonMapper.getInstance().toJson(dg, User.class,
                new String[]{"id", "name", "loginName","mobile","mobileSensitive","sexView", "sort", "defaultOrganName","companyName"}), WebUtils.JSON_TYPE);
    }


    /**
     * 添加角色关联用户
     *
     * @param model   角色
     * @param userIds 用户ID
     * @return
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:post:edit","sys:user:post:edit","sys:post:user:edit"})
    @Logging(value = "岗位管理-添加关联用户", logType = LogType.access)
    @PostMapping(value = {"addPostUser"})
    @ResponseBody
    public Result addPostUser(@ModelAttribute("model") Post model,
                              @RequestParam(value = "userIds", required = true) Set<String> userIds) {
        userIds.forEach(v->{
            String organId = UserUtils.getDefaultOrganId(v);
            postService.insertPostUsers(model.getId(),organId, Lists.newArrayList(v));
        });
        userService.clearCache();
        return Result.successResult();
    }

    /**
     * 移除角色关联用户
     *
     * @param model   角色
     * @param userIds 用户IDS
     * @return
     */
    @RequiresPermissions(logical = Logical.OR,value = {"sys:post:edit","sys:user:post:edit","sys:post:user:edit"})
    @Logging(value = "岗位管理-移除关联用户", logType = LogType.access)
    @PostMapping(value = {"removePostUser"})
    @ResponseBody
    public Result removePostUser(@ModelAttribute("model") Post model,
                                 @RequestParam(value = "userIds", required = true) Set<String> userIds) {
        userIds.forEach(v->{
            postService.deletePostUsersByPostIdAndUserId(model.getId(), v);
        });
        userService.clearCache();
        return Result.successResult();
    }


    /**
     * 设置岗位用户页面.
     */
    @GetMapping(value = {"user"})
    public String user(@ModelAttribute("model") Post model, Model uiModel) throws Exception {
        uiModel.addAttribute("organId", model.getOrganId());
        List<String> userIds = userService.findUserIdsByPostIdAndOrganId(model.getId(), model.getOrganId());
        uiModel.addAttribute("userIds", userIds);
        uiModel.addAttribute("model", model);
        return "modules/sys/post-user";
    }


    /**
     * 用户可选岗位列表 TODO
     *
     * @param selectType {@link SelectType}
     * @param userId     用户ID
     * @return
     */
    @PostMapping(value = {"userPostCombobox"})
    @ResponseBody
    public List<Combobox> userPostCombobox(String selectType, String userId, String organId) {
        List<String> userOrganIds = UserUtils.findOrganIdsByUserId(userId);
        String _organId = organId;
        if(Collections3.isNotEmpty(userOrganIds)){
            if(StringUtils.isBlank(_organId)){
                _organId = userOrganIds.get(0);
            }
            if(StringUtils.isNotBlank(_organId) && !userOrganIds.contains(_organId)){
                _organId = userOrganIds.get(0);
            }
        }

        List<Post> list = StringUtils.isNotBlank(_organId) ? postService.findPostsByOrganId(_organId) : postService.findPostsByUserId(userId, _organId);
        List<Combobox> cList = Lists.newArrayList();

        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }
        for (Post r : list) {
            Combobox combobox = new Combobox(r.getId(), r.getName());
            cList.add(combobox);
        }
        System.out.println(JsonMapper.toJsonString(cList));
        return cList;
    }

    /**
     * 详细信息
     *
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") Post model) {
        return Result.successResult().setObj(model);
    }

}
