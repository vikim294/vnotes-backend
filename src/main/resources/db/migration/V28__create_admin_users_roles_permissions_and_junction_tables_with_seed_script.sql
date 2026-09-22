create table admin_users
(
    id            bigint primary key auto_increment,
    email         varchar(255) not null unique,
    password_hash varchar(255) not null,
    status        varchar(255) not null,
    created_at    datetime(6) not null default current_timestamp(6),
    updated_at    datetime(6) not null default current_timestamp(6) on update current_timestamp(6)
);

create table admin_roles
(
    id          bigint primary key auto_increment,
    code        varchar(255) not null unique,
    name        varchar(255) not null,
    description varchar(255) null,
    created_at  datetime(6) not null default current_timestamp(6),
    updated_at  datetime(6) not null default current_timestamp(6) on update current_timestamp(6)
);

create table admin_permissions
(
    id          bigint primary key auto_increment,
    code        varchar(255) not null unique,
    name        varchar(255) not null,
    description varchar(255) null,
    created_at  datetime(6) not null default current_timestamp(6),
    updated_at  datetime(6) not null default current_timestamp(6) on update current_timestamp(6)
);

create table admin_user_roles
(
    user_id     bigint not null,
    role_id     bigint not null,
    assigned_by bigint null,
    assigned_at datetime(6) not null default current_timestamp(6),

    constraint pk_admin_user_roles_user_role PRIMARY KEY (user_id, role_id),
    constraint fk_admin_user_roles_user foreign key (user_id) references admin_users (id) on delete cascade,
    constraint fk_admin_user_roles_role foreign key (role_id) references admin_roles (id) on delete cascade,
    constraint fk_admin_user_roles_assigner foreign key (assigned_by) references admin_users (id)
);

create table admin_role_permissions
(
    role_id       bigint not null,
    permission_id bigint not null,
    created_at    datetime(6) not null default current_timestamp(6),

    constraint pk_admin_role_permissions_role_permission PRIMARY KEY (role_id, permission_id),
    constraint fk_admin_role_permissions_role foreign key (role_id) references admin_roles (id) on delete cascade,
    constraint fk_admin_role_permissions_permission foreign key (permission_id) references admin_permissions (id) on delete cascade
);

-- 审计日志表
create table admin_audit_logs
(
    id          bigint primary key auto_increment,
    user_id     bigint       not null,
    action      varchar(255) not null,
    description varchar(255) null,
    created_at  datetime(6) not null default current_timestamp(6),

    constraint fk_admin_audit_logs_user foreign key (user_id) references admin_users (id)
);

-- seed 权限表
insert into admin_permissions(code, name)
values ('admin:user:read', 'admin/用户/查阅'),
       ('admin:user:add', 'admin/用户/新增'),
       ('admin:user:enable_and_disable', 'admin/用户/启用禁用'),

       ('admin:audit:read', 'admin/日志/查阅'),
       ('admin:audit:export_excel', 'admin/日志/导出 excel');

-- seed 角色表
insert into admin_roles(code, name)
values ('admin.super_admin', 'admin/超级管理员');

-- seed 角色-权限表
insert into admin_role_permissions(role_id, permission_id)
select r.id, p.id
from admin_roles r
         join admin_permissions p
              on (r.code, p.code) IN (
                                      ('admin.super_admin', 'admin:user:read'),
                                      ('admin.super_admin', 'admin:user:add'),
                                      ('admin.super_admin', 'admin:user:enable_and_disable'),
                                      ('admin.super_admin', 'admin:audit:read'),
                                      ('admin.super_admin', 'admin:audit:export_excel')
                  );
