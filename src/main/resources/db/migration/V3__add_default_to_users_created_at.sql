alter table users
    modify column created_at datetime(6)
        not null default current_timestamp(6);