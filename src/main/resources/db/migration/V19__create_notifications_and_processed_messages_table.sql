create table notifications
(
    id         bigint primary key auto_increment,
    type       varchar(100) not null,
    user_id    bigint       not null,
    title      varchar(100) not null,
    content    text         not null,
    read_at    datetime(6) null,
    has_read   boolean      not null default false,
    created_at datetime(6) not null default current_timestamp(6),
    deleted_at datetime(6)  null,


    constraint fk_notifications_user foreign key (user_id) references users (id) on delete cascade,


    index      idx_notifications_user_has_read_created_at (user_id, has_read, created_at)

);


create table processed_messages
(
    id         bigint primary key auto_increment,
    event_id   char(36)     not null,
    consumer   varchar(255) not null,
    created_at datetime(6) not null default current_timestamp(6),

    CONSTRAINT uk_event_consumer UNIQUE (event_id, consumer)
);





