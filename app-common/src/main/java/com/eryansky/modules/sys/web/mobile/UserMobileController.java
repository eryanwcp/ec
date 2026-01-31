package com.eryansky.modules.sys.web.mobile;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.map.CaseInsensitiveMap;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.collections.Collections3;
import com.eryansky.common.utils.encode.*;
import com.eryansky.common.utils.mapper.JsonMapper;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.utils.WebUtils;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.upload.FileUploadUtils;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.encrypt.advice.DecryptRequestBodyAdvice;
import com.eryansky.encrypt.config.EncryptProvider;
import com.eryansky.encrypt.enums.CipherMode;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

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
    @Logging(logType = LogType.security, value = "修改密码")
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
                map.put("remark",v.getRemark());
                map.put("phone",v.getMobile());
                map.put("tel",v.getTel());
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
     * @param folderCode 文件夹名称
     * @param press 是否添加水印
     * @param pressText 水印文字
     */
    @PostMapping(value = {"imageUpLoad"})
    @ResponseBody
    public Result imageUpLoad(@RequestHeader Map<String, String> headers,
                              @RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile,
                              @RequestParam(value = "folderCode", defaultValue = User.FOLDER_USER_PHOTO) String folderCode,
                              @RequestParam(value = "press", defaultValue = "false") Boolean press,
                              String pressText) {
        CaseInsensitiveMap<String,String> caseInsensitiveMap = new CaseInsensitiveMap<>(headers);
        String requestEncrypt =  caseInsensitiveMap.get(DecryptRequestBodyAdvice.ENCRYPT);
        String requestEncryptKey =  caseInsensitiveMap.get(DecryptRequestBodyAdvice.ENCRYPT_KEY);
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Exception exception = null;
        File file = null;
        java.io.File tempFile = null;
        try {
//            FileUploadUtils.assertAllowed(multipartFile,FileUploadUtils.IMAGE_EXTENSION, FileUploadUtils.DEFAULT_MAX_SIZE);
            String _folderName = "IMAGE";//默认文件夹

            String filename = DiskUtils.getMultipartOriginalFilename(multipartFile);
            String extension = FilenameUtils.getExtension(filename);
            //文件解密处理
            byte[] data = null;
            if(CipherMode.SM4.name().equals(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)){
                String key = null;
                try {
                    key = RSAUtils.decryptHexString(requestEncryptKey, EncryptProvider.privateKeyBase64());
                    data =  Sm4Utils.decryptCbcPadding(EncodeUtils.hexDecode(key),multipartFile.getBytes());
                } catch (Exception e) {
                    try {
                        data =  Sm4Utils.decryptCbcPadding(EncodeUtils.base64Decode(requestEncryptKey),multipartFile.getBytes());
                    } catch (Exception e2) {
                        logger.error(e2.getMessage(),e2);
                    }
                }

            }else if(CipherMode.AES.name().equals(requestEncrypt) && StringUtils.isNotBlank(requestEncryptKey)){
                String key = null;
                try {
                    key = RSAUtils.decryptBase64String(requestEncryptKey, EncryptProvider.privateKeyBase64());
                    data =  Cryptos.aesECBDecryptBytes(multipartFile.getBytes(),EncodeUtils.base64Decode(key));
                } catch (Exception e) {
                    try {
                        data =  Cryptos.aesECBDecryptBytes(multipartFile.getBytes(),EncodeUtils.base64Decode(requestEncryptKey));
                    } catch (Exception e2) {
                        logger.error(e2.getMessage(),e2);
                    }

                }

            }else if(CipherMode.BASE64.name().equals(requestEncrypt)){
                try {
                    data =  EncodeUtils.base64Decode(multipartFile.getBytes());
                } catch (Exception e) {
                    logger.error(e.getMessage(),e);
                }
            }

            if(null != data){
                multipartFile = new CustomMultipartFile(filename,data);
            }

            //兼容处理 无后缀文件的处理
            if(StringUtils.isNotBlank(extension)){
                FileUploadUtils.assertAllowed(multipartFile,FileUploadUtils.IMAGE_EXTENSION, FileUploadUtils.DEFAULT_MAX_SIZE);
            }
            if(StringUtils.isNotBlank(folderCode)){
                _folderName = FilenameUtils.getName(folderCode);
            }

            InputStream inputStream = multipartFile.getInputStream();
            String tempFileName = Identities.uuid() +"."+ extension;
            if(press){
                // 获取偏转角度
                int angle = getAngle(multipartFile);
                // 原始图片缓存
                BufferedImage originalImage =  ImgUtil.read(multipartFile.getInputStream());

                // 水印文字
                String watermarkText = StringUtils.isNotBlank(pressText) ? pressText:sessionInfo.getLoginName();
                BufferedImage watermarkImage = null;
                if (angle != 90 && angle != 270) {
                    // 不需要旋转，直接处理
                    watermarkImage = new BufferedImage(
                            originalImage.getWidth(),
                            originalImage.getHeight(),
                            BufferedImage.TYPE_INT_RGB
                    );
                    Graphics2D g2d = (Graphics2D) watermarkImage.getGraphics();
                    g2d.setFont(new java.awt.Font("宋体", java.awt.Font.BOLD, 28)); // 设置水印字体
                    g2d.drawImage(originalImage, 0, 0, null); // 绘制原始图片
                    g2d.setColor(Color.red); // 设置水印颜色
                    g2d.drawString(watermarkText, 20, 30); // 绘制水印文字
                    g2d.dispose();
                } else {
                    // 宽高互换
                    int imgWidth = originalImage.getHeight();
                    int imgHeight = originalImage.getWidth();

                    // 中心点位置
                    double centerWidth = ((double) imgWidth) / 2;
                    double centerHeight = ((double) imgHeight) / 2;

                    // 图片缓存
                    watermarkImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

                    // 旋转对应角度
                    Graphics2D g = watermarkImage.createGraphics();
                    g.rotate(Math.toRadians(angle), centerWidth, centerHeight);
                    g.drawImage(originalImage, (imgWidth - originalImage.getWidth()) / 2, (imgHeight - originalImage.getHeight()) / 2, null);
                    g.rotate(Math.toRadians(-angle), centerWidth, centerHeight);
                    g.setFont(new java.awt.Font("宋体", java.awt.Font.BOLD, 28)); // 设置水印字体
                    g.setColor(Color.red); // 设置水印颜色
                    g.drawString(watermarkText, 20, 30); // 绘制水印文字
                    g.dispose();
                }
                tempFile = new java.io.File(tempFileName);
                ImgUtil.write(watermarkImage, tempFile);
                inputStream = new FileInputStream(tempFileName);
            }


            file = DiskUtils.saveSystemFile(_folderName, FolderType.NORMAL.getValue(), sessionInfo.getUserId(), inputStream, tempFileName);
            result = Result.successResult().setData(file).setMsg("文件上传成功！");
        } catch (InvalidExtensionException e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } catch (FileUploadSizeException e) {
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
        } catch (Exception e) {
            exception = e;
            result = Result.errorResult().setMsg(DiskUtils.UPLOAD_FAIL_MSG + e.getMessage());
        } finally {
            if (exception != null) {
                logger.error(exception.getMessage(),exception);
                if (file != null) {
                    DiskUtils.deleteFile(file);
                }
            }
            if (tempFile != null) {
                tempFile.delete();
            }

        }
        return result;

    }

    private int getAngle(MultipartFile file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if ("Orientation".equals(tag.getTagName())) {
                        String orientation = tag.getDescription();
                        if (orientation.contains("90")) {
                            return 90;
                        } else if (orientation.contains("180")) {
                            return 180;
                        } else if (orientation.contains("270")) {
                            return 270;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return 0;
        }
        return 0;
    }

}