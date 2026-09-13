alter table note_share_access_logs
    add column target varchar(64) null after note_share_id,
    add column target_id bigint null after target;

update note_share_access_logs logs
join note_shares ns on ns.id = logs.note_share_id
set logs.target = 'NOTE',
    logs.target_id = ns.directory_node_id;

alter table note_share_access_logs
    modify column target varchar(64) not null,
    modify column target_id bigint not null;
