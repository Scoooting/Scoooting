-- Electric kick scooters (standing, like Ninebot)
create table electric_kick_scooters (
    transport_id bigint primary key references transports(id) on delete cascade,
    model varchar(100) not null,
    battery_level decimal(5,2) default 100.0,
    max_speed integer -- km/h
);

-- Electric scooters (seated, like delivery)
create table electric_scooters (
    transport_id bigint primary key references transports(id) on delete cascade,
    model varchar(100) not null,
    battery_level decimal(5,2) default 100.0,
    max_speed integer, -- km/h
    has_storage_box boolean default true
);

-- Electric bicycles
create table electric_bicycles (
    transport_id bigint primary key references transports(id) on delete cascade,
    model varchar(100) not null,
    battery_level decimal(5,2) default 100.0,
    gear_count integer default 7
);

-- Gas motorcycles
create table gas_motorcycles (
    transport_id bigint primary key references transports(id) on delete cascade,
    model varchar(100) not null,
    fuel_level decimal(5,2) default 100.0,
    engine_size integer -- cc
);