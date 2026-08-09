alter table detail_nodes
    add column content_type varchar(50) not null default 'TEXT' after y;