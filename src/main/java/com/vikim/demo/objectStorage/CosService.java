package com.vikim.demo.objectStorage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.ObjectMetadata;
import com.vikim.demo.common.BusinessException;
import com.vikim.demo.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CosService implements ObjectStorageService {

    private final COSClient cosClient;
    private final CosProperties properties;

    private static final Logger log =
            LoggerFactory.getLogger(CosService.class);

    @Override
    public String upload(
            MultipartFile file,
            String objectKey,
            String contentType
    ){
        // 使用 try-with-resources 保证输入流使用后关闭
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(contentType);

            cosClient.putObject(properties.bucket(), objectKey, inputStream, metadata);

            return objectKey;
        } catch (IOException | RuntimeException e) {
            log.error(
                    "上传 COS 失败: {}",
                    objectKey,
                    e
            );

            throw new BusinessException(ErrorCode.UPLOAD_OBJECT_STORAGE_FAILED);
        }
    }

    @Override
    public void delete( String objectKey){
        cosClient.deleteObject(
                properties.bucket(),
                objectKey
        );
    }

    // 抛异常时，吞掉，只记录日志
    @Override
    public void deleteQuietly(String objectKey){
        try {
            cosClient.deleteObject(
                    properties.bucket(),
                    objectKey
            );
        } catch (Exception e) {
            log.error(
                    "删除已上传 COS 的残留文件失败: {}",
                    objectKey,
                    e
            );
        }
    }

    @Override
    public void deleteQuietly(List<String> objectKeys) {
        for(String key: objectKeys) {
            deleteQuietly(key);
        }
    }

    // 生成用来临时访问的 url（因为我创建的桶是私有读写，所以只用对象存储域名 + 资源 key 会“访问无权限”）
    @Override
    public String generatePresignedUrl(String key, Duration duration){
        if(key == null) return null;

        Date expiration = new Date(
                        System.currentTimeMillis()
                                + duration.toMillis()
                );

        URL url = cosClient.generatePresignedUrl(properties.bucket(), key, expiration, HttpMethodName.GET);
        return url.toString();
    }

    @Override
    public String generatePresignedUrl(String key){
        return generatePresignedUrl(key, Duration.ofMinutes(30));
    }
}
