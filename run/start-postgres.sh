#!/bin/bash
docker run \
    --name myPostgresDb \
    -p 5432:5432 \
    -e POSTGRES_PASSWORD=postgresPW \
    -d \
    postgres

export PGPASSWORD=postgresPW
echo "Wait 4s"
sleep 4
psql --host localhost --user postgres  -c "create database hapi"