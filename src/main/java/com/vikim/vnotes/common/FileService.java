package com.vikim.vnotes.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FileService {

    private static final Logger log =
            LoggerFactory.getLogger(FileService.class);

    private static final long MB = 1024L * 1024L;

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

    // 校验文件的大小
    public void validateFileSize(MultipartFile file, long maxMB) {
        if(file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_IS_NOT_PRESENT);
        }

        if(file.getSize() > maxMB * MB) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }
    }

    // 校验所有文件的总大小
    public void validateFilesTotalSize(Map<String, MultipartFile> files, long maxMB) {
        long totalBytes = files.values()
                .stream()
                .mapToLong(MultipartFile::getSize)
                .sum();

        if(totalBytes > maxMB * MB) {
            throw new BusinessException(ErrorCode.FILES_TOTAL_SIZE_EXCEEDED);
        }
    }

    // 校验图片宽、高、总像素
    public void validateImageDimensions(
            int width,
            int height,
            int maxWidth,
            int maxHeight,
            long maxPixels
    ){
        if (width <= 0 || height <= 0) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE);
        }

        if (width > maxWidth || height > maxHeight) {
            throw new BusinessException(ErrorCode.IMAGE_DIMENSIONS_EXCEEDED);
        }

        // 转成 long，避免 width * height 发生 int 溢出
        long pixels = (long) width * height;

        if (pixels > maxPixels) {
            throw new BusinessException(ErrorCode.IMAGE_PIXELS_EXCEEDED);
        }
    }

    public String validateAndGetImageExtension(
            MultipartFile file,
            long maxMB,
            int maxWidth,
            int maxHeight,
            long maxPixels
    ) {
        // 校验存在 和大小
        validateFileSize(file, maxMB);

        ImageReader reader = null;

        try (
                ImageInputStream input =
                        ImageIO.createImageInputStream(file.getInputStream())
        ) {
            if (input == null) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE);
            }

            // 根据文件内容寻找图片解析器
            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
            }

            reader = readers.next();
            reader.setInput(input, true, true);

            // 获取文件真实格式
            String format = reader.getFormatName()
                    .toLowerCase(Locale.ROOT);

            String extension = switch (format) {
                case "jpeg", "jpg" -> ".jpg";
                case "png" -> ".png";
                // JDK 自带的 ImageIO 通常支持 JPEG、PNG，但不原生支持 WebP
                // 确实需要 WebP 时，再加入 WebP 的 ImageIO 插件
                case "webp" -> ".webp";
                default -> throw new BusinessException(
                        ErrorCode.UNSUPPORTED_IMAGE_TYPE
                );
            };

            int width = reader.getWidth(0);
            int height = reader.getHeight(0);

            // 校验文件宽高、总像素
            validateImageDimensions(
                    width,
                    height,
                    maxWidth,
                    maxHeight,
                    maxPixels
            );

            // 尝试 完整解码，避免只有合法文件头、内容实际损坏
            BufferedImage image = reader.read(0);

            if (image == null) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE);
            }

            return extension;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    public String imageExtToMIME(String ext) {
        return switch (ext) {
            case ".jpg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        };
    }
}
