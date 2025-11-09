    create table rental_daily_stats (
        date date primary key,
        total_starts integer not null default 0,
        total_ends integer not null default 0,
        total_cancels integer not null default 0,
        average_duration decimal(10, 2) not null default 0,
        average_distance decimal(10, 2) not null default 0,
        min_duration decimal(10, 2) not null default 0,
        max_duration decimal(10, 2) not null default 0,
        min_distance decimal(10, 2) not null default 0,
        max_distance decimal(10, 2) not null default 0
    );