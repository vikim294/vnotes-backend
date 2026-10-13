alter table outbox_events
    add column claimed_at datetime(6) null after created_at,
    add column claim_version bigint not null default 0 after claimed_at,
    add index idx_outbox_events_status_created_id (status, created_at, id),
    drop index idx_outbox_events_status;
