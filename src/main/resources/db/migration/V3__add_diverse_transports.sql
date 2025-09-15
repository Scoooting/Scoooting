-- Create bikes table
create table if not exists bikes (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'FREE',
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    gear_count INTEGER DEFAULT 7,
    is_electric BOOLEAN DEFAULT FALSE
);

-- Create motorcycles table
CREATE TABLE motorcycles (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'FREE',
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    engine_size INTEGER, -- in cc
    fuel_level DECIMAL(5,2) DEFAULT 100.0 -- percentage
);

-- Add some bikes around St. Petersburg
INSERT INTO bikes (model, status, latitude, longitude, gear_count, is_electric)
SELECT
    CASE
        WHEN gs % 4 = 0 THEN 'Trek FX ' || (gs % 3 + 1)
        WHEN gs % 4 = 1 THEN 'Giant Escape ' || (gs % 2 + 1)
        WHEN gs % 4 = 2 THEN 'RadRunner Plus'
        ELSE 'Specialized Sirrus'
    END,
    CASE
        WHEN gs % 10 = 0 THEN 'MAINTENANCE'
        WHEN gs % 15 = 0 THEN 'NONACTIVE'
        ELSE 'FREE'
    END,
    59.823535 + (random() * (60.041664 - 59.823535)),
    30.184844 + (random() * (30.431322 - 30.184844)),
    CASE WHEN random() > 0.7 THEN 21 ELSE 7 END, -- Some have more gears
    CASE WHEN gs % 4 = 2 THEN TRUE ELSE FALSE END -- Every 4th bike is electric
FROM generate_series(1, 30) gs;

-- Add some motorcycles around St. Petersburg
INSERT INTO motorcycles (model, status, latitude, longitude, engine_size, fuel_level)
SELECT
    CASE
        WHEN gs % 3 = 0 THEN 'Honda CB' || (125 + (gs % 4) * 125)
        WHEN gs % 3 = 1 THEN 'Yamaha YBR' || (125 + (gs % 3) * 125)
        ELSE 'BMW G 310 R'
    END,
    CASE
        WHEN gs % 12 = 0 THEN 'MAINTENANCE'
        WHEN gs % 20 = 0 THEN 'NONACTIVE'
        WHEN gs % 25 = 0 THEN 'LOW_FUEL'
        ELSE 'FREE'
    END,
    59.823535 + (random() * (60.041664 - 59.823535)),
    30.184844 + (random() * (30.431322 - 30.184844)),
    125 + (gs % 6) * 125, -- Engine sizes: 125, 250, 375, 500, 625, 750
    20 + (random() * 80) -- Random fuel level 20-100%
FROM generate_series(1, 15) gs;

-- Update existing scooters to have better variety and proper coordinates
UPDATE scooters SET
    model = CASE
        WHEN id % 4 = 0 THEN 'Xiaomi Mi ' || (id % 5 + 1)
        WHEN id % 4 = 1 THEN 'Ninebot ES' || (id % 3 + 1)
        WHEN id % 4 = 2 THEN 'Bird One'
        ELSE 'Lime Gen4'
    END,
    status = CASE
        WHEN id % 20 = 0 THEN 'MAINTENANCE'
        WHEN id % 30 = 0 THEN 'NONACTIVE'
        WHEN id % 40 = 0 THEN 'LOW_BATTERY'
        ELSE 'FREE'
    END,
    latitude = 59.823535 + (random() * (60.041664 - 59.823535)),
    longitude = 30.184844 + (random() * (30.431322 - 30.184844))
WHERE latitude IS NULL OR longitude IS NULL OR model = 'Urent 10A8E';

-- Add indexes for performance
CREATE INDEX idx_bikes_status ON bikes(status);
CREATE INDEX idx_bikes_location ON bikes USING GIST(ST_Point(longitude, latitude));
CREATE INDEX idx_motorcycles_status ON motorcycles(status);
CREATE INDEX idx_motorcycles_location ON motorcycles USING GIST(ST_Point(longitude, latitude));

-- Add some sample rentals for testing analytics
INSERT INTO rentals (user_id, transport_id, transport_type, start_time, end_time, start_latitude, start_longitude, end_latitude, end_longitude, total_cost, duration_minutes, status)
VALUES
    -- Completed scooter rentals
    (1, 1, 'SCOOTER', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour 45 minutes', 59.9311, 30.3609, 59.9342, 30.3656, 7.50, 15, 'COMPLETED'),
    (2, 2, 'SCOOTER', NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours 30 minutes', 59.9370, 30.3550, 59.9400, 30.3500, 15.00, 30, 'COMPLETED'),
    (3, 3, 'SCOOTER', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days 23 hours', 59.9250, 30.3400, 59.9300, 30.3450, 10.00, 20, 'COMPLETED'),

    -- Completed bike rentals
    (4, 1, 'BICYCLE', NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours', 59.9311, 30.3609, 59.9280, 30.3580, 12.00, 60, 'COMPLETED'),
    (5, 2, 'BICYCLE', NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day 22 hours', 59.9350, 30.3600, 59.9380, 30.3650, 8.00, 40, 'COMPLETED'),

    -- Completed motorcycle rental
    (6, 1, 'MOTORCYCLE', NOW() - INTERVAL '1 day', NOW() - INTERVAL '23 hours 30 minutes', 59.9200, 30.3300, 59.9500, 30.3800, 45.00, 30, 'COMPLETED'),

    -- Active rental
    (7, 4, 'SCOOTER', NOW() - INTERVAL '10 minutes', NULL, 59.9311, 30.3609, NULL, NULL, NULL, NULL, 'ACTIVE');