create table photo_type(id serial primary key, name varchar(32));

create table photo_status(id serial primary key, name varchar(32));

create table transport_photos (
    user_id bigint,
    date timestamp,
    photo_type int references photo_type(id)
);