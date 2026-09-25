package com.vikim.demo.admin.audit;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminAuditMapper {

    @Insert("""
            insert into admin_audit_logs(user_id, action, description)
            values(#{userId}, #{action}, #{description})
            """)
    int createAuditLog(CreateAuditLogParam param);

    // 暂不分页
    @Select("""
            select id, user_id, action, description, created_at
            from admin_audit_logs
            order by created_at desc, id desc
            """)
    List<GetAuditLogRow> getAuditLogs();
}
