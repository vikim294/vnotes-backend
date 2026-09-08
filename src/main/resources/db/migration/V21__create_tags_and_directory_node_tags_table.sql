create table tags
(
    id         bigint primary key auto_increment,
    user_id    bigint       not null,
    name       varchar(100) not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),

    constraint fk_tags_user foreign key (user_id) references users (id),
    constraint uk_user_tag UNIQUE (user_id, name)
);

create table directory_node_tags
(
    id                bigint primary key auto_increment,
    directory_node_id bigint not null,
    tag_id            bigint not null,
    created_at        datetime(6) not null default current_timestamp(6),

    constraint fk_directory_node_tags_directory_node foreign key (directory_node_id) references directory_nodes (id) on delete cascade,
    constraint fk_directory_node_tags_tag foreign key (tag_id) references tags (id) on delete cascade,

    constraint uk_node_tag UNIQUE (directory_node_id, tag_id),

    index             idx_directory_node_tags_tag (tag_id)
);
