alter table directory_nodes
    add index idx_directory_nodes_search(user_id, deleted_at, created_at, id),
    drop index idx_directory_nodes_user_id;
