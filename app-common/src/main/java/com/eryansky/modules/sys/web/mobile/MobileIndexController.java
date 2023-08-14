package com.eryansky.modules.sys.web.mobile;

import com.eryansky.common.exception.ActionException;
import com.eryansky.common.model.Result;
import com.eryansky.common.orm._enum.StatusState;
import com.eryansky.common.utils.Identities;
import com.eryansky.common.utils.StringUtils;
import com.eryansky.common.utils.UserAgentUtils;
import com.eryansky.common.utils.encode.EncodeUtils;
import com.eryansky.common.utils.net.IpUtils;
import com.eryansky.common.web.springmvc.SimpleController;
import com.eryansky.common.web.springmvc.SpringMVCHolder;
import com.eryansky.core.aop.annotation.Logging;
import com.eryansky.core.security.SecurityUtils;
import com.eryansky.core.security.SessionInfo;
import com.eryansky.core.security.annotation.PrepareOauth2;
import com.eryansky.core.security.annotation.RequiresUser;
import com.eryansky.core.web.annotation.Mobile;
import com.eryansky.core.web.annotation.MobileValue;
import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk._enum.FolderType;
import com.eryansky.modules.disk.mapper.File;
import com.eryansky.modules.disk.utils.DiskUtils;
import com.eryansky.modules.sys._enum.LogType;
import com.eryansky.modules.sys._enum.VersionLogType;
import com.eryansky.modules.sys.mapper.VersionLog;
import com.eryansky.modules.sys.service.VersionLogService;
import com.eryansky.modules.sys.utils.DownloadFileUtils;
import com.eryansky.modules.sys.utils.VersionLogUtils;
import com.eryansky.utils.AppConstants;
import com.eryansky.utils.AppUtils;
import com.google.common.collect.Maps;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

/**
 * 手机端入口
 */
@Mobile
@Controller
@RequestMapping(value="${mobilePath}")
public class MobileIndexController extends SimpleController {

    @Autowired
    private VersionLogService versionLogService;

    @Logging(logType = LogType.access, value = "移动APP")
    @GetMapping(value="")
    public ModelAndView index() {
        return new ModelAndView("layout/index");
    }

    @Logging(logType = LogType.access, value = "移动APP")
    @GetMapping(value = {"content"})
    public ModelAndView content() {
        return new ModelAndView("layout/index_content");
    }


    /**
     * 下载页面
     * @param app 应用标识 默认值: {@link VersionLog#DEFAULT_ID}
     * @param versionLogType {@link VersionLogType}
     * @param versionCode
     * @return
     */
    @Mobile(value = MobileValue.PC)
    @RequiresUser(required = false)
    @GetMapping(value="download")
    public ModelAndView download(String app,String versionLogType, String versionCode) {
        ModelAndView modelAndView = new ModelAndView("mobile/download");
        VersionLog versionLog = null;
        String ua = UserAgentUtils.getHTTPUserAgent(SpringMVCHolder.getRequest());
        boolean likeIOS = AppUtils.likeIOS(ua);
        boolean likeAndroid = AppUtils.likeAndroid(ua);
        if (versionLogType == null) {
            if (likeIOS) {
                versionLogType = VersionLogType.iPhoneAPP.getValue();
            } else {
                versionLogType = VersionLogType.Android.getValue();
            }
        } else {
            if (VersionLogType.iPhoneAPP.getValue().equals(versionLogType)) {
                likeIOS = true;
                likeAndroid = false;
            }
        }
        if (StringUtils.isNotBlank(versionCode)) {
            versionLog = versionLogService.getByVersionCode(app,versionLogType, versionCode);
        } else {
            versionLog = versionLogService.getLatestVersionLog(app,versionLogType);
        }
        modelAndView.addObject("app", app);
        modelAndView.addObject("versionLogType", versionLogType);
        modelAndView.addObject("versionCode", versionCode);
        modelAndView.addObject("model", versionLog);
        modelAndView.addObject("likeAndroid", likeAndroid);
        modelAndView.addObject("likeIOS", likeIOS);
        return modelAndView;
    }

    /**
     * 查找更新
     * @param versionLogType {@link VersionLogType}
     * @param app 应用标识 默认值: {@link VersionLog#DEFAULT_ID}
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"getNewVersion/{versionLogType}"})
    public Result getNewVersion(@PathVariable String versionLogType,String app) {
        return getNewVersion(SpringMVCHolder.getRequest(),versionLogType,app);
    }


    /**
     * 查找更新
     * @param request
     * @param versionLogType {@link VersionLogType}
     * @param app 应用标识 默认值: {@link VersionLog#DEFAULT_ID}
     * @return
     */
    @PrepareOauth2(enable = false)
    @RequiresUser(required = false)
    @ResponseBody
    @RequestMapping(method = {RequestMethod.GET,RequestMethod.POST},value = {"getNewVersion"})
    public Result getNewVersion(HttpServletRequest request,String versionLogType,String app) {
        String _versionLogType = versionLogType;
        if(StringUtils.isBlank(versionLogType)){
            VersionLogType vt = VersionLogUtils.getLatestVersionLogType(request);
            _versionLogType = null != vt ? vt.getValue():null;
        }
        if(StringUtils.isBlank(_versionLogType)){
            throw new ActionException("未识别参数[versionLogType]");
        }
        VersionLog max = versionLogService.getLatestVersionLog(app,_versionLogType);
        Map<String,Object> data = Maps.newHashMap();
        //兼容性代码
        data.put("versionName",null != max ? max.getVersionName():null);
        data.put("versionCode",null != max ? max.getVersionCode():null);
        data.put("isTip",null != max ? max.getIsTip():null);
        data.put("remark",null != max ? max.getRemark():null);

        data.put("versionLog",max);
        data.put("appDownLoadUrl", AppUtils.getAppURL() + "/m/download?app=" + (null == app ? "" : app));
        data.put("apkDownLoadUrl",AppUtils.getAppURL()+"/m/downloadApp/"+VersionLogType.Android.getValue()+"?app="+ (null == app ? "" : app));
        return Result.successResult().setObj(data);
    }

