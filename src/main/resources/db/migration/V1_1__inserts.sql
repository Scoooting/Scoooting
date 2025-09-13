insert into users (email, name, password, role) values (
    '${adminEmail}', '${adminUsername}', '${adminPassword}', 'ADMIN'
);

insert into users (email, name, password, role)
select 
    'user' || gs || '@example.com',
    'User_' || gs,
    'password' || gs,
    'USER'
from generate_series(1, 102) as gs;

insert into cities (name, latitude_min, longitude_min, latitude_max, longitude_max)
values ('Санкт-Петербург', 59.823535, 30.184844, 60.041664, 30.431322);

insert into scooters (model, status)
    select 'Urent 10A8E', 'NONACTIVE' from generate_series(1, 50);
