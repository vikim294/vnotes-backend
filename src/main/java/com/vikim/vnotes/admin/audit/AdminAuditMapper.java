package com.vikim.vnotes.admin.audit;

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

    @Select("""
            select
                log.id,
                log.user_id,
                u.email,
                log.action,
                log.description,
                log.created_at
            from admin_audit_logs log
            left join admin_users u on u.id = log.user_id
            order by log.created_at desc, log.id desc
            """)
    List<GetAuditLogForExportRow> getAuditLogsForExport();
}
