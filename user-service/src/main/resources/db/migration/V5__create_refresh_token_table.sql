create table if not exists refresh_tokens(
    user_id bigint primary key references users(id) on delete cascade,
    token varchar(256)
);