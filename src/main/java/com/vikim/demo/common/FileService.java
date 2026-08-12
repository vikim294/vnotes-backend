package com.vikim.demo.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class FileService {

    private static final Logger log =
            LoggerFactory.getLogger(FileService.class);

    public String getFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if(filename == null) {
            throw new BusinessException(ErrorCode.FILE_NAME_IS_NULL);
        }

        if(filename.lastIndexOf('.') != -1) {
            return filename.substring(filename.lastIndexOf('.'));
        }

        throw new BusinessException(ErrorCode.FILE_EXTENSION_NOT_FOUNDED);
    }

    public String getImageExtensionByFileMimeType(MultipartFile file) {
        if(file.getContentType() == null) {
            throw new BusinessException(ErrorCode.FILE_CONTENT_TYPE_IS_NULL);
        }

        return switch (file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        };
    }

    public void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException ioException) {
            log.error(
                    "删除已上传的残留文件失败: {}",
                    target,
                    ioException
            );
        }
    }

    public void deleteQuietly(List<Path> targets) {
        for(Path target : targets) {
            deleteQuietly(target);
        }
    }
}
