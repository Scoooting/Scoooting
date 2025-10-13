create table transport_statuses (
    id bigserial primary key,
    name varchar(50) not null unique
);

-- Master transports table (coords stored here only)
create table transports (
    id bigserial primary key,
    transport_type varchar(50) not null, -- enum in java bcs changes rarely
    status_id bigint not null references transport_statuses(id),
    city_id bigint not null,           -- removed foreign keys
    latitude real not null,
    longitude real not null
);