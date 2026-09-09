create table note_shares
(
    id                bigint primary key auto_increment,
    directory_node_id bigint       not null unique,
    share_token       char(36) not null unique,
    status            varchar(32)  not null,
    created_at        datetime(6) not null default current_timestamp(6),
    updated_at        datetime(6) not null default current_timestamp(6) on update current_timestamp(6),

    constraint fk_note_shares_directory_node foreign key (directory_node_id) references directory_nodes (id) on delete cascade
);

create table note_share_access_logs
(
    id            bigint primary key auto_increment,
    note_share_id bigint not null,
    created_at    datetime(6) not null default current_timestamp(6),

    constraint fk_note_share_access_logs_note_share foreign key (note_share_id) references note_shares (id) on delete cascade,

    index         idx_note_share_access_logs_note_share (note_share_id)
);
