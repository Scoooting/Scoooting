create table rental_statuses (
    id bigserial primary key,
    name varchar(32) not null
);

-- Add rentals table (references existing scooters table)
create table if not exists rentals (
    id bigserial primary key ,
    user_id bigint references users(id),
    transport_id bigint references transports(id),
    status_id bigint references rental_statuses(id),
    start_time timestamp not null default NOW(),
    end_time timestamp,
    start_latitude real not null,
    start_longitude real not null ,
    end_latitude real,
    end_longitude real,
    total_cost decimal(10,2),
    duration_minutes integer,
    distance decimal
);

insert into rental_statuses(name) values
    ('ACTIVE'), ('COMPLETED'), ('CANCELLED');


-- Add some sample rentals for testing analytics
INSERT INTO rentals (user_id, transport_id, status_id, start_time, end_time, start_latitude, start_longitude, end_latitude, end_longitude, total_cost, duration_minutes, distance)
VALUES
    -- Completed electric bicycles rentals
    (1, 1, 2, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 45 minutes', 59.9311, 30.3609, 59.9342, 30.3656, 7.50, 15, 5),
    (2, 2, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours 30 minutes', 59.9370, 30.3550, 59.9400, 30.3500, 15.00, 30, 5),
    (3, 3, 2, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 23 hours', 59.9250, 30.3400, 59.9300, 30.3450, 10.00, 20, 5),

    -- Completed motorcycles rentals
    (4, 31, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours', 59.9311, 30.3609, 59.9280, 30.3580, 12.00, 60, 5),
    (5, 32, 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day 22 hours', 59.9350, 30.3600, 59.9380, 30.3650, 8.00, 40, 5),

    (6, 31, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours 30 minutes', 59.9200, 30.3300, 59.9500, 30.3800, 45.00, 30, 5),

    -- Active rental
    (7, 4, 1, NOW() - INTERVAL '10 minutes', NULL, 59.9311, 30.3609, NULL, NULL, NULL, NULL, NULL);