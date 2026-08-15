alter table detail_nodes
drop foreign key fk_detail_nodes_parent_node;

alter table detail_nodes
add CONSTRAINT fk_detail_nodes_parent_node
foreign key (parent_id)
references detail_nodes (id)
on delete cascade;