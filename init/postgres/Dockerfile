FROM postgres:16

COPY init_db.sql /docker-entrypoint-initdb.d/

RUN chmod 644 /docker-entrypoint-initdb.d/init_db.sql
