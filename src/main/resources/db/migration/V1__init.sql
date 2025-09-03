CREATE TABLE IF NOT EXISTS public.users (
    id bigserial primary key,
    email character varying(64) NOT NULL,
    name character varying(32) NOT NULL,
    password character varying(128) NOT NULL,
    bonuses integer NOT NULL DEFAULT 0,
    role character varying(16) NOT NULL
);