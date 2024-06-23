package com.eryansky.core.cms;

import com.eryansky.core.web.upload.exception.FileNameLengthLimitExceededException;
import com.eryansky.core.web.upload.exception.InvalidExtensionException;
import com.eryansky.modules.disk._enum.FolderType;
import com.eryansky.modules.disk.utils.DiskUtils;
import fr.opensagres.poi.xwpf.converter.core.ImageManager;
import org.apache.commons.fileupload.FileUploadBase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * 自定义图片管理器
 */
public class ImageManagerExtend extends ImageManager {

    private com.eryansky.modules.disk.mapper.File imageFile;

    public ImageManagerExtend(File baseDir, String imageSubDir) {
        super(baseDir, imageSubDir);
    }


    @Override
    public void extract(String imagePath, byte[] imageData) throws IOException {
        try {
            imageFile = DiskUtils.saveSystemFile("WORD_IMAGES", FolderType.HIDE.getValue(),null,new ByteArrayInputStream(imageData),imagePath);
        } catch (InvalidExtensionException e) {
            throw new RuntimeException(e);
        } catch (FileUploadBase.FileSizeLimitExceededException e) {
            throw new RuntimeException(e);
        } catch (FileNameLengthLimitExceededException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String resolve(String uri) {
        return imageFile.getUrl();
    }

}