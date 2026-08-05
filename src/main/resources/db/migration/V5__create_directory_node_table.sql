create table directory_nodes
(
    id         bigint primary key auto_increment,
    user_id    bigint         not null,
    label      varchar(255)   not null,
    x          decimal(12, 2) not null,
    y          decimal(12, 2) not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),

    constraint fk_directory_nodes_user foreign key (user_id) references users (id),

    index idx_directory_nodes_user_id (user_id)
);
