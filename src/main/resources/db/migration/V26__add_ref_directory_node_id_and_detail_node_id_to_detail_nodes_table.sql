alter table detail_nodes
    add column ref_directory_node_id bigint null after mark_type,
    add column ref_detail_node_id bigint null after ref_directory_node_id,

    add constraint fk_detail_nodes_ref_directory foreign key (ref_directory_node_id) references directory_nodes (id) on delete set null,
    add constraint fk_detail_nodes_ref_detail foreign key (ref_detail_node_id) references detail_nodes (id) on delete set null;