    private static final String MIME_ANDROID_TYPE = "application/vnd.android.package-archive";

    /**
     * APP下载
     *
     * @param response
     * @param request
     * @param app 应用标识 默认值: {@link VersionLog#DEFAULT_ID}
     * @param versionCode    版本号
     * @param versionLogType {@link com.eryansky.modules.sys._enum.VersionLogType}
     *                       文件ID
     */
    @Logging(logType = LogType.access, value = "APP下载")
    @RequiresUser(required = false)
    @PrepareOauth2(enable = false)
    @GetMapping(value = {"downloadApp/{versionLogType}"})
    public ModelAndView downloadApp(HttpServletResponse response,
                                    HttpServletRequest request,
                                    String app,
                                    String versionCode,
                                    @PathVariable String versionLogType) throws Exception {
        VersionLog versionLog = null;
        if (StringUtils.isNotBlank(versionCode)) {
            versionLog = versionLogService.getByVersionCode(app, versionLogType, versionCode);
        } else {
            versionLog = versionLogService.getLatestVersionLog(app, versionLogType);
        }
        ActionException fileNotFoldException = new ActionException("下载的APP文件不存在");
        if (null == versionLog || StringUtils.isBlank(versionLog.getFileId())) {
            logger.error(fileNotFoldException.getMessage() + "," + (null != versionLog ? versionLog.getFileId() : ""));
//            throw fileNotFoldException;
            // 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        File file = DiskUtils.getFile(versionLog.getFileId());
        if (VersionLogType.Android.getValue().equals(versionLogType)) {
            response.setContentType(MIME_ANDROID_TYPE);
        }
        try {
            DownloadFileUtils.downRangeFile(file.getDiskFile(),file.getName(),response,request);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            DownloadFileUtils.loggerHTTPHeader(request,response);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            throw e;
        }
        return null;
    }

    /**
     * 文件删除
     *
     * @param fileId
     * @return
     */
    @Logging(value = "删除文件", logType = LogType.access)
    @PostMapping(value = {"deleteFile"})
    @ResponseBody
    public Result deleteFile(@RequestParam(value = "fileId") String fileId) {
        DiskUtils.deleteFile(fileId);
        return Result.successResult();
    }

    /**
     * 图片文件上传
     */
    @PostMapping(value = {"base64ImageUpLoad"})
    @ResponseBody
    public Result base64ImageUpLoad(@RequestParam(value = "base64Data", required = false) String base64Data) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        if (null == sessionInfo) {
            return Result.errorResult().setMsg("未授权");
        }
        Exception exception = null;
        File file = null;
        try {

            String dataPrix = "";
            String data = "";

            if (base64Data == null || "".equals(base64Data)) {
                return Result.errorResult().setMsg("上传失败，上传图片数据为空");
            } else {
                String[] d = base64Data.split("base64,");
                if (d != null && d.length == 2) {
                    dataPrix = d[0];
                    data = d[1];
                } else {
                    return Result.errorResult().setMsg("上传失败，数据不合法");
                }
            }

            String suffix = "";
            if ("data:image/jpeg;".equalsIgnoreCase(dataPrix)) {//data:image/jpeg;base64,base64编码的jpeg图片数据
                suffix = ".jpg";
            } else if ("data:image/x-icon;".equalsIgnoreCase(dataPrix)) {//data:image/x-icon;base64,base64编码的icon图片数据
                suffix = ".ico";
            } else if ("data:image/gif;".equalsIgnoreCase(dataPrix)) {//data:image/gif;base64,base64编码的gif图片数据
                suffix = ".gif";
            } else if ("data:image/png;".equalsIgnoreCase(dataPrix)) {//data:image/png;base64,base64编码的png图片数据
                suffix = ".png";
            } else {
                return Result.errorResult().setMsg("上传图片格式不合法");
            }
            String tempFileName = Identities.uuid() + suffix;

            byte[] bs = null;
            try {
                bs = EncodeUtils.base64Decode(data);
//                bs = Base64Utils.decodeFromString(data);
            } catch (Exception e) {
                logger.info("{},{}",new Object[]{sessionInfo.getLoginName(),base64Data});
                logger.error("图片上传失败,"+e.getMessage(),e);
                return Result.errorResult().setMsg("图片上传失败,解析异常！");
            }

            file = DiskUtils.saveSystemFile("IMAGE", FolderType.NORMAL.getValue(), sessionInfo.getUserId(), new ByteArrayInputStream(bs), tempFileName);
            file.setStatus(StatusState.LOCK.getValue());
            DiskUtils.saveFile(file);
            result = Result.successResult().setObj(file).setMsg("文件上传成功！");
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

    /**
     * 图片文件上传
     * @param multipartFile
     * @param folderCode 文件夹名称
     */
    @PostMapping(value = {"imageUpLoad"})
    @ResponseBody
    public Result imageUpLoad(@RequestParam(value = "uploadFile", required = false) MultipartFile multipartFile,String folderCode) {
        Result result = null;
        SessionInfo sessionInfo = SecurityUtils.getCurrentSessionInfo();
        Exception exception = null;
        File file = null;
        try {
            String _folderName = "IMAGE";//默认文件夹
            if(StringUtils.isNotBlank(folderCode)){
                _folderName = FilenameUtils.getName(folderCode);
            }
            file = DiskUtils.saveSystemFile(_folderName, sessionInfo.getUserId(), multipartFile);
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