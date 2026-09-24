package com.vikim.demo.admin.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateRoleMapperParam {
    private Long id;

    private String name;

    private String description;
}
