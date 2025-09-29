-- Insert user roles
insert into user_roles (name) values
    ('USER'),
    ('OPERATOR'),
    ('SUPPORT'),
    ('ADMIN');

-- Insert transport statuses
insert into transport_statuses (name) values
    ('AVAILABLE'),
    ('IN_USE'),
    ('UNAVAILABLE');

-- Insert rental statuses
insert into rental_statuses (name) values
    ('ACTIVE'),
    ('COMPLETED'),
    ('CANCELLED');

-- Insert cities
insert into cities (name, center_latitude, center_longitude, radius_km) values
    ('SPB', 59.9311, 30.3609, 25),
    ('MSK', 55.7558, 37.6176, 30);