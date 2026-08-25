package com.vikim.demo.objectStorage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

public interface ObjectStorageService {

    String upload(
            MultipartFile file,
            String objectKey
    );

    void delete(String objectKey);

    void deleteQuietly(String objectKey);

    String generatePresignedUrl(String key, Duration duration);

    String generatePresignedUrl(String key);
}
