-- Reference table for user roles
create table user_roles (
    id bigserial primary key,
    name varchar(50) not null unique
);

-- Cities with circular boundaries
create table cities (
    id bigserial primary key,
    name varchar(100) not null,
    center_latitude real not null,
    center_longitude real not null,
    radius_km integer not null default 25
);