package com.eryansky.modules.notice.web;

import com.eryansky.common.model.Combobox;
import com.eryansky.common.model.Datagrid;
import com.eryansky.common.model.Result;
import com.eryansky.common.model.TreeNode;
import com.eryansky.common.orm.Page;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.modules.notice._enum.ContactGroupType;
import com.eryansky.modules.notice.mapper.ContactGroup;
import com.eryansky.modules.notice.mapper.MailContact;
import com.eryansky.modules.notice.service.ContactGroupService;
import com.eryansky.modules.notice.service.MailContactService;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.utils.SelectType;
import com.google.common.collect.Lists;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 联系人组控制器
 *
 * @author Eryan
 * @date 2014-11-07
 */
@Controller
@RequestMapping(value = "${adminPath}/notice/contactGroup")
public class ContactGroupController extends SimpleController {

    @Resource
    private ContactGroupService contactGroupService;
    @Resource
    private MailContactService mailContactService;

    @ModelAttribute("model")
    public ContactGroup get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return contactGroupService.get(id);
        } else {
            return new ContactGroup();
        }
    }

    @Logging(logType = LogType.access, value = "联系人组管理")
    @GetMapping(value = {""})
    public String list() {
        return "modules/notice/contactGroup";
    }

    @GetMapping(value = {"input"})
    public String input(@ModelAttribute("model") ContactGroup model) {
        return "modules/notice/contactGroup-input";
    }

    /**
     * 邮件联系人编辑
     */
    @GetMapping(value = {"inputContactGroupMail"})
    public String inputContactGroupMail(@ModelAttribute("model") MailContact model) {
        return "modules/notice/contactGroupMail-input";
    }

    /**
     * 联系人组 保存
     * @param model
     * @return
     */
    @Logging(logType = LogType.operate, value = "联系人组管理-保存")
    @PostMapping(value = {"save"})
    @ResponseBody
    public Result save(@ModelAttribute("model") ContactGroup model) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();

        if (StringUtils.isNotBlank(model.getId())) {
            ContactGroup existGroup = contactGroupService.get(model.getId());
            if (existGroup == null || !Objects.equals(existGroup.getUserId(), sessionInfo.getUserId())) {
                return Result.errorResult().setMsg("无权修改该联系人组");
            }
        }

        if (contactGroupService.checkExist(sessionInfo.getUserId(), model.getContactGroupType(), model.getName(), model.getId()) == null) {
            model.setUserId(sessionInfo.getUserId());
            contactGroupService.save(model);
            return Result.successResult();
        } else {
            return Result.errorResult().setMsg("用户组[" + model.getName() + "]已存在");
        }
    }

    /**
     * 个人 联系人组树形菜单 查询用
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"groupTree"})
    @ResponseBody
    public List<TreeNode> groupTree(String contactGroupType) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<TreeNode> treeNodes = Lists.newArrayList();
        TreeNode rootNode = new TreeNode("", "全部群组");
        treeNodes.add(rootNode);

        List<ContactGroup> list = contactGroupService.findUserContactGroups(sessionInfo.getUserId(), contactGroupType);
        for (ContactGroup contactGroup : list) {
            TreeNode treeNode = new TreeNode(contactGroup.getId(), contactGroup.getName());
            if (contactGroupType == null) {
                treeNode.addAttribute("contactGroupType", contactGroup.getContactGroupType());
            }
            rootNode.addChild(treeNode);
        }
        return treeNodes;
    }

    /**
     * 个人 联系人组树形菜单
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = {"tree"})
    @ResponseBody
    public List<TreeNode> tree(String contactGroupType) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        List<ContactGroup> list = contactGroupService.findUserContactGroups(sessionInfo.getUserId(), contactGroupType);

        return list.stream()
                .map(contactGroup -> new TreeNode(contactGroup.getId(), contactGroup.getName()))
                .collect(Collectors.toList());
    }

    /**
     * 联系人组 删除
     * @param ids
     * @return
     */
    @Logging(logType = LogType.operate, value = "联系人组管理-删除")
    @PostMapping(value = {"remove"})
    @ResponseBody
    public Result remove(@RequestParam(value = "ids", required = false) List<String> ids) {
        if (Collections3.isNotEmpty(ids)) {
            SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
            ids.forEach(v -> {
                ContactGroup contactGroup = contactGroupService.get(v);
                // 防越权校验：仅删除属于当前用户的联系人组
                if (contactGroup != null && Objects.equals(contactGroup.getUserId(), sessionInfo.getUserId())) {
                    contactGroupService.delete(contactGroup);
                }
            });
        }
        return Result.successResult();
    }

    /**
     * 选择联系人页面
     *
     * @param contactGroupId 角色/组ID
     */
    @GetMapping(value = {"select"})
    public ModelAndView selectPage(String contactGroupId) {
        ModelAndView modelAndView = new ModelAndView("modules/sys/user-select");
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();

        ContactGroup contactGroup = contactGroupService.get(contactGroupId);
        // 校验归属权
        if (contactGroup == null || !Objects.equals(contactGroup.getUserId(), sessionInfo.getUserId())) {
            return modelAndView;
        }

        List<User> users = null;
        List<String> excludeUserIds = contactGroup.getObjectIds();
        modelAndView.addObject("users", users);
        modelAndView.addObject("excludeUserIds", excludeUserIds);
        if (Collections3.isNotEmpty(excludeUserIds)) {
            modelAndView.addObject("excludeUserIdStrs", Collections3.convertToString(excludeUserIds, ","));
        }
        modelAndView.addObject("dataScope", "2"); // 不分级授权
        modelAndView.addObject("cascade", "true"); // 不分级授权
        modelAndView.addObject("multiple", "");
        modelAndView.addObject("userDatagridData", JsonMapper.getInstance().toJson(new Datagrid<>()));
        return modelAndView;
    }

    /**
     * 添加联系人表单页面
     *
     * @param contactGroupId 联系人组ID
     */
    @GetMapping(value = {"contactGroupUser"})
    @ResponseBody
    public ModelAndView contactGroupUser(String contactGroupId) {
        return new ModelAndView("modules/notice/contactGroup-user");
    }

    /**
     * 添加联系人
     *
     * @param addObjectIds 新增联系人 ID集合
     */
    @Logging(logType = LogType.operate, value = "联系人组管理-添加联系人")
    @PostMapping(value = {"addContactGroupUser"})
    @ResponseBody
    public Result addContactGroupUser(@ModelAttribute("model") ContactGroup model,
                                      @RequestParam(value = "addObjectIds", required = false) List<String> addObjectIds) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        ContactGroup existGroup = contactGroupService.get(model.getId());

        // 防越权校验
        if (existGroup == null || !Objects.equals(existGroup.getUserId(), sessionInfo.getUserId())) {
            return Result.errorResult().setMsg("无权操作该联系人组");
        }

        contactGroupService.deleteContactGroupObjects(model.getId(), addObjectIds);
        contactGroupService.insertContactGroupObjects(model.getId(), addObjectIds);
        return Result.successResult();
    }

    /**
     * 移除联系人
     *
     * @param removeObjectIds 需要移除的联系人 ID集合
     */
    @Logging(logType = LogType.operate, value = "联系人组管理-移除联系人")
    @PostMapping(value = {"removeContactGroupUser"})
    @ResponseBody
    public Result removeContactGroupUser(@ModelAttribute("model") ContactGroup model,
                                         @RequestParam(value = "removeObjectIds", required = true) List<String> removeObjectIds) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        ContactGroup existGroup = contactGroupService.get(model.getId());

        // 防越权校验
        if (existGroup == null || !Objects.equals(existGroup.getUserId(), sessionInfo.getUserId())) {
            return Result.errorResult().setMsg("无权操作该联系人组");
        }

        contactGroupService.deleteContactGroupObjects(model.getId(), removeObjectIds);
        return Result.successResult();
    }

    /**
     * 联系人列表
     *
     * @param id    联系人组ID
     * @param query 用户登录名或姓名
     */
    @PostMapping(value = {"contactGroupUserDatagrid"})
    @ResponseBody
    public String contactGroupUserDatagrid(String id, String query) {
        if (StringUtils.isBlank(id)) {
            return JsonMapper.getInstance().toJson(new Datagrid<>(0, Collections.emptyList()));
        }

        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        ContactGroup contactGroup = contactGroupService.get(id);

        // 防越权校验
        if (contactGroup == null || !Objects.equals(contactGroup.getUserId(), sessionInfo.getUserId())) {
            return JsonMapper.getInstance().toJson(new Datagrid<>(0, Collections.emptyList()));
        }

        Page<User> page = new Page<>(SpringMVCHolder.getRequest());
        page = contactGroupService.findContactGroupUsers(page, id, query);
        Datagrid<User> dg = new Datagrid<>(page.getTotalCount(), page.getResult());

        return JsonMapper.getInstance().toJson(dg, User.class,
                new String[]{"id", "loginName", "name", "sexView", "defaultOrganName", "email", "mobile", "tel"});
    }

    /**
     * 选择联系人列表
     *
     * @param contactGroupId 联系人组ID
     * @param loginNameOrName 用户登录名或姓名
     */
    @PostMapping(value = {"selectContactGroupUserDatagrid"})
    @ResponseBody
    public String selectContactGroupUserDatagrid(@RequestParam(value = "contactGroupId") String contactGroupId, String loginNameOrName) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        ContactGroup contactGroup = contactGroupService.get(contactGroupId);

        // 防越权校验
        if (contactGroup == null || !Objects.equals(contactGroup.getUserId(), sessionInfo.getUserId())) {
            return JsonMapper.getInstance().toJson(Collections.emptyList());
        }

        List<User> users = contactGroupService.findContactGroupUsers(contactGroupId, loginNameOrName);
        return JsonMapper.getInstance().toJson(users, User.class,
                new String[]{"id", "loginName", "name", "sexView", "organNames"});
    }

    /**
     * 类型下拉列表
     */
    @PostMapping(value = {"contactGroupTypeCombobox"})
    @ResponseBody
    public List<Combobox> contactGroupTypeCombobox(String selectType) {
        List<Combobox> cList = Lists.newArrayList();
        Combobox titleCombobox = SelectType.combobox(selectType);
        if (titleCombobox != null) {
            cList.add(titleCombobox);
        }

        List<Combobox> typeList = Arrays.stream(ContactGroupType.values())
                .map(type -> {
                    Combobox combobox = new Combobox();
                    combobox.setValue(type.getValue());
                    combobox.setText(type.getDescription());
                    return combobox;
                })
                .collect(Collectors.toList());

        cList.addAll(typeList);
        return cList;
    }

    /**
     * 排序最大值
     */
    @PostMapping(value = {"maxSort"})
    @ResponseBody
    public Result maxSort() {
        Integer maxSort = contactGroupService.getMaxSort();
        return Result.successResult().setObj(maxSort);
    }
}