-- Performance indexes for frequent queries
create index idx_transports_status on transports(status_id);
create index idx_transports_type on transports(transport_type);
create index idx_transports_city on transports(city_id);
create index idx_transports_location on transports using gist(st_point(longitude, latitude));

create index idx_users_role on users(role_id);
create index idx_users_city on users(city_id);
create index idx_users_email on users(email);

create index idx_rentals_user on rentals(user_id);
create index idx_rentals_transport on rentals(transport_id);
create index idx_rentals_status on rentals(status_id);
create index idx_rentals_start_time on rentals(start_time);
