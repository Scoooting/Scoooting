-- Create statuses
CREATE TABLE IF NOT EXISTS transport_statuses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Create main transports table
CREATE TABLE IF NOT EXISTS transports (
    id BIGSERIAL PRIMARY KEY,
    transport_type VARCHAR(50) NOT NULL,
    status_id BIGINT NOT NULL REFERENCES transport_statuses(id),
    city_id BIGINT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL
);

-- Create type tables
CREATE TABLE IF NOT EXISTS electric_kick_scooters (
    transport_id BIGINT PRIMARY KEY REFERENCES transports(id) ON DELETE CASCADE,
    model VARCHAR(100) NOT NULL,
    battery_level DECIMAL(5,2) DEFAULT 100.0,
    max_speed INTEGER
);

CREATE TABLE IF NOT EXISTS electric_scooters (
    transport_id BIGINT PRIMARY KEY REFERENCES transports(id) ON DELETE CASCADE,
    model VARCHAR(100) NOT NULL,
    battery_level DECIMAL(5,2) DEFAULT 100.0,
    max_speed INTEGER,
    has_storage_box BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS electric_bicycles (
    transport_id BIGINT PRIMARY KEY REFERENCES transports(id) ON DELETE CASCADE,
    model VARCHAR(100) NOT NULL,
    battery_level DECIMAL(5,2) DEFAULT 100.0,
    gear_count INTEGER DEFAULT 7
);

CREATE TABLE IF NOT EXISTS gas_motorcycles (
    transport_id BIGINT PRIMARY KEY REFERENCES transports(id) ON DELETE CASCADE,
    model VARCHAR(100) NOT NULL,
    fuel_level DECIMAL(5,2) DEFAULT 100.0,
    engine_size INTEGER
);

-- Insert statuses
INSERT INTO transport_statuses (name) VALUES ('AVAILABLE') ON CONFLICT (name) DO NOTHING;
INSERT INTO transport_statuses (name) VALUES ('IN_USE') ON CONFLICT (name) DO NOTHING;
INSERT INTO transport_statuses (name) VALUES ('UNAVAILABLE') ON CONFLICT (name) DO NOTHING;