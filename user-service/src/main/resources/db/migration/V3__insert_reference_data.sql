-- Insert user roles
insert into user_roles (name) values
    ('USER'),
    ('OPERATOR'),
    ('SUPPORT'),
    ('ANALYST'),
    ('ADMIN');

insert into cities (name, center_latitude, center_longitude, radius_km) values
    ('SPB', 59.9311, 30.3609, 25),
    ('MSK', 55.7558, 37.6176, 30);