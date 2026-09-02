create table outbox_events
(
    id         bigint primary key auto_increment,
    event_id   char(36)     not null unique,
    event_name varchar(100) not null,
    event_json json         not null,
    status     varchar(100) not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),

    index      idx_outbox_events_status (status)
);
