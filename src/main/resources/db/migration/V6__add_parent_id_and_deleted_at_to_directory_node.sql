alter table directory_nodes
    add column parent_id bigint null after user_id,
    add column deleted_at datetime(6) null after updated_at,

    add index idx_directory_nodes_parent_id (parent_id),
    add constraint fk_directory_nodes_parent
        foreign key (parent_id)
        references directory_nodes (id);