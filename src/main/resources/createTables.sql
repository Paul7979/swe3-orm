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


-- auto-generated definition
create table student
(
    id          varchar not null
        constraint student_pk
            primary key,
    studentNumber integer,
    lastName        varchar,
    firstName varchar,
    birthDate varchar,
    gender      varchar
);

alter table student
    owner to platform;

create unique index student_id_uindex
    on student (id);
create unique index student_num_uindex
    on student (studentNumber);

create table class
(
    id         varchar,
    name       varchar,
    fk_teacher varchar
        constraint fk_teacher
            references teachers
);
alter table class
    owner to platform;
