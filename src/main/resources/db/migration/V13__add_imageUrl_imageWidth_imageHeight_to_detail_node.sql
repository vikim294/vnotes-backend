alter table detail_nodes
    modify column label text null,
    add column image_url varchar(255) null after label,
    add column image_width decimal(10, 2) null after y,
    add column image_height decimal(10, 2) null after image_width;
