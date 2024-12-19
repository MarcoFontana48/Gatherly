FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=password
ENV MYSQL_DATABASE=user

COPY ./init.sql /docker-entrypoint-initdb.d/

EXPOSE 3306