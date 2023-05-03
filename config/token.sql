create table token
(
    id         serial,
    expired    boolean      not null,
    revoked    boolean      not null,
    token      varchar(255) not null UNIQUE,
    token_type varchar(255) not null,
    user_id    integer
        constraint token_users_id_fk
            references users
);

