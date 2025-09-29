insert into user_roles (name) values
                                  ('USER'), ('ADMIN'), ('OPERATOR'), ('SUPPORT');

insert into cities (name, latitude_min, longitude_min, latitude_max, longitude_max)
values ('SPB', 59.823535, 30.184844, 60.041664, 30.431322);

insert into users (email, name, password, role_id, city_id) values (
   '${adminEmail}', '${adminUsername}', '${adminPassword}',
   (select id from user_roles where name = 'ADMIN'),
   (select id from cities where name = 'SPB')
);

insert into users (email, name, password, role_id, city_id)
select
            'user' || gs || '@example.com',
            'User_' || gs,
            'password' || gs,
            (select id from user_roles where name = 'USER'),
            (select id from cities where name = 'SPB')
from generate_series(1, 102) as gs;
