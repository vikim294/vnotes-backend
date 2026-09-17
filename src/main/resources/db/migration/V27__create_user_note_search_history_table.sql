create table note_search_history
(
    id         bigint primary key auto_increment,
    user_id    bigint       not null,
    keyword    varchar(255) not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6),

    constraint fk_note_search_history_user foreign key (user_id) references users (id) on delete cascade,
    constraint uk_note_search_history_user_keyword UNIQUE (user_id, keyword),

    index idx_note_search_history_recent (user_id, updated_at desc, id desc)
);
