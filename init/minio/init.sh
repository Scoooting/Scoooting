mc alias set minio http://minio:9000 $MINIO_ROOT_USER $MINIO_ROOT_PASSWORD

mc mb minio/user-files/reports
mc mb minio/default

mc cp /init/files/* minio/user-files/reports