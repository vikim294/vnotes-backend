delete from detail_nodes
where deleted_at is not null;

alter table detail_nodes
drop column deleted_at;