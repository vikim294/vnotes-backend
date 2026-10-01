alter table note_share_access_logs
    ADD INDEX idx_note_share_access_logs_note_share_created_id (note_share_id, created_at, id),
    DROP INDEX idx_note_share_access_logs_note_share;