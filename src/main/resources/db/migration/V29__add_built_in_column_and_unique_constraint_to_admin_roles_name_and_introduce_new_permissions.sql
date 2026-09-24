alter table admin_roles
    add column built_in boolean not null default false after description,
    add constraint uk_admin_roles_name unique (name);

update admin_roles
set built_in = true
where code = 'admin.super_admin';

-- seed 权限表
insert into admin_permissions(code, name)
values ('admin:user:assign_role', 'admin/用户/指定角色'),

       ('admin:role:read', 'admin/角色/查阅'),
       ('admin:role:add', 'admin/角色/新增'),
       ('admin:role:edit', 'admin/角色/编辑'),
       ('admin:role:delete', 'admin/角色/删除'),
       ('admin:role:assign_permission', 'admin/角色/指定权限'),

       ('admin:permission:read', 'admin/权限/查阅');

-- seed 角色-权限表
insert into admin_role_permissions(role_id, permission_id)
select r.id, p.id
from admin_roles r
         join admin_permissions p
              on (r.code, p.code) IN (
                                      ('admin.super_admin', 'admin:user:assign_role'),
                                      ('admin.super_admin', 'admin:role:read'),
                                      ('admin.super_admin', 'admin:role:add'),
                                      ('admin.super_admin', 'admin:role:edit'),
                                      ('admin.super_admin', 'admin:role:delete'),
                                      ('admin.super_admin', 'admin:role:assign_permission'),
                                      ('admin.super_admin', 'admin:permission:read')
                  );
