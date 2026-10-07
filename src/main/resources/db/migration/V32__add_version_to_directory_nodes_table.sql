alter table directory_nodes
    add column version bigint not null default 0 after parent_id;
