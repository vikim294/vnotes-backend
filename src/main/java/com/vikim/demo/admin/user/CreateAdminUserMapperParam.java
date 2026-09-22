package com.vikim.demo.admin.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAdminUserMapperParam {
        private Long id;

        private String email;

        private String passwordHash;

        private UserStatus status;
}
