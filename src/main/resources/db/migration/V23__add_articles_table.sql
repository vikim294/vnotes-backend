create table articles
(
    id             bigint primary key auto_increment,
    detail_node_id bigint not null unique,
    content        MEDIUMTEXT null,
    created_at     datetime(6) not null default current_timestamp(6),
    updated_at     datetime(6) not null default current_timestamp(6) on update current_timestamp(6),

    constraint fk_articles_detail_node foreign key (detail_node_id) references detail_nodes (id) on DELETE cascade
);
