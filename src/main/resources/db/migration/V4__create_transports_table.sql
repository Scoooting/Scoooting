CREATE TABLE transports (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'FREE',
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    battery_level DECIMAL(5,2),
    serial_number VARCHAR(255),
    last_maintenance_date TIMESTAMP
);

-- Add indexes for performance
CREATE INDEX idx_transports_type ON transports(type);
CREATE INDEX idx_transports_status ON transports(status);
CREATE INDEX idx_transports_location ON transports USING GIST(ST_Point(longitude, latitude));