-- Create admin user
insert into users (email, name, password_hash, role_id, city_id, bonuses)
select
    '${adminEmail}',
    '${adminUsername}',
    '${adminPassword}',
    (select id from user_roles where name = 'ADMIN'),
    (select id from cities where name = 'SPB'),
    0
from user_roles r, cities c
where r.name = 'ADMIN'
and c.name = 'SPB';