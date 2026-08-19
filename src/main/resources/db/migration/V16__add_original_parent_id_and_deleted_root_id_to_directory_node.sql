alter table directory_nodes
    add column original_parent_id bigint null after deleted_at,
    add column deleted_root_id bigint null after original_parent_id,

    add index idx_directory_nodes_deleted_root_id (deleted_root_id);
