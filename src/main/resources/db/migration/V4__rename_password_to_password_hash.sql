alter table users
    change column password password_hash varchar(255) not null;