package com.vikim.vnotes.admin.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminUserMapperParam {
        private Long id;

        private String email;

        private String passwordHash;

        private AdminUserStatus status;
}
