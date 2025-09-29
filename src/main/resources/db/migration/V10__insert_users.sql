insert into users (email, name, password_hash, role_id, city_id)
select
    'user' || gs || '@example.com',
    'User_' || gs,
     -- bcrypt hash for "password123"
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    (select id from user_roles where name = 'USER'),
    (select id from cities where name = 'SPB')
from generate_series(1, 102) as gs;