package com.vikim.demo.user;


import com.vikim.demo.common.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;
    private final FileUploadProperties fileUploadProperties;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, FileService fileService, FileUploadProperties fileUploadProperties) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.fileUploadProperties = fileUploadProperties;
    }

    public List<UserResponse> findAll() {
        return userMapper.findAll().stream()
                .map(UserResponse::from)
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

    public UploadAvatarResponse uploadAvatar(Long userId, MultipartFile file) {
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

            throw e;
        }

        return new UploadAvatarResponse(fileUrl);
    }
}