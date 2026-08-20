alter table directory_nodes
drop foreign key fk_directory_nodes_parent;

alter table directory_nodes
add CONSTRAINT fk_directory_nodes_parent
foreign key (parent_id)
references directory_nodes (id)
on delete cascade;
