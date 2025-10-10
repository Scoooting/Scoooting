-- Users table
create table users (
   id bigserial primary key,
   email varchar(255) not null unique,
   name varchar(100) not null,
   password_hash varchar(255) not null,
   role_id bigint not null references user_roles(id),
   city_id bigint references cities(id),
   bonuses integer not null default 0
);
