ALTER TABLE rentals
    ALTER COLUMN start_time TYPE TIMESTAMP WITH TIME ZONE USING start_time AT TIME ZONE 'UTC';

ALTER TABLE rentals
    ALTER COLUMN end_time TYPE TIMESTAMP WITH TIME ZONE USING end_time AT TIME ZONE 'UTC';

UPDATE rentals
SET start_time = start_time AT TIME ZONE 'UTC',
    end_time = end_time AT TIME ZONE 'UTC'
WHERE start_time IS NOT NULL;