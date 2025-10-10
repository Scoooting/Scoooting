do $$
    declare
        transport_name text;
        transport_types text[] := array['ELECTRIC_BICYCLE', 'GAS_MOTORCYCLE',
            'ELECTRIC_KICK_SCOOTER', 'ELECTRIC_SCOOTER'];
    begin
        foreach transport_name in array transport_types
            loop
                insert into transports (transport_type, status_id, city_id, latitude, longitude)
                select
                    transport_name,
                    (select id from transport_statuses where name = 'AVAILABLE'),
                    0,
                    59.823535 + (random() * (60.041664 - 59.823535)),
                    30.184844 + (random() * (30.431322 - 30.184844))
                from generate_series(1, 30);
            end loop;
    end;
$$;

insert into electric_bicycles (transport_id, model, battery_level, gear_count)
select
    gs,
    case
        when gs % 4 = 0 then 'Trek FX ' || (gs % 3 + 1)
        when gs % 4 = 1 then 'Giant Escape ' || (gs % 2 + 1)
        when gs % 4 = 2 then 'RadRunner Plus'
        else 'Specialized Sirrus'
    end,
    20 + (random() * 80),
    case when random() > 0.7 then 21 else 7 end
from generate_series(1, 30) gs;

insert into gas_motorcycles (transport_id, model, fuel_level, engine_size)
select
    gs + 30,
    case
        when gs % 3 = 0 then 'Honda CB' || (125 + (gs % 4) * 125)
        when gs % 3 = 1 then 'Yamaha YBR' || (125 + (gs % 3) * 125)
        else 'BMW G 310 R'
    end,
    20 + (random() * 80),
    125 + (gs % 6) * 125
from generate_series(1, 30) gs;

insert into electric_kick_scooters (transport_id, model, battery_level)
select
    gs + 60,
    'Urent 10A8E',
    20 + (random() * 80)
from generate_series(1, 30) gs;

insert into electric_scooters (transport_id, model, battery_level, has_storage_box)
select
    gs + 90,
    case
        when gs % 4 = 0 then 'Xiaomi Mi ' || (gs % 5 + 1)
        when gs % 4 = 1 then 'Ninebot ES' || (gs % 3 + 1)
        when gs % 4 = 2 then 'Bird One'
        else 'Lime Gen4'
    end,
    20 + (random() * 80),
    false
from generate_series(1, 30) gs;