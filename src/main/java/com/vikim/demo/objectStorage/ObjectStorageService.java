package com.vikim.demo.objectStorage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

public interface ObjectStorageService {

    String upload(
            MultipartFile file,
            String objectKey,
            String contentType
    );

    void delete(String objectKey);

    void deleteQuietly(String objectKey);

    void deleteQuietly(List<String> objectKeys);

    String generatePresignedUrl(String key, Duration duration);

    String generatePresignedUrl(String key);
}
