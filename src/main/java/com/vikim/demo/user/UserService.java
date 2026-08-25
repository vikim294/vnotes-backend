package com.vikim.demo.user;


import com.vikim.demo.common.*;
import com.vikim.demo.objectStorage.ObjectStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final FileUploadProperties fileUploadProperties;
    private final ObjectStorageService objectStorageService;

    public List<UserResponse> findAll() {
        return userMapper.findAll().stream()
                .map(u -> {
                    return new UserResponse(
                            u.getId(),
                            u.getUsername(),
                            u.getEmail(),
                            objectStorageService.generatePresignedUrl(u.getAvatarUrl()),
                            u.getCreatedAt()
                    );
                })
                .toList();
    }

    public void registerAccount(RegisterAccountRequest request) {
        Optional<User> user = userMapper.findByEmail(request.email());
        if (user.isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        userMapper.registerAccount(request.username(), passwordHash, request.email());
    }

    public UploadAvatarResponse uploadAvatarLocally(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_IS_NOT_PRESENT);
        }

        // 生成随机文件名
        String filename = UUID.randomUUID()
                + fileService.getImageExtensionByFileMimeType(file);

        // 文件保存目录
        Path avatarDir = Paths.get(fileUploadProperties.uploadDir(), "avatar");

        // 文件 url
        Path target =
                avatarDir.resolve(filename);

        try {
            Files.createDirectories(avatarDir);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DIR_CREATED_FAILED);
        }

        try {
            // 写入文件到本地
            file.transferTo(target);
        } catch (IOException e) {
            fileService.deleteQuietly(target);

            throw new BusinessException(ErrorCode.FILE_WRITING_FAILED);
        }

        // 文件 url
        String fileUrl = "/uploads/avatar/" + filename;

        try {
            int affectedRows =
                    userMapper.updateAvatarUrl(
                            userId,
                            fileUrl
                    );

            if (affectedRows != 1) {
                // 抛出用户不存在
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            // 数据库直接抛异常了/上面的“用户不存在” -> 清理已上传的文件
            fileService.deleteQuietly(target);

            // 抛出原异常
            throw e;
        }

        return new UploadAvatarResponse(fileUrl);
    }

    public UserProfileResponse getProfile(Long userId) {
        UserProfileResponse response = userMapper.getProfile(userId).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_NOT_FOUND)
        );

        return new UserProfileResponse(
                response.id(),
                response.username(),
                response.email(),
                objectStorageService.generatePresignedUrl(response.avatarUrl()),
                response.createdAt()
        );
    }

    public UploadAvatarResponse uploadAvatarToObjectStorage(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_IS_NOT_PRESENT);
        }

        // 查旧头像，如果存在，则上传新头像成功后要删除
        UserProfileResponse profileResponse = userMapper.getProfile(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 生成随机文件名
        String filename = UUID.randomUUID()
                + fileService.getImageExtensionByFileMimeType(file);

        // 文件 key
        String objectKey = "avatar/" + userId + "/" + filename;

        // 存
        objectStorageService.upload(file, objectKey);

        try {
            int affectedRows =
                    userMapper.updateAvatarUrl(
                            userId,
                            objectKey
                    );

            if (affectedRows != 1) {
                // 抛出用户不存在
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            if(profileResponse.avatarUrl() != null) {
                objectStorageService.deleteQuietly(profileResponse.avatarUrl());
            }
        } catch (RuntimeException e) {
            // 数据库直接抛异常了/上面的“用户不存在” -> 清理已上传的文件
            objectStorageService.deleteQuietly(objectKey);

            // 抛出原异常
            throw e;
        }

        return new UploadAvatarResponse(objectStorageService.generatePresignedUrl(objectKey));
    }
}