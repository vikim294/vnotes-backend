alter table note_share_access_logs
    add column visitor_id char(36) null after target_id;
