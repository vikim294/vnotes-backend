create table detail_nodes
(
    id         bigint primary key auto_increment,
    parent_id   bigint null,
    directory_node_id  bigint not null,
    label      varchar(255)   not null,
    x          decimal(12, 2) not null,
    y          decimal(12, 2) not null,
    created_at datetime(6) not null default current_timestamp(6),
    updated_at datetime(6) not null default current_timestamp(6) on update current_timestamp(6),
    deleted_at datetime(6) null,

    constraint fk_detail_nodes_parent_node foreign key (parent_id) references detail_nodes (id),
    constraint fk_detail_nodes_directory_node foreign key (directory_node_id) references directory_nodes (id),

    index idx_detail_nodes_directory_node_id (directory_node_id),
    index idx_detail_nodes_parent_id (parent_id)
);
