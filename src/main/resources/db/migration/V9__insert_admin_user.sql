-- Create admin user
insert into users (email, name, password_hash, role_id, city_id, bonuses)
select
    '${adminEmail}',
    '${adminUsername}',
    '${adminPassword}',
    r.id,
    c.id,
    0
from user_roles r, cities c
where r.name = 'ADMIN'
and c.name = 'SPB';