create table users
(
    id         bigint primary key auto_increment,
    username   varchar(50)  not null,
    password   varchar(100) not null,
    created_at datetime(6) not null
);
