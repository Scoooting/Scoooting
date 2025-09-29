-- Distance calculation function (already exists, but clean version)
create or replace function earth_distance(lat1 real, lon1 real, lat2 real, lon2 real)
returns real
language sql
immutable
as $$
    select st_distance(
        st_setsrid(st_makepoint(lon1, lat1), 4326)::geography,
        st_setsrid(st_makepoint(lon2, lat2), 4326)::geography
    );
$$;

-- Function to check if transport is in city
create or replace function is_transport_in_city(
    transport_lat real,
    transport_lng real,
    city_id bigint
) returns boolean
language sql
stable
as $$
    select exists(
        select 1 from cities c
        where c.id = $3
        and earth_distance($1, $2, c.center_latitude, c.center_longitude) <= c.radius_km * 1000
    );
$$;
