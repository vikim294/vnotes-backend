package com.vikim.demo.user;


import com.vikim.demo.common.*;
import com.vikim.demo.mq.events.UserRegisteredEvent;
import com.vikim.demo.objectStorage.ObjectStorageService;
import com.vikim.demo.outbox.CreateOutboxEventMapperParam;
import com.vikim.demo.outbox.OutboxEventMapper;
import com.vikim.demo.outbox.OutboxEventStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final OutboxEventMapper outboxEventMapper;
    private final ObjectMapper objectMapper;

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

    // 用户注册成功发送站内信：先不管通过 google 登录的新用户
    @Transactional
    public void registerAccount(RegisterAccountRequest request) {
        Optional<User> user = userMapper.findByEmail(request.email());
        if (user.isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        RegisterAccountMapperParam param = new RegisterAccountMapperParam();
        param.setUsername(request.username());
        param.setPasswordHash(passwordHash);
        param.setEmail(request.email());

        userMapper.registerAccount(param);

        Long userId = param.getId();

        // --- insert outbox_events 表，之后通过定时轮询发 mq 事件
        String eventId = UUID.randomUUID().toString();

        UserRegisteredEvent event =
                new UserRegisteredEvent(
                        eventId,
                        userId
                );

        outboxEventMapper.createOutboxEvent(new CreateOutboxEventMapperParam(
                eventId,
                VnotesEvent.USER_REGISTERED,
                objectMapper.writeValueAsString(event),
                OutboxEventStatus.PENDING
        ));
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

            if (profileResponse.avatarUrl() != null) {
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