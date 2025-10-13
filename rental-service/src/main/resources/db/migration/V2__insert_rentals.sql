-- Insert rental statuses
insert into rental_statuses (name) values
                                       ('ACTIVE'),
                                       ('COMPLETED'),
                                       ('CANCELLED');

insert into rentals (user_id, transport_id, status_id, start_time, end_time, start_latitude, start_longitude, end_latitude, end_longitude, total_cost, duration_minutes, distance_km)
values
    (1, 1, 2, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 45 minutes', 59.9311, 30.3609, 59.9342, 30.3656, 7.50, 15, 0.5),
    (2, 2, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours 30 minutes', 59.9370, 30.3550, 59.9400, 30.3500, 15.00, 30, 0.8),
    (3, 3, 2, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 23 hours', 59.9250, 30.3400, 59.9300, 30.3450, 10.00, 20, 0.6),
    (4, 31, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours', 59.9311, 30.3609, 59.9280, 30.3580, 12.00, 60, 1.2),
    (5, 32, 2, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day 22 hours', 59.9350, 30.3600, 59.9380, 30.3650, 8.00, 40, 0.9),
    (6, 31, 2, NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours 30 minutes', 59.9200, 30.3300, 59.9500, 30.3800, 45.00, 30, 3.5),
    (7, 4, 1, NOW() - INTERVAL '10 minutes', NULL, 59.9311, 30.3609, NULL, NULL, NULL, NULL, NULL);