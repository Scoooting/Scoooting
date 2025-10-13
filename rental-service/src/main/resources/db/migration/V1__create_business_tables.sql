-- Reference table for rental statuses
create table rental_statuses (
    id bigserial primary key,
    name varchar(50) not null unique
);

-- Rentals
create table rentals (
    id bigserial primary key,
    user_id bigint not null,        -- removed foreign keys
    transport_id bigint not null,   -- removed foreign keys
    status_id bigint not null references rental_statuses(id),
    start_time timestamp not null default now(),
    end_time timestamp,
    start_latitude real not null,
    start_longitude real not null,
    end_latitude real,
    end_longitude real,
    total_cost decimal(10,2),
    duration_minutes integer,
    distance_km decimal(8,2)
);

-- Many-to-Many: User favorite transports
create table user_favorite_transports (
    id bigserial primary key,
    user_id bigint not null,        -- removed foreign keys
    transport_id bigint not null,
    added_at timestamp not null default now(),
    note varchar(255),
    unique(user_id, transport_id)
);

-- Many-to-Many: Maintenance records
create table maintenance_records (
    id bigserial primary key,
    transport_id bigint not null,
    operator_id bigint not null,        -- removed foreign keys
    maintenance_type varchar(100) not null,
    description text,
    cost decimal(10,2),
    performed_at timestamp not null default now()
);