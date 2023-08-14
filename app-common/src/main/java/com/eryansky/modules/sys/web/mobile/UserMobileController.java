package com.eryansky.modules.sys.web.mobile;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.encode.Encrypt;
import com.eryansky.common.utils.encode.Encryption;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.UserPasswordUpdateType;
import com.eryansky.modules.sys._enum.UserType;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.fileupload.FileUploadBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 用户管理
 */
@Mobile
@Controller
@RequestMapping(value = "${mobilePath}/sys/user")
public class UserMobileController extends SimpleController {

    @Autowired
    private UserService userService;

    @ModelAttribute("model")
    public User get(@RequestParam(required = false) String id,String userId) {
        if (StringUtils.isNotBlank(id)) {
            return userService.get(id);
        } else if (StringUtils.isNotBlank(userId)) {
            return userService.get(userId);
        } else {
            return new User();
        }
    }

    /**
     * 设置初始密码或修改密码（仅限用户自己修改）
     * @param id
     * @param loginName
     * @param encrypt 是否加密 加密方法采用base64加密方案
     * @param type 修改密码类型 1：初始化密码 2：帐号与安全修改密码
     * @param password 原始密码
     * @param newPassword 新密码
     * @param token 安全Token
     * @return
     */
    @RequiresUser(required = false)
    @Logging(logType = LogType.access, value = "修改密码")
    @PostMapping(value = "savePs")
    @ResponseBody
    public Result savePs(@RequestParam(name = "id", required = false) String id,
                         @RequestParam(name = "ln", required = false) String loginName,
                         @RequestParam(defaultValue = "false") Boolean encrypt,
                         @RequestParam(name = "type", required = false) String type,
                         @RequestParam(name = "ps", required = false) String password,
                         @RequestParam(name = "newPs", required = true) String newPassword,
                         @RequestParam(name = "token", required = false) String token) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        User model = null;
        if (StringUtils.isNotBlank(token)) {
            String tokenLoginName = SecurityUtils.getLoginNameByToken(token);
            model = UserUtils.getUserByLoginName(tokenLoginName);
            //安全校验 仅允许自己修改
            if (null != model && !model.getId().equals(id)) {
                logger.warn("未授权修改账号密码：{} {} {}", model.getLoginName(), loginName, token);
                throw new ActionException("未授权修改账号密码！");
            }
        } else {
            if (null == sessionInfo) {
                throw new ActionException("非法请求！");
            }
            if (StringUtils.isBlank(id) && StringUtils.isBlank(loginName)) {
                return Result.warnResult().setMsg("无用户信息！");
            }
            model = StringUtils.isNotBlank(loginName) ? userService.getUserByLoginName(loginName) : userService.get(id);
            //安全校验 仅允许自己修改
            if (null != model && !model.getId().equals(sessionInfo.getUserId())) {
                logger.warn("未授权修改账号密码：{} {} {}", model.getLoginName(), model.getLoginName(), token);
                throw new ActionException("未授权修改账号密码！");
            }
        }

        if (null == model) {
            logger.error("{} {} {}",id,loginName,token);
            throw new ActionException("非法请求！");
        }

        if (StringUtils.isBlank(newPassword)) {
            return Result.warnResult().setMsg("新密码为空，请完善！");
        }

        String originalPassword = model.getPassword(); //数据库存储的原始密码
        String pagePassword = null;//页面输入的原始密码（未加密）
        String _newPassword = null;
        try {
            pagePassword = encrypt ? new String(EncodeUtils.base64Decode(StringUtils.trim(password))) : StringUtils.trim(password);
            _newPassword = encrypt ? new String(EncodeUtils.base64Decode(StringUtils.trim(newPassword))) : StringUtils.trim(newPassword);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.warnResult().setMsg("密码解码错误！");
        }

        if (!UserPasswordUpdateType.UserInit.getValue().equals(type) && !originalPassword.equals(Encrypt.e(pagePassword))) {
            return Result.warnResult().setMsg("原始密码输入错误！");
        }

