-- Users table
create table users (
    id bigserial primary key,
    email varchar(255) not null unique,
    name varchar(100) not null,
    password_hash varchar(255) not null,
    role_id bigint not null references user_roles(id),
    bonuses integer not null default 0
);

-- Master transports table (coords stored here only)
create table transports (
    id bigserial primary key,
    transport_type varchar(50) not null, -- enum in java bcs changes rarely
    status_id bigint not null references transport_statuses(id),
    city_id bigint references cities(id), -- null if outside city
    latitude real not null,
    longitude real not null
);