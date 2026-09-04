package com.eryansky.modules.sys.web.mobile;

import cn.hutool.core.img.ImgUtil;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Result;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.encode.*;
import com.eryansky.common.utils.io.IoUtils;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.upload.FileUploadUtils;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.encrypt.anotation.DecryptRequestBody;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
import com.eryansky.encrypt.util.RequestEncryptUtils;
import com.eryansky.modules.disk._enum.FolderType;
import com.eryansky.modules.disk.extend.CustomMultipartFile;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.UserPasswordUpdateType;
import com.eryansky.modules.sys._enum.UserType;
import com.eryansky.modules.sys.mapper.User;
import com.eryansky.modules.sys.service.UserService;
import com.eryansky.modules.sys.utils.UserUtils;
import com.eryansky.utils.AppConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理
 */
@Mobile
@Controller
@RequestMapping(value = "${mobilePath}/sys/user")
public class UserMobileController extends SimpleController {

    @Resource
    private UserService userService;
    @Resource
    private MobileIndexController mobileIndexController;

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
     * @param paramEncrypt 是否加密 加密方法采用base64加密方案
     * @param type 修改密码类型 1：初始化密码 2：帐号与安全修改密码
     * @param password 原始密码
     * @param newPassword 新密码
     * @param token 安全Token
     * @return
     */
    @RequiresUser(required = false)
    @Logging(logType = LogType.security, value = "修改密码")
    @PostMapping(value = "savePs")
    @ResponseBody
    public Result savePs(@RequestParam(name = "id", required = false) String id,
                         @RequestParam(name = "ln", required = false) String loginName,
                         @RequestParam(name = "encrypt",defaultValue = "false") String paramEncrypt,
                         @RequestParam(name = "type", required = false) String type,
                         @RequestParam(name = "ps", required = false) String password,
                         @RequestParam(name = "newPs", required = true) String newPassword,
                         @RequestParam(name = "token", required = false) String token,
                         HttpServletRequest request) {
        String encrypt = WebUtils.getHeaderIgnoreCase(request, RequestEncryptUtils.ENCRYPT);
        String encryptKey = WebUtils.getHeaderIgnoreCase(request,RequestEncryptUtils.ENCRYPT_KEY);
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        User model = null;
        if (StringUtils.isNotBlank(token)) {
            String tokenLoginName = SecurityUtils.getLoginNameByToken(token);
            model = UserUtils.getUserByLoginName(tokenLoginName);
            //安全校验 仅允许自己修改
            if (null != model && !model.getId().equals(id)) {
                logger.warn("未授权修改账号密码：{} {} {} {}", model.getLoginName(), id, loginName, token);
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
                logger.warn("未授权修改账号密码：{} {} {} {}", model.getLoginName(), id, loginName, token);
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
        String pagePassword = StringUtils.trim(password);//页面输入的原始密码（未加密）
        String _newPassword = StringUtils.trim(newPassword);
        try {
            if ("AES".equals(encrypt)) {
                pagePassword = new String(RequestEncryptUtils.decryptDataByRequest(encrypt, encryptKey, EncodeUtils.base64Decode(StringUtils.trim(password))));
                _newPassword = new String(RequestEncryptUtils.decryptDataByRequest(encrypt, encryptKey, EncodeUtils.base64Decode(StringUtils.trim(newPassword))));
            } else if ("SM4".equals(encrypt)) {
                pagePassword = new String(RequestEncryptUtils.decryptDataByRequest(encrypt, encryptKey, EncodeUtils.hexDecode(StringUtils.trim(password))));
                _newPassword = new String(RequestEncryptUtils.decryptDataByRequest(encrypt, encryptKey, EncodeUtils.hexDecode(StringUtils.trim(newPassword))));
            }else if("true".equals(paramEncrypt)){//兼容方案 客户端升级后删除
                pagePassword =  new String(EncodeUtils.base64Decode(StringUtils.trim(password)));
                _newPassword =  new String(EncodeUtils.base64Decode(StringUtils.trim(newPassword)));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Result.warnResult().setMsg("密码解码错误！");
        }

        if (!UserPasswordUpdateType.UserInit.getValue().equals(type) && !originalPassword.equals(Encrypt.e(pagePassword))) {
            return Result.warnResult().setMsg("原始密码输入错误！");
        }

        if(AppConstants.isCheckPasswordPolicy()){
            UserUtils.checkSecurity(model.getId(), _newPassword);
        }

        //修改本地密码
        if (UserPasswordUpdateType.UserInit.getValue().equals(type)) {
            UserUtils.updateUserPasswordFirst(model.getId(), _newPassword);
        } else {
            UserUtils.updateUserPassword(model.getId(), _newPassword);
        }
        //注销当前会话信息
        if(null != sessionInfo){
            SecurityUtils.offLine(sessionInfo.getId());
        }
        return Result.successResult();
    }


    /**
     * 设置初始密码或修改密码（仅限用户自己修改）
     * @param requestData
     *      id 用户ID
     *      loginName 登录账号
     *      paramEncrypt 是否加密 加密方法采用base64加密方案
     *      type 修改密码类型 1：初始化密码 2：帐号与安全修改密码
     *      password 原始密码
     *      newPassword 新密码
     *      token 安全Token
     * @return
     */
    @RequiresUser(required = false)
    @Logging(logType = LogType.security, value = "修改密码")
    @DecryptRequestBody
    @PostMapping(value = "savePassword")
    @ResponseBody
    public Result savePassword(@RequestBody JsonNode requestData,
                               HttpServletRequest request) {
        String id = requestData.get("id").asText();
        String loginName = requestData.get("ln").asText();
        String type = requestData.get("type").asText();
        String password = requestData.get("ps").asText();
        String newPassword = requestData.get("newPs").asText();
        String token = requestData.get("token").asText();
        return savePs(id,loginName,null,type,password,newPassword,token,request);
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
    @Logging(logType = LogType.operate,value = "修改个人信息")
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
        Map<String, List<User>> listMap = personPlatformContacts.parallelStream()
                // 按拼音首字母分组（key：namePinyinHeadChar，value：对应的User列表）
                .collect(Collectors.groupingByConcurrent(
                        User::getNamePinyinHeadChar, // 分组依据：拼音首字母
                        // 收集每个分组的元素，并直接排序
                        Collectors.collectingAndThen(
                                Collectors.toList(), // 先收集为列表
                                userList -> {
                                    // 对每个分组的列表按姓名排序
                                    userList.sort(Comparator.comparing(User::getName));
                                    return userList;
                                }
                        )
                ));
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
        List<Map<String,Object>> list = personPlatformContacts.parallelStream()
                // 1. 过滤：排除非指定类型的用户（管理员）
                .filter(v -> {
                    String userType = v.getUserType();
                    return UserType.Platform.getValue().equals(userType)
                            || UserType.User.getValue().equals(userType);
                })
                // 2. 转换：将对象映射为目标 Map 结构
                .map(v -> {
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("id", v.getId());
                    map.put("name", v.getName());
                    map.put("remark", v.getRemark());
                    map.put("phone", v.getMobile());
                    map.put("tel", v.getTel());
                    // 按需添加照片字段
                    if (showPhoto) {
                        map.put("photoSrc", v.getPhotoSrc());
                    }
                    map.put("tagIndex", v.getNamePinyinHeadChar());
                    return map;
                })
                // 3. 收集结果并排序（流式收集后排序，避免并行流排序的线程安全问题）
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        collectedList -> {
                            collectedList.sort(Comparator.nullsLast(
                                    Comparator.comparing(
                                            m -> (String) m.get("name"),
                                            Comparator.nullsLast(Comparator.naturalOrder())
                                    )
                            ));
                            return collectedList;
                        }
                ));

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
        if (model == null) {
            return Result.errorResult().setMsg("用户不存在");
        }
        // 脱敏处理，置空敏感字段或转为 UserVO
        model.setPassword(null);
        model.setOriginalPassword(null);
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
        // 1. 获取当前登录用户身份
        String currentUserId = null;
        if (StringUtils.isNotBlank(token)) {
            String tokenLoginName = SecurityUtils.getLoginNameByToken(token);
            User tokenUser = UserUtils.getUserByLoginName(tokenLoginName);
            if (tokenUser != null) {
                currentUserId = tokenUser.getId();
            }
        } else if (sessionInfo != null) {
            currentUserId = sessionInfo.getUserId();
        }

        if (StringUtils.isBlank(currentUserId)) {
            throw new ActionException("非法请求！");
        }

        // 2. 查询目标用户
        model = StringUtils.isNotBlank(id) ? userService.get(id) : userService.getUserByLoginName(loginName);
        if (null == model) {
            throw new ActionException("用户不存在！");
        }

        // 3. 越权校验：非管理员仅能查询本人信息（或根据业务需要调整权限逻辑）
        if (!model.getId().equals(currentUserId) && !SecurityUtils.isCurrentUserAdmin()) {
            throw new ActionException("无权查看该用户信息！");
        }
        // 脱敏处理，置空敏感字段或转为 UserVO
        model.setPassword(null);
        model.setOriginalPassword(null);
        return Result.successResult().setObj(model);
    }


    /**
     * 图片文件上传
     * @param multipartFile
     * @param folderCode 文件夹名称
     * @param press 是否添加水印
     * @param pressText 水印文字
     */
    @PostMapping(value = {"imageUpLoad"})
    @ResponseBody
    public Result imageUpLoad(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile,
                              @RequestParam(value = "folderCode", defaultValue = User.FOLDER_USER_PHOTO) String folderCode,
                              @RequestParam(value = "press", defaultValue = "false") Boolean press,
                              @RequestParam(value = "pressText", required = false) String pressText,
                              HttpServletRequest request, HttpServletResponse response) {
        return mobileIndexController.imageUpLoad(multipartFile,folderCode,null,null,press,pressText,request,response);
    }

}