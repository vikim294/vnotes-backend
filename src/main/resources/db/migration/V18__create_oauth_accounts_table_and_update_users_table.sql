create table oauth_accounts
(
    id             bigint primary key auto_increment,
    user_id         bigint       not null,
    provider       varchar(100) not null,
    provider_user_id varchar(255) not null,

    constraint fk_oauth_accounts_user
        foreign key (user_id) references users (id),

    constraint uk_oauth_provider_user
        unique (provider, provider_user_id),

    index          idx_oauth_accounts_user_id (user_id)
);

alter table users
    modify column password_hash varchar (255) null,
    modify column email varchar (255) null;
