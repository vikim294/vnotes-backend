alter table directory_nodes
    add column node_type varchar(50)
        not null
        default 'DIRECTORY'
        after y;