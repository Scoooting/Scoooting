-- Add transport type enum
CREATE TYPE transport_type AS ENUM ('SCOOTER', 'BICYCLE', 'E_BIKE', 'MOTORCYCLE');

-- Add rental status enum
CREATE TYPE rental_status AS ENUM ('ACTIVE', 'COMPLETED', 'CANCELLED');

-- Add rentals table (references existing scooters table)
create table if not exists rentals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    transport_id BIGINT NOT NULL,
    transport_type VARCHAR(32) NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT NOW(),
    end_time TIMESTAMP,
    start_latitude REAL NOT NULL,
    start_longitude REAL NOT NULL,
    end_latitude REAL,
    end_longitude REAL,
    total_cost DECIMAL(10,2),
    duration_minutes INTEGER,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_rentals_user_id ON rentals(user_id);
CREATE INDEX idx_rentals_transport ON rentals(transport_id, transport_type);
CREATE INDEX idx_rentals_status ON rentals(status);