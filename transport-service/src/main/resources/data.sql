-- Insert test transports (only if not exists)
INSERT INTO transports (transport_type, status_id, city_id, latitude, longitude)
SELECT 'ELECTRIC_BICYCLE',
       (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE'),
       1,
       59.823535 + (random() * 0.218129),
       30.184844 + (random() * 0.246478)
FROM generate_series(1, 30)
WHERE NOT EXISTS (SELECT 1 FROM transports WHERE transport_type = 'ELECTRIC_BICYCLE' LIMIT 1);

INSERT INTO transports (transport_type, status_id, city_id, latitude, longitude)
SELECT 'GAS_MOTORCYCLE',
       (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE'),
       1,
       59.823535 + (random() * 0.218129),
       30.184844 + (random() * 0.246478)
FROM generate_series(1, 30)
WHERE NOT EXISTS (SELECT 1 FROM transports WHERE transport_type = 'GAS_MOTORCYCLE' LIMIT 1);

INSERT INTO transports (transport_type, status_id, city_id, latitude, longitude)
SELECT 'ELECTRIC_KICK_SCOOTER',
       (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE'),
       1,
       59.823535 + (random() * 0.218129),
       30.184844 + (random() * 0.246478)
FROM generate_series(1, 30)
WHERE NOT EXISTS (SELECT 1 FROM transports WHERE transport_type = 'ELECTRIC_KICK_SCOOTER' LIMIT 1);

INSERT INTO transports (transport_type, status_id, city_id, latitude, longitude)
SELECT 'ELECTRIC_SCOOTER',
       (SELECT id FROM transport_statuses WHERE name = 'AVAILABLE'),
       1,
       59.823535 + (random() * 0.218129),
       30.184844 + (random() * 0.246478)
FROM generate_series(1, 30)
WHERE NOT EXISTS (SELECT 1 FROM transports WHERE transport_type = 'ELECTRIC_SCOOTER' LIMIT 1);

-- Insert bicycle details
INSERT INTO electric_bicycles (transport_id, model, battery_level, gear_count)
SELECT t.id, 'Trek FX 1', 50 + (random() * 50), 7
FROM transports t
WHERE t.transport_type = 'ELECTRIC_BICYCLE'
  AND NOT EXISTS (SELECT 1 FROM electric_bicycles WHERE transport_id = t.id);

-- Insert motorcycle details
INSERT INTO gas_motorcycles (transport_id, model, fuel_level, engine_size)
SELECT t.id, 'Honda CB125', 50 + (random() * 50), 125
FROM transports t
WHERE t.transport_type = 'GAS_MOTORCYCLE'
  AND NOT EXISTS (SELECT 1 FROM gas_motorcycles WHERE transport_id = t.id);

-- Insert kick scooter details
INSERT INTO electric_kick_scooters (transport_id, model, battery_level)
SELECT t.id, 'Urent 10A8E', 50 + (random() * 50)
FROM transports t
WHERE t.transport_type = 'ELECTRIC_KICK_SCOOTER'
  AND NOT EXISTS (SELECT 1 FROM electric_kick_scooters WHERE transport_id = t.id);

-- Insert scooter details
INSERT INTO electric_scooters (transport_id, model, battery_level, has_storage_box)
SELECT t.id, 'Xiaomi Mi 1', 50 + (random() * 50), false
FROM transports t
WHERE t.transport_type = 'ELECTRIC_SCOOTER'
  AND NOT EXISTS (SELECT 1 FROM electric_scooters WHERE transport_id = t.id);