        UserUtils.checkSecurity(model.getId(), _newPassword);
        //修改本地密码
        if (UserPasswordUpdateType.UserInit.getValue().equals(type)) {
            UserUtils.updateUserPasswordFirst(model.getId(), _newPassword);
        } else {
            UserUtils.updateUserPassword(model.getId(), _newPassword);
        }
        //注销当前会话信息
        if(null != sessionInfo){
            SecurityUtils.offLine(sessionInfo.getSessionId());
        }
        return Result.successResult();
    }

    /**
     * 修改个人信息 页面
     *
     * @param model
     * @param msg
     * @param uiModel
     * @return
     */
    @GetMapping(value = "input")
    public String input(@ModelAttribute("model")User model, String msg, Model uiModel) {
        if(null == model || StringUtils.isBlank(model.getId())){
            model = SecurityUtils.getCurrentUser();
        }
        uiModel.addAttribute("model",model);
        if(StringUtils.isNotBlank(msg)){
            addMessage(uiModel,msg);
        }
        return "modules/sys/user-input";
    }

    /**
     * 修改个人信息 保存
     *
     * @param model
     * @return
     */
    @Logging(logType = LogType.access,value = "修改个人信息")
    @PostMapping(value = "saveUserInfo")
    @ResponseBody
    public Result saveUserInfo(@ModelAttribute("model")User model) {
        if (model == null || StringUtils.isBlank(model.getId())) {
            throw new ActionException("用户[" + (null == model ? "":model.getId()) + "]不存在.");
        }
        SessionInfo sessionInfo =  SecurityUtils.getCurrentSessionInfo();
        if (null == sessionInfo || !sessionInfo.getUserId().equals(model.getId())) {
            throw new ActionException("未授权修改账号信息！");
        }
        userService.save(model);
        try {
            //刷新Session信息
            SecurityUtils.reloadSession(model.getId());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return Result.successResult();
    }


    /**
     * 通讯录 全部
     *
     * @param companyId
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "contactData")
    public String contactData(String companyId,HttpServletRequest request, HttpServletResponse response) {
        List<User> personPlatformContacts = StringUtils.isBlank(companyId) ? userService.findAllNormal():userService.findUsersByCompanyId(companyId);
        Map<String, List<User>> listMap = Maps.newConcurrentMap();
        personPlatformContacts.parallelStream().forEach(v->{
            List<User> list = listMap.get(v.getNamePinyinHeadChar());
            if (Collections3.isEmpty(list)) {
                list = Lists.newCopyOnWriteArrayList();
                list.add(v);
            } else {
                list.add(v);
            }
            listMap.put(v.getNamePinyinHeadChar(), list);
        });
        Set<String> keySet = listMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            List<User> userList = listMap.get(key);
            userList.sort(Comparator.comparing(User::getName));
        }
        Result result = Result.successResult().setObj(listMap);
        String json = JsonMapper.getInstance().toJson(result, User.class, new String[]{"id", "name","mobile"});
        return renderString(response,json, WebUtils.JSON_TYPE);
    }

    /**
     * 通讯录 全部
     *
     * @param companyId
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "contactTagData")
    public String contactTagData(String companyId,
                                 @RequestParam(value = "showPhoto",defaultValue = "false") Boolean showPhoto,
                                 HttpServletRequest request, HttpServletResponse response) {
        List<User> personPlatformContacts = StringUtils.isBlank(companyId) ? userService.findAllNormal():userService.findUsersByCompanyId(companyId);
        List<Map<String,Object>> list = Lists.newArrayList();
        personPlatformContacts.parallelStream().forEach(v->{
            //排除管理员
            if(UserType.Platform.getValue().equals(v.getUserType()) || UserType.User.getValue().equals(v.getUserType())){
                Map<String,Object> map = Maps.newHashMap();
                map.put("id",v.getId());
                map.put("name",v.getName());
                map.put("phone",v.getMobile());
                if(showPhoto){
                    map.put("photoSrc",v.getPhotoSrc());
                }
                map.put("tagIndex",v.getNamePinyinHeadChar());
                list.add(map);
            }

        });
        list.sort(Comparator.nullsLast(Comparator.comparing(m -> (String) m.get("name"),
                Comparator.nullsLast(Comparator.naturalOrder()))));
        return renderString(response,Result.successResult().setObj(list));
    }

    /**
     * 详细信息
     *
     * @param model
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"detail"})
    @ResponseBody
    public Result detail(@ModelAttribute("model") User model) {
        return Result.successResult().setObj(model);
    }


    /**
     * 详细信息
     *
     * @param id
     * @param loginName
     * @param token
     * @return
     */
    @RequiresUser(required = false)
    @PostMapping(value = {"detailByIdOrLoginName"})
    @ResponseBody
    public Result detailByIdOrLoginName(String id,
                                        String loginName,
                                        String token) {
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        User model = null;
        if (StringUtils.isNotBlank(token)) {
            String tokenLoginName = SecurityUtils.getLoginNameByToken(token);
            model = UserUtils.getUserByLoginName(tokenLoginName);
        }else{
            if (null == sessionInfo) {
                throw new ActionException("非法请求！");
            }
            model = StringUtils.isNotBlank(id) ? userService.get(id) : userService.getUserByLoginName(loginName);
        }

        if (null == model) {
            throw new ActionException("非法请求！");
        }
        return Result.successResult().setObj(model);
    }


    /**
     * 图片文件上传
     * @param multipartFile
     */
    @PostMapping(value = {"imageUpLoad"})
    @ResponseBody
    public Result imageUpLoad(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Exception exception = null;
        File file = null;
        try {
            file = DiskUtils.saveSystemFile(User.FOLDER_USER_PHOTO, sessionInfo.getUserId(), multipartFile);
            DiskUtils.saveFile(file);
            Map<String, Object> _data = Maps.newHashMap();
            String data = "data:image/jpeg;base64," + Base64Utils.encodeToString(FileCopyUtils.copyToByteArray(new FileInputStream(file.getDiskFile())));
            _data.put("file", file);
            _data.put("data", data);
            _data.put("url", AppConstants.getAdminPath() + "/disk/fileDownload/" + file.getId());
            result = Result.successResult().setObj(_data).setMsg("文件上传成功！");
        } catch (InvalidExtensionException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (FileNameLengthLimitExceededException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG);
        } catch (ActionException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (IOException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } finally {
            if (exception != null) {
                logger.error(exception.getMessage(),exception);
                if (file != null) {
                    DiskUtils.deleteFile(file.getId());
                }
            }
        }
        return result;

    }

}