create table if not exists transport_statuses (
    id bigserial primary key,
    name varchar(32) not null unique
);

create table if not exists transports (
    id bigserial primary key ,
    type varchar(32) not null,
    status_id bigint references transport_statuses(id),
    city_id bigint references cities(id),
    latitude real not null,
    longitude real not null
);

create index idx_transports_type on transports(type);
create index idx_transports_status on transports(status_id);
create index idx_transports_location on transports using GIST(ST_Point(longitude, latitude));

create table if not exists electric_bicycles (
    transport_id bigint primary key references transports(id),
    model varchar(64) not null,
    battery_level decimal not null default 100,
    gear_count integer default 7
);

create table if not exists gas_motorcycles (
    transport_id bigint primary key references transports(id),
    model varchar(64) not null,
    fuel_level decimal not null default 100,
    engine_size integer not null
);

create table if not exists electric_kick_scooters (
    transport_id bigserial primary key references transports(id),
    model varchar(64) NOT NULL,
    battery_level decimal not null default 100,
    max_speed integer not null default 25
);

create table if not exists electric_scooters (
    transport_id bigserial primary key references transports(id),
    model varchar(64) NOT NULL,
    battery_level decimal not null default 100,
    max_speed integer not null default 45,
    has_storage_box boolean not null
);
