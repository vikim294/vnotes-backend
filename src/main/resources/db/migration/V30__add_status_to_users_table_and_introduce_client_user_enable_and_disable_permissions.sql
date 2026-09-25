alter table users
    add column status varchar(50) not null default 'ACTIVE' after avatar_url;

-- seed 权限表
insert into admin_permissions(code, name)
values
       ('client:user:read', 'client/客户端用户/查阅'),
       ('client:user:enable_and_disable', 'client/客户端用户/启用禁用');

-- seed 角色-权限表
insert into admin_role_permissions(role_id, permission_id)
select r.id, p.id
from admin_roles r
         join admin_permissions p
              on (r.code, p.code) IN (
                                      ('admin.super_admin', 'client:user:read'),
                                      ('admin.super_admin', 'client:user:enable_and_disable')
                  );
