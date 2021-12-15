-- auto-generated definition
create table teachers
(
    id          varchar not null
        constraint teachers_pk
            primary key,
    salary      integer,
    hireDate  varchar,
    counter     integer,
    name        varchar,
    firstName varchar,
    birthDate varchar,
    gender      varchar
);

alter table teachers
    owner to platform;

create unique index teachers_id_uindex
    on teachers (id);