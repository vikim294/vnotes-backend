create table note_access_history
(
    id                bigint primary key auto_increment,
    user_id           bigint not null,
    directory_node_id bigint not null,
    accessed_at       datetime(6) not null default current_timestamp(6),

    constraint fk_note_access_history_user foreign key (user_id) references users (id) on delete cascade,
    constraint fk_note_access_history_directory_node foreign key (directory_node_id) references directory_nodes (id) on delete cascade,
    constraint uk_note_access_history_user_node unique (user_id, directory_node_id),

    index             idx_note_access_history_user (user_id, accessed_at desc, id desc)
);
