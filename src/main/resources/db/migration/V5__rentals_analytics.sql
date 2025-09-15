-- Add some completed rentals for analytics testing
INSERT INTO rentals (user_id, transport_id, transport_type, start_time, end_time,
                    start_latitude, start_longitude, end_latitude, end_longitude,
                    total_cost, duration_minutes, status)
SELECT
    (u.id),
    (t.id),
    t.type,
    NOW() - INTERVAL '1 day' * (gs % 30 + 1),
    NOW() - INTERVAL '1 day' * (gs % 30 + 1) + INTERVAL '1 hour' * (gs % 3 + 1),
    59.9 + (random() * 0.1),
    30.3 + (random() * 0.1),
    59.9 + (random() * 0.1),
    30.3 + (random() * 0.1),
    5.50 + (random() * 10),
    30 + (gs % 60),
    'COMPLETED'
FROM generate_series(1, 20) gs
CROSS JOIN (SELECT id FROM users LIMIT 3) u
CROSS JOIN (SELECT id, type FROM transports LIMIT 2) t
WHERE NOT EXISTS (
    SELECT 1 FROM rentals
    WHERE status = 'COMPLETED'
    AND start_time > NOW() - INTERVAL '30 days'
    LIMIT 10
);