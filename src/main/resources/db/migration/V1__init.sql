create extension if not exists postgis;

create table if not exists users (
    id bigserial primary key,
    email character varying(64) NOT NULL,
    name character varying(32) NOT NULL,
    password character varying(128) NOT NULL,
    bonuses integer NOT NULL DEFAULT 0,
    role character varying(16) NOT NULL
);

create table if not exists scooters (
    id bigserial primary key,
    model varchar(32) NOT NULL,
    status varchar(32) NOT NULL,
    latitude real,
    longitude real
);

create table area (
    id bigserial primary key,
    city varchar(32) not null,
    district varchar(32) not null
)