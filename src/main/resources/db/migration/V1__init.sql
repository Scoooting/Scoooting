create extension if not exists postgis;

create table user_roles (
    id bigserial primary key,
    name varchar(32) not null
);

create table if not exists cities (
    id bigserial primary key,
    name varchar(32) not null,
    latitude_min real not null,
    longitude_min real not null,
    latitude_max real not null,
    longitude_max real not null
);

create table if not exists users (
    id bigserial primary key,
    email character varying(64) NOT NULL,
    name character varying(32) NOT NULL,
    password character varying(128) NOT NULL,
    role_id bigint references user_roles(id),
    city_id bigint references cities(id),
    bonuses integer NOT NULL DEFAULT 0
);

create function earth_distance(lat1 real, lon1 real, lat2 real, lon2 real)
    returns real
    language sql
    return (SELECT st_distance(
        ST_SetSRID(ST_MakePoint(lat1, lon1), 4326)::geography,
        ST_SetSRID(ST_MakePoint(lat2, lon2), 4326)::geography));
